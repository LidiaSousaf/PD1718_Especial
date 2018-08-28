/**
 * Created by Lídia on 27/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;
import CommunicationCommons.PairRequest;
import CommunicationCommons.PlayerLogin;
import CommunicationCommons.RemoteClientInterface;
import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class CallbackUpdatesPanel extends JPanel implements Observer {

    private GlobalController controller;
    private LoggedClientsPanel loggedClientsPanel;
    private RemoteGameClient callback;
    private PairPanel pairPanel;
    private JLabel lbUserName;
    private JLabel lbName;

    public CallbackUpdatesPanel(GlobalController controller) {
        this.controller = controller;

        createComponents();
        setUpLayout();

        controller.addObserver(this);
    }

    private void createComponents() {
        this.loggedClientsPanel = new LoggedClientsPanel(controller);
        pairPanel = new PairPanel(controller);
        lbUserName = new JLabel();
        lbUserName.setFont(lbUserName.getFont().deriveFont(18.0f));
        lbName = new JLabel();
        lbName.setFont(lbName.getFont().deriveFont(13.0f));

        try {
            this.callback = new RemoteGameClient();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar interface de callback!");
            e.printStackTrace();
            controller.shutdownClient(-1);
        }
    }

    private void setUpLayout() {

        this.setSize(320, 600);
        this.setMinimumSize(new Dimension(300, 500));

        setLayout(new GridBagLayout());
        GridBagConstraints constraints = new GridBagConstraints();

        //UserName label constraints
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 10, 10, 10);
//        constraints.weighty = 0.2;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(lbUserName, constraints);

        //Name label constraints
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(40, 20, 20, 10);
//        constraints.weighty = 0.2;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(lbName, constraints);

        //PairPanel constraints
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(80, 10, 20, 10);
//        constraints.weighty = 0.2;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.gridx = 0;
        constraints.gridy = 0;
        add(pairPanel, constraints);

        //loggedClientsPanel constraints
        constraints.fill = GridBagConstraints.HORIZONTAL;
        constraints.insets = new Insets(10, 0, 20, 0);
        constraints.ipady = 0;
        constraints.weighty = 0.5;
        constraints.anchor = GridBagConstraints.PAGE_START;
        constraints.gridx = 0;
        constraints.gridy = 1;
        add(loggedClientsPanel, constraints);

        validate();
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof PlayerLogin) {
            if (controller.getLogin() != null) {
                lbUserName.setText("UserName: " + controller.getLogin().getUserName());
            }
        }

        if (arg instanceof String) {
            if (controller.getPlayerName() != null) {
                lbName.setText("Nome: " + controller.getPlayerName());
            }
        }

        validate();
    }

    public void registerClientCallback() {
        controller.registerClientCallback(callback);
    }

    public void unbindClientCallback() {
        try {
            UnicastRemoteObject.unexportObject(callback, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }

    public void updateLoggedClientsPanel(List<LoggedPlayerInfo> playerList) {
        loggedClientsPanel.updateLoggedPlayers(playerList);
    }

    //--------------------------- INNER CLASS FOR -------------------------
    //-------------------- REMOTE CALLBACK IMPLEMENTATION -----------------
    class RemoteGameClient extends UnicastRemoteObject implements RemoteClientInterface {
        public RemoteGameClient() throws RemoteException {

        }

        //TODO: implement client side DbPair operations
        @Override
        public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) throws RemoteException {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateLoggedClientsPanel(playerList);
                }
            });
        }

        @Override
        public void notifyNewPairRequest(PairRequest pairRequest) throws RemoteException {
//            JOptionPane.showMessageDialog(null,
//                    "O jogador " + pairRequest.getPlayer1() + " pediu para formar par.");
            String[] options = {"Aceitar", "Rejeitar"};
            int answer = JOptionPane.showOptionDialog(null,
                    "O jogador " + pairRequest.getPlayer1() + " pediu para formar par.",
                    "Pedido de par",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE,
                    null,
                    options,
                    options[1]);

            if(answer == 0){
                controller.acceptPair(pairRequest);
            }else{
                controller.rejectPair(pairRequest);
            }
        }

        @Override
        public void notifyCanceledPair(String cancelingPlayer) throws RemoteException {
            controller.setPairRequest(null);
            JOptionPane.showMessageDialog(null,
                    "O jogador " + cancelingPlayer + " desfez o par.");
        }

        @Override
        public void notifyRejectedPair(String invitedPlayer) throws RemoteException {
            controller.setPairRequest(null);
            JOptionPane.showMessageDialog(null,
                    "O jogador " + invitedPlayer + " rejeitou o pedido de par.");
        }

        @Override
        public void notifyAcceptedPair(PairRequest pairRequest) throws RemoteException {
            controller.setPairRequest(pairRequest);

            JOptionPane.showMessageDialog(null,
                    "O jogador " + pairRequest.getPlayer2() + " aceitou o pedido de par.");
        }

        @Override
        public void forceLogout() throws RemoteException {
            JOptionPane.showMessageDialog(null, "O servidor de gestão encerrou.\n" +
                    "A aplicação vai ser fechada.");
        }
    }
}
