package mw.zookeeper.util;


import java.io.*;


public class MWTextFileReader {

    private final BufferedReader reader;
    private final String encoding;

    private long bytesRemaining;


    public MWTextFileReader(String filePath, long startIndex, long length) throws IOException {
        // Create reader
        MWBufferedRandomAccessFileInputStream input = new MWBufferedRandomAccessFileInputStream(filePath);
        InputStreamReader inputReader = new InputStreamReader(input);
        reader = new BufferedReader(inputReader);

        bytesRemaining = length;

        // Determine encoding
        FileReader fileReader = new FileReader(filePath);
        encoding = fileReader.getEncoding();
        fileReader.close();

        // Start with the first complete line
        input.seek(startIndex);
        if (startIndex > 0) {
            readLine();
        }
    }


    public String readLine() throws IOException {
        if (bytesRemaining < 0) {
            return null;
        }
        return forceReadLine();
    }

    public String forceReadLine() throws IOException {
        // Read next line
        String line = reader.readLine();
        if (line == null) {
            return null;
        }

        // Calculate how much bytes were read for this line
        int bytesRead = line.getBytes(encoding).length + 1;
        bytesRemaining -= bytesRead;

        return line;
    }


    public void close() throws IOException {
        reader.close();
    }


    // ########################################
    // # INPUT STREAM FOR RANDOM-ACCESS FILES #
    // ########################################

    private class MWBufferedRandomAccessFileInputStream extends InputStream {

        private final RandomAccessFile file;

        private final byte[] buffer;
        private int nextIndex;
        private int lastIndex;


        public MWBufferedRandomAccessFileInputStream(String filePath) throws FileNotFoundException {
            file = new RandomAccessFile(filePath, "r");
            buffer = new byte[8192];
            resetBuffer();
        }


        private void resetBuffer() {
            nextIndex = 0;
            lastIndex = 0;
        }


        public void seek(long pos) throws IOException {
            file.seek(pos);
            resetBuffer();
        }


        @Override
        public int read() throws IOException {
            // Check whether the buffer needs to be filled
            if (nextIndex == lastIndex) {
                nextIndex = 0;
                lastIndex = file.read(buffer);
            }

            // Check whether file end is reached
            if (lastIndex < 0) {
                return lastIndex;
            }

            // Return next byte from the buffer
            return buffer[nextIndex++];
        }

    }

}
