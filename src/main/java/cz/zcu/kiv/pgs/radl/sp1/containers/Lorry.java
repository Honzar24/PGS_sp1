package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;
import cz.zcu.kiv.pgs.radl.sp1.Main;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Lorry implements Runnable, ResourceContainer {

    public static final long S = Main.S;

    private static final Random RD = new Random();
    private static final Logger LOGGER = Main.logger;

    private static int maxTime;
    private static int capacity;
    private static int numberInstances;

    private final int instanceNumber;
    private final Destination UnloadingSpot;
    private final Destination LoadingSpot;
    private final SaveResourceContainer load;

    private Destination currentLocation;
    private boolean canLoadAndNotLast;


    public Lorry(Destination loadingSpot, Destination unloadingSpot, int capacity) {
        currentLocation = loadingSpot;
        LoadingSpot = loadingSpot;
        UnloadingSpot = unloadingSpot;
        instanceNumber = ++numberInstances;
        load = new SaveResourceContainer(capacity, String.format(getName() + " cargo", instanceNumber));
        canLoadAndNotLast = true;
        LOGGER.trace(String.format("New %s created", getName()));
    }

    public Lorry(Destination loadingSpot, Destination unloadingSpot) {
        this(loadingSpot, unloadingSpot, capacity);
    }

    public static void setCapacity(int capacity) {
        Lorry.capacity = capacity;
    }

    @Override
    public void run() {
        loading();
        tripTo(UnloadingSpot);
        ferryRide(Ferry.getInstance());
        tripTo(LoadingSpot);
        LOGGER.debug(getName() + " done");
    }

    private void ferryRide(Ferry ferry) {
        try {
            CyclicBarrier barrier = ferry.getBarrier();
            LOGGER.debug(String.format("%s arrived at ferry there is %d/%d lorries waiting", getName(), barrier.getNumberWaiting(), barrier.getParties()));
            boolean last = (barrier.getNumberWaiting() + 1) == barrier.getParties();
            if (last || canLoadAndNotLast) {
                barrier.await();
            } else {
                LOGGER.info(String.format("Forced %s giving chance(%d s) too others lorries to finish their trips.", getName(), maxTime));
                //Ensures that forced lorry is last lorry at barrier
                saveSleepTo(System.nanoTime() + maxTime * S);
                barrier.await(1000, TimeUnit.MILLISECONDS);
            }
        } catch (InterruptedException e) {
            LOGGER.trace("unexpected interruption");
            ferryRide(ferry);
        } catch (BrokenBarrierException e) {
            LOGGER.info("Barrier broke by last lorry hopefully. {} released", getName());
        } catch (TimeoutException e) {
            LOGGER.info("Forced lorry waited too long. Breaks barrier to prevent deadlock");
            Ferry.getInstance().getBarrier().reset();
        }
        LOGGER.debug(String.format("%s leaving ferry", getName()));
        ferry.transfer(load, load.getResourceCount());
    }


    private void tripTo(Destination destination) {
        long start = System.nanoTime();
        long travelTime = (RD.nextInt(maxTime) + 1) * S;
        LOGGER.debug(String.format("%s is in %s going to %s estimated travel time %d s", getName(), currentLocation, destination, travelTime / S));
        saveSleepTo(start + travelTime);
        setCurrentLocation(destination, (System.nanoTime() - start) / S);
    }

    public void setCurrentLocation(Destination newLocation, long travelTime) {
        this.currentLocation = newLocation;
        LOGGER.info(String.format("%s is on new location %s travel time %d s", getName(), currentLocation, travelTime));
    }

    private synchronized void saveSleepTo(long end) {
        do {
            try {
                //noinspection BusyWait
                Thread.sleep(100);
            } catch (InterruptedException e) {
                LOGGER.trace("unexpected interruption");
            }
        } while (System.nanoTime() < end);
    }

    private void loading() {
        long startOfLoading = System.nanoTime();
        synchronized (this) {
            while ((!load.isFull()) && canLoadAndNotLast) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.trace("unexpected interruption");
                }
            }
        }
        long end = System.nanoTime() - startOfLoading;
        LOGGER.info(String.format("%s is loaded with %d after %d s", getName(), load.getResourceCount(), end / S));
    }

    public static void setMaxTime(int maxTime) {
        Lorry.maxTime = maxTime;
    }

    @Override
    public synchronized void add(int count) {
        load.add(count);
    }

    @Override
    public synchronized int remove(int count) {
        return load.remove(count);
    }

    @Override
    public synchronized int getResourceCount() {
        return load.getResourceCount();
    }

    @Override
    public synchronized boolean isFull() {
        return load.isFull();
    }

    @Override
    public synchronized boolean isEmpty() {
        return load.isEmpty();
    }

    @Override
    public synchronized boolean transfer(ResourceContainer from, int amount) {
        if (load.transfer(from, amount)) {

            saveSleepTo(System.nanoTime() + S);
            notifyAll();
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Lorry %d (%d/%d)", instanceNumber, load.getResourceCount(), load.capacity);
    }

    @Override
    public final String getName() {
        return String.format("Lorry %d", instanceNumber);
    }

    /**
     * This methode is called for last lorry this lorry have special role to break barrier
     */
    public synchronized void forceRide() {
        LOGGER.info(getName() + " is forced to stop loading");
        canLoadAndNotLast = false;
        notifyAll();
    }
}
