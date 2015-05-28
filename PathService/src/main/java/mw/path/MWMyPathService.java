package mw.path;

import mw.MWRegistryAccess;
import mw.cache.MWCacheClient;
import mw.cache.MWNoSuchKeyException;
import mw.facebookclient.MWFacebookService;
import mw.facebookclient.MWMyFacebookService;
import mw.facebookclient.MWUnknownIDException_Exception;
import net.java.dev.jaxb.array.StringArray;

import javax.jws.WebMethod;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import java.io.IOException;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.*;

@WebService(name = "MWPathService", serviceName = "MWPathService")
@SOAPBinding(style = SOAPBinding.Style.RPC)
public class MWMyPathService implements MWPathServiceInterface {

    private final MWCacheClient cacheClient = new MWCacheClient();
    private MWMyFacebookService mwMyFacebookService;

    public MWMyPathService() {

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url = null;
        try {
            url = mwRegistryAccess.getServiceURL("facebook", "MWFacebookService");
        } catch (JAXRException e) {
            e.printStackTrace();
            System.err.printf("Die Registry reagiert nicht%n");
            System.exit(-1);
        } catch (NoSuchElementException e) {
            e.printStackTrace();
            System.err.printf("Keine Einträge für den Service \"MWFacebookService\" vorhanden%n");
            System.exit(-1);
        } catch (IOException e) {
            e.printStackTrace();
            System.err.printf("Konnte die Propertysdatei nicht laden%n");
            System.exit(-1);
        }

        try {
            MWFacebookService mwFacebookService = new MWFacebookService(new URL(url));
            mwMyFacebookService = mwFacebookService.getMWMyFacebookServicePort();
        } catch (MalformedURLException e) {
            System.err.printf("Die von der Registry übergebene URL \"%s\" ist ungültig%n", url);
            System.exit(-1);
        }
    }

    public static void main(String[] args) {
        MWPathServiceInterface mwPathService = new MWMyPathService();

        String wsdl;
        try {
            wsdl = String.format("http://%s:12347/mwPathService?wsdl", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return;
        }

        Endpoint e;
        try {
            e = Endpoint.publish(wsdl, mwPathService);
        } catch (Exception e1) {
            e1.printStackTrace();
            return;
        }

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url;
        try {
            url = MWRegistryAccess.getRegistryURL();
        } catch (IOException e1) {
            System.err.printf("Konnte die URL nicht laden%n");
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

        mwRegistryAccess.registerService(config.getProperty("user"), "MWPathService", wsdl);
        mwRegistryAccess.closeConnection();


        System.out.println("mwPathService ready: " + e.isPublished());
        System.out.println("Run at " + wsdl);

        Scanner scanner = new Scanner(System.in);

        do {
            System.out.println("\"exit\" for exit");
        } while (!scanner.next().equals("exit"));

        System.out.println("Shutdown MWCache service");
        e.stop();
    }

    @Override
    @WebMethod
    public String[] calculatePath(String startID, String endID) throws MWNoPathException {

        String key = String.format("calculatePath(%s, %s)", startID, endID).replaceAll("\\s*", "");

        try {
            String response = cacheClient.getObject(key);
            return response.split(";");
        } catch (MWNoSuchKeyException ignored) {

        }

        Set<String> startIDs = new HashSet<>();
        Set<String> endIDs = new HashSet<>();

        Set<String> allStart = new HashSet<>();
        Set<String> allEnd = new HashSet<>();

        startIDs.add(startID);
        endIDs.add(endID);

        allStart.addAll(startIDs);
        allEnd.addAll(endIDs);

        try {
            do {
                StringArray sa = new StringArray();
                sa.getItem().addAll(startIDs);
                startIDs.clear();
                List<StringArray> ret = mwMyFacebookService.getFriendsBatch(sa).getItem();
                for (StringArray stringArray : ret) {
                    startIDs.addAll(stringArray.getItem());
                }

                sa = new StringArray();
                sa.getItem().addAll(endIDs);
                endIDs.clear();
                ret = mwMyFacebookService.getFriendsBatch(sa).getItem();
                for (StringArray stringArray : ret) {
                    endIDs.addAll(stringArray.getItem());
                }

                if (startIDs.isEmpty() || endIDs.isEmpty()) {
                    throw new MWNoPathException("Keine weiteren IDs verfügbar => kein Pfad zwischen beiden IDs");
                }

                allStart.addAll(startIDs);
                allEnd.addAll(endIDs);

            } while (Collections.disjoint(allStart, allEnd));

        } catch (MWUnknownIDException_Exception e) {
            throw new MWNoPathException(e.getLocalizedMessage());
        }

        allStart.addAll(allEnd);

        String[] ret = MWDijkstra.getShortestPath(mwMyFacebookService, allStart, startID, endID);

        StringBuilder sb = new StringBuilder();

        if (ret != null) {
            for (String n : ret) {
                if (sb.length() > 0) {
                    sb.append(';');
                }
                sb.append(n);
            }
            cacheClient.addObject(key, sb.toString());
        }

        return ret;
    }
}
