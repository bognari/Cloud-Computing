//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.25 um 04:00:47 AM CEST 
//


package mw.demo.printer.generated;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each Java content interface and Java element interface generated in the
 * mw.demo.printer.generated package. <p>An ObjectFactory allows you to programatically construct new instances of the
 * Java representation for XML content. The Java representation of XML content can consist of schema derived interfaces
 * and classes representing the binding of schema type definitions, element declarations and model groups.  Factory
 * methods for each of these are provided in this class.
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _MWPrinterRequest_QNAME = new QName("", "MWPrinterRequest");
    private final static QName _MWPrinterReply_QNAME = new QName("", "MWPrinterReply");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package:
     * mw.demo.printer.generated
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link MWText }
     */
    public MWText createMWText() {
        return new MWText();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MWText }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "MWPrinterRequest")
    public JAXBElement<MWText> createMWPrinterRequest(MWText value) {
        return new JAXBElement<MWText>(_MWPrinterRequest_QNAME, MWText.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link MWText }{@code >}}
     */
    @XmlElementDecl(namespace = "", name = "MWPrinterReply")
    public JAXBElement<MWText> createMWPrinterReply(MWText value) {
        return new JAXBElement<MWText>(_MWPrinterReply_QNAME, MWText.class, null, value);
    }

}
