/**
 * Created by Lídia on 03/09/2018
 */

package GameClient.gui;

import CommunicationCommons.PairRequest;
import GameClient.GlobalController;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class OutgoingRequestsPanel extends JPanel implements Observer {
    private static final String[] COLUMN_TITLES = {"Jogador", "Cancelar"};

    private GlobalController controller;
    private List<PairRequest> outgoingRequests;
    private JTable table;
    private JLabel panelTitle;
    private JScrollPane scroll;
    private RequestsTableModel tableModel;

    public OutgoingRequestsPanel(GlobalController controller) {
        this.controller = controller;
//        this.outgoingRequests = controller.getOutgoingRequests();

        controller.addObserver(this);

        createComponents();
        setUpLayout();
    }

    private void createComponents() {
        tableModel = new RequestsTableModel(null, COLUMN_TITLES);
        table = new JTable(tableModel) {
            public Dimension getPreferredScrollableViewportSize() {
                return new Dimension(190, 180);
            }
        };
        setColumnDimensions();

        table.addMouseListener(new TableButtonListener());

        scroll = new JScrollPane(table);
        scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        panelTitle = new JLabel("Pedidos Enviados");
        panelTitle.setFont(panelTitle.getFont().deriveFont(14.0f));
    }

    private void setColumnDimensions() {
        table.getColumn(COLUMN_TITLES[0]).setPreferredWidth(100);
        table.getColumn(COLUMN_TITLES[1]).setPreferredWidth(90);
        table.getColumn(COLUMN_TITLES[1]).setCellRenderer(new ButtonRenderer());
    }

    private void setUpLayout() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        setSize(200, 180);
        setPreferredSize(new Dimension(200, 180));
        setMaximumSize(new Dimension(200, 180));

        panelTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(panelTitle);

        add(Box.createVerticalStrut(10));

        scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(scroll);
        setVisible(true);
    }

    private void updateTable() {
        tableModel.setRowCount(0);
        for (PairRequest request : outgoingRequests) {
            JButton cancelButton = new JButton("Cancelar");
            tableModel.addRow(new Object[]{request.getPlayer2(), cancelButton});
        }
    }

    @Override
    public void update(Observable o, Object arg) {

        outgoingRequests = controller.getOutgoingRequests();

        updateTable();

    }

    //--------------------- INNER TABLE MODEL CLASS -----------------------
    private class RequestsTableModel extends DefaultTableModel {
        public RequestsTableModel(Object[][] values, String[] titles) {
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
                    controller.cancelPair(outgoingRequests.get(row));
                }
            }
        }
    }
}
