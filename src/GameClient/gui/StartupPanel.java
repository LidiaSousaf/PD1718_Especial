/**
 * Created by Lídia on 25/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class StartupPanel extends JPanel implements Observer {

    private GlobalController globalController;
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;

    public StartupPanel(GlobalController globalController) {
        this.globalController = globalController;
        this.globalController.addObserver(this);

        createComponents();
        setUpLayout();

        setVisible(globalController.getLogin() == null);

        validate();
    }

    private void createComponents() {
        loginPanel = new LoginPanel(globalController);
        registerPanel = new RegisterPanel(globalController);
    }

    private void setUpLayout() {
//        setLayout(new BorderLayout());
//
//        add(loginPanel, BorderLayout.WEST);
//        add(registerPanel, BorderLayout.EAST);

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

        add(loginPanel);
        add(registerPanel);
    }

    @Override
    public void update(Observable o, Object arg) {
        setVisible(globalController.getLogin() == null);
    }
}
