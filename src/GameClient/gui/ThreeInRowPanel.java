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

    public ThreeInRowPanel(GlobalController controller){
        this.controller = controller;
        this.controller.addObserver(this);
        setBackground(Color.BLACK);

        setUpLayout();
        setVisible(controller.getLogin() != null);

        validate();
    }

    private void setUpLayout(){
        this.setSize(700, 500);
        this.setMinimumSize(new Dimension(650, 450));
        JLabel label = new JLabel("Welcome to Hell");
        label.setFont(label.getFont().deriveFont(40.0f).deriveFont(Font.BOLD));
        label.setForeground(Color.WHITE);

        setLayout(new BorderLayout());
        add(label, BorderLayout.CENTER);
    }

    @Override
    public void update(Observable o, Object arg) {
        setVisible(controller.getLogin() != null);
    }
}
