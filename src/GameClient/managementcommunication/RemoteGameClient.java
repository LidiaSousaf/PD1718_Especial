package GameClient.managementcommunication; /**
 * Created by Lídia on 23/08/2018
 */

import CommunicationCommons.RemoteClientInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class RemoteGameClient extends UnicastRemoteObject implements RemoteClientInterface {

    public RemoteGameClient() throws RemoteException {

    }

    @Override
    public void logout() throws RemoteException {

    }

    @Override
    public void updateLoggedPlayers() throws RemoteException {

    }
}
