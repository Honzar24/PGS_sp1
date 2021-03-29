package cz.zcu.kiv.pgs.radl.sp1.containers;

import cz.zcu.kiv.pgs.radl.sp1.Destination;

import java.util.concurrent.CyclicBarrier;

public final class Ferry implements Destination {


    private static Ferry instance;

    private final CyclicBarrier barrier;

    private Ferry(int numberOfLorries) {
        barrier = new CyclicBarrier(numberOfLorries, () -> System.out.println("Ferry barrier"));
        //TODO
    }

    public static void create(int numberOfLorries)
    {
        if (instance != null)
        {
            throw new RuntimeException("Singleton create cannot be called multiple times");
        }
        instance = new Ferry(numberOfLorries);
    }

    public static Ferry getInstance() {
        assert instance != null :"Crete Ferry first";
        return instance;
    }

    @Override
    public String getName() {
        return toString();
    }

    @Override
    public String toString() {
        return "Ferry";
    }
}
