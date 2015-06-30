package mw.mapreduce.core;

import mw.mapreduce.util.MWKeyValuesIterator;
import mw.mapreduce.util.MWTextFileReader;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class MWFileIterator<KEYIN, VALUEIN> implements MWKeyValuesIterator<KEYIN, VALUEIN> {
    protected final MWTextFileReader reader;

    private String next;
    private int line;

    public MWFileIterator(String inFile, long startIndex, long length) {
        MWTextFileReader tmp;
        try {
            tmp = new MWTextFileReader(inFile, startIndex, length);
        } catch (IOException e) {
            tmp = null;
            e.printStackTrace();
        }
        reader = tmp;
    }

    public MWFileIterator(String inFile) {
        MWTextFileReader tmp;
        try {
            tmp = new MWTextFileReader(inFile, 0, (new File(inFile)).length());
        } catch (IOException e) {
            tmp = null;
            e.printStackTrace();
        }
        reader = tmp;
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean nextKeyValues() {
        if (reader == null) {
            return false;
        }
        try {
            next = reader.readLine();
            line++;
        } catch (IOException e) {
            next = null;
        }
        return next != null;
    }

    public boolean forceNextKeyValues() {
        if (reader == null) {
            return false;
        }
        try {
            next = reader.forceReadLine();
            line++;
        } catch (IOException e) {
            next = null;
        }
        return next != null;
    }

    @Override
    public KEYIN getCurrentKey() {
        if (next == null) {
            return null;
        }
        String[] args = next.split("\\t");
        if (args.length < 2) {
            return (KEYIN) String.valueOf(line);
        }
        return (KEYIN) args[0];
    }

    @Override
    public Iterable<VALUEIN> getCurrentValues() {
        if (next == null) {
            return null;
        }
        String[] args = next.split("\\t");
        if (args.length < 2) {
            return Arrays.asList((VALUEIN[]) args);
        }
        return Arrays.asList((VALUEIN[]) args).subList(1, args.length);
    }
}
