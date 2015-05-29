package mw;

import org.jetbrains.annotations.NotNull;

import javax.xml.registry.*;
import javax.xml.registry.infomodel.Organization;
import javax.xml.registry.infomodel.Service;
import javax.xml.registry.infomodel.ServiceBinding;
import java.io.IOException;
import java.io.InputStream;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MWRegistryAccess {

    public static final String CONFIG_PATH = "config.properties";
    private Connection connection;

    public MWRegistryAccess() {
        Locale.setDefault(Locale.US);
    }

    /**
     * Gibt die config.properties zurück
     *
     * @return die config.properties
     *
     * @throws IOException
     */
    public static Properties getConfig() throws IOException {
        Properties properties = new Properties();

        InputStream inputStream = Files.newInputStream(Paths.get(CONFIG_PATH));
        properties.load(inputStream);
        inputStream.close();

        return properties;
    }

    /**
     * Lädt die URL der Registry.
     *
     * @return URL der Registry als {@link String}-Objekt
     *
     * @throws IOException
     */
    public static String getRegistryURL() throws IOException {
        Properties config = getConfig();
        URI uri = URI.create(config.getProperty("url"));

        Scanner scanner = new Scanner(uri.toURL().openStream());

        return scanner.nextLine();
    }

    /**
     * Main halt
     *
     * @param args
     */
    public static void main(String[] args) {
        switch (args.length) {
            case 0:
                throw new IllegalArgumentException("Keine Option übergeben");
            case 1:
                throw new IllegalArgumentException("Kein Service übergeben");
            default:
                if (!"LIST".equals(args[0].toUpperCase())) {
                    throw new IllegalArgumentException(String.format("Die Option %s ist nicht unterstützt%n", args[0]));
                } else {
                    MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();
                    mwRegistryAccess.list(args[1]);
                }
        }
    }

    /**
     * Öffnet eine Verbindung
     *
     * @param queryManagerURL
     * @param lifeCycleManagerURL
     */
    public void openConnection(@NotNull String queryManagerURL, @NotNull String lifeCycleManagerURL) {
        if (connection != null) {
            throw new IllegalStateException("noch verbunden%n");
        }

        Properties props = new Properties();
        props.setProperty("javax.xml.registry.queryManagerURL", queryManagerURL);
        props.setProperty("javax.xml.registry.lifeCycleManagerURL", lifeCycleManagerURL);

        try {
            ConnectionFactory fact = ConnectionFactory.newInstance();
            fact.setProperties(props);
            connection = fact.createConnection();
        } catch (JAXRException e) {
            System.err.printf("Konnte keine Verbindung herstellen%n");
            connection = null;
        }
    }

    /**
     * Schließt die Verbindung
     */
    public void closeConnection() {
        if (connection == null) {
            throw new IllegalStateException("Keine Verbindung offen%n");
        }

        try {
            connection.close();
            connection = null;
        } catch (JAXRException e) {
            System.err.printf("Konnte Verbindung nicht schließen%n");
        }
    }

    /**
     * @param serviceName
     */
    public void listWSDLs(@NotNull String serviceName) {
        if (connection == null) {
            throw new IllegalStateException("Keine Verbindung%n");
        }

        RegistryService regSvc;
        try {
            regSvc = connection.getRegistryService();
        } catch (JAXRException e) {
            System.err.printf("Keine Verbindung zur Registry möglich%n");
            return;
        }

        Collection<String> findQualifiers = new ArrayList<>();
        findQualifiers.add(FindQualifier.SORT_BY_NAME_ASC);

        Collection<String> namePatterns = new ArrayList<>();
        namePatterns.add(serviceName);

        try {
            BusinessQueryManager m = regSvc.getBusinessQueryManager();
            BulkResponse br = m.findServices(null, findQualifiers, namePatterns, null, null);
            Collection<Service> services = br.getCollection();
            for (Service s : services) {
                System.out.printf("Name: %s%n", s.getName().getValue());
                System.out.printf("Organisation: %s%n", s.getProvidingOrganization().getName().getValue());
                Collection<ServiceBinding> serviceBindings = s.getServiceBindings();

                for (ServiceBinding sb : serviceBindings) {
                    System.out.printf("URL: %s", sb.getAccessURI());
                }
                System.out.println();
            }
        } catch (JAXRException e) {
            System.err.printf("Service konnte nicht abgefragt werden%n");
        }
    }

    /**
     * Führt die "LIST" Operation aus
     *
     * @param serviceName der Service
     */
    private void list(@NotNull String serviceName) {
        String url;
        try {
            url = getRegistryURL();
        } catch (IOException e) {
            System.err.printf("Konnte die URL nicht laden%n");
            return;
        }

        openConnection(url + "/inquiry", url + "/publish");
        listWSDLs(serviceName);
        closeConnection();
    }

    /**
     * ...
     *
     * @param orgName     Dienstanbieter
     * @param serviceName Dienstbezeichner
     *
     * @return URL des Webservices als {@link String}-Objekt.
     *
     * @throws JAXRException
     * @throws IOException
     * @throws NoSuchElementException
     */
    public String getServiceURL(@NotNull String orgName, @NotNull String serviceName) throws JAXRException, IOException, NoSuchElementException {
        String registryURL = getRegistryURL();
        openConnection(registryURL + "/inquiry", registryURL + "/publish");

        if (connection == null) {
            throw new IllegalStateException("Keine Verbindung%n");
        }

        RegistryService regSvc = connection.getRegistryService();

        Collection<String> findQualifiers = new ArrayList<>();
        findQualifiers.add(FindQualifier.EXACT_NAME_MATCH);

        Collection<String> namePatterns = new ArrayList<>();
        namePatterns.add(serviceName);

        BusinessQueryManager m = regSvc.getBusinessQueryManager();
        BulkResponse br = m.findServices(null, findQualifiers, namePatterns, null, null);
        Collection<Service> services = br.getCollection();

        List<String> urls = new LinkedList<>();


        for (Service s : services) {
            if (s.getProvidingOrganization().getName().getValue().equals(orgName)) {
                Collection<ServiceBinding> serviceBindings = s.getServiceBindings();
                for (ServiceBinding sb : serviceBindings) {
                    urls.add(sb.getAccessURI());
                }
            }
        }

        closeConnection();

        if (urls.size() < 1) {
            throw new NoSuchElementException(String.format("Keine URLS für den Service \"%s\" vorhanden", serviceName));
        }

        // if urls.size() >= 1, pick one at random
        return urls.get((int) (urls.size() * Math.random()));
    }

    public void authenticate(@NotNull String userName, @NotNull String password) throws JAXRException {

        if (connection == null) {
            throw new IllegalStateException("Keine Verbindung%n");
        }

        PasswordAuthentication pa = new PasswordAuthentication(userName, password.toCharArray());
        Set<PasswordAuthentication> credentials = new HashSet<>();
        credentials.add(pa);

        connection.setCredentials(credentials);
    }

    public void registerService(@NotNull String orgName, @NotNull String serviceName, @NotNull String wsdlURL) {
        if (connection == null) {
            throw new IllegalStateException("Keine Verbindung%n");
        }

        RegistryService regSvc;
        try {
            regSvc = connection.getRegistryService();
        } catch (JAXRException e) {
            System.err.printf("Keine Verbindung zur Registry möglich%n");
            return;
        }

        Collection<String> findQualifiers = new ArrayList<>();
        findQualifiers.add(FindQualifier.EXACT_NAME_MATCH);

        Collection<String> orgPatterns = new ArrayList<>();
        orgPatterns.add(orgName);

        try {
            BusinessQueryManager m = regSvc.getBusinessQueryManager();
            BusinessLifeCycleManager lcm = regSvc.getBusinessLifeCycleManager();
            BulkResponse bulkResponse = m.findOrganizations(findQualifiers, orgPatterns, null, null, null, null);

            Organization organization;

            if (bulkResponse.getCollection().isEmpty()) {
                organization = lcm.createOrganization(orgName);
            } else {
                organization = (Organization) bulkResponse.getCollection().toArray()[0];
            }

            //organization.removeServices(organization.getServices());

            Iterator<Service> iterator = organization.getServices().iterator();
            List<Service> toBeRemoved = new LinkedList<>();
            while (iterator.hasNext()) {
                Service service = iterator.next();
                if (service.getName().getValue().equals(serviceName)) {
                    toBeRemoved.add(service);
                }
            }
            organization.removeServices(toBeRemoved);

            Service service = lcm.createService(serviceName);

            ServiceBinding serviceBinding = lcm.createServiceBinding();
            serviceBinding.setAccessURI(wsdlURL);

            service.addServiceBinding(serviceBinding);

            organization.addService(service);

            Collection<Organization> orgs = new ArrayList<>(1);
            orgs.add(organization);

            BulkResponse response = lcm.saveOrganizations(orgs);

            Collection<Exception> exceptions = response.getExceptions();

            if (exceptions != null) {
                //exceptions.forEach(java.lang.Exception::printStackTrace);
                for (Exception exception : exceptions) {
                    exception.printStackTrace();
                }
            }

        } catch (JAXRException e) {
            e.printStackTrace();
        }
    }
}
