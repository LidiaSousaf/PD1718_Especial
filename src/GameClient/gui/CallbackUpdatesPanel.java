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
    private ChatPanel chatPanel;
    private IncomingRequestsPanel incomingRequestsPanel;
    private OutgoingRequestsPanel outgoingRequestsPanel;

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

        chatPanel = new ChatPanel(controller);

        incomingRequestsPanel = new IncomingRequestsPanel(controller);

        outgoingRequestsPanel = new OutgoingRequestsPanel(controller);

        try {
            this.callback = new RemoteGameClient();
        } catch (RemoteException e) {
            JOptionPane.showMessageDialog(null, "Erro ao criar interface de callback!");
            e.printStackTrace();
            controller.shutdownClient(-1);
        }
    }

    private void setUpLayout() {

        this.setSize(600, 620);
        this.setMinimumSize(new Dimension(580, 540));

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setUpIdentification();
        setUpCenter();

//        setLayout(new GridBagLayout());
//        GridBagConstraints constraints = new GridBagConstraints();
//
//        //UserName label constraints
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = new Insets(10, 10, 10, 10);
//        constraints.weightx = 0.8;
//        constraints.anchor = GridBagConstraints.PAGE_START;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.gridwidth = 2;
//        add(lbUserName, constraints);
//
//        //Name label constraints
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = new Insets(40, 20, 20, 10);
////        constraints.weighty = 0.2;
//        constraints.anchor = GridBagConstraints.PAGE_START;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.gridwidth = 2;
//        add(lbName, constraints);
//
//        //PairPanel constraints
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = new Insets(80, 10, 20, 10);
////        constraints.weighty = 0.2;
//        constraints.anchor = GridBagConstraints.PAGE_START;
//        constraints.gridx = 0;
//        constraints.gridy = 0;
//        constraints.gridwidth = 1;
//        add(pairPanel, constraints);
//
//        //loggedClientsPanel constraints
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = new Insets(10, 0, 20, 0);
//        constraints.ipady = 0;
//        constraints.weighty = 0.5;
//        constraints.anchor = GridBagConstraints.PAGE_START;
//        constraints.gridx = 1;
//        constraints.gridy = 1;
//        constraints.gridwidth = 1;
//        add(loggedClientsPanel, constraints);
//
//        //chatPanel constraints
//        constraints.fill = GridBagConstraints.HORIZONTAL;
//        constraints.insets = new Insets(10, 0, 20, 0);
//        constraints.ipady = 0;
//        constraints.weighty = 0.5;
//        constraints.anchor = GridBagConstraints.PAGE_END;
//        constraints.gridx = 1;
//        constraints.gridy = 2;
//        constraints.gridwidth = 1;
//        add(chatPanel, constraints);

        validate();
    }

    private void setUpIdentification() {
        Box box = Box.createVerticalBox();

        box.add(Box.createVerticalStrut(10));
        lbUserName.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lbUserName);

        box.add(Box.createVerticalStrut(5));
        lbName.setAlignmentX(Component.CENTER_ALIGNMENT);
        box.add(lbName);

        box.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(box);

        add(Box.createVerticalStrut(20));
    }

    private void setUpCenter() {
        Box center = Box.createHorizontalBox();

        Box left = Box.createVerticalBox();

        pairPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(pairPanel);

        left.add(Box.createVerticalStrut(20));

        outgoingRequestsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(outgoingRequestsPanel);

        left.add(Box.createVerticalStrut(20));
        incomingRequestsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        left.add(incomingRequestsPanel);

        Box right = Box.createVerticalBox();
        loggedClientsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(loggedClientsPanel);

        right.add(Box.createVerticalStrut(20));

        chatPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        right.add(chatPanel);

        center.add(left);
        center.add(Box.createHorizontalStrut(10));
        center.add(right);

        add(center);
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

    public void updateChatPanel(String sender, String target, String message) {
        chatPanel.receiveMessage(sender, target, message);
    }

//    public void addIncomingPairRequest(PairRequest request) {
//        incomingRequestsPanel.addIncomingRequest(request);
//    }
//
//    public void removeIncomingPairRequest(String player1) {
//        incomingRequestsPanel.removeIncomingRequest(player1);
//    }
//
//    public void clearIncomingRequests() {
//        incomingRequestsPanel.clearIncomingRequests();
//    }

    //--------------------------- INNER CLASS FOR -------------------------
    //-------------------- REMOTE CALLBACK IMPLEMENTATION -----------------
    class RemoteGameClient extends UnicastRemoteObject implements RemoteClientInterface {
        public RemoteGameClient() throws RemoteException {

        }

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
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
//                    String[] options = {"Aceitar", "Rejeitar"};
//                    int answer = JOptionPane.showOptionDialog(null,
//                            "O jogador " + pairRequest.getPlayer1() + " pediu para formar par.",
//                            "Pedido de par",
//                            JOptionPane.YES_NO_OPTION,
//                            JOptionPane.QUESTION_MESSAGE,
//                            null,
//                            options,
//                            options[1]);
//
//                    if (answer == 0) {
//                        controller.acceptPair(pairRequest);
//                    } else {
//                        controller.rejectPair(pairRequest);
//                    }

//                    addIncomingPairRequest(pairRequest);
                    controller.addIncomingRequest(pairRequest);
                }
            });

        }

        @Override
        public void notifyCanceledPair(String cancelingPlayer) throws RemoteException {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    PairRequest currentPair = controller.getCurrentPair();
                    if (controller.getCurrentPair() != null
                            && (currentPair.getPlayer1().equals(cancelingPlayer)
                            || currentPair.getPlayer2().equals(cancelingPlayer))) {

                        controller.setCurrentPair(null);

                        JOptionPane.showMessageDialog(null,
                                "O jogador " + cancelingPlayer + " desfez o par.");
                    } else {
                        controller.removeIncomingRequest(cancelingPlayer);
                    }
                }
            });

        }

        @Override
        public void notifyRejectedPair(String invitedPlayer) throws RemoteException {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.removeOutgoingRequest(invitedPlayer);
//                    controller.setCurrentPair(null);
//                    JOptionPane.showMessageDialog(null,
//                            "O jogador " + invitedPlayer + " rejeitou o pedido de par.");
                }
            });

        }

        @Override
        public void notifyAcceptedPair(PairRequest pairRequest) throws RemoteException {

            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    controller.setCurrentPair(pairRequest);
                    controller.clearOutgoingRequests();
                    controller.clearIncomingRequests();
//                    JOptionPane.showMessageDialog(null,
//                            "O jogador " + pairRequest.getPlayer2() + " aceitou o pedido de par.");
                }
            });

        }

        @Override
        public void forceLogout() throws RemoteException {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    JOptionPane.showMessageDialog(null, "O servidor de gestão encerrou.\n" +
                            "A aplicação vai ser fechada.");

                    controller.shutdownClient(0);
                }
            });

        }

        @Override
        public void receiveMessage(String sender, String target, String message) throws RemoteException {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateChatPanel(sender, target, message);
                }
            });
        }
    }
}
