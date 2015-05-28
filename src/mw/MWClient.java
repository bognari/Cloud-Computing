package mw;

import mw.facebookclient.MWFacebookService;
import mw.facebookclient.MWMyFacebookService;
import mw.facebookclient.MWUnknownIDException_Exception;
import mw.pathclient.MWNoPathException_Exception;
import mw.pathclient.MWPathService;
import mw.pathclient.MWPathService_Service;
import net.java.dev.jaxb.array.StringArray;

import javax.xml.registry.JAXRException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.NoSuchElementException;

public class MWClient {

    private MWMyFacebookService mwMyFacebookService;
    private MWPathService mwMyPathService;

    public MWClient() {
        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String facebookServiceURL = null;
        String pathServiceURL = null;
        try {
            facebookServiceURL = mwRegistryAccess.getServiceURL("facebook", "MWFacebookService");
            pathServiceURL = mwRegistryAccess.getServiceURL("gruppe11", "MWPathService");
        } catch (JAXRException e) {
            System.err.printf("Die Registry reagiert nicht.%n");
            System.exit(-1);
        } catch (NoSuchElementException e) {
            System.err.printf("Keine Einträge für den Service \"MWFacebookService\" vorhande.n%n");
            System.exit(-1);
        } catch (IOException e) {
            System.err.printf("Konnte die Propertiesdatei nicht laden.%n");
            System.exit(-1);
        }

        try {
            MWFacebookService mwFacebookService = new MWFacebookService(new URL(facebookServiceURL));
            mwMyFacebookService = mwFacebookService.getMWMyFacebookServicePort();

            MWPathService_Service mwPathService = new MWPathService_Service(new URL(pathServiceURL));
            mwMyPathService = mwPathService.getMWPathServicePort();
        } catch (MalformedURLException malformedURLException) {
            System.err.printf("Die von der Registry übergebene URL \"%s\" ist ungültig.%n", facebookServiceURL);
            System.exit(-1);
        }
    }

    /**
     * Main halt
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            if (args.length < 1) {
                throw new IllegalArgumentException("Keine Option übergeben");
            }

            MWClient client = new MWClient();

            switch (args[0].toUpperCase()) {
                case "SEARCH":
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Kein Name übergeben");
                    }
                    client.searchIDs(args[1]);
                    break;

                case "FRIENDS":
                    if (args.length < 2) {
                        throw new IllegalArgumentException("Keine ID übergeben");
                    }
                    client.getFriends(args[1]);
                    break;

                case "PATH":
                    if (args.length < 3) {
                        throw new IllegalArgumentException("Keine IDs übergeben");
                    }
                    client.getPath(args[1], args[2]);
                    break;

                default:
                    throw new IllegalArgumentException(String.format("Die Option %s ist nicht unterstützt%n", args[0]));
            }
        } catch (IllegalArgumentException illegalArgumentException) {
            System.out.println(illegalArgumentException.getLocalizedMessage());
        }
    }

    public void searchIDs(String name) {
        StringArray ids = mwMyFacebookService.searchIDs(name);

        System.out.printf("Resultate für die Suche nach %s%n%n", name);

        for (String id : ids.getItem()) {
            try {
                System.out.printf("ID: %20s \t Name: %s%n", id, mwMyFacebookService.getName(id));
            } catch (MWUnknownIDException_Exception e) {
                System.err.printf("Die ID \"%s\" ist unbekannt", id);
            }
        }
        System.out.println();
    }

    public void getFriends(String id) {
        try {
            StringArray friends = mwMyFacebookService.getFriends(id);

            System.out.printf("Freunde von %s (%s)%n%n", mwMyFacebookService.getName(id), id);
            for (String friend : friends.getItem()) {
                System.out.printf("ID: %20s \t Name: %s%n", friend, mwMyFacebookService.getName(friend));
            }
            System.out.println();
        } catch (MWUnknownIDException_Exception e) {
            System.err.printf("Die ID \"%s\" ist unbekannt", id);
        }
    }

    public void getPath(String startId, String endId) {
        try {
            System.out.printf("Kuerzeste Verbindung zwischen %s (%s) und %s (%s)%n%n", mwMyFacebookService.getName(startId), startId, mwMyFacebookService.getName(endId), endId);

            StringArray path = mwMyPathService.calculatePath(startId, endId);
            for (String id : path.getItem()) {
                System.out.printf("ID: %20s \t Name: %s%n", id, mwMyFacebookService.getName(id));
            }
            System.out.println();
        } catch (MWUnknownIDException_Exception | MWNoPathException_Exception e) {
            System.err.println(e.getLocalizedMessage());
        }
    }
}
