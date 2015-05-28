
package mw.facebookclient;

import javax.xml.namespace.QName;
import javax.xml.ws.*;
import java.net.MalformedURLException;
import java.net.URL;


/**
 * This class was generated by the JAX-WS RI.
 * JAX-WS RI 2.2.9-b130926.1035
 * Generated source version: 2.2
 * 
 */
@WebServiceClient(name = "MWFacebookService", targetNamespace = "http://facebook.mw/", wsdlLocation = "http://134.169.47.205:4223/MWFacebookService?wsdl")
public class MWFacebookService
    extends Service {

    private final static URL MWFACEBOOKSERVICE_WSDL_LOCATION;
    private final static WebServiceException MWFACEBOOKSERVICE_EXCEPTION;
    private final static QName MWFACEBOOKSERVICE_QNAME = new QName("http://facebook.mw/", "MWFacebookService");

    static {
        URL url = null;
        WebServiceException e = null;
        try {
            url = new URL("http://134.169.47.205:4223/MWFacebookService?wsdl");
        } catch (MalformedURLException ex) {
            e = new WebServiceException(ex);
        }
        MWFACEBOOKSERVICE_WSDL_LOCATION = url;
        MWFACEBOOKSERVICE_EXCEPTION = e;
    }

    public MWFacebookService() {
        super(__getWsdlLocation(), MWFACEBOOKSERVICE_QNAME);
    }

    public MWFacebookService(WebServiceFeature... features) {
        super(__getWsdlLocation(), MWFACEBOOKSERVICE_QNAME, features);
    }

    public MWFacebookService(URL wsdlLocation) {
        super(wsdlLocation, MWFACEBOOKSERVICE_QNAME);
    }

    public MWFacebookService(URL wsdlLocation, WebServiceFeature... features) {
        super(wsdlLocation, MWFACEBOOKSERVICE_QNAME, features);
    }

    public MWFacebookService(URL wsdlLocation, QName serviceName) {
        super(wsdlLocation, serviceName);
    }

    public MWFacebookService(URL wsdlLocation, QName serviceName, WebServiceFeature... features) {
        super(wsdlLocation, serviceName, features);
    }

    private static URL __getWsdlLocation() {
        if (MWFACEBOOKSERVICE_EXCEPTION != null) {
            throw MWFACEBOOKSERVICE_EXCEPTION;
        }
        return MWFACEBOOKSERVICE_WSDL_LOCATION;
    }

    /**
     *
     * @return
     *     returns MWMyFacebookService
     */
    @WebEndpoint(name = "MWMyFacebookServicePort")
    public MWMyFacebookService getMWMyFacebookServicePort() {
        return super.getPort(new QName("http://facebook.mw/", "MWMyFacebookServicePort"), MWMyFacebookService.class);
    }

    /**
     *
     * @param features
     *     A list of {@link WebServiceFeature} to configure on the proxy.  Supported features not in the <code>features</code> parameter will have their default values.
     * @return
     *     returns MWMyFacebookService
     */
    @WebEndpoint(name = "MWMyFacebookServicePort")
    public MWMyFacebookService getMWMyFacebookServicePort(WebServiceFeature... features) {
        return super.getPort(new QName("http://facebook.mw/", "MWMyFacebookServicePort"), MWMyFacebookService.class, features);
    }

}