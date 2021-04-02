package cz.zcu.kiv.pgs.radl.sp1;

import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Stream;

public final class FileLoaderUtils {
    public static final Logger LOGGER = Main.logger;

    private FileLoaderUtils() {
    }

    /**
     * Transform filename to lines of file if this file exist
     * Firstly try load file from resources if this file not exist there then uses current location
     *
     * @param filename filename
     * @return if can not find file returns empty array of lines otherwise array of lines separated by system endline separator
     */
    public static String[] loadFile(String filename) {
        try {
            LOGGER.trace("Loading " + filename);
            InputStream resourceAsStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);
            if (resourceAsStream == null) {
                LOGGER.debug(filename + " not found in res loading from current dir");
                resourceAsStream = Files.newInputStream(Paths.get(filename));
            }
            Stream<String> lines = new BufferedReader(new InputStreamReader(resourceAsStream)).lines();
            LOGGER.trace("returning lines");
            String[] ret = lines.toArray(String[]::new);
            resourceAsStream.close();
            lines.close();
            return ret;
        } catch (IOException e) {
            LOGGER.error(e);
        }
        LOGGER.trace("returning empty list");
        return new String[]{};
    }
}
