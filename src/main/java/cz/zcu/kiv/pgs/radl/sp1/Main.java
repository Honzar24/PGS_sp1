package cz.zcu.kiv.pgs.radl.sp1;

import cz.zcu.kiv.pgs.radl.sp1.containers.Ferry;
import cz.zcu.kiv.pgs.radl.sp1.containers.Lorry;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Chief;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Query;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Worker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Arrays;
import java.util.HashMap;

public final class Main {
    public static final long S = 1_000_000L;

    private static final String HELP = "-i <vstupni soubor> -o <vystupni soubor> -cWorker <int> -tWorker <int> -capLorry <int> -tLorry <int> -capFerry <int>";

    public static Logger logger;

    private Main() {
    }

    public static void main(String[] args) {
        System.out.println("Arguments: " + Arrays.toString(args));
        HashMap<String, Integer> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            arguments.put(args[i], i);
        }
        String[] required = {"-i", "-o", "-cWorker", "-tWorker", "-capLorry", "-tLorry", "-capFerry"};
        for (String s : required) {
            if (!arguments.containsKey(s)) {
                System.out.printf("Missing parameter:%s please run program again with parameters: %s%n", s, HELP);
                return;
            }
        }

        String inputFilename = args[arguments.get("-i") + 1];
        String outputFilename = args[arguments.get("-o") + 1];
        int workerCount = Integer.parseInt(args[arguments.get("-cWorker") + 1]);
        int maxTimeWorker = Integer.parseInt(args[arguments.get("-tWorker") + 1]);
        int lorryCapacity = Integer.parseInt(args[arguments.get("-capLorry") + 1]);
        int maxTimeLorry = Integer.parseInt(args[arguments.get("-tLorry") + 1]);
        int ferryCapacity = Integer.parseInt(args[arguments.get("-capFerry") + 1]);

        System.setProperty("logFilename", outputFilename);
        System.setProperty("log4j.configurationFile", "log_config.xml");
        logger = LogManager.getLogger(Main.class);

        Ferry.create(ferryCapacity / lorryCapacity);
        Lorry.setMaxTime(maxTimeLorry);
        Lorry.setCapacity(lorryCapacity);
        Worker.setDefaultMiningTime(maxTimeWorker);

        Query query = new Query(inputFilename);
        Chief chief = new Chief(workerCount, query);

        chief.waitUntilWorkDone();
        chief.logProductivity();
        logger.info("Ferry transported {} of resource to other side of river", Ferry.getInstance().getResourceCount());
        logger.info("End");
    }
}
