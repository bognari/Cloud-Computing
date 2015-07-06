package mw.zookeeper.core;

public interface MWJob {
    public MWMapper createMapper(String inFile, long startIndex, long length, String tmpFile);

    public MWReducer createReducer(String inFile, long startIndex, long length, String outFile);

    public java.util.Comparator getComparator();
}
