package cz.zcu.kiv.pgs.radl.sp1.queryStaff;

import cz.zcu.kiv.pgs.radl.sp1.Destination;

public class Query implements Destination {
    public Query(Cheif cheif) {

    }

    @Override
    public String toString() {
        return "Query";
    }

    @Override
    public String getName() {
        return toString();
    }
}
