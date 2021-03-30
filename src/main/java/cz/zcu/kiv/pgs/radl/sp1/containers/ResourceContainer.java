package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;

public interface ResourceContainer extends Destination {
    /**
     * Tries add count resources to container. If failed nothing is added
     *
     * @param count positive number of added resources
     * @throws IllegalArgumentException If count is not positive(>=0) or container capacity is too small to add count.
     */
    void add(int count);

    /**
     * Tries remove count resources to container. If failed nothing is removed
     *
     * @param count positive number of removed resources
     * @throws IllegalArgumentException If count is not positive(>=0) or container content is too small to remove count.
     */
    int remove(int count);

    int getResourceCount();

    /**
     * @return resourceCount == capacity
     */
    boolean isFull();

    /**
     * @return resourceCount == 0
     */
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
