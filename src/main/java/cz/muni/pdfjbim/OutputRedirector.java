package cz.muni.pdfjbim;

import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Created by rhatlapa on 1/2/16.
 */
public class OutputRedirector extends Thread {

    private static final Logger log = LoggerFactory.getLogger(OutputRedirector.class);

    private final InputStream inputStream;
    private final OutputStream outputStream;

    public OutputRedirector(InputStream inputStream, OutputStream outputStream) {
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public OutputRedirector(InputStream inputStream) {
        this.inputStream = inputStream;
        this.outputStream = NullOutputStream.NULL_OUTPUT_STREAM;
    }

    @Override
    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            String line;

            while ((line = reader.readLine()) != null) {
                writer.write(line);
                log.debug(line);
            }
        } catch (IOException ex) {
            log.warn("Reading process output failed", ex);
        }
    }
}