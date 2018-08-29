/**
 * Created by Lídia on 29/08/2018
 */

package ManagementServer.gameservercommunication;

import CommunicationCommons.Heartbeat;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class HeartbeatServiceHandler extends Thread {

    private HeartbeatService heartbeatService;
    private boolean stop;

    public HeartbeatServiceHandler(HeartbeatService heartbeatService) {
        this.stop = false;
        this.heartbeatService = heartbeatService;
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(Heartbeat.TIME);
                heartbeatService.setCount(heartbeatService.getCount() + 1);
                if (heartbeatService.getCount() > Heartbeat.MAX_COUNT) {
                    heartbeatService.resetHeartbeat();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        unbindHeartbeatService();
    }

    private void unbindHeartbeatService() {
        try {
            //End the service
            UnicastRemoteObject.unexportObject(heartbeatService, true);
            System.out.println("Heartbeat service ended.");
        } catch (RemoteException e) {
            System.err.println("An error occurred:" + e);
//            System.exit(-1);
        }
    }
}
