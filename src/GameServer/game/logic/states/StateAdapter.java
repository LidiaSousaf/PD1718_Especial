package GameServer.game.logic.states;

import GameServer.game.logic.Constants;
import GameServer.game.logic.GameData;

public class StateAdapter implements IStates, Constants
{
    private GameData game;

    public StateAdapter(GameData g)
    {
        this.game =g;
    }

    public GameData getGame() 
    {
        return game;
    }
    
    public void setGame(GameData game) 
    {
        this.game = game;
    }

    @Override
    public IStates setNumberPlayers(int num){ return this;}

    @Override
    public IStates setName(int num, String name){ return this;}

    @Override
    public IStates startGame(){ return this;}

    @Override
    public IStates placeToken(int linha, int coluna){ return this;}

    @Override
    public IStates returnToken(int linha, int coluna){ return this;}

    @Override
    public IStates quit(){ return this;}
    
}
