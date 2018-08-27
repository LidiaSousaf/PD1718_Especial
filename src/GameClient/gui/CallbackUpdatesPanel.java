/**
 * Created by Lídia on 27/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;
import CommunicationCommons.RemoteClientInterface;
import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class CallbackUpdatesPanel extends JPanel {

    private GlobalController controller;
    private LoggedClientsPanel loggedClientsPanel;
    private RemoteGameClient callback;

    public CallbackUpdatesPanel(GlobalController controller){
        this.controller = controller;

        createComponents();
        setUpLayout();
    }

    private void createComponents(){
        this.loggedClientsPanel = new LoggedClientsPanel();
//        this.loggedClientsPanel.setPlayerList(controller.getLoggedPlayers());
        try {
            this.callback = new RemoteGameClient();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar interface de callback!");
            e.printStackTrace();
            controller.shutdownClient(-1);
        }
    }

    private void setUpLayout(){
        setLayout(new BorderLayout());
        add(loggedClientsPanel);
    }

    public void registerClientCallback(){
        this.loggedClientsPanel.setPlayerList(controller.getLoggedPlayers());
        controller.registerClientCallback(callback);
    }

    public void unbindClientCallback(){
        try {
            UnicastRemoteObject.unexportObject(callback, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }


    //--------------------------- INNER CLASS FOR -------------------------
    //-------------------- REMOTE CALLBACK IMPLEMENTATION -----------------
    class RemoteGameClient extends UnicastRemoteObject implements RemoteClientInterface{
        public RemoteGameClient() throws RemoteException {

        }

        @Override
        public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) throws RemoteException {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    loggedClientsPanel.updateLoggedPlayers(playerList);
                }
            });
        }
    }
}
