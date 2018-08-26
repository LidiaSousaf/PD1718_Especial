/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Observable;
import java.util.Observer;

public class ThreeInRowPanel extends JPanel implements Observer {

    private GlobalController controller;
    private LoggedClientsPanel clientsPanel;

    public ThreeInRowPanel(GlobalController controller) {
        this.controller = controller;
        this.controller.addObserver(this);

        createComponents();
        setUpLayout();
        setVisible(controller.getLogin() != null);

        validate();
    }

    private void createComponents() {
        try {
            clientsPanel = new LoggedClientsPanel();
            UnicastRemoteObject.exportObject(clientsPanel, 0);
        } catch (RemoteException e) {
            System.err.println("Erro ao criar interface de callback no cliente:\n" + e);
            System.exit(-1);
        }
    }

    private void setUpLayout() {
        this.setSize(700, 500);
        this.setMinimumSize(new Dimension(650, 450));

        setLayout(new BorderLayout());
        add(clientsPanel, BorderLayout.CENTER);

    }

    @Override
    public void update(Observable o, Object arg) {
        setVisible(controller.getLogin() != null);
    }

    public void registerClientCallback() {
        controller.registerClientCallback(clientsPanel);
        clientsPanel.setPlayerList(controller.getLoggedPlayers());
    }

    public void unbindClientCallback() {
        try {
            UnicastRemoteObject.unexportObject(clientsPanel, true);
        } catch (NoSuchObjectException e) {
            e.printStackTrace();
        }
    }
}
