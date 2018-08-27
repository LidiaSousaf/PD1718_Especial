/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class LoggedClientsPanel extends JPanel{

    private List<LoggedPlayerInfo> playerList;

    public LoggedClientsPanel(){
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

    public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList){
        //TODO: update logged clients table
        setPlayerList(playerList);
    }
}
