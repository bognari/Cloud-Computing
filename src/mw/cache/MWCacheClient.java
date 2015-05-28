package mw.cache;

import mw.cache.generated.MWEntry;
import mw.cache.generated.ObjectFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.http.HTTPBinding;
import java.util.HashMap;
import java.util.Map;


/**
 * Client to the MWCache service implementation.
 * <p/>
 * This class provides two methods ({@link MWCacheClient#addObject(String, String)}, {@link
 * MWCacheClient#getObject(String}) &ndash; which allow for accessing the cache as if it was running locally.
 *
 * @author Florian Bahr
 * @since Aufgabe 2.1
 */
public class MWCacheClient {

    /**
     * ...
     */
    private static final String NAMESPACE_URI = "http://www.ibr.cs.tu-bs.de/courses/ss15/cc/ue02/cache";
    /**
     * Max. number of dispatch objects to be kept for later reuse; probably rather a pseudo-optimization.
     */
    private static final int MAX_NUMBER_OF_DISPATCH_OBJECTS = 1000;
    /**
     * {@link mw.cache.generated.ObjectFactory} instance.
     */
    protected final ObjectFactory factory;
    /**
     * Binding context of JAXB objects.
     */
    private final JAXBContext jaxbContext;
    /**
     * The {@code Dispatch} interface provides support for dynamic invocation of service endpoint operations; {@code
     * dispatches} keeps up to {@code MAX_NUMBER_OF_DISPATCH_OBJECTS} instantiated dispatch facilities for later reuse.
     */
    private final Map<String, Dispatch<Object>> dispatches;
    /**
     * Client view of a web service.
     */
    protected Service service;
    /**
     * ...
     */
    private boolean debug_mode;


    // -----------------------------------------------------------------------------------------------------------------
    // Constructors
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Custom constructor.
     *
     * @param debug_mode
     *
     * @throws JAXBException
     */
    public MWCacheClient(boolean debug_mode) throws JAXBException {
        this.debug_mode = debug_mode;

        // mw.cache.generated.ObjectFactory
        this.factory = new ObjectFactory();

        // Creation of binding context for JACB objects
        this.jaxbContext = JAXBContext.newInstance(MWCache.CONTEXT_PATH);

        // Configuration of service connection
        // this.service = Service.create(new QName(MWCacheClient.NAMESPACE_URI, "MWCacheService"));

        // Probably useless attempt to improve efficiency of management of dispatch objects ...
        this.dispatches = new HashMap<String, Dispatch<Object>>((int) 1.5 * MWCacheClient.MAX_NUMBER_OF_DISPATCH_OBJECTS);
    }


    /**
     * Default construtor.
     *
     * @throws JAXBException
     */
    public MWCacheClient() throws JAXBException {
        this(false);
    }


    // -----------------------------------------------------------------------------------------------------------------

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

        try {
            MWCacheClient cacheClient = new MWCacheClient(true);

            switch (args[0].toUpperCase()) {
                case "GET":
                    cacheClient.getObject(args[1]);
                    break;

                case "ADD":
                    cacheClient.addObject(args[1], args[2]);
                    break;
            }
        } catch (JAXBException exc) {
            exc.printStackTrace();
            System.exit(-1);
        }
    }

    /**
     * Auxiliary (builder) method.
     * <p/>
     * The {@code javax.xml.ws.Dispatch} interface allows for dynamic invocation of a service endpoint operations.
     * <p/>
     * ...otherwise, {@code javax.xml.ws.Service} interface acts as a factory for the creation of Dispatch instances.
     *
     * @param httpMethod HTTP method to be used ({@code POST}, {@code GET})
     * @param port       actually, a resource name (i.e., key)
     */
    private Dispatch<Object> createDispatch(String httpMethod, String port) {
        // Remember: the Dispatch interface provides support for dynamic invocation of service endpoint operations
        Dispatch<Object> dispatch;

        if (this.dispatches.containsKey(port)) {
            dispatch = this.dispatches.get(port);
        } else {
            // Every now and then...
            if (this.dispatches.size() % 1000 == 0) {
                // ...drop all references to previously used dispatch objects
                this.dispatches.clear();
                // also, re-init service connection
                this.service = Service.create(new QName(MWCacheClient.NAMESPACE_URI, "MWCacheService"));
            }

            // service endpoint name
            QName qname = new QName(MWCache.SERVICE_URL.concat(port), port);
            // service port (port, or actually, resource name and binding method)
            this.service.addPort(qname, HTTPBinding.HTTP_BINDING, MWCache.SERVICE_URL.concat(port));

            // Specification of properties of the dispatch facility
            // - Service.Mode.{MESSAGE, PAYLOAD} [allows for access of message header+body, body, respectively]
            // - binding context
            // Here: synchronous, payload-oriented, with JAXB objects
            dispatch = service.createDispatch(qname, this.jaxbContext, Service.Mode.PAYLOAD);

            // store dispatch object for later reuse [probably rather a pseudo-optimization]
            this.dispatches.put(port, dispatch);
        }

        // Definition of the HTTP method (POST, GET) to be used
        dispatch.getRequestContext().put(MessageContext.HTTP_REQUEST_METHOD, httpMethod);

        return dispatch;
    }

    /**
     * Adds an object ({@code value}) to be identified using {@code key} to the cache.
     *
     * @param key   object identifier
     * @param value object
     */
    public void addObject(String key, String value) {
        // DEBUG -------------------------------------------------------------------------------------------------------
        if (this.debug_mode) {
            System.out.printf("REQUEST: ADD <KEY:\"%s\", VALUE:\"%s\">%n", key, value);
        }
        // -------------------------------------------------------------------------------------------------------------

        // Preparing request object
        MWEntry requestMessageBody = this.factory.createMWEntry();
        requestMessageBody.setValue(value);

        // Marshalling the request object
        JAXBElement<MWEntry> request = this.factory.createMWMessageBody(requestMessageBody);

        // Creation [or reuse] of a (JAXB object-oriented) dispatch facility
        Dispatch<Object> dispatch = this.createDispatch("POST", key);

        // Invoking synchronous dispatch, i.e.: send request, block 'til retrieving response
        JAXBElement response = (JAXBElement) dispatch.invoke(request);
        MWEntry entry = (MWEntry) response.getValue();
        String status = entry.getStatus();

        // DEBUG -------------------------------------------------------------------------------------------------------
        if (this.debug_mode) {
            System.out.printf(
                "RESPONSE:%s%n",
                status.equals(MWCache.HTTP_CREATED) ? " ".concat(status) : String.format("%n  STATUS_MSG: %s", status));

            switch (status) {
                case MWCache.HTTP_OK:
                    System.out.printf("  NEW VALUE: %s [REPLACED: %s]%n", value, entry.getValue());
                    break;

                case MWCache.HTTP_CREATED:
                    // System.out.printf("  VALUE: %s%n", value);
                    break;

                default:
                    System.out.printf("  DETAILS: %s%n", entry.getMessage());
                    break;
            }
        }
        // -------------------------------------------------------------------------------------------------------------

        if (!(status.equals(MWCache.HTTP_OK) || status.equals(MWCache.HTTP_CREATED))) {
            System.out.printf("Warning: invocation of MWCacheClient::addObject(%s,%s) failed with status: %s%n", key, value, status);
        }

//?     boolean HTTP_INTERNAL_ERROR = entry.getStatus().equals(MWCache.HTTP_INTERNAL_ERROR);
//?		if (HTTP_INTERNAL_ERROR) {
//?			throw new HTTPException(500);
//?     }
//?     ...
    }


    // -----------------------------------------------------------------------------------------------------------------
    // main(String[]) method
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Retrieves a cached value (object) by its key from {@link MWCache}. In case of a cache miss, a {@link
     * MWNoSuchKeyException} is thrown.
     *
     * @param key the key (object identifier) associated with a value (object)
     *
     * @return the cached value
     *
     * @throws MWNoSuchKeyException in case of a cache miss
     */
    public String getObject(String key) throws MWNoSuchKeyException {
        // DEBUG -------------------------------------------------------------------------------------------------------
        if (this.debug_mode) {
            System.out.println("REQUEST: GET " + key);
        }
        // -------------------------------------------------------------------------------------------------------------

        // Creation of dispatch facility
        Dispatch<Object> dispatch = this.createDispatch("GET", key);

        // Dispatching request, receiving response object
        JAXBElement response = (JAXBElement) dispatch.invoke(null);

        // Auswertung der Antwort
        MWEntry entry = (MWEntry) response.getValue();
        String status = entry.getStatus();

        // DEBUG -------------------------------------------------------------------------------------------------------
        if (this.debug_mode) {
            System.out.printf("RESPONSE:%n  STATUS_MSG: %s%n", status);

            switch (status) {
                case MWCache.HTTP_INTERNAL_ERROR:
                case MWCache.HTTP_NOT_FOUND:
                    System.out.printf("  DETAILS: %s%n", entry.getMessage());
                    break;

                case MWCache.HTTP_OK:
                    System.out.printf("  VALUE: %s%n", entry.getValue());
                    break;
            }
        }
        // -------------------------------------------------------------------------------------------------------------

        if (status.equals(MWCache.HTTP_NOT_FOUND)) {
            throw new MWNoSuchKeyException(status + ": Key '" + entry.getKey() + "' unknown.");
        }

        return entry.getValue();
    }

}
