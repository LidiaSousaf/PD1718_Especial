/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;
import CommunicationCommons.RemoteClientInterface;

import javax.swing.*;
import java.awt.*;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class LoggedClientsPanel extends JPanel implements RemoteClientInterface {

    private List<LoggedPlayerInfo> playerList;

    public LoggedClientsPanel() throws RemoteException{
        this.playerList = new ArrayList<>();
    }

    private void printPlayers(){
        removeAll();
        for(int i = 0; i < playerList.size(); i++){
            JLabel label= new JLabel(playerList.get(i).getUserName());
            add(label);
        }

        validate();
    }

    public void setPlayerList(List<LoggedPlayerInfo> playerList){
        this.playerList = playerList;
        printPlayers();
    }

    @Override
    public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) throws RemoteException {
        //TODO: update logged clients table
        setPlayerList(playerList);
    }
}
