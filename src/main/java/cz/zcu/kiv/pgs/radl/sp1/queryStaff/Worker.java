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
    private static int defaultMiningTime;

    public final int instanceNumber;
    public final int maxMiningTime;
    public final Chief chief;
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

    public static void setDefaultMiningTime(int defaultMiningTime) {
        Worker.defaultMiningTime = defaultMiningTime;
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

    private void loadLorry() {
        while (!load.isEmpty()) {
            Lorry lorry = chief.getLorryInQuery();
            if (!lorry.isFull()) {
                lorry.transfer(this, 1);
            }
        }
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
        long miningTime = (RD.nextInt(maxMiningTime - 1) + 1) * S;
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
