package mw.mapreduce.jobs;

import mw.mapreduce.core.*;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MWFriendExtractJob implements MWJob {
    @Override
    public MWMapper createMapper(String inFile, long startIndex, long length, String tmpFile) {
        MWMapper<String, String, String, String> mapper = new MWFriendExtractMapper();
        mapper.setContext(new MWMapContext<>(inFile, startIndex, length, getComparator(), tmpFile));
        return mapper;
    }

    @Override
    public MWReducer createReducer(String inFile, long startIndex, long length, String outFile) {
        MWReducer<String, String, String, String> reducer = new MWFriendExtractReducer();
        reducer.setContext(new MWReduceContext<String, String, String, String>(inFile, startIndex, length, outFile));
        return reducer;
    }

    @Override
    public Comparator getComparator() {
        return new MWFriendExtractComperator();
    }

    class MWFriendExtractReducer extends MWReducer<String, String, String, String> {
        public void run() {
            while (context.nextKeyValues()) {
                Set<String> set = new HashSet<>();
                for (String v : context.getCurrentValues()) {
                    set.add(v);
                }
                reduce(context.getCurrentKey(), set, context);
            }
            try {
                context.outputComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    class MWFriendExtractMapper extends MWMapper<String, String, String, String> {
        //private final Pattern prof = Pattern.compile("<title>.* | Facebook</title>");
        private final Pattern prof = Pattern.compile("<html.*>");
        private final Pattern profId = Pattern.compile("<input type=\"hidden\" id=\"next\" name=\"next\" value=\"http://.*\\.facebook\\.com/(people/.*/)?(.*)\" autocomplete=\"off\" />");
        private final Pattern profFriend = Pattern.compile("<a class=\"title\" href=\"http://.*\\.facebook\\.com/(people/.*/)?(.*)\" rel=\"friend\" title=\".*\"><img class=\"UIProfileImage UIProfileImage_LARGE img\" src=\".*\\.jpg\" alt=\".*\"></img></a>");

        boolean isInProfile = false;
        String id = "";

        public void run() {

            while (context.nextKeyValues() && !isInProfile) {
                Matcher matcher = prof.matcher(context.getCurrentValues().iterator().next());
                isInProfile = matcher.find();
            }

            while (context.nextKeyValues()) {
                String line = context.getCurrentValues().iterator().next();
                check(line);
            }

            while (isInProfile && context.forceNextKeyValues()) {
                String line = context.getCurrentValues().iterator().next();
                check(line);
            }

            try {
                context.outputComplete();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void check(String line) {
            if (isInProfile) {
                Matcher matcher = prof.matcher(line);
                if (matcher.find()) {
                    isInProfile = false;
                }
                matcher = profId.matcher(line);
                if (matcher.find()) {
                    id = matcher.group(2);
                }
                matcher = profFriend.matcher(line);
                if (matcher.find()) {
                    String friend = matcher.group(2);
                    map(id, friend, context);
                    map(friend, id, context);
                }
            } else {
                Matcher matcher = prof.matcher(line);
                if (matcher.find()) {
                    isInProfile = true;
                }
            }
        }
    }

    class MWFriendExtractComperator implements Comparator<String> {

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
        public int compare(String o1, String o2) {
            return o1.compareTo(o2);
        }
    }
}
