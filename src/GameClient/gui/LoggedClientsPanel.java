/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;
import GameClient.GlobalController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class LoggedClientsPanel extends JPanel {

    private static final String[] COLUMN_TITLES = {"Username", "Nome", "Pedir par"};
    private GlobalController controller;
    private List<LoggedPlayerInfo> playerList;
    private JTable table;
    private JLabel panelTitle;
    private JScrollPane scroll;
    private PlayersTableModel tableModel;

    public LoggedClientsPanel(GlobalController controller) {
        this.controller = controller;
        this.playerList = controller.getLoggedPlayers();
        createComponents();
        setUpLayout();
    }

    private void createComponents() {
        tableModel = new PlayersTableModel(null, COLUMN_TITLES);
        table = new JTable(tableModel) {
            //Table will only show 5 players in its viewport
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(300, getRowHeight() * 5);
            }
        };
        setColumnDimensions();

        table.addMouseListener(new TableButtonListener());

        scroll = new JScrollPane(table);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        panelTitle = new JLabel("Jogadores Online");
        panelTitle.setFont(panelTitle.getFont().deriveFont(14.0f));

    }

    private void setUpLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createVerticalStrut(10));

        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createVerticalStrut(5));

        add(scroll);
        setVisible(true);
    }

    private void setColumnDimensions() {
        table.getColumn(COLUMN_TITLES[0]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[1]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[2]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[2]).setCellRenderer(new ButtonRenderer());
    }


    public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) {
        this.playerList = playerList;
        tableModel.setRowCount(0);
//        System.out.println("updateLoggedPlayers");
        for (LoggedPlayerInfo playerInfo : playerList) {
//            System.out.println(playerInfo.getUserName());
            boolean isSamePlayer = playerInfo.getUserName().equals(controller.getLogin().getUserName());
            String buttonText = isSamePlayer ? " - " : "Pedir par";
            JButton button = new JButton(buttonText);
            button.setEnabled(!playerInfo.isPaired() && !isSamePlayer);

            tableModel.addRow(new Object[]{playerInfo.getUserName(), playerInfo.getName(), button});
        }
    }

    //--------------------- INNER TABLE MODEL CLASS -----------------------
    private class PlayersTableModel extends DefaultTableModel {
        public PlayersTableModel(Object[][] values, String[] titles) {
            super(values, titles);
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            return false;
        }
    }

    //-------------------- INNER CELL RENDERER CLASS ----------------------
    private class ButtonRenderer implements TableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JButton button = (JButton) value;

            return button;
        }
    }

    //------------------------- INNER CLASS FOR ---------------------------
    //---------------- LISTENING CLICKS ON TABLE BUTTONS ------------------
    private class TableButtonListener extends MouseAdapter {

        public TableButtonListener() {

        }

        public void mouseClicked(MouseEvent e) {
            //get the column of the clicked cell
            int column = table.getColumnModel().getColumnIndexAtX(e.getX());
            //get the row of the clicked cell
            int row = e.getY() / table.getRowHeight();

            //check if row and column values are valid
            if (row >= 0 && row < table.getRowCount() && column >= 0 && column < table.getColumnCount()) {
                //get the object at the clicked cell
                Object value = table.getValueAt(row, column);
                //check if the object is a button
                if (value instanceof JButton) {
                    JButton button = (JButton) value;
                    //call requestPair() only if the button is enabled
                    if (button.isEnabled()) {
                        controller.requestPair(playerList.get(row).getUserName());
                    }
                }
            }
        }
    }
}
