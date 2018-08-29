/**
 * Created by Lídia on 29/08/2018
 */

package GameServer;

import CommunicationCommons.HeartbeatInterface;

import java.net.InetAddress;
import java.rmi.Naming;

public class GameServer {

    private static final String MANAGEMENT_ADDRESS = "localhost";

    public static void main(String args[]) {
        String managementServerAddr = MANAGEMENT_ADDRESS;
        if (args.length >= 1) {
            managementServerAddr = args[0];
        }

        System.out.println("Game Server started.");
        startManagementServerConnection(managementServerAddr);
    }

    private static void startManagementServerConnection(String managementAddress) {
        try {
            String heartbeatServiceURL = "rmi://" + managementAddress + "/HeartbeatService";
            HeartbeatInterface heartbeatService = (HeartbeatInterface) Naming.lookup(heartbeatServiceURL);

            String localIp = InetAddress.getLocalHost().getHostAddress();
            HeartbeatHandler heartbeatHandler = new HeartbeatHandler(heartbeatService, localIp);
            heartbeatHandler.start();

            System.out.println("Press \"Enter\" to exit.");
            System.in.read();

            heartbeatHandler.setStop(true);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        } finally {

        }
    }
}
