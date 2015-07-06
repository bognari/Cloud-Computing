package mw.pathclient;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * mw.path package. <p>An ObjectFactory allows you to programatically construct new instances of the Java representation
 * for XML content. The Java representation of XML content can consist of schema derived interfaces and classes
 * representing the binding of schema type definitions, element declarations and model groups.  Factory methods for each
 * of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MWNoPathException_QNAME = new QName("http://path.mw/", "MWNoPathException");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * mw.pathclient
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MWNoPathException }
     */
    public MWNoPathException createMWNoPathException() {
        return new MWNoPathException();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MWNoPathException }{@code >}}
     */
    @XmlElementDecl(namespace = "http://path.mw/", name = "MWNoPathException")
    public JAXBElement<MWNoPathException> createMWNoPathException(MWNoPathException value) {
        return new JAXBElement<MWNoPathException>(_MWNoPathException_QNAME, MWNoPathException.class, null, value);
    }

}
