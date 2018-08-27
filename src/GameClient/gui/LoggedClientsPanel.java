/**
 * Created by Lídia on 26/08/2018
 */

package GameClient.gui;

import CommunicationCommons.LoggedPlayerInfo;
import GameClient.GlobalController;
import GameClient.gui.tablemodels.PlayersTableModel;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class LoggedClientsPanel extends JPanel {

    private GlobalController controller;
    private List<LoggedPlayerInfo> playerList;
    private JTable table;
    private JLabel panelTitle;
    private JScrollPane scroll;

    public LoggedClientsPanel(GlobalController controller) {
        this.controller = controller;
        this.playerList = controller.getLoggedPlayers();
        createComponents();
        setUpLayout();
    }

    private void createComponents() {
        PlayersTableModel model = new PlayersTableModel(playerList, controller);
        table = new JTable(model) {
            //Table will only show 5 players in its viewport
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(300, getRowHeight() * 5);
            }
        };
        setColumnDimensions();

        scroll = new JScrollPane(table);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        panelTitle = new JLabel("Jogadores logados");
        panelTitle.setFont(panelTitle.getFont().deriveFont(14.0f));

    }

    private void setUpLayout(){
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        add(Box.createVerticalStrut(10));

        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createVerticalStrut(5));

        add(scroll);
        setVisible(true);
    }

    private void setColumnDimensions() {
        table.getColumnModel().getColumn(0).setPreferredWidth(100);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setCellRenderer(new ButtonRenderer());
        table.getColumnModel().getColumn(2).setCellEditor(new ButtonEditor());

    }
//
//    private void printPlayers() {
//        removeAll();
//        for (int i = 0; i < playerList.size(); i++) {
//            JLabel label = new JLabel(playerList.get(i).getUserName());
//            add(label);
//        }
//
//        validate();
//    }

//    public void setPlayerList(List<LoggedPlayerInfo> playerList) {
//        this.playerList = playerList;
//        PlayersTableModel model = (PlayersTableModel) table.getModel();
////        model.setPlayersList(playerList);
//        table.revalidate();
////        printPlayers();
//    }

//    public void updateLoggedPlayers(List<LoggedPlayerInfo> playerList) {
//        setPlayerList(playerList);
//        System.out.println("updateLoggedPlayers");
//        for(int i =0 ; i<playerList.size();i++){
//            System.out.println(playerList.get(i).getUserName());
//        }
//    }

    class ButtonRenderer extends JButton implements TableCellRenderer {

        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
//            if (isSelected) {
//                setForeground(table.getSelectionForeground());
//                setBackground(table.getSelectionBackground());
//            } else {
//                setForeground(UIManager.getColor("Button.disabledText"));
//                setBackground(UIManager.getColor("Button.disabledBackground"));
//            }
            boolean isSamePlayer = playerList.get(row).getUserName().equals(controller.getLogin().getUserName());
            boolean hasPair;
            if (value != null) {
                hasPair = (boolean) value;
                if (isSamePlayer) {
                    setText("-");
                } else {
                    setText(hasPair ? "Tem par" : "Pedir par");
                }
                if (hasPair || isSamePlayer) {
                    setEnabled(false);
                    setForeground(UIManager.getColor("Button.disabledText"));
                    setBackground(UIManager.getColor("Button.disabledBackground"));
                } else {
                    setEnabled(true);
                    setForeground(table.getForeground());
                    setBackground(table.getBackground());
                }
            } else {
                setText("");
            }
            return this;
        }
    }

    class ButtonEditor extends AbstractCellEditor implements TableCellEditor {

        protected JButton button;
        private String label;
        private boolean isPushed;
        private int row;

        public ButtonEditor() {
            super();
            button = new JButton();
            button.setOpaque(true);

            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    isPushed = true;
                    fireEditingStopped();
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            boolean hasPair = false;
            boolean isSamePlayer = false;
            if (value != null) {
                hasPair = (boolean) value;
                isSamePlayer = playerList.get(row).getUserName().equals(controller.getLogin().getUserName());
                if (isSamePlayer) {
                    label = "-";
                } else {
                    label = (hasPair ? "Tem par" : "Pedir par");
                }
            } else {
                label = "";
            }

//            if (isSelected) {
//                button.setForeground(table.getSelectionForeground());
//                button.setBackground(table.getSelectionBackground());
//            } else {
//                button.setForeground(table.getForeground());
//                button.setBackground(table.getBackground());
//            }
//            label = (value == null ? "" : value.toString());
            button.setText(label);
            this.row = row;
            isPushed = true;

            button.setEnabled(!hasPair && !isSamePlayer);
            if (hasPair || isSamePlayer) {
                setForeground(UIManager.getColor("Button.disabledText"));
                setBackground(UIManager.getColor("Button.disabledBackground"));
            } else {
                button.setForeground(table.getForeground());
                button.setBackground(table.getBackground());
            }
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                //invocar método para enviar pedido de formação de par a o servidor de gestão
                System.out.println("Botão " + row + " premido");
                //TODO: implementar pedido de par
//                controller.requestPair(playerList.get(row).getUserName());
            }
            isPushed = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            isPushed = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }
}
