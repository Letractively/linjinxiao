
/**
 * InsertRequesttype.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:34:40 IST)
 */

            
                package org.aurora_framework.www.schema;
            

            /**
            *  InsertRequesttype bean class
            */
            @SuppressWarnings({"unchecked","unused"})
        
        public  class InsertRequesttype
        implements org.apache.axis2.databinding.ADBBean{
        
                public static final javax.xml.namespace.QName MY_QNAME = new javax.xml.namespace.QName(
                "http://www.aurora-framework.org/schema",
                "insertRequesttype",
                "ns1");

            

                        /**
                        * field for OPERATION_TYPE
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localOPERATION_TYPE ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getOPERATION_TYPE(){
                               return localOPERATION_TYPE;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param OPERATION_TYPE
                               */
                               public void setOPERATION_TYPE(java.lang.String param){
                            
                                            this.localOPERATION_TYPE=param;
                                    

                               }
                            

                        /**
                        * field for PARAMETER1
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localPARAMETER1 ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPARAMETER1(){
                               return localPARAMETER1;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PARAMETER1
                               */
                               public void setPARAMETER1(java.lang.String param){
                            
                                            this.localPARAMETER1=param;
                                    

                               }
                            

                        /**
                        * field for PARAMETER2
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localPARAMETER2 ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPARAMETER2(){
                               return localPARAMETER2;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PARAMETER2
                               */
                               public void setPARAMETER2(java.lang.String param){
                            
                                            this.localPARAMETER2=param;
                                    

                               }
                            

                        /**
                        * field for PARAMETER3
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localPARAMETER3 ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPARAMETER3(){
                               return localPARAMETER3;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PARAMETER3
                               */
                               public void setPARAMETER3(java.lang.String param){
                            
                                            this.localPARAMETER3=param;
                                    

                               }
                            

                        /**
                        * field for PARAMETER4
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localPARAMETER4 ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPARAMETER4(){
                               return localPARAMETER4;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PARAMETER4
                               */
                               public void setPARAMETER4(java.lang.String param){
                            
                                            this.localPARAMETER4=param;
                                    

                               }
                            

                        /**
                        * field for PARAMETER5
                        * This was an Attribute!
                        */

                        
                                    protected java.lang.String localPARAMETER5 ;
                                

                           /**
                           * Auto generated getter method
                           * @return java.lang.String
                           */
                           public  java.lang.String getPARAMETER5(){
                               return localPARAMETER5;
                           }

                           
                        
                            /**
                               * Auto generated setter method
                               * @param param PARAMETER5
                               */
                               public void setPARAMETER5(java.lang.String param){
                            
                                            this.localPARAMETER5=param;
                                    

                               }
                            

     
     
        /**
        *
        * @param parentQName
        * @param factory
        * @return org.apache.axiom.om.OMElement
        */
       public org.apache.axiom.om.OMElement getOMElement (
               final javax.xml.namespace.QName parentQName,
               final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException{


        
               org.apache.axiom.om.OMDataSource dataSource =
                       new org.apache.axis2.databinding.ADBDataSource(this,MY_QNAME);
               return factory.createOMElement(dataSource,MY_QNAME);
            
        }

         public void serialize(final javax.xml.namespace.QName parentQName,
                                       javax.xml.stream.XMLStreamWriter xmlWriter)
                                throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
                           serialize(parentQName,xmlWriter,false);
         }

         public void serialize(final javax.xml.namespace.QName parentQName,
                               javax.xml.stream.XMLStreamWriter xmlWriter,
                               boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException{
            
                


                java.lang.String prefix = null;
                java.lang.String namespace = null;
                

                    prefix = parentQName.getPrefix();
                    namespace = parentQName.getNamespaceURI();
                    writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);
                
                  if (serializeType){
               

                   java.lang.String namespacePrefix = registerPrefix(xmlWriter,"http://www.aurora-framework.org/schema");
                   if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)){
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           namespacePrefix+":insertRequesttype",
                           xmlWriter);
                   } else {
                       writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance","type",
                           "insertRequesttype",
                           xmlWriter);
                   }

               
                   }
               
                                            if (localOPERATION_TYPE != null){
                                        
                                                writeAttribute("",
                                                         "OPERATION_TYPE",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOPERATION_TYPE), xmlWriter);

                                            
                                      }
                                    
                                            if (localPARAMETER1 != null){
                                        
                                                writeAttribute("",
                                                         "PARAMETER1",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER1), xmlWriter);

                                            
                                      }
                                    
                                            if (localPARAMETER2 != null){
                                        
                                                writeAttribute("",
                                                         "PARAMETER2",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER2), xmlWriter);

                                            
                                      }
                                    
                                            if (localPARAMETER3 != null){
                                        
                                                writeAttribute("",
                                                         "PARAMETER3",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER3), xmlWriter);

                                            
                                      }
                                    
                                            if (localPARAMETER4 != null){
                                        
                                                writeAttribute("",
                                                         "PARAMETER4",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER4), xmlWriter);

                                            
                                      }
                                    
                                            if (localPARAMETER5 != null){
                                        
                                                writeAttribute("",
                                                         "PARAMETER5",
                                                         org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER5), xmlWriter);

                                            
                                      }
                                    
                    xmlWriter.writeEndElement();
               

        }

        private static java.lang.String generatePrefix(java.lang.String namespace) {
            if(namespace.equals("http://www.aurora-framework.org/schema")){
                return "ns1";
            }
            return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
        }

        /**
         * Utility method to write an element start tag.
         */
        private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                       javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
            if (writerPrefix != null) {
                xmlWriter.writeStartElement(namespace, localPart);
            } else {
                if (namespace.length() == 0) {
                    prefix = "";
                } else if (prefix == null) {
                    prefix = generatePrefix(namespace);
                }

                xmlWriter.writeStartElement(prefix, localPart, namespace);
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
        }
        
        /**
         * Util method to write an attribute with the ns prefix
         */
        private void writeAttribute(java.lang.String prefix,java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (xmlWriter.getPrefix(namespace) == null) {
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            xmlWriter.writeAttribute(namespace,attName,attValue);
        }

        /**
         * Util method to write an attribute without the ns prefix
         */
        private void writeAttribute(java.lang.String namespace,java.lang.String attName,
                                    java.lang.String attValue,javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException{
            if (namespace.equals("")) {
                xmlWriter.writeAttribute(attName,attValue);
            } else {
                registerPrefix(xmlWriter, namespace);
                xmlWriter.writeAttribute(namespace,attName,attValue);
            }
        }


           /**
             * Util method to write an attribute without the ns prefix
             */
            private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                             javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

                java.lang.String attributeNamespace = qname.getNamespaceURI();
                java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
                if (attributePrefix == null) {
                    attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
                }
                java.lang.String attributeValue;
                if (attributePrefix.trim().length() > 0) {
                    attributeValue = attributePrefix + ":" + qname.getLocalPart();
                } else {
                    attributeValue = qname.getLocalPart();
                }

                if (namespace.equals("")) {
                    xmlWriter.writeAttribute(attName, attributeValue);
                } else {
                    registerPrefix(xmlWriter, namespace);
                    xmlWriter.writeAttribute(namespace, attName, attributeValue);
                }
            }
        /**
         *  method to handle Qnames
         */

        private void writeQName(javax.xml.namespace.QName qname,
                                javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
            java.lang.String namespaceURI = qname.getNamespaceURI();
            if (namespaceURI != null) {
                java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
                if (prefix == null) {
                    prefix = generatePrefix(namespaceURI);
                    xmlWriter.writeNamespace(prefix, namespaceURI);
                    xmlWriter.setPrefix(prefix,namespaceURI);
                }

                if (prefix.trim().length() > 0){
                    xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                } else {
                    // i.e this is the default namespace
                    xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
                }

            } else {
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }
        }

        private void writeQNames(javax.xml.namespace.QName[] qnames,
                                 javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

            if (qnames != null) {
                // we have to store this data until last moment since it is not possible to write any
                // namespace data after writing the charactor data
                java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
                java.lang.String namespaceURI = null;
                java.lang.String prefix = null;

                for (int i = 0; i < qnames.length; i++) {
                    if (i > 0) {
                        stringToWrite.append(" ");
                    }
                    namespaceURI = qnames[i].getNamespaceURI();
                    if (namespaceURI != null) {
                        prefix = xmlWriter.getPrefix(namespaceURI);
                        if ((prefix == null) || (prefix.length() == 0)) {
                            prefix = generatePrefix(namespaceURI);
                            xmlWriter.writeNamespace(prefix, namespaceURI);
                            xmlWriter.setPrefix(prefix,namespaceURI);
                        }

                        if (prefix.trim().length() > 0){
                            stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        } else {
                            stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                        }
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                }
                xmlWriter.writeCharacters(stringToWrite.toString());
            }

        }


        /**
         * Register a namespace prefix
         */
        private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
            java.lang.String prefix = xmlWriter.getPrefix(namespace);
            if (prefix == null) {
                prefix = generatePrefix(namespace);
                javax.xml.namespace.NamespaceContext nsContext = xmlWriter.getNamespaceContext();
                while (true) {
                    java.lang.String uri = nsContext.getNamespaceURI(prefix);
                    if (uri == null || uri.length() == 0) {
                        break;
                    }
                    prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
                }
                xmlWriter.writeNamespace(prefix, namespace);
                xmlWriter.setPrefix(prefix, namespace);
            }
            return prefix;
        }


  
        /**
        * databinding method to get an XML representation of this object
        *
        */
        public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
                    throws org.apache.axis2.databinding.ADBException{


        
                 java.util.ArrayList elementList = new java.util.ArrayList();
                 java.util.ArrayList attribList = new java.util.ArrayList();

                
                            attribList.add(
                            new javax.xml.namespace.QName("","OPERATION_TYPE"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localOPERATION_TYPE));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","PARAMETER1"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER1));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","PARAMETER2"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER2));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","PARAMETER3"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER3));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","PARAMETER4"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER4));
                                
                            attribList.add(
                            new javax.xml.namespace.QName("","PARAMETER5"));
                            
                                      attribList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localPARAMETER5));
                                

                return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());
            
            

        }

  

     /**
      *  Factory class that keeps the parse method
      */
    public static class Factory{

        
        

        /**
        * static method to create the object
        * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
        *                If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
        * Postcondition: If this object is an element, the reader is positioned at its end element
        *                If this object is a complex type, the reader is positioned at the end element of its outer element
        */
        public static InsertRequesttype parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception{
            InsertRequesttype object =
                new InsertRequesttype();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix ="";
            java.lang.String namespaceuri ="";
            try {
                
                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                
                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance","type")!=null){
                  java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                        "type");
                  if (fullTypeName!=null){
                    java.lang.String nsPrefix = null;
                    if (fullTypeName.indexOf(":") > -1){
                        nsPrefix = fullTypeName.substring(0,fullTypeName.indexOf(":"));
                    }
                    nsPrefix = nsPrefix==null?"":nsPrefix;

                    java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":")+1);
                    
                            if (!"insertRequesttype".equals(type)){
                                //find namespace for the prefix
                                java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                                return (InsertRequesttype)org.aurora_framework.www.schema.ExtensionMapper.getTypeObject(
                                     nsUri,type,reader);
                              }
                        

                  }
                

                }

                

                
                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();
                

                
                    // handle attribute "OPERATION_TYPE"
                    java.lang.String tempAttribOPERATION_TYPE =
                        
                                reader.getAttributeValue(null,"OPERATION_TYPE");
                            
                   if (tempAttribOPERATION_TYPE!=null){
                         java.lang.String content = tempAttribOPERATION_TYPE;
                        
                                                 object.setOPERATION_TYPE(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribOPERATION_TYPE));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("OPERATION_TYPE");
                    
                    // handle attribute "PARAMETER1"
                    java.lang.String tempAttribPARAMETER1 =
                        
                                reader.getAttributeValue(null,"PARAMETER1");
                            
                   if (tempAttribPARAMETER1!=null){
                         java.lang.String content = tempAttribPARAMETER1;
                        
                                                 object.setPARAMETER1(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribPARAMETER1));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("PARAMETER1");
                    
                    // handle attribute "PARAMETER2"
                    java.lang.String tempAttribPARAMETER2 =
                        
                                reader.getAttributeValue(null,"PARAMETER2");
                            
                   if (tempAttribPARAMETER2!=null){
                         java.lang.String content = tempAttribPARAMETER2;
                        
                                                 object.setPARAMETER2(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribPARAMETER2));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("PARAMETER2");
                    
                    // handle attribute "PARAMETER3"
                    java.lang.String tempAttribPARAMETER3 =
                        
                                reader.getAttributeValue(null,"PARAMETER3");
                            
                   if (tempAttribPARAMETER3!=null){
                         java.lang.String content = tempAttribPARAMETER3;
                        
                                                 object.setPARAMETER3(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribPARAMETER3));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("PARAMETER3");
                    
                    // handle attribute "PARAMETER4"
                    java.lang.String tempAttribPARAMETER4 =
                        
                                reader.getAttributeValue(null,"PARAMETER4");
                            
                   if (tempAttribPARAMETER4!=null){
                         java.lang.String content = tempAttribPARAMETER4;
                        
                                                 object.setPARAMETER4(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribPARAMETER4));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("PARAMETER4");
                    
                    // handle attribute "PARAMETER5"
                    java.lang.String tempAttribPARAMETER5 =
                        
                                reader.getAttributeValue(null,"PARAMETER5");
                            
                   if (tempAttribPARAMETER5!=null){
                         java.lang.String content = tempAttribPARAMETER5;
                        
                                                 object.setPARAMETER5(
                                                    org.apache.axis2.databinding.utils.ConverterUtil.convertToString(tempAttribPARAMETER5));
                                            
                    } else {
                       
                    }
                    handledAttributes.add("PARAMETER5");
                    
                    
                    reader.next();
                



            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

        }//end of factory class

        

        }
           
    