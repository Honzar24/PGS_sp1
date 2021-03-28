package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;

import java.util.Random;

public class Lorry implements Runnable, ResourceContainer {

    private static final Random rd = new Random();
    public static final long S = 1_000_000_000L;

    private static int maxTime;
    private static int numberInstances = 0;


    private final int instanceNumber;
    private final Destination UnloadingSpot;
    private final Destination LoadingSpot;
    private final SaveResourceContainer load;

    private Destination currentLocation;


    public Lorry(Destination loadingSpot, Destination unloadingSpot, int capacity) {
        currentLocation = loadingSpot;
        LoadingSpot = loadingSpot;
        UnloadingSpot = unloadingSpot;
        instanceNumber = ++numberInstances;
        load = new SaveResourceContainer(capacity,String.format("Lorry %d cargo",instanceNumber));
    }

    @Override
    public String toString() {
        return String.format("Lorry %d (%d/%d)", instanceNumber, load.getResourceCount(), load.capacity);
    }

    @Override
    public void run() {
        loading();
        tripTo(UnloadingSpot);
        ferryRide(Ferry.getInstance());
        tripTo(LoadingSpot);
    }

    private void ferryRide(Ferry ferry) {

    }


    private void tripTo(Destination destination) {

        long travelTime = rd.nextInt(maxTime) * S;
        long end = System.nanoTime() + travelTime;
        System.out.println(this + " is in " + currentLocation + " traveling to " + destination.getName() +" estimated travel time " + travelTime/S +" s");
        saveSleapTo(end);
        setCurrentLocation(destination);
    }

    public void setCurrentLocation(Destination newLocation) {
        this.currentLocation = newLocation;
        System.out.printf("%s on new location %s%n",this,currentLocation);
    }

    private synchronized void saveSleapTo(long end) {
        do {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
                // unexpected interruption
            }
        } while (System.nanoTime() < end);
    }

    private synchronized void loading() {
        while (!load.isFull()){
            try {
                wait();
            } catch (InterruptedException e) {
                // unexpected interruption
            }
        }
        System.out.println(this + " is loaded");
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
    public synchronized int empty() {
        return load.empty();
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
        if(load.transfer(from,amount))
        {
            saveSleapTo(System.nanoTime() + S);
            System.out.println(this +" form "+ from);
            notifyAll();
            return true;
        }
        return false;
    }

    @Override
    public String getName() {
        return toString();
    }
}
