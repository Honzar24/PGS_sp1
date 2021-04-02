package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

import cz.zcu.kiv.pgs.radl.sp1.Main;
import cz.zcu.kiv.pgs.radl.sp1.containers.Lorry;
import cz.zcu.kiv.pgs.radl.sp1.containers.ResourceContainer;
import cz.zcu.kiv.pgs.radl.sp1.containers.SaveResourceContainer;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class Worker implements Runnable, ResourceContainer {

    public static final Logger LOGGER = Main.logger;
    public static final long S = Main.S;
    private static final Random RD = new Random();

    private static int numberInstances;
    /**
     * upper bound of maxim ms to mine one resource from block if not filled in constructor
     */
    private static int defaultMiningTime;
    /**
     * number of worker in program
     */
    public final int instanceNumber;
    /**
     * upper bound of maxim ms to mine one resource from block
     */
    public final int maxMiningTime;
    /**
     * chief of this worker
     */
    public final Chief chief;
    /**
     * Workers "unlimited" backpack
     */
    private final ResourceContainer load;

    public Worker(Chief chief, int maxMiningTime) {
        instanceNumber = ++numberInstances;
        this.maxMiningTime = maxMiningTime;
        this.chief = chief;
        load = new SaveResourceContainer(Integer.MAX_VALUE, String.format("Worker %d backpack", instanceNumber));
        LOGGER.trace(String.format("New %s created with %d max mining time", this.getName(), maxMiningTime));
    }

    public Worker(Chief chief) {
        this(chief, defaultMiningTime);
    }


    @Override
    public void run() {
        int resourceMined = 0;
        while (chief.hasWork()) {
            Block block = chief.giveWork();
            LOGGER.trace(String.format("%s received %s to mine.", getName(), block));
            mineBlock(block);
            resourceMined += load.getResourceCount();
            loadLorry();
        }
        chief.shiftEnd(this, resourceMined);
        LOGGER.debug(String.format("%s done mined %d resources", getName(), resourceMined));
    }

    public static void setDefaultMiningTime(int defaultMiningTime) {
        Worker.defaultMiningTime = defaultMiningTime;
    }

    /**
     * Load lorry with one resource from backpack
     */
    private void loadLorry() {
        while (!load.isEmpty()) {
            Lorry lorry = chief.getLorryInQuery();
            if (!lorry.isFull()) {
                lorry.transfer(this, 1);
            }
        }
    }

    /**
     * Mine block received form chief
     *
     * @param block
     */
    private void mineBlock(Block block) {
        long start = System.nanoTime();
        for (int i = 0; i < block.getBlockSize(); i++) {
            long resourceStart = System.nanoTime();
            mineResource(block);
            long resourceMineTime = System.nanoTime() - resourceStart;
            LOGGER.info(String.format("%s mined one resource in %d ms", getName(), resourceMineTime / S));
        }
        long mineTime = System.nanoTime() - start;
        LOGGER.info(String.format("%s mined %s with size %d in %d ms", getName(), block.getName(), block.getBlockSize(), mineTime / S));
    }

    /**
     * Mine one resource form block
     *
     * @param block
     */
    private void mineResource(Block block) {
        long start = System.nanoTime();
        long miningTime = (RD.nextInt(maxMiningTime - 1) + 1) * S;
        LOGGER.trace(String.format("%s is mining resource estimated mining time %d ms", getName(), miningTime / S));
        saveSleepTo(start + miningTime);
        load.transfer(block, 1);
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
