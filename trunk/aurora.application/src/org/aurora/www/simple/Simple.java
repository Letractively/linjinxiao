

/**
 * Simple.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.6.2  Built on : Apr 17, 2012 (05:33:49 IST)
 */

    package org.aurora.www.simple;

    /*
     *  Simple java interface
     */

    public interface Simple {
          

        /**
          * Auto generated method signature
          * 
                    * @param cancatRequest0
                
         */

         
                     public org.aurora.www.simple.CancatResponse cancat(

                        org.aurora.www.simple.CancatRequest cancatRequest0)
                        throws java.rmi.RemoteException
             ;

        
         /**
            * Auto generated method signature for Asynchronous Invocations
            * 
                * @param cancatRequest0
            
          */
        public void startcancat(

            org.aurora.www.simple.CancatRequest cancatRequest0,

            final org.aurora.www.simple.SimpleCallbackHandler callback)

            throws java.rmi.RemoteException;

     

        
       //
       }
    