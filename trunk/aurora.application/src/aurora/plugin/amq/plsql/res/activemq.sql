

call dbms_java.grant_permission( 'HEC2DEV', 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );--用户名大写
call dbms_java.grant_permission( 'HEC2DEV', 'SYS:java.util.PropertyPermission', '*', 'read,write' );

call dbms_java.grant_permission( 'HEC2DEV', 'SYS:java.net.SocketPermission', '*', 'listen,resolve');
call dbms_java.grant_permission( 'HEC2DEV', 'SYS:java.net.SocketPermission', '*', 'accept,resolve');
call dbms_java.grant_permission( 'HEC2DEV', 'SYS:java.net.SocketPermission', '*', 'connect,resolve');

call dbms_java.grant_permission('HEC2DEV','SYS:java.io.FilePermission','activemq-all-5.5.0.jar','read');

CALL dbms_java.loadjava(' -r -v -definer -g public  /u01/activemq-all-5.4.2.jar');

loadjava -v -r -u hec2dev/hec2dev -resolver "((* HEC2DEV) (* PUBLIC) (* -))" ./activemq-all-5.5.0.jar; 

loadjava -v -r -u hec2dev/hec2dev -resolver "((* HEC2DEV) (* PUBLIC) (* -))" /u01/amq_plsql.jar;


CREATE OR REPLACE PROCEDURE sendJmsMessage(url IN VARCHAR2,topic IN VARCHAR2,message IN VARCHAR2)
AS LANGUAGE JAVA NAME 'aurora.plugin.amq.plsql.MessageDispatcher.sendMessage(java.lang.String,java.lang.String,java.lang.String)';

CREATE OR REPLACE PROCEDURE sendTestMessage(message IN VARCHAR2)
AS LANGUAGE JAVA NAME 'aurora.plugin.amq.plsql.MessageDispatcher.test(java.lang.String)';

CREATE OR REPLACE PROCEDURE sendMessage
AS LANGUAGE JAVA NAME 'aurora.plugin.amq.plsql.MessageTest.sendMessage()';

loadjava -v -r -u hec2dev/hec2dev amq_plsql.jar;


dropjava -user  hec2dev/hec2dev  aurora.plugin.amq.plsql.MessageDispatcher

dropjava -user  hec2dev/hec2dev   amq_plsql.jar;

loadjava -v -r -u hec2dev/hec2dev -resolver "((* HEC2DEV) (* PUBLIC) (* -))" /u01/amq_plsql.jar; 

tar -zxvf apache-activemq-4.1.2-bin.tar.gz

loadjava -v -r -u hec2dev/hec2dev activemq-all-5.4.2.jar

------------------

create or replace and compile java source named TestJava1 as   
public class TestJava1   
{   
  public static void test()   
  {   
   System.out.println("Hello");   
  }   
}  

create or replace procedure testJava1 as language java name 'TestJava1.test()';

SET SERVEROUTPUT ON;   
CALL dbms_java.set_output(2000);  

exec testJava1(); 
