package mw.zookeeper;

import java.util.Scanner;

/**
 * Created by stephan on 07.07.15.
 */
public class MWZooKeeperTest {
    public static void main(String[] args) throws Exception {

        MWZooKeeper zookeeper = new MWZooKeeper();


        Scanner scanner = new Scanner(System.in);
        String cmd;
        while (!(cmd = scanner.nextLine()).isEmpty()) {

            try {
                String[] params = cmd.split("\\s+");

                switch (params[0]) {
                    case "create":
                        String answ = zookeeper.create(params[1], params[2].getBytes());
                        System.out.println(answ);
                        break;
                    case "delete":
                        zookeeper.delete(params[1], Integer.parseInt(params[2]));
                        break;
                    case "getData": {
                        MWStat answStat = new MWStat(Integer.parseInt(params[2]));
                        byte[] answByte = zookeeper.getData(params[1], answStat);
                        System.out.println("Antwort: " + new String(answByte) + " - Version: " + answStat.getVersion());
                        break;
                    }
                    case "setData":
                        MWStat newAnswStat = zookeeper.setData(params[1], params[2].getBytes(), Integer.parseInt
                            (params[3]));
                        System.out.println("Neue Version: " + newAnswStat.getVersion());
                        break;
                    case "attack":
                        for (int i = 0; i < 1000; i++) {
                            try {
                                MWStat answStat = new MWStat(Integer.parseInt(params[2]));
                                zookeeper.getData("/attack", answStat);
                                answStat = zookeeper.setData("/attack", new byte[]{1}, answStat.getVersion());
                                System.out.println("Neue Version: " + answStat.getVersion());
                            } catch (MWZooKeeperException e) {
                                System.err.println(e.getMessage());
                            }
                        }
                        break;
                    case "readtest": {
                        int n = 50;
                        MWStat answStat = new MWStat(Integer.parseInt(params[2]));
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < n; i++) {
                            zookeeper.getData("/attack", answStat);
                        }
                        long end = System.currentTimeMillis();

                        System.out.println("Read Average time: " + (end - start) / n + "ms");
                        break;
                    }
                    case "writetest": {
                        int n = 50;
                        MWStat answStat = new MWStat(Integer.parseInt(params[2]));
                        zookeeper.getData("/attack", answStat);
                        long start = System.currentTimeMillis();
                        for (int i = 0; i < n; i++) {
                            answStat = zookeeper.setData("/attack", new byte[]{1}, answStat.getVersion());
                        }
                        long end = System.currentTimeMillis();

                        System.out.println("Write Average time: " + (end - start) / n + "ms");
                        break;
                    }
                    default:
                        System.err.println("Invalid command");
                        break;
                }

            } catch (Exception e) {
                if (e instanceof ArrayIndexOutOfBoundsException) {
                    System.err.println("Provide all required parameters, stupid!");
                } else {
                    if (!e.getMessage().isEmpty()) {
                        System.err.println(e.getMessage());
                    }
                }
            }
        }
    }
}
