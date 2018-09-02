/**
 * Created by Lídia on 25/08/2018
 */

package GameClient.gui;

import CommunicationCommons.PlayerLogin;
import GameClient.GlobalController;
import GameClient.gui.dialogs.FinishedGamesDialog;
import GameClient.gui.dialogs.UnfinishedGamesDialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Observable;
import java.util.Observer;

public class ClientWindow extends JFrame implements Observer {

    private GlobalController controller;
    private StartupPanel startupPanel;
    private ThreeInRowPanel threeInRowPanel;
    private JMenuBar menuBar;

    public ClientWindow(GlobalController controller) {
        super("Three In a Row");

        this.controller = controller;
        this.controller.addObserver(this);

        createComponents();
        setUpLayout();
        createMenu();

        setVisible(true);
        this.setSize(840, 600);
        this.setMinimumSize(new Dimension(750, 500));
//        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
//                super.windowClosing(e);
                exit();
            }
        });

        validate();
    }

    private void createComponents() {
        startupPanel = new StartupPanel(controller);
        threeInRowPanel = new ThreeInRowPanel(controller);
    }

    private void setUpLayout() {
        //TODO: complete stuff...
        Container container = getContentPane();

        container.setLayout(new BorderLayout());
//        container.add(threeInRowPanel, BorderLayout.CENTER);
        container.add(startupPanel, BorderLayout.CENTER);

    }

    private void createMenu() {
        menuBar = new JMenuBar();
        JMenu menu = new JMenu("Menu");

        JMenuItem unfinishedGames = new JMenuItem("Jogos inacabados");
        unfinishedGames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openUnfinishedGamesDialog();
            }
        });

        JMenuItem finishedGames = new JMenuItem("Jogos concluídos");
        finishedGames.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openFinishedGamesDialog();
            }
        });

        JMenuItem exit = new JMenuItem("Sair");
        exit.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exit();
            }
        });

        menu.add(unfinishedGames);
        menu.add(finishedGames);
        menu.addSeparator();
        menu.add(exit);

        menuBar.add(menu);

        Container cp = getContentPane();
        cp.add(menuBar, BorderLayout.PAGE_START);
        menuBar.setVisible(false);
    }

    private void openUnfinishedGamesDialog() {
        JDialog dialog = new UnfinishedGamesDialog(this, controller);
        dialog.setVisible(true);
    }

    private void openFinishedGamesDialog() {
        JDialog dialog = new FinishedGamesDialog(this, controller);
        dialog.setVisible(true);
    }

    private void exit() {
        ClientWindow.this.threeInRowPanel.unbindClientCallback();
        ClientWindow.this.dispose();
        ClientWindow.this.controller.shutdownClient(0);
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof PlayerLogin) {
            if (controller.getLogin() != null) {
                remove(startupPanel);
                add(threeInRowPanel, BorderLayout.CENTER);
                threeInRowPanel.registerClientCallback();
                menuBar.setVisible(true);
            } else {
                threeInRowPanel.unbindClientCallback();
                remove(threeInRowPanel);
                add(startupPanel, BorderLayout.CENTER);
                menuBar.setVisible(false);
            }
        }

        repaint();
        revalidate();
    }
}
