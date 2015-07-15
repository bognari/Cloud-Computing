package mw.zookeeper;

import java.io.Serializable;
import java.util.Objects;

/**
 * Created by stephan on 04.07.15.
 */
public class MWStat implements Serializable {

    private long time;
    private int version;

    public MWStat(long time, int version) {
        this.time = time;
        this.version = version;
    }

    public MWStat(long time) {
        this(time, 0);
    }

    public long getTime() {
        return time;
    }

    public int getVersion() {
        return version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MWStat mwStat = (MWStat) o;
        return Objects.equals(version, mwStat.version);
    }

    @Override
    public int hashCode() {
        return Objects.hash(time, version);
    }

    public void setVersion(int version, long time) {
        this.version = version;
        this.time = time;
    }
}
