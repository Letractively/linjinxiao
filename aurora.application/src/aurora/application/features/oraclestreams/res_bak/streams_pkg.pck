create or replace package streams_pkg is

  -- Author  : LINJINXIAO
  -- Created : 2011/12/20 15:21:46
  -- Purpose : 测试

  procedure dml_handler(p_row_data ANYDATA);
  procedure print_lcr(lcr IN ANYDATA);
  procedure print_transaction(ltxnid IN VARCHAR2);
  PROCEDURE print_errors;
end streams_pkg;
/
create or replace package body streams_pkg is

  g_this_package_name varchar2(30) := 'streams_pkg';
  g_user_id           number := 0;
  type varchar_array is table of varchar2(30) index by varchar2(30);
  -- Refactored procedure multiConsumer 
  procedure multiConsumer(v_message_properties in out dbms_aq.message_properties_t) is
    v_recipients DBMS_AQ.AQ$_RECIPIENT_LIST_T;
  begin
    v_message_properties.sender_id := SYS.AQ$_AGENT('LOCAL_AGENT',
                                                    NULL,
                                                    NULL);
    v_recipients(1) := SYS.AQ$_AGENT(name     => 'LOCAL_AGENT',
                                     address  => NULL,
                                     protocol => NULL);
    v_message_properties.recipient_list := v_recipients;
  end multiConsumer;

  -- Refactored procedure log_lcr_message 
  procedure log_lcr_message(p_table_name      varchar2,
                            p_column_name     varchar2,
                            p_key_value       varchar2,
                            p_old_value       varchar2,
                            p_new_value       varchar2,
                            p_transaction_id  varchar2,
                            p_transaction_seq varchar2) is
  begin
    insert into LOG_TABLE
      (TABLE_NAME,
       COLUMN_NAME,
       KEY_VALUE,
       OLD_VALUE,
       NEW_VALUE,
       TIMESTAMP,
       TRANSACTION_ID,
       TRANSACTION_SEQ)
    values
      (p_table_name,
       p_column_name,
       p_key_value,
       p_old_value,
       p_new_value,
       sysdate,
       p_transaction_id,
       p_transaction_seq);
  end log_lcr_message;

  -- Refactored procedure accessValue 
  function accessValue(data AnyData, p_column_type varchar2) return varchar2 is
    v_value varchar2(2000);
    e_undefined_column_type exception;
  begin
    if (p_column_type = 'VARCHAR2') then
      v_value := SYS.AnyData.AccessVarchar2(data);
    else
      if (p_column_type = 'NUMBER') then
        v_value := to_char(SYS.AnyData.AccessNumber(data));
      else
        raise e_undefined_column_type;
      end if;
    end if;
    return v_value;
  end accessValue;

  -- Refactored procedure accessValue 
  function accessValue(p_values      in SYS.LCR$_ROW_LIST,
                       p_column_name varchar2,
                       p_column_type varchar2) return varchar2 is
    v_value varchar2(2000);
    e_undefined_column_type exception;
  begin
    for i in 1 .. p_values.Count loop
      if (p_column_name = p_values(i).column_name) then
        return accessValue(p_values(i).data, p_column_type);
      end if;
    end loop;
  end accessValue;

  -- Refactored procedure parser_pk_columns 
  procedure parser_pk_columns(p_values              in SYS.LCR$_ROW_LIST,
                              p_pk_colums_array     in varchar_array,
                              p_new_value_flag      in varchar2,
                              p_dml_lcr_column_list in out streams_dml_lcr_column_list,
                              p_pk_column_index     in out number) is
  
    v_column_type varchar2(30);
    v_value       varchar2(2000);
  begin
    for i in 1 .. p_values.Count loop
      begin
        v_column_type := p_pk_colums_array(p_values(i).column_name);
      exception
        when no_data_found then
          v_column_type := null;
      end;
      if (v_column_type is not null) then
        v_value := accessValue(p_values(i).data, v_column_type);
        p_dml_lcr_column_list(p_pk_column_index) := streams_dml_lcr_column(column_name    => p_values(i)
                                                                                             .column_name,
                                                                           column_type    => v_column_type,
                                                                           value          => v_value,
                                                                           new_value_flag => p_new_value_flag);
        p_pk_column_index := p_pk_column_index + 1;
      end if;
    end loop;
  end parser_pk_columns;

  procedure dml_handler(p_row_data ANYDATA) is
    -- 主键类型目前仅支持varchar2和number
    v_user_name          varchar2(30) := 'HEC2DEV';
    v_queue_name         varchar2(30) := 'HEC2DEV.JMS_QUEUE';
    c_typename           varchar2(30) := 'SYS.LCR$_ROW_RECORD';
    v_queue_options      dbms_aq.enqueue_options_t;
    v_message_properties dbms_aq.message_properties_t;
    v_message_id         raw(16);
    v_dml_message        varchar2(2000);
    v_lcr_record         sys.lcr$_row_record;
    v_rc                 pls_integer;
    v_values             sys.lcr$_row_list;
    cursor c_pk_colums(p_own varchar2, p_table_name varchar2) is
      select col.column_name, col.data_type
        from user_constraints c, user_cons_columns pk, user_tab_cols col
       where c.owner = p_own
         and c.table_name = p_table_name
         and c.constraint_type = 'P'
         and pk.owner = c.owner
         and pk.constraint_name = c.constraint_name
         and pk.table_name = c.table_name
         and col.table_name = pk.table_name
         and col.column_name = pk.column_name;
  
    v_pk_colums_array varchar_array;
  
    v_dml_lcr_column_list streams_dml_lcr_column_list := streams_dml_lcr_column_list();
  
    e_undefined_command_type exception;
    v_dml_lcr         streams_dml_lcr;
    v_pk_column_index number := 1;
  
  begin
    if (c_typeName != p_row_data.GetTypeName) then
      return;
    end if;
    v_rc := p_row_data.GETOBJECT(v_lcr_record);
    if (v_user_name != v_lcr_record.get_object_owner()) then
      return;
    end if;
    v_dml_message := v_lcr_record.get_object_owner() ||
                     v_lcr_record.get_object_name();
    for c_pk_colum in c_pk_colums(v_lcr_record.get_object_owner(),
                                  v_lcr_record.get_object_name()) loop
      v_pk_colums_array(c_pk_colum.column_name) := c_pk_colum.data_type;
      v_dml_message := v_dml_message || ':' || c_pk_colum.data_type || ':' ||
                       c_pk_colum.column_name;
    end loop;
    v_dml_lcr_column_list.extend(10);
    if (v_lcr_record.get_command_type() = 'DELETE') then
      v_values := v_lcr_record.get_values('old');
      parser_pk_columns(p_values              => v_values,
                        p_pk_colums_array     => v_pk_colums_array,
                        p_new_value_flag      => 'N',
                        p_dml_lcr_column_list => v_dml_lcr_column_list,
                        p_pk_column_index     => v_pk_column_index);
    else
      if (v_lcr_record.get_command_type() = 'UPDATE' or
         v_lcr_record.get_command_type() = 'INSERT') then
        v_values := v_lcr_record.get_values('new');
        parser_pk_columns(p_values              => v_values,
                          p_pk_colums_array     => v_pk_colums_array,
                          p_new_value_flag      => 'Y',
                          p_dml_lcr_column_list => v_dml_lcr_column_list,
                          p_pk_column_index     => v_pk_column_index);
      else
        raise e_undefined_command_type;
      end if;
    end if;
    v_dml_lcr := streams_dml_lcr(owner          => v_lcr_record.get_object_owner,
                                 object_name    => v_lcr_record.get_object_name,
                                 command_type   => v_lcr_record.get_command_type,
                                 pk_column_list => v_dml_lcr_column_list);
  
    --multiConsumer(v_message_properties);
  
    log_lcr_message(p_table_name      => v_lcr_record.get_object_name,
                    p_column_name     => 'column_name',
                    p_key_value       => v_dml_message,
                    p_old_value       => '',
                    p_new_value       => '',
                    p_transaction_id  => v_lcr_record.get_transaction_id,
                    p_transaction_seq => '');
  
    DBMS_AQ.ENQUEUE(queue_name         => v_queue_name,
                    enqueue_options    => v_queue_options,
                    message_properties => v_message_properties,
                    payload            => v_dml_lcr,
                    msgid              => v_message_id);
  exception
    when others then
      rollback;
      sys_raise_app_error_pkg.raise_sys_others_error(p_message                 => dbms_utility.format_error_backtrace || ' ' ||
                                                                                  sqlerrm,
                                                     p_created_by              => g_user_id,
                                                     p_package_name            => g_this_package_name,
                                                     p_procedure_function_name => 'dml_handler');
      raise_application_error(sys_raise_app_error_pkg.c_error_number,
                              sys_raise_app_error_pkg.g_err_line_id);
    
  end dml_handler;

  procedure lookupcode_dml_handler(p_row_data ANYDATA) is
    -- 主键类型目前仅支持varchar2和number
    v_user_name          varchar2(30) := 'HEC2DEV';
    v_queue_name         varchar2(30) := 'HEC2DEV.JMS_QUEUE';
    c_typename           varchar2(30) := 'SYS.LCR$_ROW_RECORD';
    v_queue_options      dbms_aq.enqueue_options_t;
    v_message_properties dbms_aq.message_properties_t;
    v_message_id         raw(16);
    v_dml_message        varchar2(2000);
    v_lcr_record         sys.lcr$_row_record;
    v_rc                 pls_integer;
    v_values             sys.lcr$_row_list;
    cursor c_pk_colums(p_own varchar2, p_table_name varchar2) is
      select col.column_name, col.data_type
        from user_constraints c, user_cons_columns pk, user_tab_cols col
       where c.owner = p_own
         and c.table_name = p_table_name
         and c.constraint_type = 'P'
         and pk.owner = c.owner
         and pk.constraint_name = c.constraint_name
         and pk.table_name = c.table_name
         and col.table_name = pk.table_name
         and col.column_name = pk.column_name;
  
    v_pk_colums_array varchar_array;
  
    v_dml_lcr_column_list streams_dml_lcr_column_list := streams_dml_lcr_column_list();
  
    e_undefined_command_type exception;
    v_dml_lcr         streams_dml_lcr;
    v_pk_column_index number := 1;
    v_code            varchar2(30);
    c_table_name      varchar2(30) := 'sys_service_lookup_v';
    c_column_name     varchar2(30) := 'code';
    c_column_type     varchar2(30) := 'VARCHAR2';
  begin
    if (c_typeName != p_row_data.GetTypeName) then
      return;
    end if;
    v_rc := p_row_data.GETOBJECT(v_lcr_record);
    if (v_user_name != v_lcr_record.get_object_owner()) then
      return;
    end if;
    v_dml_lcr_column_list.extend(10);
    if (v_lcr_record.get_command_type() = 'DELETE') then
      v_values := v_lcr_record.get_values('old');
    end if;
    if (v_lcr_record.get_command_type() = 'UPDATE' or
       v_lcr_record.get_command_type() = 'INSERT') then
      v_values := v_lcr_record.get_values('new');
    end if;
    if ('sys_codes' = v_lcr_record.get_object_name) then
      v_code := accessValue(p_values      => v_values,
                            p_column_name => c_column_name,
                            p_column_type => c_column_type);
      v_dml_lcr_column_list(1) := streams_dml_lcr_column(column_name    => c_column_name,
                                                         column_type    => c_column_type,
                                                         value          => v_code,
                                                         new_value_flag => 'Y');
      v_dml_lcr := streams_dml_lcr(owner          => v_lcr_record.get_object_owner,
                                   object_name    => c_table_name,
                                   command_type   => v_lcr_record.get_command_type,
                                   pk_column_list => v_dml_lcr_column_list);
    end if;
  
    v_dml_message := v_lcr_record.get_object_owner() ||
                     v_lcr_record.get_object_name();
    for c_pk_colum in c_pk_colums(v_lcr_record.get_object_owner(),
                                  v_lcr_record.get_object_name()) loop
      v_pk_colums_array(c_pk_colum.column_name) := c_pk_colum.data_type;
      v_dml_message := v_dml_message || ':' || c_pk_colum.data_type || ':' ||
                       c_pk_colum.column_name;
    end loop;
  
    if (v_lcr_record.get_command_type() = 'DELETE') then
      v_values := v_lcr_record.get_values('old');
      parser_pk_columns(p_values              => v_values,
                        p_pk_colums_array     => v_pk_colums_array,
                        p_new_value_flag      => 'N',
                        p_dml_lcr_column_list => v_dml_lcr_column_list,
                        p_pk_column_index     => v_pk_column_index);
    else
      if (v_lcr_record.get_command_type() = 'UPDATE' or
         v_lcr_record.get_command_type() = 'INSERT') then
        v_values := v_lcr_record.get_values('new');
        parser_pk_columns(p_values              => v_values,
                          p_pk_colums_array     => v_pk_colums_array,
                          p_new_value_flag      => 'Y',
                          p_dml_lcr_column_list => v_dml_lcr_column_list,
                          p_pk_column_index     => v_pk_column_index);
      else
        raise e_undefined_command_type;
      end if;
    end if;
    v_dml_lcr := streams_dml_lcr(owner          => v_lcr_record.get_object_owner,
                                 object_name    => v_lcr_record.get_object_name,
                                 command_type   => v_lcr_record.get_command_type,
                                 pk_column_list => v_dml_lcr_column_list);
  
    --multiConsumer(v_message_properties);
  
    log_lcr_message(p_table_name      => v_lcr_record.get_object_name,
                    p_column_name     => 'column_name',
                    p_key_value       => v_dml_message,
                    p_old_value       => '',
                    p_new_value       => '',
                    p_transaction_id  => v_lcr_record.get_transaction_id,
                    p_transaction_seq => '');
  
    DBMS_AQ.ENQUEUE(queue_name         => v_queue_name,
                    enqueue_options    => v_queue_options,
                    message_properties => v_message_properties,
                    payload            => v_dml_lcr,
                    msgid              => v_message_id);
  exception
    when others then
      rollback;
      sys_raise_app_error_pkg.raise_sys_others_error(p_message                 => dbms_utility.format_error_backtrace || ' ' ||
                                                                                  sqlerrm,
                                                     p_created_by              => g_user_id,
                                                     p_package_name            => g_this_package_name,
                                                     p_procedure_function_name => 'dml_handler');
      raise_application_error(sys_raise_app_error_pkg.c_error_number,
                              sys_raise_app_error_pkg.g_err_line_id);
    
  end lookupcode_dml_handler;

  PROCEDURE print_any(DATA IN ANYDATA) IS
    tn  VARCHAR2(61);
    str VARCHAR2(4000);
    CHR VARCHAR2(1000);
    num NUMBER;
    dat DATE;
    rw  RAW(4000);
    res NUMBER;
  BEGIN
    IF DATA IS NULL THEN
      DBMS_OUTPUT.put_line('NULL value');
      RETURN;
    END IF;
  
    tn := DATA.gettypename();
  
    IF tn = 'SYS.VARCHAR2' THEN
      res := DATA.getvarchar2(str);
      DBMS_OUTPUT.put_line(SUBSTR(str, 0, 253));
    ELSIF tn = 'SYS.CHAR' THEN
      res := DATA.getchar(CHR);
      DBMS_OUTPUT.put_line(SUBSTR(CHR, 0, 253));
    ELSIF tn = 'SYS.VARCHAR' THEN
      res := DATA.getvarchar(CHR);
      DBMS_OUTPUT.put_line(CHR);
    ELSIF tn = 'SYS.NUMBER' THEN
      res := DATA.getnumber(num);
      DBMS_OUTPUT.put_line(num);
    ELSIF tn = 'SYS.DATE' THEN
      res := DATA.getdate(dat);
      DBMS_OUTPUT.put_line(dat);
    ELSIF tn = 'SYS.RAW' THEN
      -- res := data.GETRAW(rw);
      -- DBMS_OUTPUT.PUT_LINE(SUBSTR(DBMS_LOB.SUBSTR(rw),0,253));
      DBMS_OUTPUT.put_line('BLOB Value');
    ELSIF tn = 'SYS.BLOB' THEN
      DBMS_OUTPUT.put_line('BLOB Found');
    ELSE
      DBMS_OUTPUT.put_line('typename is ' || tn);
    END IF;
  END print_any;

  procedure print_lcr(lcr IN ANYDATA) IS
    typenm   VARCHAR2(61);
    ddllcr   SYS.lcr$_ddl_record;
    proclcr  SYS.lcr$_procedure_record;
    rowlcr   SYS.lcr$_row_record;
    res      NUMBER;
    newlist  SYS.lcr$_row_list;
    oldlist  SYS.lcr$_row_list;
    ddl_text CLOB;
    ext_attr ANYDATA;
  BEGIN
    typenm := lcr.gettypename();
    DBMS_OUTPUT.put_line('type name: ' || typenm);
  
    IF (typenm = 'SYS.LCR$_DDL_RECORD') THEN
      res := lcr.getobject(ddllcr);
      DBMS_OUTPUT.put_line('source database: ' ||
                           ddllcr.get_source_database_name);
      DBMS_OUTPUT.put_line('owner: ' || ddllcr.get_object_owner);
      DBMS_OUTPUT.put_line('object: ' || ddllcr.get_object_name);
      DBMS_OUTPUT.put_line('is tag null: ' || ddllcr.is_null_tag);
      DBMS_LOB.createtemporary(ddl_text, TRUE);
      ddllcr.get_ddl_text(ddl_text);
      DBMS_OUTPUT.put_line('ddl: ' || ddl_text);
      -- Print extra attributes in DDL LCR
      ext_attr := ddllcr.get_extra_attribute('serial#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('serial#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := ddllcr.get_extra_attribute('session#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('session#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := ddllcr.get_extra_attribute('thread#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('thread#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := ddllcr.get_extra_attribute('tx_name');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('transaction name: ' ||
                             ext_attr.accessvarchar2());
      END IF;
    
      ext_attr := ddllcr.get_extra_attribute('username');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('username: ' || ext_attr.accessvarchar2());
      END IF;
    
      DBMS_LOB.freetemporary(ddl_text);
    ELSIF (typenm = 'SYS.LCR$_ROW_RECORD') THEN
      res := lcr.getobject(rowlcr);
      DBMS_OUTPUT.put_line('source database: ' ||
                           rowlcr.get_source_database_name);
      DBMS_OUTPUT.put_line('owner: ' || rowlcr.get_object_owner);
      DBMS_OUTPUT.put_line('object: ' || rowlcr.get_object_name);
      DBMS_OUTPUT.put_line('is tag null: ' || rowlcr.is_null_tag);
      DBMS_OUTPUT.put_line('command_type: ' || rowlcr.get_command_type);
      oldlist := rowlcr.get_values('old');
    
      FOR i IN 1 .. oldlist.COUNT LOOP
        IF oldlist(i) IS NOT NULL THEN
          DBMS_OUTPUT.put_line('old(' || i || '): ' || oldlist(i)
                               .column_name);
          print_any(oldlist(i).DATA);
        END IF;
      END LOOP;
    
      newlist := rowlcr.get_values('new', 'n');
    
      FOR i IN 1 .. newlist.COUNT LOOP
        IF newlist(i) IS NOT NULL THEN
          DBMS_OUTPUT.put_line('new(' || i || '): ' || newlist(i)
                               .column_name);
          print_any(newlist(i).DATA);
        END IF;
      END LOOP;
    
      -- Print extra attributes in row LCR
      ext_attr := rowlcr.get_extra_attribute('row_id');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('row_id: ' || ext_attr.accessurowid());
      END IF;
    
      ext_attr := rowlcr.get_extra_attribute('serial#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('serial#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := rowlcr.get_extra_attribute('session#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('session#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := rowlcr.get_extra_attribute('thread#');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('thread#: ' || ext_attr.accessnumber());
      END IF;
    
      ext_attr := rowlcr.get_extra_attribute('tx_name');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('transaction name: ' ||
                             ext_attr.accessvarchar2());
      END IF;
    
      ext_attr := rowlcr.get_extra_attribute('username');
    
      IF (ext_attr IS NOT NULL) THEN
        DBMS_OUTPUT.put_line('username: ' || ext_attr.accessvarchar2());
      END IF;
    ELSE
      DBMS_OUTPUT.put_line('Non-LCR Message with type ' || typenm);
    END IF;
  END print_lcr;

  PROCEDURE print_errors IS
    CURSOR c IS
      SELECT local_transaction_id,
             source_database,
             message_number,
             message_count,
             error_number,
             error_message
        FROM dba_apply_error
       ORDER BY source_database, source_commit_scn;
  
    i      NUMBER;
    txnid  VARCHAR2(30);
    SOURCE VARCHAR2(128);
    msgno  NUMBER;
    msgcnt NUMBER;
    errnum NUMBER := 0;
    errno  NUMBER;
    errmsg VARCHAR2(255);
    lcr    ANYDATA;
    r      NUMBER;
  BEGIN
    FOR r IN c LOOP
      errnum := errnum + 1;
      msgcnt := r.message_count;
      txnid  := r.local_transaction_id;
      SOURCE := r.source_database;
      msgno  := r.message_number;
      errno  := r.error_number;
      errmsg := r.error_message;
      DBMS_OUTPUT.put_line('*************************************************');
      DBMS_OUTPUT.put_line('----- ERROR #' || errnum);
      DBMS_OUTPUT.put_line('----- Local Transaction ID: ' || txnid);
      DBMS_OUTPUT.put_line('----- Source Database: ' || SOURCE);
      DBMS_OUTPUT.put_line('----Error in Message: ' || msgno);
      DBMS_OUTPUT.put_line('----Error Number: ' || errno);
      DBMS_OUTPUT.put_line('----Message Text: ' || errmsg);
    
      FOR i IN 1 .. msgcnt LOOP
        DBMS_OUTPUT.put_line('--message: ' || i);
        lcr := DBMS_APPLY_ADM.get_error_message(i, txnid);
        print_lcr(lcr);
      END LOOP;
    END LOOP;
  END print_errors;

  PROCEDURE print_transaction(ltxnid IN VARCHAR2) IS
    i      NUMBER;
    txnid  VARCHAR2(30);
    SOURCE VARCHAR2(128);
    msgno  NUMBER;
    msgcnt NUMBER;
    errno  NUMBER;
    errmsg VARCHAR2(128);
    lcr    ANYDATA;
  BEGIN
    SELECT local_transaction_id,
           source_database,
           message_number,
           message_count,
           error_number,
           error_message
      INTO txnid, SOURCE, msgno, msgcnt, errno, errmsg
      FROM dba_apply_error
     WHERE local_transaction_id = ltxnid;
  
    DBMS_OUTPUT.put_line('----- Local Transaction ID: ' || txnid);
    DBMS_OUTPUT.put_line('----- Source Database: ' || SOURCE);
    DBMS_OUTPUT.put_line('----Error in Message: ' || msgno);
    DBMS_OUTPUT.put_line('----Error Number: ' || errno);
    DBMS_OUTPUT.put_line('----Message Text: ' || errmsg);
  
    FOR i IN 1 .. msgcnt LOOP
      DBMS_OUTPUT.put_line('--message: ' || i);
      lcr := DBMS_APPLY_ADM.get_error_message(i, txnid); -- gets the LCR
      print_lcr(lcr);
    END LOOP;
  END print_transaction;

end streams_pkg;
/
