package cz.zcu.kiv.pgs.radl.sp1.queryStaff;


import cz.zcu.kiv.pgs.radl.sp1.Main;
import cz.zcu.kiv.pgs.radl.sp1.containers.Ferry;
import cz.zcu.kiv.pgs.radl.sp1.containers.Lorry;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cheif {
    public static final Logger LOGGER = Main.LOGGER;

    private final Queue<Block> blocks;
    private final Query query;
    private Lorry lorryInQuery;

    public Cheif(int numberOfWorkers, Query query) {
        blocks = new LinkedList<>();
        this.query = query;
        blocks.addAll(parseMap(this.query.getMap()));
        int resourceCount = blocks.stream().mapToInt(Block::getBlockSize).reduce(0, Integer::sum);
        LOGGER.info(String.format("Found %d resources in %d blocks", resourceCount, blocks.size()));
        prepareLorry();
        runWorkers(numberOfWorkers);
    }

    public static List<Block> parseMap(String[] lines) {
        List<Block> blocks = new ArrayList<>();
        for (String line : lines) {
            Pattern pattern = Pattern.compile("x+");
            Matcher matcher = pattern.matcher(line);
            while (matcher.find()) {
                blocks.add(parseBlock(matcher.group()));
            }
        }
        return blocks;
    }

    private static Block parseBlock(String s) {
        return new Block(s.trim().length());
    }

    public Lorry getLorryInQuery() {
        return lorryInQuery;
    }

    public synchronized void prepareLorry() {
        lorryInQuery = new Lorry(query, Ferry.getInstance());
        new Thread(lorryInQuery).start();
    }

    public synchronized Block giveWork() throws NoSuchElementException {
        return blocks.remove();
    }

    private void runWorkers(int numberOfWorkers) {
        ExecutorService workers = Executors.newFixedThreadPool(numberOfWorkers);
        for (int i = 0; i < numberOfWorkers; i++) {
            workers.execute(new Worker(this));
        }
        workers.shutdown();
    }

    public synchronized boolean hasWork() {
        return !blocks.isEmpty();
    }

}
