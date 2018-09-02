/**
 * Created by Lídia on 02/09/2018
 */

package GameClient.gui.dialogs;

import CommunicationCommons.GameInfo;
import GameClient.GlobalController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class FinishedGamesDialog extends JDialog {

    private static final String[] COLUMN_TITLES = {"Jogador 1", "Jogador 2", "Vencedor"};

    private GlobalController controller;
    private JPanel content;

    public FinishedGamesDialog(JFrame parent, GlobalController controller) {
        super(parent, "Histórico de jogos concluídos");

        this.controller = controller;

        setUpLayout();

        this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        this.setContentPane(content);
        this.pack();
        this.setVisible(true);
    }

    private void setUpLayout() {
        content = new JPanel();
        content.setLayout(new BorderLayout());
        content.setOpaque(true);

        List<GameInfo> gameList = controller.getFinishedGames();

        if (gameList != null && gameList.size() > 0) {
            createTable(gameList);
        } else {
            JLabel label = new JLabel("Não existem jogos inacabados para mostrar.");
            content.add(label, BorderLayout.CENTER);
        }

        setMinimumSize(new Dimension(300, 200));
    }

    private void createTable(List<GameInfo> gameList) {
        GamesTableModel tableModel = new GamesTableModel(null, COLUMN_TITLES);
        JTable table = new JTable(tableModel) {
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(300, getRowHeight() * 5);
            }
        };

        table.getColumn(COLUMN_TITLES[0]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[1]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[2]).setPreferredWidth(100);

        JScrollPane scroll = new JScrollPane(table);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        content.add(scroll, BorderLayout.CENTER);

        populateTable(gameList, tableModel);
    }

    private void populateTable(List<GameInfo> gameList, GamesTableModel tableModel) {
        tableModel.setRowCount(0);
        for (GameInfo game : gameList) {
            tableModel.addRow(new Object[]{game.getPlayer1(), game.getPlayer2(), game.getWinner()});
        }
    }

    //--------------------- INNER TABLE MODEL CLASS -----------------------
    private class GamesTableModel extends DefaultTableModel {
        public GamesTableModel(Object[][] values, String[] titles) {
            super(values, titles);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }
}
