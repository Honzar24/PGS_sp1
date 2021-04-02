package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

import cz.zcu.kiv.pgs.radl.sp1.containers.SaveResourceContainer;

public class Block extends SaveResourceContainer {
    /**
     * Number of instances
     */
    private static int instanceCount;

    public Block(int blockSize) {
        super(blockSize, "Query block " + (++instanceCount));
        add(blockSize);
    }

    public int getBlockSize() {
        return capacity;
    }

}
