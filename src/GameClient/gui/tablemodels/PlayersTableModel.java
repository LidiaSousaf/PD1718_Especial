/**
 * Created by Lídia on 27/08/2018
 */

package GameClient.gui.tablemodels;

import CommunicationCommons.LoggedPlayerInfo;
import GameClient.GlobalController;

import javax.swing.table.AbstractTableModel;
import java.util.List;

public class PlayersTableModel extends AbstractTableModel {

    private List<LoggedPlayerInfo> playersList;
    private Object[][] data;
    private String columnNames[];
    private GlobalController controller;

    public PlayersTableModel(List<LoggedPlayerInfo> playersList, GlobalController controller) {
        this.playersList = playersList;
        this.controller = controller;

        constructColumns();
        fillDataMatrix();
    }

//    public void setPlayersList(List<LoggedPlayerInfo> playersList){
//        this.playersList = playersList;
//        fillDataMatrix();
//        fireTableDataChanged();
//    }

    private void constructColumns() {
        columnNames = new String[3];
        columnNames[0] = "Username";
        columnNames[1] = "Nome";
        columnNames[2] = "Pedir par";
    }

    private void fillDataMatrix() {
        data = new Object[playersList.size()][];
        for (int i = 0; i < playersList.size(); i++) {
            data[i] = new Object[columnNames.length];
            data[i][0] = playersList.get(i).getUserName();
            data[i][1] = playersList.get(i).getName();
            data[i][2] = playersList.get(i).getHasPair();
        }
    }

    @Override
    public int getRowCount() {
//        return playersList.size();
        return data.length;
    }

    @Override
    public int getColumnCount() {
        return columnNames.length;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        return data[rowIndex][columnIndex];
    }

    @Override
    public boolean isCellEditable(int row, int col) {
        return (col != 0 && col != 1 && !controller.getHasRequestedPair() && !(Boolean) data[row][col]);
    }

    @Override
    public String getColumnName(int col) {
        return columnNames[col];
    }
}
