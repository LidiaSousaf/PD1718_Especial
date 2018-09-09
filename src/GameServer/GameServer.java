/**
 * Created by Lídia on 29/08/2018
 */

package GameServer;

import CommunicationCommons.HeartbeatInterface;

import java.net.InetAddress;
import java.rmi.Naming;
import java.util.Scanner;

public class GameServer {

    private static final String MANAGEMENT_ADDRESS = "localhost";

    public static boolean stopThreads = false;

    public static void main(String args[]) {
//        String managementServerAddr = MANAGEMENT_ADDRESS;
//        if (args.length >= 1) {
//            managementServerAddr = args[0];
//        }


        String managementServerAddr = null;

        if (args.length >= 1) {
            managementServerAddr = args[0];
        } else {
            Scanner sc = new Scanner(System.in);

            while (managementServerAddr == null || managementServerAddr.isEmpty()) {
                System.out.println("Insert the IP address of the Management Server:");
                managementServerAddr = sc.nextLine();
            }
        }

        startManagementServerConnection(managementServerAddr);
    }

    private static void startManagementServerConnection(String managementAddress) {
        try {
            String heartbeatServiceURL = "rmi://" + managementAddress + "/HeartbeatService";
            HeartbeatInterface heartbeatService = (HeartbeatInterface) Naming.lookup(heartbeatServiceURL);

            System.out.println("Game Server started.");

            String localIp = InetAddress.getLocalHost().getHostAddress();
            HeartbeatHandler heartbeatHandler = new HeartbeatHandler(heartbeatService, localIp);
            heartbeatHandler.start();

            System.out.println("Press \"Enter\" to exit.");
            System.in.read();

//            heartbeatHandler.setStop(true);
            stopThreads = true;
        } catch (Exception e) {
            System.err.println("An error occurred: " + e);
//            e.printStackTrace();
            System.exit(-1);
        } finally {

        }
    }
}
