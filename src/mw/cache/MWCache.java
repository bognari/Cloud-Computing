package mw.cache;

import mw.cache.generated.MWEntry;
import mw.cache.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Source;
import javax.xml.ws.*;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import javax.xml.ws.http.HTTPException;
import java.util.HashMap;
import java.util.Map;


/**
 * Caching service.
 * <p/>
 * Aus der Aufgabenstellung: &quot;Die Cache-Implementierung wird von der Klasse {@code MWCache} bereitgestellt. Diese
 * implementiert die Provider-Schnittstelle f�r Web-Services, da sie direkt die Payloads der eintreffenden SOAP-Anfragen
 * verarbeiten soll. <code> public class MWCache implements Provider<Source> { public Source invoke(Source source); }
 * </code> Der Web-Service-Endpunkt sorgt daf�r, dass f�r jede an den Cache-Service gerichtete Anfrage die Methode
 * {@code invoke()} aufgerufen wird, unabh�ngig davon, ob es sich um einen {@code addObject()}- oder einen {@code
 * getObject()}-Aufruf handelt. Eine einfache M�glichkeit, Anfragen beider Varianten voneinander zu unterscheiden ist
 * die verwendete HTTP-Methode: Wie in REST �blich sollen zum Anlegen von Objekten {@code POST}-Anfragen zum Einsatz
 * kommen, das Abfragen von Objekten erfolgt mit {@code GET}. Jedem im Cache verwalteten Objekt ist eine eindeutige
 * Objekt-URL zugeordnet, die sowohl beim Anlegen als auch beim Auslesen des Objekts verwendet wird. Diese setzt sich
 * typischerweise aus der URL des Cache-Diensts sowie dem Schl�ssel des adressierten Objekts zusammen, z. B. {@code
 * http://localhost:12345/cache/my-object-key}.&quot;
 *
 * @author Florian Bahr
 * @since Aufgabe 2.1.2
 */
@WebServiceProvider
@ServiceMode(value = Service.Mode.PAYLOAD)
@BindingType(value = HTTPBinding.HTTP_BINDING)
public class MWCache implements Provider<Source> {

    public static final String HTTP_OK = "200 OK";
    public static final String HTTP_CREATED = "201 CREATED";
    public static final String HTTP_BAD_REQUEST = "400 BAD REQUEST";
    public static final String HTTP_NOT_FOUND = "404 NOT FOUND";
    public static final String HTTP_BAD_METHOD = "405 BAD METHOD";
    public static final String HTTP_INTERNAL_ERROR = "500 INTERNAL SERVER ERROR";

    /**
     public static final String STATUS_HTTP_OK          = "Request processed successfully.";
     public static final String STATUS_HTTP_OK_REPLACED = "Request processed successfully. A previous cache entry has been overwritten.";
     public static final String STATUS_HTTP_NOT_FOUND   = "The key provided does not exist.";
     public static final String STATUS_MSG_BAD_METHOD   = "Unsupported HTTP method used. This WebService only supports HTTP GET and POST methods.";
     public static final String STATUS_INTERNAL_ERROR   = "An internal error occured while processing the request.";
     */

    /**
     * The default URL under which the web service will be made available when no other URL is specified.
     */
    public static final String SERVICE_URL = "http://localhost:12345/cache-service/";

    /**
     * ...
     */
    public static final String CONTEXT_PATH = "mw.cache.generated";
    /**
     * ...
     */
    protected final JAXBContext jaxbContext;
    /**
     * ...
     */
    protected final Unmarshaller unmarshaller;
    /**
     * ...
     */
    protected final ObjectFactory factory;
    /**
     * Reference to the package of the xjc-generated classes (required to unmarshal incoming requests).
     */
    @javax.annotation.Resource(type = WebServiceContext.class)
    protected WebServiceContext wsContext;
    /**
     * The HashMap that contains the cached content.
     */
    protected Map<String, String> hashmap; // not thread-safe; but: good enough
    /**
     * ...
     */
    private boolean debug_mode;
    /**
     * ...
     */
    private boolean isAlive;


    // --------------------------------------------------------------------------------------------

    /**
     * Custom constructor.
     */
    public MWCache(boolean debug_mode) {
        this.debug_mode = debug_mode;
        this.isAlive = true;

        // Instantiation of the binding context, unmarshaller
        try {
            this.jaxbContext = JAXBContext.newInstance(MWCache.CONTEXT_PATH);
            this.unmarshaller = jaxbContext.createUnmarshaller();
        } catch (Exception exc) {
            throw new RuntimeException(exc);
        }

        this.factory = new ObjectFactory(); //< mw.cache.generated.ObjectFactory

        this.hashmap = new HashMap<String, String>(10000);
    }


    /**
     * Default constructor.
     */
    public MWCache() {
        this(false);
    }


    // -----------------------------------------------------------------------------------------------------------------
    //  Web service methods
    //------------------------------------------------------------------------------------------------------------------

    /**
     * Starts the web service.
     * <p/>
     * ...
     */
    public static void main(String[] args) {
        System.out.println("Initializing MWCache service...");

        while (true) {
            MWCache mwcache = null;
            Endpoint endpoint = null;

            try {
                // Instantiation and publication of the service endpoint ...
                mwcache = new MWCache();
                endpoint = Endpoint.create(HTTPBinding.HTTP_BINDING, mwcache);
                endpoint.publish(MWCache.SERVICE_URL);

                // if (endpoint.isPublished()) {
                System.out.println("MWCache service is ready to process client requests.");
                // }
                // else {
                //     ...
                // }

                while (mwcache.isAlive()) {
                    Thread.sleep(Long.MAX_VALUE);
                }
            } catch (InterruptedException interruptedException) {
                if (mwcache.isAlive()) {
                    System.out.println("Initialization failed!");
                    break;
                } else {
                    if (endpoint != null) {
                        endpoint.stop();
                    }
                    System.out.println("Internal server error; restarting MWCache service...");
                }
            } catch (RuntimeException runtimeException)
        }
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
        Source responseSource = null;
        Exception exception = null;

        // Analyze request, compose & submit response
        try {
            if (wsContext == null) {
                // Dependency injection failed...
                throw new HTTPException(500);
            }

            // Access the message context and extract request details (httpMethod, pathInfo) ...
            MessageContext messageContext = wsContext.getMessageContext();
            String httpMethod = ((String) messageContext.get(MessageContext.HTTP_REQUEST_METHOD)).toUpperCase();
            String pathInfo = (String) messageContext.get(MessageContext.PATH_INFO);

            System.out.println("HTTP_METHOD: " + httpMethod);
            System.out.println("PATH_INFO  : " + pathInfo);

            switch (httpMethod) {
                case "POST":
                    responseSource = this.processAddRequest(pathInfo, requestSource);
                    break;

                case "GET":
                    responseSource = this.processGetRequest(pathInfo);
                    break;

                default:
                    throw new HTTPException(405);
            }

            isAlive = true;
        } catch (Exception exc) {
            exception = exc;
        }

        // In case sth. went wrong... try to:
        // (a) inform client abt. the type of exception, and - if necessary -
        // (b) try to restart service
        if (exception != null) {
            try {
                boolean internal_error = !(exception instanceof HTTPException) || (((HTTPException) exception).getStatusCode() == 500);

                MWEntry mwEntry = this.factory.createMWEntry();
                mwEntry.setStatus(internal_error ? MWCache.HTTP_INTERNAL_ERROR : MWCache.HTTP_BAD_REQUEST); // eigentl. MWCache.HTTP_BAD_METHOD, aber...
                mwEntry.setMessage(exception.getMessage());

                responseSource = new JAXBSource(this.jaxbContext, mwEntry);

                this.isAlive = !internal_error;
            } catch (JAXBException jaxbexc) {
                this.isAlive = false;
            }
        }

        return responseSource;
    }

    /**
     * Adds an object to the cache and returns a {@link Source} instance ...
     *
     * @param key           key of the object to be added to cache
     * @param requestSource (unmarshalled) details for the incoming request
     *
     * @return {@link Source} instance ...
     */
    private Source processAddRequest(String key, Source requestSource) throws JAXBException {
        JAXBElement requestMessageBody = (JAXBElement) unmarshaller.unmarshal(requestSource);
        MWEntry entry = (MWEntry) requestMessageBody.getValue();

        /**
         ...what if: mismatch btw. key and entry.getKey()?
         */

        String previousValue = this.hashmap.put(key, entry.getValue());
        MWEntry responseMessageBody = this.factory.createMWEntry();

        if (previousValue == null) {
            responseMessageBody.setKey(key);
            responseMessageBody.setStatus(MWCache.HTTP_CREATED);
        } else {
            responseMessageBody.setKey(key);
            responseMessageBody.setValue(previousValue);
            responseMessageBody.setStatus(MWCache.HTTP_OK);
        }

        return new JAXBSource(this.jaxbContext, this.factory.createMWMessageBody(responseMessageBody));
    }


    // -----------------------------------------------------------------------------------------------------------------
    // ...
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Looks up an object in the cache by its key and returns a {@link Source} instance marshalling a {@link MWEntry}
     * object comprised of a key, its associated value, and a {@code HTTP_OK} status message &ndash; if the key exists.
     * Otherwise, the response message object is flagged with a {@code HTTP_NOT_FOUND} error code.
     *
     * @param key key of the object to look up in cache
     *
     * @return {@link Source} instance ...
     */
    private Source processGetRequest(String key) throws JAXBException {
        MWEntry responseMessageBody = this.factory.createMWEntry();

        if (!this.hashmap.containsKey(key)) {
            responseMessageBody.setStatus(MWCache.HTTP_NOT_FOUND);
            responseMessageBody.setKey(key);
        } else {
            responseMessageBody.setStatus(MWCache.HTTP_OK);
            responseMessageBody.setKey(key);
            responseMessageBody.setValue(this.hashmap.get(key));
        }

        return new JAXBSource(this.jaxbContext, this.factory.createMWMessageBody(responseMessageBody));
    }


    // -----------------------------------------------------------------------------------------------------------------
    //  main(String[]) method
    // -----------------------------------------------------------------------------------------------------------------

    public boolean isAlive() {
        return isAlive;
    }

}
