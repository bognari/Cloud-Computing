package mw.mapreduce.core;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MWReduceContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> extends MWContext<KEYIN, VALUEIN, KEYOUT, VALUEOUT> {

    private final Map<KEYOUT, List<VALUEOUT>> data;
    private final String outFile;
    private boolean isFinish;

    public MWReduceContext(String inFile, long startIndex, long length, String outFile) {
        super(inFile, startIndex, length);
        data = new LinkedHashMap<>();
        this.outFile = outFile;
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
        BufferedWriter bufferedWriter = Files.newBufferedWriter(Paths.get(outFile));
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
