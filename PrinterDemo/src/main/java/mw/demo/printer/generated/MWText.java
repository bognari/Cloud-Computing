//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2015.05.25 um 04:00:47 AM CEST 
//


package mw.demo.printer.generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für MWText complex type.
 * <p>
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p>
 * <pre>
 * &lt;complexType name="MWText">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="text" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MWText", propOrder = {
    "text"
})
public class MWText {

    @XmlElement(required = true)
    protected String text;

    /**
     * Ruft den Wert der text-Eigenschaft ab.
     *
     * @return possible object is {@link String }
     */
    public String getText() {
        return text;
    }

    /**
     * Legt den Wert der text-Eigenschaft fest.
     *
     * @param value allowed object is {@link String }
     */
    public void setText(String value) {
        this.text = value;
    }

}
