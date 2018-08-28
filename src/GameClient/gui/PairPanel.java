/**
 * Created by Lídia on 28/08/2018
 */

package GameClient.gui;

import CommunicationCommons.PairRequest;
import GameClient.GlobalController;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class PairPanel extends JPanel implements Observer {

    private GlobalController controller;
    private JLabel pairName;
    private JLabel pairState;
    private JButton cancelButton;

    public PairPanel(GlobalController controller) {
        this.controller = controller;

        createComponents();
        setUpLayout();

        controller.addObserver(this);

        draw();
    }

    private void createComponents() {
        pairName = new JLabel();
        pairName.setFont(pairName.getFont().deriveFont(Font.BOLD).deriveFont(13.0f));
        pairState = new JLabel();
        pairState.setFont(pairState.getFont().deriveFont(Font.ITALIC));
        cancelButton = new JButton();
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.cancelPair();
            }
        });
    }

    private void setUpLayout() {
        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        setBorder(new TitledBorder("Par"));

        Box box = Box.createVerticalBox();
        box.add(Box.createRigidArea(new Dimension(0, 10)));
        box.add(Box.createRigidArea(new Dimension(20, 0)));
        box.add(pairName);
        box.add(pairState);
        box.add(Box.createRigidArea(new Dimension(0, 10)));

        add(box);

        add(Box.createRigidArea(new Dimension(40, 0)));
        cancelButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
        add(cancelButton);
        add(Box.createRigidArea(new Dimension(0, 10)));
    }

    private void draw() {
        PairRequest pair = controller.getPairRequest();
        if (pair != null) {
            setVisible(true);
            String opponent = pair.getPlayer1().equals(controller.getLogin().getUserName()) ?
                    pair.getPlayer2() : pair.getPlayer1();
            pairName.setText(opponent);

            String state = "(" + (pair.isFormed() ? "Formado" : "Pendente") + ")";
            pairState.setText(state);
            cancelButton.setText(pair.isFormed() ? "Desfazer par" : "Cancelar pedido");

        } else {
            setVisible(false);
        }

        validate();
    }

    @Override
    public void update(Observable o, Object arg) {
        draw();
    }
}
