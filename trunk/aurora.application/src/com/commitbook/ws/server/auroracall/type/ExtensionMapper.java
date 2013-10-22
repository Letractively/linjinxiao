
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:34:40 IST)
 */

        
            package com.commitbook.ws.server.auroracall.type;
        
            /**
            *  ExtensionMapper class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class ExtensionMapper{

          public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                       java.lang.String typeName,
                                                       javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{

              
                  if (
                  "http://www.aurora-framework.org/schema".equals(namespaceURI) &&
                  "record_type0".equals(typeName)){
                   
                            return  com.commitbook.ws.server.auroracall.type.Record_type0.Factory.parse(reader);
                        

                  }

              
                  if (
                  "http://www.aurora-framework.org/schema".equals(namespaceURI) &&
                  "records_type0".equals(typeName)){
                   
                            return  com.commitbook.ws.server.auroracall.type.Records_type0.Factory.parse(reader);
                        

                  }

              
             throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
          }

        }
    