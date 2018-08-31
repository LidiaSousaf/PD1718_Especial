package GameLogic.three_in_row.logic.states;

import java.io.Serializable;

/*
 * List of expected events when taking into account all the states.
 */
public interface IStates extends Serializable
{
    public static final long serialVersionUID = 10L;

   IStates setNumberPlayers(int num);
   IStates setName(int num, String name);
   IStates startGame();
   IStates placeToken(int line, int column);
   IStates returnToken(int line, int column);
   IStates quit();
}
