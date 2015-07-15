package mw.zookeeper;

import java.io.Serializable;

/**
 * Created by stephan on 06.07.15.
 */
public class MWZooKeeperMessage implements Serializable {
    private final String method;
    private final String path;
    private final MWStat stat;
    private final byte[] data;


    private MWZooKeeperMessage(String method, String path, MWStat stat, byte[] data) {
        this.method = method;
        this.path = path;
        this.stat = stat;
        this.data = data;
    }

    public static MWZooKeeperMessage create(String path, byte[] data) {
        return new MWZooKeeperMessage("create", path, null, data);
    }

    public static MWZooKeeperMessage delete(String path, int version) {
        return new MWZooKeeperMessage("delete", path, new MWStat(0, version), null);
    }

    public static MWZooKeeperMessage setData(String path, byte[] data, int version) {
        return new MWZooKeeperMessage("setData", path, new MWStat(0, version), data);
    }

    public static MWZooKeeperMessage getData(String path, MWStat stat) {
        return new MWZooKeeperMessage("getData", path, stat, null);
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }

    public MWStat getStat() {
        return stat;
    }

    public byte[] getData() {
        return data;
    }
}
