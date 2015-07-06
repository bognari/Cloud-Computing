package mw.zookeeper.core;

import java.io.IOException;

public class MWMapper<KEYIN, VALUEIN, KEYOUT, VALUEOUT> implements Runnable {

    protected MWMapContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context;

    public void setContext(MWMapContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context) {
        this.context = context;
    }

    public void run() {
        while (context.nextKeyValues()) {
            for (VALUEIN valuein : context.getCurrentValues()) {
                map(context.getCurrentKey(), valuein, context);
            }
        }
        try {
            context.outputComplete();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void map(KEYIN key, VALUEIN value, MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> context) {
        try {
            context.write((KEYOUT) key, (VALUEOUT) value);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}