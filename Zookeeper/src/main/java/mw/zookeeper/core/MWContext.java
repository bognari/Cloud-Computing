package mw.zookeeper.core;

import java.io.IOException;

public abstract class MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends MWFileIterator<KEYIN, VALUEIN> {

    public MWContext(String inFile, long startIndex, long length) {
        super(inFile, startIndex, length);
    }

    public abstract void write(KEYOUT key, VALUEOUT value) throws IOException;

    public abstract void outputComplete() throws IOException;
}