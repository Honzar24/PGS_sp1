package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.HasName;

public interface ResourceContainer extends HasName {

    void add(int count);

    int remove(int count);

    int getResourceCount();

    boolean isFull();

    boolean isEmpty();

    /**
     * Transfer amount of resources between containers.
     * If transfer can not be done revert to state before transfer
     *
     * @param from   Container where resource should be taken
     * @param amount of resources
     * @return false - transfer can not be done, true - transfer done
     */
    boolean transfer(ResourceContainer from, int amount);

}
