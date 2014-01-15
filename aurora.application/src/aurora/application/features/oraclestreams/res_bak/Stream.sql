--query---
select * from log_table;
select * from dba_apply_error t ORDER BY t.error_creation_time desc ;
select * from jms_queue_table;
select * from dba_apply_dml_handlers;
select * from dba_apply;
select * from dba_streams_rules; 


truncate table log_table
truncate table sys_raise_app_errors
truncate table sys.apply$_error
delete from jms_queue_table

--ÅäÖÃ--

BEGIN
  DBMS_STREAMS_ADM.ADD_schema_RULES(schema_name      => 'hec2dev',
                                   streams_type    => 'apply',
                                   streams_name    => 'apply_masst',
                                   queue_name      => 'strmadmin.masst_queue',
                                   include_dml     => true,
                                   include_ddl     => false,
                                   source_database => 'masdev');
END;
/

begin
  dbms_streams_adm.add_schema_rules(schema_name        => 'hec2dev',
                                    streams_type       => 'capture',
                                    streams_name       => 'capture_masst',
                                    queue_name         => 'strmadmin.masst_queue',
                                    include_dml        => true,
                                    include_ddl        => true,
                                    include_tagged_lcr => false,
                                    source_database    => null,
                                    inclusion_rule     => true);
end;
/

DECLARE
  iscn NUMBER; -- Variable to hold instantiation SCN value  
BEGIN
  iscn := DBMS_FLASHBACK.GET_SYSTEM_CHANGE_NUMBER();
  DBMS_APPLY_ADM.set_schema_instantiation_scn(source_schema_name   => 'hec2dev',
                                             source_database_name => 'masdev',
                                             instantiation_scn    => iscn);
END;
/


exec DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'scott.jms_queue_table',queue_payload_type => 'scott.streams_dml_lcr');



begin
  dbms_streams_adm.set_up_queue(queue_table => 'scott.jms_queue_table',
                                queue_name  => 'scott.jms_queue',
                                queue_user=>'scott');
end;

EXEC DBMS_AQADM.CREATE_QUEUE_TABLE(queue_table => 'queue_message_table',queue_payload_type => 'queue_message_type');

EXEC DBMS_AQADM.CREATE_QUEUE(queue_name => 'message_queue',queue_table => 'queue_message_table');

EXEC DBMS_AQADM.START_QUEUE(queue_name => 'scott.jms_queue');

streams_pkg


DECLARE
    queue_options       DBMS_AQ.ENQUEUE_OPTIONS_T;
    message_properties  DBMS_AQ.MESSAGE_PROPERTIES_T;
    message_id          RAW(16);
    my_message          strmadmin.queue_message_type;
BEGIN
    my_message := strmadmin.queue_message_type(
            1111,
            'This is a sample message',
            'This message has been posted on ' ||
            TO_CHAR(SYSDATE,'DD.MM.YYYY HH24:MI:SS'));
    DBMS_AQ.ENQUEUE(
        queue_name => 'strmadmin.message_queue',
        enqueue_options => queue_options,
        message_properties => message_properties,
        payload => my_message,
        msgid => message_id);
    COMMIT;
END;
/
dept

log_table


DECLARE
  subscriber SYS.AQ$_AGENT;
BEGIN
  subscriber :=  SYS.AQ$_AGENT('LOCAL_AGENT', NULL, NULL);
  SYS.DBMS_AQADM.ADD_SUBSCRIBER(
    queue_name  =>  'strmadmin.masst_jms_queue',
    subscriber  =>  subscriber);
END;
/

BEGIN
  DBMS_AQADM.ENABLE_DB_ACCESS(
    agent_name  => 'local_agent',
    db_username => 'strmadmin');
END;
/

streams_pkg;
QUEUE_MESSAGE_TYPE;

EXEC DBMS_AQADM.CREATE_QUEUE_TABLE(
queue_table => 'multi_message_table',
queue_payload_type => 'SYS.AQ$_JMS_OBJECT_MESSAGE',
multiple_consumers => TRUE);

Grant execute on dbms_aqadm to scott; 
Grant execute on dbms_aq to scott;
Grant execute on DBMS_APPLY_ADM to scott; 
grant execute on dbms_streams_adm to scott
Grant enqueue any queue,dequeue any queue to scott;


BEGIN
  DBMS_APPLY_ADM.SET_DML_HANDLER(object_name    => 'SCOTT.DEPT',
                                 object_type    => 'TABLE',
                                 operation_name => 'DEFAULT',
                                 user_procedure => 'SCOTT.streams_pkg.dml_handler');
END;
/
Grant execute on SCOTT.streams_pkg to strmadmin; 
Grant execute on scott.streams_dml_lcr_column to strmadmin; 

CREATE OR REPLACE TYPE streams_dml_lcr_column_list AS TABLE OF streams_dml_lcr_column


begin
  dbms_streams_adm.add_schema_rules(schema_name        => 'hec2dev',
                                    streams_type       => 'capture',
                                    streams_name       => 'capture_masst',
                                    queue_name         => 'strmadmin.masst_queue',
                                    include_dml        => true,
                                    include_ddl        => true,
                                    include_tagged_lcr => false,
                                    source_database    => null,
                                    inclusion_rule     => true);
end;
/


BEGIN
  DBMS_APPLY_ADM.SET_DML_HANDLER(object_name    => 'hec2dev.fnd_descriptions',
                                 object_type    => 'TABLE',
                                 operation_name => 'UPDATE',
                                 user_procedure => 'HEC2DEV.streams_pkg.dml_handler');
END;
/

begin
  DBMS_STREAMS_ADM.ADD_TABLE_RULES(table_name      => 'hec2dev.BGT_BUDGET_ITEMS',
                                   streams_type    => 'apply',
                                   streams_name    => 'apply_masst',
                                   queue_name      => 'strmadmin.masst_queue',
                                   include_dml     => true,
                                   include_ddl     => false,
                                   source_database => 'masdev');
END;
/

begin
  dbms_streams_adm.add_table_rules(table_name        => 'hec2dev.BGT_BUDGET_ITEMS',
                                    streams_type       => 'capture',
                                    streams_name       => 'capture_masst',
                                    queue_name         => 'strmadmin.masst_queue',
                                    include_dml        => true,
                                    include_ddl        => true,
                                    include_tagged_lcr => false,
                                    source_database    => null,
                                    inclusion_rule     => true);
end;
/

begin
  dbms_streams_adm.add_schema_rules(schema_name        => 'hec2dev',
                                    streams_type       => 'capture',
                                    streams_name       => 'capture_masst',
                                    queue_name         => 'strmadmin.masst_queue',
                                    include_dml        => true,
                                    include_ddl        => true,
                                    include_tagged_lcr => false,
                                    source_database    => null,
                                    inclusion_rule     => true);
end;
/
;
begin
  dbms_streams_adm.remove_rule(rule_name        => 'HEC2DEV71',
                                streams_type     => 'APPLY',
                                streams_name     => 'APPLY_MASST',
                                drop_unused_rule => true,
                                inclusion_rule   => true);
end;
/

create tablespace tbs_stream datafile '/u01/oracle/oradata/masdev/tbs_stream01.dbf' size 100m autoextend on maxsize unlimited segment space management auto;

begin
  dbms_capture_adm.start_capture(capture_name => 'capture_masst');
end;

DECLARE
  iscn NUMBER; -- Variable to hold instantiation SCN value  
BEGIN
  iscn := DBMS_FLASHBACK.GET_SYSTEM_CHANGE_NUMBER();
  DBMS_APPLY_ADM.SET_TABLE_INSTANTIATION_SCN(source_object_name   => 'hec2dev.fnd_descriptions',
                                             source_database_name => 'masdev',
                                             instantiation_scn    => iscn);
END;
/

BEGIN
  DBMS_STREAMS_ADM.ADD_TABLE_RULES(table_name      => 'hec2dev.fnd_descriptions',
                                   streams_type    => 'apply',
                                   streams_name    => 'apply_masst',
                                   queue_name      => 'strmadmin.masst_queue',
                                   include_dml     => true,
                                   include_ddl     => false,
                                   source_database => 'masdev');
END;

BEGIN
  DBMS_APPLY_ADM.SET_PARAMETER(apply_name => 'apply_masst',
                               parameter  => 'disable_on_error',
                               value      => 'n');
END;
/

BEGIN
  DBMS_APPLY_ADM.START_APPLY(apply_name => 'apply_masst');
END;
/
