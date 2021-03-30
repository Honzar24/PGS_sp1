package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;

public class Warehouse implements ResourceContainer, Destination {

    private final ResourceContainer storage;

    public Warehouse() {
        this(new SaveResourceContainer(Integer.MAX_VALUE, "Warehouse storage"));
    }

    public Warehouse(ResourceContainer storage) {
        this.storage = storage;
    }


    @Override
    public String getName() {
        return "Warehouse";
    }

    @Override
    public void add(int count) {
        storage.add(count);
    }

    @Override
    public int remove(int count) {
        return storage.remove(count);
    }

    @Override
    public int getResourceCount() {
        return storage.getResourceCount();
    }

    @Override
    public boolean isFull() {
        return storage.isFull();
    }

    @Override
    public boolean isEmpty() {
        return storage.isEmpty();
    }

    @Override
    public boolean transfer(ResourceContainer from, int amount) {
        return storage.transfer(from, amount);
    }
}
