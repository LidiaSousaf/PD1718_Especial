/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;
import GameClient.gui.gameui.GamePanel;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ThreeInRowPanel extends JPanel implements Observer {

    private GlobalController controller;
    private CallbackUpdatesPanel updatesPanel;
    private GamePanel gamePanel;

    public ThreeInRowPanel(GlobalController controller) {
        this.controller = controller;
        this.controller.addObserver(this);

        createComponents();
        setUpLayout();
        setVisible(controller.getLogin() != null);

        validate();
    }

    private void createComponents() {
        updatesPanel = new CallbackUpdatesPanel(controller);
        gamePanel = new GamePanel(controller);
    }

    private void setUpLayout() {
        this.setSize(1024, 600);
        this.setMinimumSize(new Dimension(900, 500));

        setLayout(new BorderLayout());
        add(updatesPanel, BorderLayout.EAST);
        add(gamePanel, BorderLayout.CENTER);
    }

    @Override
    public void update(Observable o, Object arg) {
        setVisible(controller.getLogin() != null);
    }

    public void registerClientCallback(){
        updatesPanel.registerClientCallback();
    }

    public void unbindClientCallback() {
        updatesPanel.unbindClientCallback();
    }
}
