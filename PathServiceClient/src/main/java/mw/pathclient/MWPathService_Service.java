package mw.pathclient;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This class was generated by the JAX-WS RI. JAX-WS RI 2.2.9-b130926.1035 Generated source version: 2.2
 */
@WebServiceClient(name = "MWPathService", targetNamespace = "http://path.mw/", wsdlLocation = "http://134.169.237.171:12347/mwPathService?wsdl")
public class MWPathService_Service
    extends Service {

    private final static URL MWPATHSERVICE_WSDL_LOCATION;
    private final static WebServiceException MWPATHSERVICE_EXCEPTION;
    private final static QName MWPATHSERVICE_QNAME = new QName("http://path.mw/", "MWPathService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://134.169.237.171:12347/mwPathService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        MWPATHSERVICE_WSDL_LOCATION = url;
        MWPATHSERVICE_EXCEPTION = e;
    }

    public MWPathService_Service() {
        super(__getWsdlLocation(), MWPATHSERVICE_QNAME);
    }

    public MWPathService_Service(WebServiceFeature... features) {
        super(__getWsdlLocation(), MWPATHSERVICE_QNAME, features);
    }

    public MWPathService_Service(URL wsdlLocation) {
        super(wsdlLocation, MWPATHSERVICE_QNAME);
    }

    public MWPathService_Service(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, MWPATHSERVICE_QNAME, features);
    }

    public MWPathService_Service(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MWPathService_Service(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    private static URL __getWsdlLocation() {
        if (MWPATHSERVICE_EXCEPTION != null) {
            throw MWPATHSERVICE_EXCEPTION;
        }
        return MWPATHSERVICE_WSDL_LOCATION;
    }

    /**
     * @return returns MWPathService
     */
    @WebEndpoint(name = "MWPathServicePort")
    public MWPathService getMWPathServicePort() {
        return super.getPort(new QName("http://path.mw/", "MWPathServicePort"), MWPathService.class);
    }

    /**
     * @param features A list of {@link WebServiceFeature} to configure on the proxy.  Supported features not in the
     *                 <code>features</code> parameter will have their default values.
     *
     * @return returns MWPathService
     */
    @WebEndpoint(name = "MWPathServicePort")
    public MWPathService getMWPathServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://path.mw/", "MWPathServicePort"), MWPathService.class, features);
    }

}
