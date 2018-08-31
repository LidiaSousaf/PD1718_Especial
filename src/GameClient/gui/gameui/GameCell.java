
package GameClient.gui.gameui;

import CommunicationCommons.GameCommConstants;
import CommunicationCommons.GameMove;
import GameClient.GlobalController;
import GameLogic.three_in_row.logic.ObservableGame;
import GameLogic.three_in_row.logic.Player;
import GameLogic.three_in_row.logic.Token;
import GameLogic.three_in_row.logic.states.AwaitBeginning;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * Componente grafico que representa uma celula da grelha.
 * Define o listener do rato de forma a enviar para a logica do game as mensagens
 * que indicam as jogadas efectuadas.
 *
 * @author JMSousa (base)
 */
class GameCell extends JPanel {
    int row, col;
    ObservableGame game;

    private GlobalController controller; //additional logic

    static final String imageFiles[] = {"images/sun.gif", "images/moon.png"};
    static Image playerIcons[] = new Image[imageFiles.length];
    static boolean imagesLoaded = false;

    /**
     * fabrica de objectos que devolve imagem associada a cada jogador de um game
     *
     * @param jogador
     * @param game
     * @return
     */
    static Image getPlayerIcon(Player jogador, ObservableGame game) {
        if (!imagesLoaded) {
            int i = 0;
            imagesLoaded = true;
            for (String fileName : imageFiles) {
                try {
                    playerIcons[i++] = ImageIO.read(Resources.getResourceFile(fileName));
                } catch (IOException e) {
                    System.err.println(e.getMessage());
                }
            }
        }

//        if(game.getPlayer1()==jogador){
        if (game.getPlayer1().equals(jogador)) {
            return playerIcons[0];
//        }else if(game.getPlayer2()==jogador){
        } else if (game.getPlayer2().equals(jogador)) {
            return playerIcons[1];
        } else {
            return null;
        }
    }

    GameCell(ObservableGame j, int r, int c, GlobalController controller) {
        row = r;
        col = c;
        this.game = j;
        this.controller = controller;

        setPreferredSize(new Dimension(100, 100));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent ev) {
//                if (game.getState() instanceof AwaitPlacement) {
//                    game.placeToken(row, col);
//                } else {
//                    game.returnToken(row, col);
//                }
                GameCell.this.controller.sendMove(new GameMove(GameCommConstants.MAKE_MOVE, row, col));
            }
        });

    }

    @Override
    public void paintBorder(Graphics g) {
        super.paintBorder(g);
        g.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (game.getState() instanceof AwaitBeginning) {
            setBackground(Color.LIGHT_GRAY);
        } else {
            setBackground(Color.WHITE);
        }

        Token p = game.getToken(row, col);

        if (p == null) {
            return;
        }

        Player j = p.getPlayer();
        g.drawImage(getPlayerIcon(j, game), 0, 0, getWidth() - 1, getHeight() - 1, null);
        g.setColor(Color.black);
    }

}