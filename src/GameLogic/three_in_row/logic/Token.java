package GameLogic.three_in_row.logic;

import java.io.Serializable;

public class Token implements Serializable
{
    public static final long serialVersionUID = 10L;
    
    private Player player;

    public Token(Player player) 
    {
        this.player = player;
    }

    public Player getPlayer()
    {
        return player;
    }

    @Override
    public String toString()
    {
        return "" + player.getName().charAt(0);
    }
    
}
