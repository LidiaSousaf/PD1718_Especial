/**
 * Created by Lídia on 31/08/2018
 */

package CommunicationCommons;

import java.io.Serializable;

public class GameMove implements Serializable {

    public static final long serialVersionUID = 6L;

    private Integer action;
    private int row;
    private int col;

    public GameMove(Integer action, int row, int col) {
        this.action = action;
        this.row = row;
        this.col = col;
    }

    public Integer getAction() {
        return action;
    }

    public void setAction(Integer action) {
        this.action = action;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
