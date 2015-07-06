package mw.pathclient;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für MWNoPathException complex type.
 * <p/>
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * <p/>
 * <pre>
 * &lt;complexType name="MWNoPathException">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="message" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MWNoPathException", propOrder = {
    "message"
})
public class MWNoPathException {

    protected String message;

    /**
     * Ruft den Wert der message-Eigenschaft ab.
     *
     * @return possible object is {@link String }
     */
    public String getMessage() {
        return message;
    }

    /**
     * Legt den Wert der message-Eigenschaft fest.
     *
     * @param value allowed object is {@link String }
     */
    public void setMessage(String value) {
        this.message = value;
    }

}
