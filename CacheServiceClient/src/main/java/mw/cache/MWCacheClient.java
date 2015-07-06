package mw.cache;

import mw.MWRegistryAccess;
import mw.cache.generated.MWEntry;
import mw.cache.generated.MWStatus;
import mw.cache.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.registry.JAXRException;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.io.IOException;
import java.util.Map;
import java.util.NoSuchElementException;


/**
 * Client to the MWCache service implementation.
 * <p/>
 * This class provides two methods ({@link MWCacheClient#addObject(String, String)}, {@link
 * MWCacheClient#getObject(String)} &ndash; which allow for accessing the cache as if it was running locally.
 *
 * @author Florian Bahr
 * @since Aufgabe 2.1
 */
public class MWCacheClient {

    /**
     * ...
     */
    public static final String CONTEXT_PATH = "mw.cache.generated";

    private final JAXBContext jaxbContext;
    /**
     * {@link mw.cache.generated.ObjectFactory} instance.
     */
    private final ObjectFactory factory;
    private final QName qName;
    private final String url;

    public MWCacheClient() {
        JAXBContext jaxbContext1;
        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();
        String url = "";
        try {
            url = mwRegistryAccess.getServiceURL("gruppe11", "MWCacheService");
        } catch (JAXRException e) {
            System.err.printf("Die Registry reagiert nicht.%n");
            System.exit(-1);
        } catch (NoSuchElementException e) {
            System.err.printf("Keine Einträge für den Service \"MWCacheService\" vorhande.n%n");
            System.exit(-1);
        } catch (IOException e) {
            System.err.printf("Konnte die Propertiesdatei nicht laden.%n");
            System.exit(-1);
        }

        this.url = url;

        qName = new QName("", "MWCacheService");

        try {
            jaxbContext1 = JAXBContext.newInstance(MWCacheClient.CONTEXT_PATH);
        } catch (JAXBException e) {
            jaxbContext1 = null;
            System.err.printf("Konnte den Kontext nicht laden.%n");
            System.exit(-1);
        }

        jaxbContext = jaxbContext1;
        factory = new ObjectFactory();
    }

    /**
     * Main method.
     *
     * @param args
     */
    public static void main(String[] args) {
        // args = new String[] {"add", "hello", "world"};
        // args = new String[] {"get", "hello"};
        // args = new String[] {"get", "1387911094"};

        boolean wellFormedRequest = ((args.length == 2) && args[0].toUpperCase().equals("GET"))
            || ((args.length == 3) && args[0].toUpperCase().equals("ADD"));

        if (!wellFormedRequest) {
            System.out.println("Aufrufschema: MWCacheClient (ADD|GET) key [value]");
            System.exit(-1);
        }

        MWCacheClient cacheClient = new MWCacheClient();

        switch (args[0].toUpperCase()) {
            case "GET":
                cacheClient.getObject(args[1]);
                break;

            case "ADD":
                cacheClient.addObject(args[1], args[2]);
                break;
        }
    }

    /**
     * Adds an object ({@code value}) to be identified using {@code key} to the cache.
     *
     * @param key   object identifier
     * @param value object
     */
    public void addObject(String key, String value) {
        Service service = Service.create(qName);
        System.out.printf("Post %s/%s %s%n", url, key, value);
        service.addPort(qName, HTTPBinding.HTTP_BINDING, String.format("%s/%s", url, key));
        Dispatch<Object> disp = service.createDispatch(qName, jaxbContext, Service.Mode.PAYLOAD);

        // Preparing request object
        MWEntry requestMessageBody = factory.createMWEntry();
        requestMessageBody.setMessage(value);
        requestMessageBody.setStatus(MWStatus.HTTP_OK);

        // Marshalling the request object
        JAXBElement<MWEntry> request = factory.createMWMessageBody(requestMessageBody);

        Map<String, Object> rc = disp.getRequestContext();
        rc.put(MessageContext.HTTP_REQUEST_METHOD, "POST");

        // Invoking synchronous dispatch, i.e.: send request, block 'til retrieving response
        JAXBElement reply = (JAXBElement) disp.invoke(request);

        MWEntry entry = (MWEntry) reply.getValue();
        MWStatus status = entry.getStatus();


        if (!(status.equals(MWStatus.HTTP_OK) || status.equals(MWStatus.HTTP_CREATED))) {
            System.out.printf("Warning: invocation of MWCacheClient::addObject(%s, ...) failed with status: %s%n", key, status);
        }
    }


    /**
     * Retrieves a cached value (object) by its key from MWCache. In case of a cache miss, a {@link
     * MWNoSuchKeyException} is thrown.
     *
     * @param key the key (object identifier) associated with a value (object)
     *
     * @return the cached value
     *
     * @throws MWNoSuchKeyException in case of a cache miss
     */
    public String getObject(String key) throws MWNoSuchKeyException {
        Service service = Service.create(qName);
        System.out.printf("Get %s/%s%n", url, key);
        service.addPort(qName, HTTPBinding.HTTP_BINDING, String.format("%s/%s", url, key));
        Dispatch<Object> disp = service.createDispatch(qName, jaxbContext, Service.Mode.PAYLOAD);

        // Preparing request object
        MWEntry requestMessageBody = factory.createMWEntry();

        // Marshalling the request object
        JAXBElement<MWEntry> request = factory.createMWMessageBody(requestMessageBody);

        Map<String, Object> rc = disp.getRequestContext();
        rc.put(MessageContext.HTTP_REQUEST_METHOD, "GET");

        // Invoking synchronous dispatch, i.e.: send request, block 'til retrieving response
        JAXBElement reply = (JAXBElement) disp.invoke(request);

        MWEntry entry = (MWEntry) reply.getValue();
        MWStatus status = entry.getStatus();


        if (status.equals(MWStatus.HTTP_NOT_FOUND)) {
            throw new MWNoSuchKeyException(status + ": Key '" + key + "' unknown.");
        }

        return entry.getMessage();
    }

}
