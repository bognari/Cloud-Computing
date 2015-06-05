
package mw.facebookclient;

import javax.xml.ws.WebFault;


/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.9-b130926.1035 Generated source version: 2.2
 */
@WebFault(name = "MWUnknownIDException", targetNamespace = "http://facebook.mw/")
public class MWUnknownIDException_Exception
    extends Exception {

    /**
     * Java type that goes as soapenv:Fault detail element.
     */
    private MWUnknownIDException faultInfo;

    /**
     * @param faultInfo
     * @param message
     */
    public MWUnknownIDException_Exception(String message, MWUnknownIDException faultInfo) {
        super(message);
        this.faultInfo = faultInfo;
    }

    /**
     * @param faultInfo
     * @param cause
     * @param message
     */
    public MWUnknownIDException_Exception(String message, MWUnknownIDException faultInfo, Throwable cause) {
        super(message, cause);
        this.faultInfo = faultInfo;
    }

    /**
     * @return returns fault bean: mw.facebook.MWUnknownIDException
     */
    public MWUnknownIDException getFaultInfo() {
        return faultInfo;
    }

}