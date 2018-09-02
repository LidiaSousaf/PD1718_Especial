package GameClient.gui.gameui;

import GameClient.GlobalController;
import GameLogic.three_in_row.logic.ObservableGame;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Painel que contem todos os elementos que aparecem na janela.
 *
 * @author JMSousa (base)
 */
public class GamePanel extends JPanel implements Observer {
    ObservableGame game;
    //    StartOptionPanel optionPanel;
    GameGrid theGrid;
    PlayerData pd1, pd2;

    private GameOptionsPanel optionsPanel;

    private GlobalController controller;

    public GamePanel(/*ObservableGame game, */GlobalController controller) {
        this.controller = controller;

        this.game = this.controller.getGame();
        this.game.addObserver(this);

        setupComponents();
        setupLayout();
    }

    private void setupComponents() {
//        optionPanel = new StartOptionPanel(game);
        theGrid = new GameGrid(game, controller);
        pd1 = new PlayerData(game, 1);
        pd2 = new PlayerData(game, 2);

        optionsPanel = new GameOptionsPanel(controller);

    }

    private void setupLayout() {
        JPanel pCenter, pSouth;

        setLayout(new BorderLayout());

        pCenter = new JPanel();
        pCenter.setLayout(new BorderLayout());
//        pCenter.add(theGrid, BorderLayout.NORTH);
        pCenter.add(theGrid, BorderLayout.CENTER);

        pSouth = new JPanel();
        pSouth.add(pd1);
        pSouth.add(pd2);
        pCenter.add(pSouth, BorderLayout.SOUTH);


        add(pCenter, BorderLayout.CENTER);

//        add(optionPanel, BorderLayout.EAST);

        add(optionsPanel, BorderLayout.NORTH);

        validate();
    }

    @Override
    public void update(Observable o, Object arg) {
        repaint();
        revalidate();
    }
}
