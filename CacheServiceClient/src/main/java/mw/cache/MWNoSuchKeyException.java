package mw.cache;

/**
 * Exception thrown/used by MWCacheClient in case of a cache miss.
 *
 * @author Florian Bahr
 * @since Aufgabe 2.1
 */
public class MWNoSuchKeyException extends RuntimeException {

    public MWNoSuchKeyException() {
        ;
    }

    public MWNoSuchKeyException(java.lang.String msg) {
        super(msg);
    }

    public MWNoSuchKeyException(Exception e) {
        super(e);
    }

}
