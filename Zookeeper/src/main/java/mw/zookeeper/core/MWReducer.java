package mw.zookeeper.core;

import java.io.IOException;

public class MWReducer<KEYIN, VALUEIN, KEYOUT, VALUEOUT> implements Runnable {
    protected MWReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context;

    public void setContext(MWReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context) {
        this.context = context;
    }

    public void run() {
        while (context.nextKeyValues()) {
            reduce(context.getCurrentKey(), context.getCurrentValues(), context);
        }
        try {
            context.outputComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void reduce(KEYIN key, Iterable<VALUEIN> values, MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context) {
        for (VALUEIN value : values) {
            try {
                context.write((KEYOUT) key, (VALUEOUT) value);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
