package mw.pathclient;

import net.java.dev.jaxb.array.StringArray;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.ws.Action;
import javax.xml.ws.FaultAction;


/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.9-b130926.1035 Generated source version: 2.2
 */
@WebService(name = "MWPathService", targetNamespace = "http://path.mw/")
@SOAPBinding(style = SOAPBinding.Style.RPC)
@XmlSeeAlso({
    ObjectFactory.class,
    net.java.dev.jaxb.array.ObjectFactory.class
})
public interface MWPathService {

    /**
     * @param arg1
     * @param arg0
     *
     * @return returns net.java.dev.jaxb.array.StringArray
     *
     * @throws MWNoPathException_Exception
     */
    @WebMethod
    @WebResult(partName = "return")
    @Action(input = "http://path.mw/MWPathService/calculatePathRequest", output = "http://path.mw/MWPathService/calculatePathResponse", fault = {
        @FaultAction(className = MWNoPathException_Exception.class, value = "http://path.mw/MWPathService/calculatePath/Fault/MWNoPathException")
    })
    public StringArray calculatePath(
        @WebParam(name = "arg0", partName = "arg0")
        String arg0,
        @WebParam(name = "arg1", partName = "arg1")
        String arg1)
        throws MWNoPathException_Exception
    ;

}
