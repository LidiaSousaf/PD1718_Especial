/**
 * Created by Lídia on 29/08/2018
 */

package GameServer;

import CommunicationCommons.Heartbeat;
import CommunicationCommons.HeartbeatInterface;
import DatabaseCommunication.DatabaseCommunication;
import GameServer.clientcommunication.TcpServer;

import java.rmi.RemoteException;

public class HeartbeatHandler extends Thread {

    private static final String DATABASE_NAME = "three_in_row";
    private static final String DATABASE_USER_NAME = "GameServer";
    private static final String DATABASE_PASSWORD = "ThreeInRow_1718";

    private String databaseIp;
    private String localIp;
    private HeartbeatInterface heartbeatService;
//    private boolean stop;
//    private DatabaseCommunication databaseCommunication;

    public HeartbeatHandler(HeartbeatInterface heartbeatService, String localIp) {
        this.databaseIp = null;
        this.localIp = localIp;
        this.heartbeatService = heartbeatService;
//        this.stop = false;
//        this.databaseCommunication = null;
    }

    public String getDatabaseIp() {
        return databaseIp;
    }

    public void setDatabaseIp(String databaseIp) {
        this.databaseIp = databaseIp;
    }

//    public DatabaseCommunication getDatabaseCommunication() {
//        return databaseCommunication;
//    }

//    public void setStop(boolean stop) {
//        this.stop = stop;
//    }

//    private void connectToDatabase() {
//        databaseCommunication = new DatabaseCommunication(DATABASE_NAME,
//                DATABASE_USER_NAME, DATABASE_PASSWORD, databaseIp);
//
//        if (!databaseCommunication.connect()) {
//            System.out.println("Connection to database failed. Shutting down Game Server");
//            System.exit(-1);
//        }else {
//
//            System.out.println("Connection do database established.");
//        }
//    }

    private void launchTcpServer(){
        Thread tcpThread = new Thread(new TcpServer(databaseIp));
        tcpThread.start();
    }

    @Override
    public void run() {
        System.out.println("Started sending heartbeats to Management Server.");
        while (!GameServer.stopThreads) {
            try {
                String tempIp = heartbeatService.heartbeat(localIp);
                if (databaseIp == null) {
                    databaseIp = tempIp;
//                    connectToDatabase();
//                    launchTcpServer();
                }
                try {
                    Thread.sleep(Heartbeat.TIME);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            } catch (RemoteException e) {
//                e.printStackTrace();
                System.err.println("Communication to Management Server failed.\nShutting down Game Server.");
                GameServer.stopThreads = true;
                break;
            }
        }

//        if (databaseCommunication != null) {
//            databaseCommunication.closeConnection();
//            System.out.println("Connection to database closed.");
//        }
    }
}
