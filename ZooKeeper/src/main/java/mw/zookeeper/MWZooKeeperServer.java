package mw.zookeeper;

import mw.MWRegistryAccess;
import org.apache.zookeeper.zab.*;

import javax.xml.registry.JAXRException;
import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by stephan on 06.07.15.
 */
public class MWZooKeeperServer implements ZabCallback {
    private final MWDataTree root;
    private final ExecutorService threadpool;
    private final ServerSocket socket;
    private Zab node;

    public MWZooKeeperServer() throws IOException {
        root = new MWDataTree();
        threadpool = Executors.newCachedThreadPool();
        socket = new ServerSocket(0);
    }

    public static void main(String[] args) {

        MWZooKeeperServer server = null;
        String url = "";
        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();
        try {
            server = new MWZooKeeperServer();

            try {
                if (args.length > 0 && args[0].equals("l")) {
                    throw new IOException();
                }
                Scanner scanner = new Scanner(new URL("http://169.254.169.254/latest/meta-data/public-ipv4").openStream());
                url = String.format("http://%s:%d", scanner.nextLine(), server.getPort());
            } catch (IOException e) {
                //e.printStackTrace();
                try {
                    url = String.format("http://%s:%d", InetAddress.getLocalHost().getHostAddress(), server.getPort());
                } catch (UnknownHostException e1) {
                    //e1.printStackTrace();
                    url = String.format("http://localhost:%d", server.getPort());
                }
            }

            String url2;
            try {
                url2 = MWRegistryAccess.getRegistryURL();
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

            mwRegistryAccess.openConnection(url2 + "/inquiry", url2 + "/publish");

            try {
                mwRegistryAccess.authenticate(config.getProperty("user"), config.getProperty("password"));
            } catch (JAXRException e1) {
                System.err.printf("Keine Anmeldung an der Registry möglich%n");
                return;
            }

            mwRegistryAccess.registerService(config.getProperty("user"), "MWZooKeeper", url, true);

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }


        Properties zabProperties = new Properties();
        //zabProperties.setProperty("myid", String.valueOf(server.getPort()));

        try {
            int i = 0;
            mwRegistryAccess.closeConnection();
            for (String peer : mwRegistryAccess.getAllServiceURLs("gruppe11", "MWZooKeeper")) {
                if (url.equals(peer)) {
                    zabProperties.setProperty("myid", String.valueOf(i));
                }

                String[] data = peer.split(":");
                zabProperties.setProperty("peer" + i, String.format("%s%s", data[1].substring(2), data[2]));
                i++;
                //}
            }
        } catch (JAXRException | IOException e) {
            e.printStackTrace();
        }

        Zab zabNode = new MultiZab(server, zabProperties);
        try {
            zabNode.startup();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.printf("Server started at %s%n", url);

        while (true) {
            try {
                System.out.println("waiting for connection");
                server.waitForClient();
            } catch (final IOException e) {
                System.err.println("fail to connect with client");
                e.printStackTrace();
            }
        }
    }

    public void setNode(Zab node) {
        this.node = node;
    }

    public int getPort() {
        return socket.getLocalPort();
    }

    public void waitForClient() throws IOException {
        threadpool.execute(new MWZooKeeperWorker(socket.accept()));
    }

    public Serializable deserialize(byte[] arr) throws Exception {

        ByteArrayInputStream bais = new ByteArrayInputStream(arr);
        ObjectInputStream ois = new ObjectInputStream(bais);
        Serializable ret = (Serializable) ois.readObject();
        bais.close();
        ois.close();
        return ret;
    }

    public byte[] serialize(Serializable obj) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(obj);
        oos.close();
        baos.close();
        return baos.toByteArray();
    }

    @Override
    public void deliver(ZabTxnCookie zabTxnCookie, byte[] bytes) {
        MWZooKeeperMessage msg;
        try {
            msg = (MWZooKeeperMessage) deserialize(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        switch (msg.getMethod()) {
            case "create":
                synchronized (root) {
                    root.create(msg.getPath(), msg.getData(), msg.getStat().getTime());
                }
                break;
            case "delete":
                synchronized (root) {
                    root.delete(msg.getPath(), msg.getStat().getVersion());
                }
                break;
            case "setData":
                synchronized (root) {
                    root.setData(msg.getPath(), msg.getData(), msg.getStat().getVersion(), msg.getStat().getTime());
                }
        }
    }

    @Override
    public void deliverSync(ZabTxnCookie zabTxnCookie) {

    }

    @Override
    public void status(ZabStatus zabStatus, String s) {

    }

    @Override
    public void getState(OutputStream outputStream) throws IOException {

    }

    @Override
    public void setState(InputStream inputStream, ZabTxnCookie zabTxnCookie) throws IOException {

    }

    private class MWZooKeeperWorker implements Runnable {
        private Socket client;
        private ObjectOutputStream objectOutputStream;
        private ObjectInputStream objectInputStream;

        public MWZooKeeperWorker(Socket client) throws IOException {
            this.client = client;
            objectInputStream = new ObjectInputStream(client.getInputStream());
            objectOutputStream = new ObjectOutputStream(client.getOutputStream());
        }

        /**
         * When an object implementing interface <code>Runnable</code> is used to create a thread, starting the thread
         * causes the object's <code>run</code> method to be called in that separately executing thread.
         * <p/>
         * The general contract of the method <code>run</code> is that it may take any action whatsoever.
         *
         * @see Thread#run()
         */
        @Override
        public void run() {
            while (client.isConnected()) {
                try {
                    MWZooKeeperMessage request = (MWZooKeeperMessage) objectInputStream.readObject();
                    switch (request.getMethod()) {
                        case "create":
                            create(request);
                            break;
                        case "delete":
                            delete(request);
                            break;
                        case "setData":
                            setData(request);
                            break;
                        case "getData":
                            getData(request);
                            break;
                    }
                    node.propose(serialize(request));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        private void getData(MWZooKeeperMessage request) throws IOException, ClassNotFoundException {
            try {
                byte[] ret;
                synchronized (root) {
                    ret = root.getData(request.getPath(), request.getStat());
                }
                objectOutputStream.writeObject(ret);
            } catch (MWZooKeeperException e) {
                objectOutputStream.writeObject(e);
            }
            objectOutputStream.flush();
        }

        private void setData(MWZooKeeperMessage request) throws IOException, ClassNotFoundException {
            try {
                MWStat stat;
                synchronized (root) {
                    stat = root.setData(request.getPath(), request.getData(), request.getStat().getVersion(), System.currentTimeMillis());
                }
                objectOutputStream.writeObject(stat);
            } catch (MWZooKeeperException e) {
                objectOutputStream.writeObject(e);
            }
            objectOutputStream.flush();
        }

        private void delete(MWZooKeeperMessage request) throws IOException, ClassNotFoundException {
            try {
                synchronized (root) {
                    root.delete(request.getPath(), request.getStat().getVersion());
                }
                objectOutputStream.writeObject(new Object());
            } catch (MWZooKeeperException e) {
                objectOutputStream.writeObject(e);
            }
            objectOutputStream.flush();
        }

        private void create(MWZooKeeperMessage request) throws IOException, ClassNotFoundException {
            try {
                String path;
                synchronized (root) {
                    path = root.create(request.getPath(), request.getData(), System.currentTimeMillis());
                }
                objectOutputStream.writeObject(path);
            } catch (MWZooKeeperException e) {
                objectOutputStream.writeObject(e);
            }
            objectOutputStream.flush();
        }
    }
}
