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

    private static Ferry instance;

    private final CyclicBarrier barrier;
    private long startOfLoading;

    private Ferry(int numberOfLorries) {
        Runnable ferry_full = () -> {
            long end = System.nanoTime();
            long waitingTime = end - startOfLoading;
            startOfLoading = end;
            LOGGER.info(String.format("%s is taking a trip after %d s", getName(), waitingTime / S));
        };
        barrier = new CyclicBarrier(numberOfLorries, ferry_full);
        startOfLoading = System.nanoTime();
    }

    public static void create(int numberOfLorries) {
        if (instance != null) {
            throw new RuntimeException("Singleton create cannot be called multiple times");
        }
        instance = new Ferry(numberOfLorries);
    }

    public static Ferry getInstance() {
        assert instance != null :"Crete Ferry first";
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
