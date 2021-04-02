package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;
import cz.zcu.kiv.pgs.radl.sp1.Main;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CyclicBarrier;

/**
 * Singleton
 */
public final class Ferry implements Destination {

    public static final long S = Main.S;
    private static final Logger LOGGER = Main.logger;
    /**
     * Singleton instance
     */
    private static Ferry instance;
    /**
     * Barrier queue for lorrys size of queue is set on singleton creation
     */
    private final CyclicBarrier barrier;
    /**
     * Last time when ferry was going to other side
     */
    private long startOfLoading;

    /**
     * @param numberOfLorries size of queue for lorrys
     */
    private Ferry(int numberOfLorries) {
        Runnable ferry_full = () -> {
            long end = System.nanoTime();
            long waitingTime = end - startOfLoading;
            startOfLoading = end;
            LOGGER.info(String.format("%s is taking a trip after %d ms", getName(), waitingTime / S));
        };
        barrier = new CyclicBarrier(numberOfLorries, ferry_full);
        startOfLoading = System.nanoTime();
    }

    /**
     * Singleton creation if call multiple times then trows RuntimeException
     *
     * @param numberOfLorries size of queue
     */
    public static void create(int numberOfLorries) {
        if (instance != null) {
            throw new RuntimeException("Singleton create cannot be called multiple times");
        }
        instance = new Ferry(numberOfLorries);
    }

    /**
     * Return singleton instance if there is one otherwise throw RuntimeException
     *
     * @return singleton's instance
     */
    public static Ferry getInstance() {
        if (instance == null) {
            throw new RuntimeException("Call create firstly");
        }
        return instance;
    }

    @Override
    public String getName() {
        return "Ferry";
    }

    @Override
    public String toString() {
        return "Ferry";
    }

    public CyclicBarrier getBarrier() {
        return barrier;
    }
}
