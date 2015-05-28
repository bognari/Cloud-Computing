package mw.demo;

import mw.demo.printer.generated.MWText;
import mw.demo.printer.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.Map;

/**
 * Created by stephan on 25.05.15.
 */
public class Client {

    public static void main(String[] args) {
        Client client = new Client();
        client.print("testprinter", "test hallo du da");
    }

    public void print(String printer, String text) {
// Zusammenstellung der Ressourcen-Adresse
        String path = "http://localhost:12345/printer-service/" + printer;
// Konfiguration der Service-Verbindung
        QName qName = new QName("", ""); // Service-Endpunkt-Name
        Service service = Service.create(qName);
        service.addPort(qName, HTTPBinding.HTTP_BINDING, path);
        // Erzeugung des Binding-Context
        String contextPath = "mw.demo.printer.generated";
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(contextPath);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
// Erzeugung des Dispatch
        Dispatch<Object> disp = service.createDispatch(qName, jc, Service.Mode.PAYLOAD);
// Festlegung der HTTP-Methode
        Map<String, Object> rc = disp.getRequestContext();
        rc.put(MessageContext.HTTP_REQUEST_METHOD, "POST");

        // Erzeugung der Objekt-Factory
        ObjectFactory f = new ObjectFactory();
// Erzeugung des Aufrufparameters
        MWText input = f.createMWText();
        input.setText(text); // text: zu druckende Zeichenkette
// Erzeugung der Anfrage
        JAXBElement<MWText> request = f.createMWPrinterRequest(input);


        // Senden der Anfrage und Empfang der Antwort
        JAXBElement reply = (JAXBElement) disp.invoke(request);
// Auswertung der Antwort
        MWText status = (MWText) reply.getValue();

        System.out.println("STATUS: " + status.getText());
    }
}
