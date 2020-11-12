package com.tetris.task;

import com.tetris.Board;
import java.util.TimerTask;
import javax.swing.JLabel;

/**
 *
 * @author GogoDev
 */
public class HandleGameTask extends TimerTask{
    int state;
    int cooldownSeconds;
    JLabel coolDownbar;
    JLabel statusbar;
    Board board;
    public HandleGameTask(int state, int cooldownSeconds, JLabel coolDownbar, JLabel statusbar, Board board){
        this.state = state;
        this.cooldownSeconds = cooldownSeconds;
        this.coolDownbar = coolDownbar;
        this.statusbar = statusbar;
        this.board = board;
    }

    public int getState() {
        return state;
    }

    
    public int getCooldownSeconds() {
        return cooldownSeconds;
    }
    
    @Override
    public void run() {
        cooldownSeconds = cooldownSeconds -1;
        coolDownbar.setText(String.format(" State  %d                         Score %d                    Time: %d", state, state * board.getScoreState(), cooldownSeconds));
        if(cooldownSeconds ==0 ){
            cancel();
            board.disableKeyBoard();
            board.setIsPaused(true);
            statusbar.setText(String.format("----------------------  Game over   Score : %d  ---------------", board.getNumLinesRemoved()));
        }
    }
}
