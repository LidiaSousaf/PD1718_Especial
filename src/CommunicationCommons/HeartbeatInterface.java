package CommunicationCommons;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface HeartbeatInterface extends Remote {
    //return the IP address of the database
    String heartbeat(String gameServer) throws RemoteException;
}
