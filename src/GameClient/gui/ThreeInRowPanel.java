/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import GameClient.GlobalController;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

public class ThreeInRowPanel extends JPanel implements Observer {

    private GlobalController controller;
    private CallbackUpdatesPanel updatesPanel;

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
    }

    private void setUpLayout() {
        this.setSize(700, 500);
        this.setMinimumSize(new Dimension(650, 450));

        setLayout(new BorderLayout());
        add(updatesPanel);
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
