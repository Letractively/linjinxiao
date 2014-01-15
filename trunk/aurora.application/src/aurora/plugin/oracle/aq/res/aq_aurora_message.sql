create or replace type aurora_message_property AS OBJECT
(
  key    varchar2(200),
  value  varchar2(200)
);

CREATE OR REPLACE TYPE aurora_message_properties AS varray(10) OF aurora_message_property;

create or replace type aurora_message as object
(
  message    varchar2(200),
  properties aurora_message_properties
);


EXEC DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'aurora_message_table',queue_payload_type => 'aurora_message',multiple_consumers => TRUE);
EXEC DBMS_AQADM.CREATE_QUEUE(queue_name => 'aurora_message_queue',queue_table => 'aurora_message_table');
EXEC DBMS_AQADM.START_QUEUE(queue_name => 'aurora_message_queue');

EXEC DBMS_AQADM.CREATE_QUEUE(queue_name => 'application_foundation',queue_table => 'aurora_message_table');
EXEC DBMS_AQADM.START_QUEUE(queue_name => 'application_foundation');

EXEC DBMS_AQADM.CREATE_QUEUE(queue_name => 'dml_event',queue_table => 'aurora_message_table');
EXEC DBMS_AQADM.START_QUEUE(queue_name => 'dml_event');


BEGIN
   DBMS_AQADM.ADD_SUBSCRIBER 
      ('aurora_message_queue', SYS.AQ$_AGENT ('AGENT_B', NULL, NULL));
end;
----

truncate table aurora_message_table;

        
select * from aurora_message_table;

-------
declare
   sublist DBMS_AQADM.AQ$_SUBSCRIBER_LIST_T;
   v_row PLS_INTEGER;
BEGIN
   /* Retrieve the list. */
   sublist := DBMS_AQADM.QUEUE_SUBSCRIBERS ('aurora_message_queue');

   v_row := sublist.FIRST;
    DBMS_OUTPUT.PUT_LINE('text');
   LOOP
      EXIT WHEN v_row IS NULL;
     /* DBMS_OUTPUT.PUT_LINE (v_row);*/
      DBMS_OUTPUT.PUT_LINE (sublist(v_row).name);
    /*  DBMS_AQADM.REMOVE_SUBSCRIBER ('aurora_message_queue', SYS.AQ$_AGENT(name=> sublist(v_row).name,
                                   address  => NULL,
                                   protocol => NULL));*/
      v_row := sublist.NEXT (v_row);
   END LOOP;
END;


------------------------------------------------------
DECLARE
  v_queue_options      DBMS_AQ.ENQUEUE_OPTIONS_T;
  v_message_properties DBMS_AQ.MESSAGE_PROPERTIES_T;
  v_message_id         RAW(16);
  v_message            aurora_message;
  v_recipients         DBMS_AQ.AQ$_RECIPIENT_LIST_T;
  v_ok                 varchar2(200);
begin
  select to_char(sysdate, 'mm-ss') into v_ok from dual;

  v_message := aurora_message(v_ok, null);
  /*    v_message_properties.sender_id := SYS.AQ$_AGENT('LOCAL_AGENT1',
  NULL,
  NULL);*/
  v_recipients(1) := SYS.AQ$_AGENT(name     => 'AGENT_A',
                                   address  => NULL,
                                   protocol => NULL);
  v_recipients(2) := SYS.AQ$_AGENT(name     => 'AGENT_B',
                                   address  => NULL,
                                   protocol => NULL);
  v_message_properties.recipient_list := v_recipients;
  v_message_properties.delay := DBMS_AQ.NO_DELAY;
  v_queue_options.visibility := DBMS_AQ.IMMEDIATE;
  /*
    v_queue_options.delivery_mode := DBMS_AQ.BUFFERED;
  */
  DBMS_AQ.ENQUEUE(queue_name         => 'aurora_message_queue',
                  enqueue_options    => v_queue_options,
                  message_properties => v_message_properties,
                  payload            => v_message,
                  msgid              => v_message_id);
  COMMIT;
END;
  
