package mw.zookeeper;

import java.io.Serializable;

/**
 * Created by stephan on 04.07.15.
 */
public class MWZooKeeperException extends RuntimeException implements Serializable {
    public MWZooKeeperException(String s) {
        super(s);
    }
}
