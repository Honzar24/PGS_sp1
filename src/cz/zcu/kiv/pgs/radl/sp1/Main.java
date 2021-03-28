package cz.zcu.kiv.pgs.radl.sp1;

import cz.zcu.kiv.pgs.radl.sp1.containers.Ferry;
import cz.zcu.kiv.pgs.radl.sp1.containers.Lorry;
import cz.zcu.kiv.pgs.radl.sp1.containers.ResourceContainer;
import cz.zcu.kiv.pgs.radl.sp1.containers.SaveResourceContainer;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Cheif;
import cz.zcu.kiv.pgs.radl.sp1.queryStaff.Query;

import java.util.Arrays;
import java.util.HashMap;

public class Main {


    static  final private String help = "-i <vstupni soubor> -o <vystupni soubor> -cWorker <int> -tWorker <int> -capLorry <int> -tLorry <int> -capFerry <int>";
    public static void main(String[] args) {
        System.out.println("Arguments: " + Arrays.toString(args));
        HashMap<String, Integer> arguments = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            arguments.put(args[i], i);
        }
        String[] required = {"-i","-o","-cWorker","-tWorker","-capLorry","-tLorry","-capFerry"};
        for (String s:required)
        {
            if (!arguments.containsKey(s)) {
                System.out.printf("Missing parameter:%s please run program again with parameters: %s%n", s, help);
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



        Ferry.create(ferryCapacity/lorryCapacity);

        Lorry.setMaxTime(maxTimeLorry);

        Query query = new Query(new Cheif(inputFilename, workerCount));


        ResourceContainer container = new SaveResourceContainer(100,"1");
        container.add(100);
        ResourceContainer container3 = new SaveResourceContainer(100,"3");
        container3.add(100);

        Lorry lorry = new Lorry(query,Ferry.getInstance(), 20);
        ResourceContainer container2 = lorry;

        new Thread(()->{while (!container2.isFull()){
            System.out.printf("%b from %s%n",container2.transfer(container,1),container);
        }
            System.out.println(container);
        }).start();
        new Thread(()->{while (!container2.isFull()){
            System.out.printf("%b from %s%n",container2.transfer(container,1),container);
        }
            System.out.println(container);
        }).start();
        new Thread(()->{while (!container2.isFull()){
            System.out.printf("%b from %s%n",container2.transfer(container3,1),container3);
        }
            System.out.println(container3);
        }).start();
        new Thread(lorry).start();

    }
}
