package mw.path;

import mw.MWRegistryAccess;
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

    private MWMyFacebookService mwMyFacebookService;

    public MWMyPathService() {

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url = null;
        try {
            url = mwRegistryAccess.getServiceURL("MWFacebookService", "facebook");
        } catch (JAXRException e) {
            System.err.printf("Die Registry reagiert nicht%n");
            System.exit(-1);
        } catch (NoSuchElementException e) {
            System.err.printf("Keine Einträge für den Service \"MWFacebookService\" vorhanden%n");
            System.exit(-1);
        } catch (IOException e) {
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
        // mwMyPathService.test();

        String wsdl = null;
        try {
            wsdl = String.format("http://%s:12347/mwPathService?wsdl", InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        Endpoint endpoint = null;
        try {
            endpoint = Endpoint.publish(wsdl, mwPathService);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url;
        try {
            url = MWRegistryAccess.getRegistryURL();
        } catch (IOException ioexc) {
            System.err.printf("Konnte die URL nicht laden%n");
            return;
        }

        Properties config;
        try {
            config = MWRegistryAccess.getConfig();
        } catch (IOException ioexc) {
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


        System.out.println("mwPathService ready: " + endpoint.isPublished());
        System.out.println("Run at " + wsdl);

        //e.stop();
        while (true) {
            try {
                Thread.sleep(Long.MAX_VALUE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
            }
        }
    }

    @Override
    @WebMethod
    public String[] calculatePath(String startID, String endID) throws MWNoPathException {
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

        return MWDijkstra.getShortestPath(mwMyFacebookService, allStart, startID, endID);
    }

    public void test() {
        try {
            for (String id : calculatePath("1694452301", "100000859170147")) {
                try {
                    System.out.printf("ID: %20s \t Name: %s%n", id, mwMyFacebookService.getName(id));
                } catch (MWUnknownIDException_Exception e) {
                    System.err.printf("Die ID \"%s\" ist unbekannt", id);
                }
            }

        } catch (MWNoPathException e) {
            e.printStackTrace();
        }
    }
}
