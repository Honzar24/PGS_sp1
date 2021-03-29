package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.HasName;

public interface ResourceContainer extends HasName {

    void add(int count);

    int remove(int count);

    int empty();

    int getResourceCount();

    boolean isFull();

    boolean isEmpty();

    boolean transfer(ResourceContainer from, int amount);

}
