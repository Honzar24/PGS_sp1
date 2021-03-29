package cz.zcu.kiv.pgs.radl.sp1;

import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Block;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Chief;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChiefTest {

    @ParameterizedTest(name = "{index}:nb:{1} data:{0}")
    @CsvFileSource(resources = "numberOfBlocks.csv", delimiter = ';', numLinesToSkip = 1)
    void numberOfBlocks(String string, int numberOfBlocks) {
        assertEquals(numberOfBlocks, Chief.parseMap(new String[]{string}).size());
    }

    @ParameterizedTest(name = "{index}:nx:{2} data:{0}")
    @CsvFileSource(resources = "numberOfBlocks.csv", delimiter = ';', numLinesToSkip = 1)
    void numberOfX(String string, int unused, int numberOfX) {
        List<Block> blocks = Chief.parseMap(string);
        int x = blocks.stream().mapToInt(Block::getBlockSize).reduce(0, Integer::sum);
        assertEquals(numberOfX, x);
    }
}