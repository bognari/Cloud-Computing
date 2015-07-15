package mw.zookeeper;

import mw.MWRegistryAccess;

import javax.xml.registry.JAXRException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Created by stephan on 06.07.15.
 */
public class MWZooKeeper {

    private Socket server;
    private ObjectOutputStream objectOutputStream;
    private ObjectInputStream objectInputStream;

    public MWZooKeeper() {
        String url = "";

        MWRegistryAccess mwRegistryAccess = new MWRegistryAccess();

        String url2;
        try {
            url2 = MWRegistryAccess.getRegistryURL();
        } catch (IOException e1) {
            System.err.printf("Konnte die URL nicht laden%n");
            e1.printStackTrace();
            return;
        }

        mwRegistryAccess.openConnection(url2 + "/inquiry", url2 + "/publish");

        try {
            url = mwRegistryAccess.getServiceURL("gruppe11", "MWZooKeeper");
        } catch (JAXRException | IOException e) {
            e.printStackTrace();
            System.exit(-1);
        }

        try {
            String[] data = url.split(":");

            server = new Socket(data[1].substring(2), Integer.parseInt(data[2]));
            server.setKeepAlive(true);
            objectOutputStream = new ObjectOutputStream(server.getOutputStream());
            objectInputStream = new ObjectInputStream(server.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String create(String path, byte[] data) {
        if (server == null) {
            throw new MWZooKeeperException("404. That’s an error.\n" +
                "\n" +
                "The requested URL was not found on this server. That’s all we know.");
        }
        Object ret;
        try {
            objectOutputStream.writeObject(MWZooKeeperMessage.create(path, data));
            objectOutputStream.flush();

            ret = objectInputStream.readObject();

            if (ret instanceof MWZooKeeperException) {
                throw (MWZooKeeperException) ret;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new MWZooKeeperException("das hätte nicht passieren dürfen, das tut uns leid :(");
        }
        return (String) ret;
    }

    public void delete(String path, int version) {
        if (server == null) {
            throw new MWZooKeeperException("404. That’s an error.\n" +
                "\n" +
                "The requested URL was not found on this server. That’s all we know.");
        }
        try {
            objectOutputStream.writeObject(MWZooKeeperMessage.delete(path, version));
            objectOutputStream.flush();

            Object ret = objectInputStream.readObject();

            if (ret instanceof MWZooKeeperException) {
                throw (MWZooKeeperException) ret;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new MWZooKeeperException("das hätte nicht passieren dürfen, das tut uns leid :(");
        }
    }

    public MWStat setData(String path, byte[] data, int version) {
        if (server == null) {
            throw new MWZooKeeperException("404. That’s an error.\n" +
                "\n" +
                "The requested URL was not found on this server. That’s all we know.");
        }
        Object ret;
        try {
            objectOutputStream.writeObject(MWZooKeeperMessage.setData(path, data, version));
            objectOutputStream.flush();

            ret = objectInputStream.readObject();

            if (ret instanceof MWZooKeeperException) {
                throw (MWZooKeeperException) ret;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new MWZooKeeperException("das hätte nicht passieren dürfen, das tut uns leid :(");
        }
        return (MWStat) ret;
    }

    public byte[] getData(String path, MWStat stat) {
        if (server == null) {
            throw new MWZooKeeperException("404. That’s an error.\n" +
                "\n" +
                "The requested URL was not found on this server. That’s all we know.");
        }
        Object ret;
        try {
            objectOutputStream.writeObject(MWZooKeeperMessage.getData(path, stat));
            objectOutputStream.flush();

            ret = objectInputStream.readObject();

            if (ret instanceof MWZooKeeperException) {
                throw (MWZooKeeperException) ret;
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new MWZooKeeperException("das hätte nicht passieren dürfen, das tut uns leid :(");
        }
        return (byte[]) ret;
    }
}