package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Main;
import org.apache.logging.log4j.Logger;

public class SaveResourceContainer implements ResourceContainer {
    public static final Logger LOGGER = Main.logger;
    public final int capacity;
    private final String name;

    private volatile int resourceCount;

    public SaveResourceContainer(int capacity, String name) {
        this.capacity = capacity;
        this.name = name;
        resourceCount = 0;
    }

    public SaveResourceContainer(int capacity) {
        this(capacity,"anonymous");
    }

    @Override
    public synchronized void add(int count)
    {
        isPositive(count);

        if ((resourceCount + count) <= capacity)
        {
            resourceCount += count;
            LOGGER.trace(String.format("%s added %d resources", getName(), count));
            return;
        }
        String format = "Container capacity is insufficient. Capacity:%d Amount:%d In container:%d";
        throw new IllegalArgumentException(String.format(format,capacity,count,resourceCount));
    }

    private void isPositive(int count) {
        if (count < 0)
        {
            throw new IllegalArgumentException("Amount must be positive number not "+ count);
        }
    }

    @Override
    public synchronized int remove(int count)
    {
        isPositive(count);
        if ((resourceCount - count) >= 0) {
            resourceCount -= count;
            LOGGER.trace(String.format("%s removed %d resources", getName(), count));
            return count;
        }
        String format = "Container content is insufficient.Trying to remove:%d In container:%d";
        throw new IllegalArgumentException(String.format(format,count,resourceCount));
    }

    @Override
    public synchronized int getResourceCount() {
        return resourceCount;
    }

    @Override
    public synchronized boolean isFull()
    {
        return resourceCount == capacity;
    }
    @Override
    public synchronized boolean isEmpty()
    {
        return resourceCount == 0;
    }

    @Override
    public synchronized boolean transfer(ResourceContainer from, int amount) {
        int removed = 0;
        try {
            removed = from.remove(amount);
            add(removed);
            LOGGER.trace(String.format("Transferred %d resource from %s to %s", removed, from.getName(), this.getName()));
            return true;
        } catch (IllegalArgumentException e) {
            LOGGER.error(e);
            from.add(removed);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("%s (%d/%d)", getName(), getResourceCount(), capacity);
    }

    @Override
    public String getName() {
        return name;
    }
}
