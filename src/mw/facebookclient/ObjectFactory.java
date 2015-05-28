
package mw.facebookclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the mw.facebook package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MWUnknownIDException_QNAME = new QName("http://facebook.mw/", "MWUnknownIDException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: mw.facebook
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MWUnknownIDException }
     * 
     */
    public MWUnknownIDException createMWUnknownIDException() {
        return new MWUnknownIDException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MWUnknownIDException }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://facebook.mw/", name = "MWUnknownIDException")
    public JAXBElement<MWUnknownIDException> createMWUnknownIDException(MWUnknownIDException value) {
        return new JAXBElement<MWUnknownIDException>(_MWUnknownIDException_QNAME, MWUnknownIDException.class, null, value);
    }

}
