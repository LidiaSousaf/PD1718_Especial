package CommunicationCommons;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RemoteClientInterface extends Remote {
    void logout() throws RemoteException;
    void updateLoggedPlayers() throws RemoteException;
}
