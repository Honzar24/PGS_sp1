package cz.zcu.kiv.pgs.radl.sp1.queryStaff;


import cz.zcu.kiv.pgs.radl.sp1.MapLoader;

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Queue;

public class Cheif {

    private final Queue<Block> blocks;

    public Cheif(String file, int numberOfWorkers) {
        blocks = new LinkedList<>();
        blocks.addAll(MapLoader.parseFile(file));
        createWorkers(numberOfWorkers);
    }

    private void createWorkers(int numberOfWorkers) {

    }

    public synchronized Block giveWork() throws NoSuchElementException
    {
        return blocks.remove();
    }
}
