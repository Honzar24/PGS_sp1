package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;
import cz.zcu.kiv.pgs.radl.sp1.Main;
import org.apache.logging.log4j.Logger;

import java.util.Random;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

public class Lorry implements Runnable, ResourceContainer {

    public static final long S = Main.S;

    private static final Random RD = new Random();
    private static final Logger LOGGER = Main.logger;
    /**
     * Number of ms waiting after load
     */
    public static final int LOADMS = 10;
    /**
     * upper bound of travel time used if is not filled in constructor
     */
    private static int defMaxTravelTime;
    /**
     * capacity used if capacity is not filled in constructor
     */
    private static int defCapacity;
    /**
     * number of lorries from start of program
     */
    private static int numberInstances;
    /**
     * number of lorry in program
     */
    private final int instanceNumber;
    /**
     * upper bound of maxim ms to travel to some location
     */
    private final int maxTravelTime;
    /**
     * Lorries route from loading point to ferry minimum size in 2
     */
    private final Destination[] route;
    /**
     * Location of storage facility
     */
    private final ResourceContainer UnloadingSpot;
    /**
     * inventory of lorry
     */
    private final SaveResourceContainer load;
    /**
     * current location of lorry
     */
    private Destination currentLocation;
    /**
     * false-lorry stop loading phase before full
     */
    private boolean canLoad = true;

    /**
     * Lorry with routeToFerry with locations minimum size is 2 first is start location an d last is ferry
     *
     * @param unloadingSpot end point of lorry and resource storage
     * @param capacity      capacity of lorry in number of resources
     * @param maxTravelTime upper bound of maxim ms to travel to some location
     * @param routeToFerry  minimum size is 2 first is start location an d last is ferry
     */
    public Lorry(ResourceContainer unloadingSpot, int capacity, int maxTravelTime, Destination... routeToFerry) {
        UnloadingSpot = unloadingSpot;
        instanceNumber = ++numberInstances;
        this.route = routeToFerry;
        this.currentLocation = routeToFerry[0];
        this.maxTravelTime = maxTravelTime;
        load = new SaveResourceContainer(capacity, String.format(getName() + " cargo", instanceNumber));
        LOGGER.trace(String.format("New %s created", getName()));
    }

    /**
     * @param loadingSpot   Start location of lorry
     * @param ferryLocation Location of ferry
     * @param unloadingSpot Location of storage and end point of lorry
     */
    public Lorry(Destination loadingSpot, Destination ferryLocation, ResourceContainer unloadingSpot) {
        this(unloadingSpot, defCapacity, defMaxTravelTime, loadingSpot, ferryLocation);
    }

    public static void setDefCapacity(int defCapacity) {
        Lorry.defCapacity = defCapacity;
    }

    public static void setDefMaxTravelTime(int defMaxTravelTime) {
        Lorry.defMaxTravelTime = defMaxTravelTime;
    }

    @Override
    public void run() {
        load();
        for (int i = 1; i < route.length; i++) {
            tripTo(route[i]);
        }
        ferryRide(Ferry.getInstance());
        tripTo(UnloadingSpot);
        UnloadingSpot.transfer(load, getResourceCount());
        LOGGER.debug(getName() + " done");
    }

    /**
     * Join waiting queue at ferry
     *
     * @param ferry
     */
    private void ferryRide(Ferry ferry) {
        try {
            CyclicBarrier barrier = ferry.getBarrier();
            LOGGER.debug(String.format("%s arrived at ferry there is %d/%d lorries waiting", getName(), barrier.getNumberWaiting(), barrier.getParties()));
            barrier.await();
        } catch (InterruptedException e) {
            LOGGER.trace("unexpected interruption");
            ferryRide(ferry);
        } catch (BrokenBarrierException e) {
            LOGGER.info("Barrier broke for {}", getName());
        }
        LOGGER.debug(String.format("%s leaving ferry", getName()));
    }

    /**
     * Change currentLocation to new place rolls for travel time
     *
     * @param destination new destination where lorry should by
     */
    private void tripTo(Destination destination) {
        long start = System.nanoTime();
        long travelTime = (RD.nextInt(maxTravelTime) + 1) * S;
        LOGGER.debug(String.format("%s is in %s going to %s estimated travel time %d ms", getName(), currentLocation.getName(), destination.getName(), travelTime / S));
        saveSleepTo(start + travelTime);
        setCurrentLocation(destination, (System.nanoTime() - start) / S);
    }

    public void setCurrentLocation(Destination newLocation, long travelTime) {
        this.currentLocation = newLocation;
        LOGGER.info(String.format("%s is on new location %s travel time %d ms", getName(), currentLocation.getName(), travelTime));
    }

    /**
     * Busy waiting to end time stamp
     *
     * @param end timestamp in nanos
     */
    private synchronized void saveSleepTo(long end) {
        do {
            try {
                //noinspection BusyWait
                Thread.sleep(1);
            } catch (InterruptedException e) {
                LOGGER.trace("unexpected interruption");
            }
        } while (System.nanoTime() < end);
    }

    /**
     * Loading loop if lorry is not full then there is noting to do
     */
    private void load() {
        long startOfLoading = System.nanoTime();
        synchronized (this) {
            while ((!load.isFull()) && canLoad) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    LOGGER.trace("unexpected interruption");
                }
            }
        }
        long end = System.nanoTime() - startOfLoading;
        LOGGER.info(String.format("%s is loaded with %d resources after %d ms", getName(), load.getResourceCount(), end / S));
    }

    /**
     * Force lorry to stop load and take a trip
     */
    public void stopLoad() {
        synchronized (this) {
            LOGGER.debug(String.format("%s force stop loading", getName()));
            canLoad = false;
            notifyAll();
        }
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
        long end = System.nanoTime() + S * LOADMS;
        if (load.transfer(from, amount)) {
            saveSleepTo(end);
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
}
