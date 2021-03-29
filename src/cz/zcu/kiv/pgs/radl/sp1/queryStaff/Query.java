package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

import cz.zcu.kiv.pgs.radl.sp1.Destination;
import cz.zcu.kiv.pgs.radl.sp1.FileLoaderUtils;

public class Query implements Destination {

    private final String mapFile;

    public Query(String inputFilename) {
        mapFile = inputFilename;
    }

    @Override
    public String toString() {
        return "Query";
    }

    @Override
    public String getName() {
        return toString();
    }

    public String[] getMap() {
        return FileLoaderUtils.loadFile(mapFile);
    }
}
