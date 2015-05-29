package mw.cache;

import mw.MWRegistryAccess;
import mw.cache.generated.MWEntry;
import mw.cache.generated.MWStatus;
import mw.cache.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.registry.JAXRException;
import javax.xml.transform.Source;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;


/**
 * Caching service.
 * <p>
 * Aus der Aufgabenstellung: &quot;Die Cache-Implementierung wird von der Klasse {@code MWCache} bereitgestellt. Diese
 * implementiert die Provider-Schnittstelle für Web-Services, da sie direkt die Payloads der eintreffenden SOAP-Anfragen
 * verarbeiten soll. <code> public class MWCache implements Provider<Source> { public Source invoke(Source source); }
 * </code> Der Web-Service-Endpunkt sorgt dafür, dass für jede an den Cache-Service gerichtete Anfrage die Methode
 * {@code invoke()} aufgerufen wird, unabhängig davon, ob es sich um einen {@code addObject()}- oder einen {@code
 * getObject()}-Aufruf handelt. Eine einfache Möglichkeit, Anfragen beider Varianten voneinander zu unterscheiden ist
 * die verwendete HTTP-Methode: Wie in REST üblich sollen zum Anlegen von Objekten {@code POST}-Anfragen zum Einsatz
 * kommen, das Abfragen von Objekten erfolgt mit {@code GET}. Jedem im Cache verwalteten Objekt ist eine eindeutige
 * Objekt-URL zugeordnet, die sowohl beim Anlegen als auch beim Auslesen des Objekts verwendet wird. Diese setzt sich
 * typischerweise aus der URL des Cache-Diensts sowie dem Schlüssel des adressierten Objekts zusammen, z. B. {@code
 * http://localhost:12345/cache/my-object-key}.&quot;
 *
 * @author Florian Bahr
 * @since Aufgabe 2.1.2
 */
@WebServiceProvider
@ServiceMode(value = Service.Mode.PAYLOAD)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public class MWCache implements Provider<Source> {

    /**
     * ...
     */
    private static final String CONTEXT_PATH = "mw.cache.generated";
    /**
     * The HashMap that contains the cached content.
     */
    protected final Map<String, String> cache; // not thread-safe; but: good enough
    /**
     * ...
     */
    private final JAXBContext jaxbContext;
    /**
     * ...
     */
    private final Unmarshaller unmarshaller;
    /**
     * ...
     */
    private final ObjectFactory factory;
    /**
     * Reference to the package of the xjc-generated classes (required to unmarshal incoming requests).
     */
    @javax.annotation.Resource(type = WebServiceContext.class)
    protected WebServiceContext wsContext;

    public MWCache() {
        // Instantiation of the binding context, unmarshaller
        try {
            jaxbContext = JAXBContext.newInstance(MWCache.CONTEXT_PATH);
            unmarshaller = jaxbContext.createUnmarshaller();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        factory = new ObjectFactory(); //< mw.cache.generated.ObjectFactory

        cache = Collections.synchronizedMap(new MyMap<String, String>(1000));
    }

    /**
     * Starts the web service.
     * <p>
     * ...
     */
    public static void main(String[] args) {
        String serviceUrl_out;

        try {
            if (args.length > 0 && args[0].equals("l")) {
                throw new IOException();
            }
            Scanner scanner = new Scanner(new URL("http://169.254.169.254/latest/meta-data/public-ipv4").openStream());
            serviceUrl_out = String.format("http://%s:12345/cache-service", scanner.nextLine());
        } catch (IOException e) {
            e.printStackTrace();
            try {
                serviceUrl_out = String.format("http://%s:12345/cache-service", InetAddress.getLocalHost().getHostAddress());
            } catch (UnknownHostException e1) {
                e1.printStackTrace();
                serviceUrl_out = "http://localhost:12345/cache-service";
            }
        }

        String serviceUrl_in;
        try {
            serviceUrl_in = String.format("http://%s:12345/cache-service", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e1) {
            e1.printStackTrace();
            serviceUrl_in = "http://localhost:12345/cache-service";
        }

        System.out.println("Initializing MWCache service...");
        System.out.println("serviceUrl_out = " + serviceUrl_out);
        System.out.println("serviceUrl_in = " + serviceUrl_in);

        // Instantiation and publication of the service endpoint ...
        MWCache mwcache = new MWCache();

        Endpoint endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING, mwcache);
        endpoint.publish(serviceUrl_in);

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url;
        try {
            url = MWRegistryAccess.getRegistryURL();
        } catch (IOException e1) {
            System.err.printf("Konnte die URL nicht laden%n");
            e1.printStackTrace();
            return;
        }

        Properties config;

        try {
            config = MWRegistryAccess.getConfig();
        } catch (IOException e1) {
            System.err.printf("Konnte config.property nicht finden%n");
            return;
        }

        mwRegistryAccess.openConnection(url + "/inquiry", url + "/publish");

        try {
            mwRegistryAccess.authenticate(config.getProperty("user"), config.getProperty("password"));
        } catch (JAXRException e1) {
            System.err.printf("Keine Anmeldung an der Registry möglich%n");
            return;
        }

        mwRegistryAccess.registerService(config.getProperty("user"), "MWCacheService", serviceUrl_out);
        mwRegistryAccess.closeConnection();

        System.out.println("mwCache ready: " + endpoint.isPublished());
        System.out.println("Run at " + serviceUrl_out);

        Scanner scanner = new Scanner(System.in);

        try {
            do {
                System.out.println("\"exit\" for exit");
            } while (!scanner.next().equals("exit"));

        } catch (NoSuchElementException ignored) {
            while (true) {
                try {
                    Thread.sleep(Long.MAX_VALUE);
                } catch (InterruptedException ignored1) {

                }
            }
        }

        System.out.println("Shutdown MWCache service");
        endpoint.stop();
    }

    /**
     * Called on every request to the web service.
     *
     * @param requestSource {@link Source} instance provided by client call
     *
     * @return {@link Source} instance returned to client
     */
    @Override
    public Source invoke(Source requestSource) {

        // Analyze request, compose & submit response
        if (wsContext == null) {
            // Dependency injection failed...
            return sendException(new HTTPException(MWStatus.HTTP_INTERNAL_ERROR.value()));
        } else {
            // Access the message context and extract request details (httpMethod, pathInfo) ...
            MessageContext messageContext = wsContext.getMessageContext();
            String httpMethod = ((String) messageContext.get(MessageContext.HTTP_REQUEST_METHOD)).toUpperCase(Locale.ENGLISH);
            String pathInfo = (String) messageContext.get(MessageContext.PATH_INFO);

            System.out.println("HTTP_METHOD: " + httpMethod);
            System.out.println("PATH_INFO  : " + pathInfo);

            switch (httpMethod) {
                case "POST":
                    return processAddRequest(pathInfo, requestSource);

                case "GET":
                    return processGetRequest(pathInfo);

                default:
                    return sendException(new HTTPException(MWStatus.HTTP_BAD_METHOD.value()));
            }
        }
    }

    private Source sendException(Exception exception) {
        boolean internal_error = !(exception instanceof HTTPException) || (((HTTPException) exception).getStatusCode() == MWStatus.HTTP_INTERNAL_ERROR.value());

        MWEntry mwEntry = factory.createMWEntry();
        mwEntry.setStatus(internal_error ? MWStatus.HTTP_INTERNAL_ERROR : MWStatus.HTTP_BAD_REQUEST); // eigentl. MWCache.HTTP_BAD_METHOD, aber...
        mwEntry.setMessage(exception.getMessage());
        JAXBElement<MWEntry> response = factory.createMWMessageBody(mwEntry);

        try {
            return new JAXBSource(jaxbContext, response);
        } catch (JAXBException e) {
            return null;
        }
    }


    /**
     * Adds an object to the cache and returns a {@link Source} instance ...
     *
     * @param key           key of the object to be added to cache
     * @param requestSource (unmarshalled) details for the incoming request
     *
     * @return {@link Source} instance ...
     */
    private Source processAddRequest(String key, Source requestSource) {
        try {

            JAXBElement requestMessageBody = (JAXBElement) unmarshaller.unmarshal(requestSource);
            MWEntry entry = (MWEntry) requestMessageBody.getValue();

            String previousValue = cache.put(key, entry.getMessage());
            MWEntry responseMessageBody = factory.createMWEntry();

            if (previousValue == null) {
                responseMessageBody.setStatus(MWStatus.HTTP_CREATED);
            } else {
                responseMessageBody.setMessage(previousValue);
                responseMessageBody.setStatus(MWStatus.HTTP_OK);
            }

            return new JAXBSource(jaxbContext, factory.createMWMessageBody(responseMessageBody));
        } catch (JAXBException e) {
            return sendException(e);
        }
    }

    /**
     * Looks up an object in the cache by its key and returns a {@link Source} instance marshalling a {@link MWEntry}
     * object comprised of a key, its associated value, and a {@code HTTP_OK} status message &ndash; if the key exists.
     * Otherwise, the response message object is flagged with a {@code HTTP_NOT_FOUND} error code.
     *
     * @param key key of the object to look up in cache
     *
     * @return {@link Source} instance ...
     */
    private Source processGetRequest(String key) {
        try {
            MWEntry responseMessageBody = factory.createMWEntry();

            if (!cache.containsKey(key)) {
                responseMessageBody.setStatus(MWStatus.HTTP_NOT_FOUND);
                responseMessageBody.setMessage(key);
            } else {
                responseMessageBody.setStatus(MWStatus.HTTP_OK);
                responseMessageBody.setMessage(cache.get(key));
            }

            return new JAXBSource(jaxbContext, factory.createMWMessageBody(responseMessageBody));
        } catch (JAXBException e) {
            return sendException(e);
        }
    }
}
