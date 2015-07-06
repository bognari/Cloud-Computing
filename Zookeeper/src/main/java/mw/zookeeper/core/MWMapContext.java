package mw.zookeeper.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class MWMapContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    private final Map<KEYOUT, List<VALUEOUT>> data;
    private final String tmpFile;
    private boolean isFinish;

    public MWMapContext(String inFile, long startIndex, long length, Comparator<KEYOUT> comparator, String tmpFile) {
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
        close();
        isFinish = true;
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(tmpFile));
        boolean start = true;
        for (KEYOUT key : data.keySet()) {
            if (start) {
                start = false;
            } else {
                bufferedWriter.newLine();
            }
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
