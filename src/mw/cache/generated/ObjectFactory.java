//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Aenderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren.
// Generiert: 2015.05.22 um 03:32:09 PM CEST 
//


package mw.cache.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * mw.cache.generated package. <p>An ObjectFactory allows you to programatically construct new instances of the Java
 * representation for XML content. The Java representation of XML content can consist of schema derived interfaces and
 * classes representing the binding of schema type definitions, element declarations and model groups.  Factory methods
 * for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MWMessageBody_QNAME = new QName("http://www.ibr.cs.tu-bs.de/courses/ss15/cc/ue02/cache/schema", "MWMessageBody");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * mw.cache.generated
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MWEntry }
     */
    public MWEntry createMWEntry() {
        return new MWEntry();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MWEntry }{@code >}}
     */
    @XmlElementDecl(namespace = "http://www.ibr.cs.tu-bs.de/courses/ss15/cc/ue02/cache/schema", name = "MWMessageBody")
    public JAXBElement<MWEntry> createMWMessageBody(MWEntry value) {
        return new JAXBElement<MWEntry>(_MWMessageBody_QNAME, MWEntry.class, null, value);
    }

}
