package mw.maprecude.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MWReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    private final Map<KEYOUT, List<VALUEOUT>> data;
    private final String tmpFile;
    private boolean isFinish;

    public MWReduceContext(String inFile, long startIndex, long length, Comparator<KEYOUT> comparator, String tmpFile) {
        super(inFile, startIndex, length);
        data = new TreeMap<>(comparator);
        this.tmpFile = tmpFile;
    }

    @Override
    public void write(Object key, Object value) throws IOException {
        if (isFinish) {
            return;
        }
        if (!data.containsKey((KEYOUT) key)) {
            data.put((KEYOUT) key, new LinkedList<VALUEOUT>());
        }
        data.get((KEYOUT) key).add((VALUEOUT) value);
    }

    @Override
    public void outputComplete() throws IOException {
        super.outputComplete();
        isFinish = true;
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(tmpFile));
        for (KEYOUT key : data.keySet()) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(key);
            for (VALUEOUT value : data.get(key)) {
                stringBuilder.append("\t").append(value);
            }
            bufferedWriter.write(stringBuilder.toString());
        }
        bufferedWriter.close();
    }
}
