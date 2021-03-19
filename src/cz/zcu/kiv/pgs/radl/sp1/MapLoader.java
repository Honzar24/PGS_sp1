package cz.zcu.kiv.pgs.radl.sp1;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MapLoader {
    public static List<Block> parseFile(String filename)
    {
        try {
            InputStream inputStream = MapLoader.class.getResource(filename).openStream();
            Stream<String> lines = new BufferedReader(new InputStreamReader(inputStream)).lines();
            return parseMap(lines.toArray(String[]::new));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return List.of();
    }


    public static List<Block> parseMap(String[] lines)
    {
        List<Block> blocks = new ArrayList<>();
        for (String line : lines)
        {
            Pattern pattern = Pattern.compile("x{1,}");
            Matcher matcher = pattern.matcher(line);
            while(matcher.find()){
                blocks.add(parseBlock(matcher.group()));
            }
        }
        return blocks;
    }

    private static Block parseBlock(String s) {
        return new Block(s.trim().length());
    }
}
