package cz.zcu.kiv.pgs.radl.sp1.containers;

public class SaveResourceContainer implements ResourceContainer {
    public final int capacity;

    private int resourceCount;

    private String name;

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
            return count;
        }
        String format = "Container content is insufficient.Trying to remove:%d In container:%d";
        throw new IllegalArgumentException(String.format(format,count,resourceCount));
    }

    @Override
    public synchronized int empty()
    {
        int count = this.resourceCount;
        resourceCount = 0;
        return count;
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
            System.out.printf("Transferred %d from %s to %s%n",removed,from,this);
            return true;
        } catch (IllegalArgumentException e) {
            from.add(removed);
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("Container %s (%d/%d)",getName(), getResourceCount(),capacity);
    }

    @Override
    public String getName() {
        return name;
    }
}
