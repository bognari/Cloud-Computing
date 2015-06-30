package mw.mapreduce;

import mw.mapreduce.core.MWFileIterator;
import mw.mapreduce.core.MWJob;
import mw.mapreduce.core.MWMapper;
import mw.mapreduce.core.MWReducer;
import mw.mapreduce.jobs.MWFriendCountJob;
import mw.mapreduce.jobs.MWFriendExtractJob;
import mw.mapreduce.jobs.MWFriendSortJob;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MWMapReduce {

    private static final int REDUCER = 3;
    private static final int MAPPER = 3;

    private final MWJob app;
    private final String inFile;
    private final String outPrefix;
    private final String tmpPrefix;

    private final ExecutorService threadpool;

    private final List<Future> mappers;
    private final List<Future> reducers;


    public MWMapReduce(MWJob app, String inFile, String tmpPrefix, String outPrefix) {
        this.app = app;
        this.inFile = inFile;
        this.outPrefix = outPrefix;
        this.tmpPrefix = tmpPrefix;

        threadpool = Executors.newCachedThreadPool();

        mappers = new LinkedList<>();
        reducers = new LinkedList<>();
    }

    public static void main(String[] args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Keine 4 Parameter");
        }

        MWJob app;

        switch (args[0]) {
            case "friendsort":
                app = new MWFriendSortJob();
                break;
            case "friendextract":
                app = new MWFriendExtractJob();
                break;
            case "friendcount":
                app = new MWFriendCountJob();
                break;
            default:
                throw new IllegalArgumentException("Unbekannte Anweisung");
        }

        MWMapReduce mwMapReduce = new MWMapReduce(app, args[1], args[2], args[3]);

        mwMapReduce.mapping();

        try {
            mwMapReduce.mergingMap();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        mwMapReduce.reducing();
        try {
            mwMapReduce.mergeReduce();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        System.out.println("finish");

    }

    private void mapping() {
        File file = new File(inFile);
        long fileSize = file.length();
        long step = (long) Math.ceil(fileSize / (double) MAPPER);
        long start = 0;
        for (int i = 0; i < MAPPER; i++) {
            MWMapper mapper = app.createMapper(inFile, start, step, String.format("%s%d.dat", tmpPrefix, i));
            start += step;
            mappers.add(threadpool.submit(mapper));
        }
    }

    private void reducing() {
        File file = new File(String.format("%s.dat", tmpPrefix));
        long fileSize = file.length();
        long step = (long) Math.ceil(fileSize / (double) REDUCER);
        long start = 0;
        for (int i = 0; i < REDUCER; i++) {
            MWReducer reducer = app.createReducer(String.format("%s.dat", tmpPrefix), start, step, String.format("%s%d.dat", outPrefix, i));
            start += step;
            reducers.add(threadpool.submit(reducer));
        }
    }

    private void mergingMap() throws IOException {

        for (Future future : mappers) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        BufferedWriter out = Files.newBufferedWriter(Paths.get(String.format("%s.dat", tmpPrefix)));

        PriorityQueue<AbstractMap.SimpleEntry<String, Iterable<String>>> priorityQueue = new PriorityQueue<>(new PrioComp());

        for (int i = 0; i < MAPPER; i++) {
            MWFileIterator<String, String> fileIterator = new MWFileIterator<>(String.format("%s%d.dat", tmpPrefix,
                i));
            while (fileIterator.nextKeyValues()) {
                priorityQueue.add(new AbstractMap.SimpleEntry<>(fileIterator.getCurrentKey(), fileIterator
                    .getCurrentValues()));
            }
        }

        String oldKey = null;
        StringBuilder line = new StringBuilder();
        boolean start = true;
        while (!priorityQueue.isEmpty()) {
            AbstractMap.SimpleEntry<String, Iterable<String>> entry = priorityQueue.poll();
            if (entry.getKey().equals(oldKey)) {
                for (String value : entry.getValue()) {
                    line.append("\t").append(value);
                }
            } else {
                if (start) {
                    start = false;
                } else {
                    out.write(line.append("\n").toString());
                    line.setLength(0);
                }
                line.append(entry.getKey());
                oldKey = entry.getKey();
                for (String value : entry.getValue()) {
                    line.append("\t").append(value);
                }
            }
        }
        if (line.length() > 0) {
            out.write(line.toString());
        }
        out.close();
    }

    private void mergeReduce() throws IOException {
        for (Future future : reducers) {
            try {
                future.get();
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
        threadpool.shutdown();
        BufferedWriter out = Files.newBufferedWriter(Paths.get(String.format("%s.dat", outPrefix)));

        for (int i = 0; i < REDUCER; i++) {
            BufferedReader bufferedReader = Files.newBufferedReader(Paths.get(String.format("%s%d.dat", outPrefix, i)));
            String line;
            boolean start = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (start) {
                    start = false;
                } else {
                    out.newLine();
                }
                out.write(line);
            }
            bufferedReader.close();
        }
        out.close();
    }

    class PrioComp implements Comparator<AbstractMap.SimpleEntry<String, Iterable<String>>> {
        /**
         * Compares its two arguments for order.  Returns a negative integer, zero, or a positive integer as the first
         * argument is less than, equal to, or greater than the second.<p>
         * <p/>
         * In the foregoing description, the notation <tt>sgn(</tt><i>expression</i><tt>)</tt> designates the
         * mathematical <i>signum</i> function, which is defined to return one of <tt>-1</tt>, <tt>0</tt>, or <tt>1</tt>
         * according to whether the value of <i>expression</i> is negative, zero or positive.<p>
         * <p/>
         * The implementor must ensure that <tt>sgn(compare(x, y)) == -sgn(compare(y, x))</tt> for all <tt>x</tt> and
         * <tt>y</tt>.  (This implies that <tt>compare(x, y)</tt> must throw an exception if and only if <tt>compare(y,
         * x)</tt> throws an exception.)<p>
         * <p/>
         * The implementor must also ensure that the relation is transitive: <tt>((compare(x, y)&gt;0) &amp;&amp;
         * (compare(y, z)&gt;0))</tt> implies <tt>compare(x, z)&gt;0</tt>.<p>
         * <p/>
         * Finally, the implementor must ensure that <tt>compare(x, y)==0</tt> implies that <tt>sgn(compare(x,
         * z))==sgn(compare(y, z))</tt> for all <tt>z</tt>.<p>
         * <p/>
         * It is generally the case, but <i>not</i> strictly required that <tt>(compare(x, y)==0) == (x.equals(y))</tt>.
         * Generally speaking, any comparator that violates this condition should clearly indicate this fact.  The
         * recommended language is "Note: this comparator imposes orderings that are inconsistent with equals."
         *
         * @param o1 the first object to be compared.
         * @param o2 the second object to be compared.
         *
         * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
         * greater than the second.
         *
         * @throws NullPointerException if an argument is null and this comparator does not permit null arguments
         * @throws ClassCastException   if the arguments' types prevent them from being compared by this comparator.
         */
        @Override
        public int compare(AbstractMap.SimpleEntry<String, Iterable<String>> o1, AbstractMap.SimpleEntry<String, Iterable<String>> o2) {
            return app.getComparator().compare(o1.getKey(), o2.getKey());
        }
    }
}
