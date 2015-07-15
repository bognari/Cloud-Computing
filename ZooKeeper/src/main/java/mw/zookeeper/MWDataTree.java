package mw.zookeeper;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by stephan on 04.07.15.
 */
public class MWDataTree {

    private final Map<String, MWDataTree> children;
    private final MWStat status;
    private byte[] data;

    public MWDataTree(byte[] data, long time) {
        this.data = data;
        this.status = new MWStat(time);
        children = Collections.synchronizedMap(new HashMap<String, MWDataTree>());
    }

    public MWDataTree() {
        this(null, 0);
    }

    public String create(String path, byte[] data, long time) {
        return create(path, data, time, path.substring(1).split("/"), 0);
    }

    private String create(String path, byte[] data, long time, String[] split, int index) {
        if (split.length == index + 1) {
            if (children.containsKey(split[index])) {
                throw new MWZooKeeperException(String.format("Datei \"%s\" ist schon vorhanden", split[index]));
            }
            children.put(split[index], new MWDataTree(data, time));
            return path;
        }
        if (children.containsKey(split[index])) {
            return create(path, data, time, split, ++index);
        }
        throw new MWZooKeeperException(String.format("Ordner \"%s\" ist nicht vorhanden", split[index]));
    }


    public void delete(String path, int version) {
        delete(version, path.substring(1).split("/"), 0);
    }

    private void delete(int version, String[] split, int index) {
        if (split.length == index + 1) {
            if (children.containsKey(split[index])) {
                int v = children.get(split[index]).status.getVersion();
                if (v == version) {
                    children.remove(split[index]);
                    return;
                }
                throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" besitzt die Version %d aber " +
                    "Version %s soll gelöscht werden", split[index], v, version));
            }
            throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" nicht vorhanden", split[index]));
        }
        if (children.containsKey(split[index])) {
            delete(version, split, ++index);
            return;
        }
        throw new MWZooKeeperException(String.format("Ordner \"%s\" ist nicht vorhanden", split[index]));
    }

    public MWStat setData(String path, byte[] data, int version, long time) {
        return setData(data, version, time, path.substring(1).split("/"), 0);
    }

    private MWStat setData(byte[] data, int version, long time, String[] split, int index) {
        if (split.length == index + 1) {
            if (children.containsKey(split[index])) {
                MWDataTree child = children.get(split[index]);
                int v = child.status.getVersion();
                if (v == version) {
                    return child.setData(data, ++version, time);
                }
                throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" besitzt die Version %d aber " +
                    "Version %s soll aktualisiert werden", split[index], v, version));
            }
            throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" nicht vorhanden", split[index]));
        }
        if (children.containsKey(split[index])) {
            return setData(data, version, time, split, ++index);
        }
        throw new MWZooKeeperException(String.format("Ordner \"%s\" ist nicht vorhanden", split[index]));
    }

    private MWStat setData(byte[] data, int version, long time) {
        this.data = data;
        this.status.setVersion(version, time);
        return status;
    }

    public byte[] getData(String path, MWStat stat) {
        return getData(stat, path.substring(1).split("/"), 0);
    }

    private byte[] getData(MWStat stat, String[] split, int index) {
        if (split.length == index + 1) {
            if (children.containsKey(split[index])) {
                MWDataTree child = children.get(split[index]);
                if (child.status.equals(stat)) {
                    return child.data;
                }
                throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" besitzt keinen passenden " +
                    "Statuseintrag", split[index]));
            }
            throw new MWZooKeeperException(String.format("Datei oder Ordner \"%s\" nicht vorhanden", split[index]));
        }
        if (children.containsKey(split[index])) {
            return getData(stat, split, index);
        }
        throw new MWZooKeeperException(String.format("Ordner \"%s\" ist nicht vorhanden", split[index]));
    }
}
