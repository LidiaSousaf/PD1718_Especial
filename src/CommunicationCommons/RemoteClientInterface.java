package CommunicationCommons;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface RemoteClientInterface extends Remote {
    void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) throws RemoteException;
    void notifyNewPairRequest(PairRequest pairRequest) throws RemoteException;
}
