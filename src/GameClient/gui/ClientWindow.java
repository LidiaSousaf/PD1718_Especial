/**
 * Created by Lídia on 25/08/2018
 */

package GameClient.gui;

import CommunicationCommons.PlayerLogin;
import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

public class ClientWindow extends JFrame implements Observer {

    private GlobalController globalController;
    private StartupPanel startupPanel;
    private ThreeInRowPanel threeInRowPanel;

    public ClientWindow(GlobalController globalController) {
        super("Three In a Row");

        this.globalController = globalController;
        this.globalController.addObserver(this);

        createComponents();
        setUpLayout();

        setVisible(true);
        this.setSize(700, 500);
        this.setMinimumSize(new Dimension(650, 450));
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                super.windowClosing(e);
                threeInRowPanel.unbindClientCallback();
                dispose();
                globalController.shutdownClient(0);
            }
        });

        validate();
    }

    private void createComponents() {
        startupPanel = new StartupPanel(globalController);
        threeInRowPanel = new ThreeInRowPanel(globalController);
    }

    private void setUpLayout() {
        //TODO: complete stuff...
        Container container = getContentPane();

        container.setLayout(new BorderLayout());
//        container.add(threeInRowPanel, BorderLayout.CENTER);
        container.add(startupPanel, BorderLayout.CENTER);

    }


    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof PlayerLogin) {
            if (globalController.getLogin() != null) {
                remove(startupPanel);
                add(threeInRowPanel, BorderLayout.CENTER);
                threeInRowPanel.registerClientCallback();
            } else {
                remove(threeInRowPanel);
                add(startupPanel, BorderLayout.CENTER);
            }

            repaint();
            revalidate();
        }

    }
}
