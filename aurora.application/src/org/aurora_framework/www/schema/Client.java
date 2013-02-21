package org.aurora_framework.www.schema;


public class Client {
 public static void main(String[] args) throws Exception {
	FndAccount_test_serviceStub stub = new FndAccount_test_serviceStub();
	InsertRequesttype insertRequesttype1 = new InsertRequesttype();
	insertRequesttype1.setOPERATION_TYPE("test");
	insertRequesttype1.setPARAMETER1("param1");
	insertRequesttype1.setPARAMETER2("param2");
	insertRequesttype1.setPARAMETER3("param3");
	insertRequesttype1.setPARAMETER4("param4");
	insertRequesttype1.setPARAMETER5("param5");
	stub.insert(insertRequesttype1);
}
}
