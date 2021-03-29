package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

import cz.zcu.kiv.pgs.radl.sp1.Main;
import cz.zcu.kiv.pgs.radl.sp1.containers.ResourceContainer;
import cz.zcu.kiv.pgs.radl.sp1.containers.SaveResourceContainer;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class Worker implements Runnable, ResourceContainer {

    public static final Logger LOGGER = Main.LOGGER;
    public static final long S = 1_000_000_000L;
    private static final Random rd = new Random();
    private static int numberInstances = 0;
    private static int maxMiningTime;

    private final int instanceNumber;
    private final Cheif cheif;
    private final ResourceContainer load;

    public Worker(Cheif cheif) {
        this.cheif = cheif;
        instanceNumber = ++numberInstances;
        load = new SaveResourceContainer(Integer.MAX_VALUE, String.format("Worker %d backpack", instanceNumber));
    }

    public static void setMaxMiningTime(int maxMiningTime) {
        Worker.maxMiningTime = maxMiningTime;
    }

    @Override
    public void run() {
        while (cheif.hasWork()) {
            Block block = cheif.giveWork();
            LOGGER.trace(String.format("%s received %s to mine.", getName(), block));
            mineBlock(block);
            //TODO Lorry load
        }
        LOGGER.debug(getName() + " done");
    }

    private void mineBlock(Block block) {
        long start = System.nanoTime();
        for (int i = 0; i < block.getBlockSize(); i++) {
            long resourceStart = System.nanoTime();
            mineResource(block);
            long resourceMineTime = System.nanoTime() - resourceStart;
            LOGGER.info(String.format("%s mined one resource in %d s", getName(), resourceMineTime / S));
        }
        long mineTime = System.nanoTime() - start;
        LOGGER.info(String.format("%s mined %s with size %d in %d s", getName(), block.getName(), block.getBlockSize(), mineTime / S));
    }

    private void mineResource(Block block) {
        long start = System.nanoTime();
        long miningTime = rd.nextInt(maxMiningTime) * S;
        LOGGER.trace(String.format("%s is mining resource estimated mining time %d s", getName(), miningTime / S));
        saveSleepTo(start + miningTime);
        load.transfer(block, 1);
    }

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

    @Override
    public String getName() {
        return String.format("Worker %d", instanceNumber);
    }

    @Override
    public String toString() {
        return getName();
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
        return load.transfer(from, amount);
    }
}
