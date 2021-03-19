package cz.zcu.kiv.pgs.radl.sp1;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Block> blocks = MapLoader.parseFile(args[0]);
        blocks.forEach(block -> System.out.println(block.toString()));
    }
}
