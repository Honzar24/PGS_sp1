package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

public class Block {
    final int blockSize;
    int available;

    public Block(int blockSize) {
        this.blockSize = blockSize;
        available = blockSize;
    }

    public int getBlockSize() {
        return blockSize;
    }

    @Override
    public String toString() {
        return "Block{" +
                "blockSize=" + blockSize +
                ", available=" + available +
                '}';
    }
}
