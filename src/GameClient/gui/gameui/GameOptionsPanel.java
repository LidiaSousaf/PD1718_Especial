/**
 * Created by Lídia on 31/08/2018
 */

package GameClient.gui.gameui;

import CommunicationCommons.GameCommConstants;
import CommunicationCommons.GameMove;
import GameClient.GlobalController;
import GameLogic.three_in_row.logic.ObservableGame;
import GameLogic.three_in_row.logic.Player;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Observable;
import java.util.Observer;

public class GameOptionsPanel extends JPanel implements Observer {

    private GlobalController controller;
    private ObservableGame game;

    private JButton startButton;
    private JButton stopButton;
    private JButton giveUpButton;

    public GameOptionsPanel(GlobalController controller) {
        this.controller = controller;
        this.game = controller.getGame();

        controller.addObserver(this);
        game.addObserver(this);

        createComponents();
        setUpLayout();

        draw();
    }

    private void createComponents() {
        startButton = new JButton("Começar");
        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.startGame();
            }
        });

        stopButton = new JButton("Parar");
        stopButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.sendMove(new GameMove(GameCommConstants.INTERRUPT, 0, 0));
            }
        });

        giveUpButton = new JButton("Desistir");
        giveUpButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                controller.sendMove(new GameMove(GameCommConstants.GIVE_UP, 0, 0));
            }
        });
    }

    private void setUpLayout() {
        setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        FlowLayout layout = new FlowLayout(FlowLayout.CENTER, 20, 10);
        setLayout(layout);

        add(startButton);
        add(stopButton);
        add(giveUpButton);
    }

    private void draw() {
        setVisible(controller.getPairRequest() != null
                && controller.getPairRequest().isFormed());

        startButton.setVisible(game.isInterrupted() || game.isOver());

        stopButton.setVisible(!game.isInterrupted() && !game.isOver());

        giveUpButton.setVisible(!game.isInterrupted() && !game.isOver());

        if (controller.getLogin() != null) {
            boolean enabled = controller.getLogin().getUserName().equals(game.getCurrentPlayer().getName());
            stopButton.setEnabled(enabled);
            giveUpButton.setEnabled(enabled);
        }

        validate();
    }

    @Override
    public void update(Observable o, Object arg) {
        draw();
    }
}
