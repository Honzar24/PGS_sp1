package cz.zcu.kiv.pgs.radl.sp1;

import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Path;
import java.util.stream.Stream;

public class FileLoader {
    public static final Logger LOGGER = Main.LOGGER;

    public static String[] loadFile(String filename) {
        try {
            LOGGER.trace("Loading " + filename);
            InputStream resourceAsStream = Main.class.getClassLoader().getResourceAsStream(filename);
            if (resourceAsStream == null) {
                LOGGER.debug(filename + " not found in res loading from current dir");
                resourceAsStream = new FileInputStream(Path.of(filename).toFile());
            }
            Stream<String> lines = new BufferedReader(new InputStreamReader(resourceAsStream)).lines();
            LOGGER.trace("returning lines");
            return lines.toArray(String[]::new);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
        LOGGER.trace("returning empty list");
        return new String[]{};
    }
}
