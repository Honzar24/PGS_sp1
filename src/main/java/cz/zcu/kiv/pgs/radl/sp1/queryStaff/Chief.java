package cz.zcu.kiv.pgs.radl.sp1.queryStaff;


import cz.zcu.kiv.pgs.radl.sp1.Main;
import cz.zcu.kiv.pgs.radl.sp1.containers.Ferry;
import cz.zcu.kiv.pgs.radl.sp1.containers.Lorry;
import cz.zcu.kiv.pgs.radl.sp1.containers.ResourceContainer;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chief {
    public static final Logger LOGGER = Main.logger;

    private final Queue<Block> blocks;
    private final Query query;
    private final ExecutorService lorries;
    private final ExecutorService workers;
    private final List<WorkerProductivity> workersProductivity;
    private final ResourceContainer endDestinations;

    private Lorry lorryInQuery;


    public Chief(int numberOfWorkers, Query query, ResourceContainer endDestination) {
        blocks = new LinkedList<>();
        lorries = Executors.newFixedThreadPool(Integer.MAX_VALUE);
        workers = Executors.newFixedThreadPool(numberOfWorkers);
        workersProductivity = new LinkedList<>();
        this.query = query;
        this.endDestinations = endDestination;
        blocks.addAll(parseMap(this.query.getMap()));
        int resourceCount = blocks.stream().mapToInt(Block::getBlockSize).reduce(0, Integer::sum);
        LOGGER.info(String.format("Found %d resources in %d blocks", resourceCount, blocks.size()));
        if (hasWork()) {
            prepareLorry();
            runWorkers(numberOfWorkers);
        }

    }

    public static List<Block> parseMap(String... lines) {
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

    public synchronized Lorry getLorryInQuery() {
        if (lorryInQuery.isFull()) {
            prepareLorry();
        }
        return lorryInQuery;
    }

    public final synchronized void prepareLorry() {
        LOGGER.trace("Preparing new lorry");
        lorryInQuery = new Lorry(query, Ferry.getInstance(), endDestinations);
        lorries.execute(lorryInQuery);
    }

    /**
     * Method for workers
     *
     * @return block that need to be mined.
     */
    public synchronized Block giveWork() {
        return blocks.remove();
    }

    private void runWorkers(int numberOfWorkers) {
        for (int i = 0; i < numberOfWorkers; i++) {
            workers.execute(new Worker(this));
        }
    }

    public synchronized boolean hasWork() {
        return !blocks.isEmpty();
    }

    /**
     * Busy wait methode to wait after all block in query are done.
     */
    public void waitUntilWorkDone() {
        waitUntilDone(workers);
        if (lorryInQuery != null) {
            lorryInQuery.forceRide();
        }
        waitUntilDone(lorries);
        LOGGER.info("Job done");
    }

    /**
     * Busy wait for service task completion
     *
     * @param service
     */
    private void waitUntilDone(ExecutorService service) {
        service.shutdown();
        while (!service.isTerminated()) {
            try {
                //noinspection BusyWait
                Thread.sleep(10);
            } catch (InterruptedException e) {
                LOGGER.trace("Unexpected interruption");
            }
        }
    }

    /**
     * Workers call this methode to signalised that worker is done for today and his productivity can be logged.
     *
     * @param worker
     * @param resourceMined
     */
    public synchronized void shiftEnd(Worker worker, int resourceMined) {
        workersProductivity.add(new WorkerProductivity(worker, resourceMined));
    }

    public void logProductivity() {
        WorkerProductivity[] array = workersProductivity.stream().sorted().toArray(WorkerProductivity[]::new);
        LOGGER.info(String.format("Worker productivity list of %d workers", array.length));
        for (WorkerProductivity ak : array) {
            LOGGER.info(ak.toString());
        }
        LOGGER.info("End of list");
    }

    private static class WorkerProductivity implements Comparable<WorkerProductivity> {
        public final Worker worker;
        public final int resourceMined;

        public WorkerProductivity(Worker worker, int resourceMined) {
            this.worker = worker;
            this.resourceMined = resourceMined;
        }

        @Override
        public int compareTo(Chief.WorkerProductivity other) {
            return this.worker.instanceNumber - other.worker.instanceNumber;
        }

        @Override
        public String toString() {
            return String.format("%s have mined %d resources", worker, resourceMined);
        }
    }
}
