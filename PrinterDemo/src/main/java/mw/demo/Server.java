package mw.demo;

import mw.demo.printer.generated.MWText;
import mw.demo.printer.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.Scanner;

/**
 * Created by stephan on 25.05.15.
 */
@WebServiceProvider
@ServiceMode(value = Service.Mode.PAYLOAD)
public class Server implements Provider<Source> {
    @javax.annotation.Resource(type = WebServiceContext.class)
    protected WebServiceContext wsContext;

    public static void main(String[] args) {
        Endpoint endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING,
            new Server());
        endpoint.publish("http://localhost:12345/printer-service/");

        System.out.println("Printer service is ready to process client requests.");

        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("\"exit\" for exit");
        } while (!scanner.next().equals("exit"));

        System.out.println("Shutdown Printer service");
        endpoint.stop();
    }

    public Source invoke(Source source) {
        MessageContext mc = wsContext.getMessageContext();
        String httpMethod = (String) mc.get(MessageContext.HTTP_REQUEST_METHOD);
        String path = (String) mc.get(MessageContext.PATH_INFO);
        System.out.println(httpMethod + " request, printer " + path);

        // Erzeugung des Binding-Context
        String contextPath = "mw.demo.printer.generated";
        JAXBContext jc = null;
        try {
            jc = JAXBContext.newInstance(contextPath);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
// Unmarshalling der Anfrage
        Unmarshaller u = null;
        try {
            u = jc.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        JAXBElement request = null;
        try {
            request = (JAXBElement) u.unmarshal(source);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
// Auspacken des Aufrufparameters
        MWText input = (MWText) request.getValue();
        String text = input.getText();


        // Erzeugung der Objekt-Factory
        ObjectFactory f = new ObjectFactory();
// Erzeugung des Rueckgabewerts
        MWText status = f.createMWText();
        status.setText("OK");
// Erzeugung der Antwort
        JAXBElement<MWText> reply = f.createMWPrinterReply(status);
// Return aus der invoke()-Methode
        Source replySource = null;
        try {
            replySource = new JAXBSource(jc, reply);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        return replySource;

    }
}
