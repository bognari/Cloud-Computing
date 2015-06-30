package mw.mapreduce.util;


public interface MWKeyValuesIterator<KEY, VALUE> {

    public boolean nextKeyValues();

    public KEY getCurrentKey();

    public Iterable<VALUE> getCurrentValues();

}
