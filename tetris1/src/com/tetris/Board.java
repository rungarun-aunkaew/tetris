package com.tetris;

import com.tetris.Shape.Tetrominoe;
import com.tetris.task.HandleGameTask;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.TimerTask;
import javax.swing.ImageIcon;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class Board extends JPanel {

    private final int BOARD_WIDTH = 10;
    private final int BOARD_HEIGHT = 22;
   // private final int PERIOD_INTERVAL = 300;
    private final int PERIOD_INTERVAL = 500;

    private Timer timer;
    private boolean isFallingFinished = false;
    private boolean isPaused = false;
    private int numLinesRemoved = 0;
    private int curX = 0;
    private int curY = 0;
    private JLabel statusbar;
    private JLabel coolDownbar;
    private Shape curPiece;
    private Tetrominoe[] board;
    private java.util.Timer coolDownTimer;
    private HandleGameTask handlGameTask;
    private final TAdapter tAdapter = new TAdapter();
    private final TAdapter_P tAdapterPBtn = new TAdapter_P();
    
    //-------------Custom game config
    private final int timeState = 120; //seconds unit
    private final int scoreState = 3;
    private final int maxState = 3;
    //--------------------------
    public Board(Tetris parent) {
        initBoard(parent);
    }

    private void initBoard(Tetris parent) {

        setFocusable(true);
        statusbar = parent.getStatusBar();
        coolDownbar = parent.getCoolDownBar();
        addKeyListener(tAdapter);
    }

    private int squareWidth() {

        return (int) getSize().getWidth() / BOARD_WIDTH;
    }

    private int squareHeight() {

        return (int) getSize().getHeight() / BOARD_HEIGHT;
    }

    private Tetrominoe shapeAt(int x, int y) {

        return board[(y * BOARD_WIDTH) + x];
    }

    void start() {

        curPiece = new Shape();
        board = new Tetrominoe[BOARD_WIDTH * BOARD_HEIGHT];

        clearBoard();
        newPiece();

        timer = new Timer(PERIOD_INTERVAL, new GameCycle());
        timer.start();
        
        coolDownTimer = new java.util.Timer();
        
        handlGameTask = new HandleGameTask(1, timeState, coolDownbar, statusbar, this);
        coolDownTimer.schedule(handlGameTask, 0, 1000);
    }
    

    private void pause() {

        isPaused = !isPaused;

        if (isPaused) {

            statusbar.setText("paused");
            
            handlGameTask.cancel();
            //disableKeyBoard();
            removeKeyListener(tAdapter);
            addKeyListener(tAdapterPBtn);
        } else {
            removeKeyListener(tAdapterPBtn);
            enableKeyBoard();
            statusbar.setText(String.valueOf(numLinesRemoved));
            
            coolDownTimer = new java.util.Timer();
            handlGameTask.cancel();
            handlGameTask = new HandleGameTask(handlGameTask.getState(), handlGameTask.getCooldownSeconds(), coolDownbar, statusbar, this);
            coolDownTimer.schedule(handlGameTask, 0, 1000);
        }

        repaint();
    }
    //paint box
    @Override
    public void paintComponent(Graphics g) {

        super.paintComponent(g);
        doDrawing(g);
    }

    private void doDrawing(Graphics g) {

        Dimension size = getSize();
        int boardTop = (int) size.getHeight() - BOARD_HEIGHT * squareHeight();

        for (int i = 0; i < BOARD_HEIGHT; i++) {

            for (int j = 0; j < BOARD_WIDTH; j++) {

                Tetrominoe shape = shapeAt(j, BOARD_HEIGHT - i - 1);

                if (shape != Tetrominoe.NoShape) {

                    drawSquare(g, j * squareWidth(),
                            boardTop + i * squareHeight(), shape);
                }
            }
        }

        if (curPiece.getShape() != Tetrominoe.NoShape) {

            for (int i = 0; i < 4; i++) {

                int x = curX + curPiece.x(i);
                int y = curY - curPiece.y(i);

                drawSquare(g, x * squareWidth(),
                        boardTop + (BOARD_HEIGHT - y - 1) * squareHeight(),
                        curPiece.getShape());
            }
        }
    }
    //drop 
    private void dropDown() {

        int newY = curY;

        while (newY > 0) {

            if (!tryMove(curPiece, curX, newY - 1)) {

                break;
            }

            newY--;
        }

        pieceDropped();
    }

    private void oneLineDown() {

        if (!tryMove(curPiece, curX, curY - 1)) {

            pieceDropped();
        }
    }
    //clear
    private void clearBoard() {

        for (int i = 0; i < BOARD_HEIGHT * BOARD_WIDTH; i++) {

            board[i] = Tetrominoe.NoShape;
        }
    }

    private void pieceDropped() {

        for (int i = 0; i < 4; i++) {

            int x = curX + curPiece.x(i);
            int y = curY - curPiece.y(i);
            board[(y * BOARD_WIDTH) + x] = curPiece.getShape();
        }

        removeFullLines();

        if (!isFallingFinished) {

            newPiece();
        }
        
        //Check win game
        if(numLinesRemoved >= handlGameTask.getState() * scoreState && handlGameTask.getState() == maxState){
        //if(numLinesRemoved > 0 && handlGameTask.getState() == 1){    
            handlGameTask.cancel();
            isPaused = true;
            // Render text winner
            // stop game
            //remove addKeyListener
            disableKeyBoard();
            statusbar.setText("---------------------------You win!------------------------------");
        } //Check win current state 
        else if(numLinesRemoved >= handlGameTask.getState() * scoreState){
            // render new state
            coolDownTimer = new java.util.Timer();
            handlGameTask.cancel();
            handlGameTask = new HandleGameTask(handlGameTask.getState() + 1, timeState, coolDownbar, statusbar, this);
            coolDownTimer.schedule(handlGameTask, 0, 1000);
        }
    }
    
    public void disableKeyBoard(){
        removeKeyListener(tAdapter);
    }
    
    public void disableKeyBoardExceptPBtn(){
        removeKeyListener(tAdapter);
    }
    
    public void enableKeyBoard(){
        addKeyListener(tAdapter);
    }

    private void newPiece() {

        curPiece.setRandomShape();
        curX = BOARD_WIDTH / 2 + 1;
        curY = BOARD_HEIGHT - 1 + curPiece.minY();

        if (!tryMove(curPiece, curX, curY)) {

            curPiece.setShape(Tetrominoe.NoShape);
            timer.stop();

            String msg = String.format("----------------------  Game over   Score : %d  ---------------", numLinesRemoved);
            statusbar.setText(msg);
            
            // Stop cooldown timer when Game over
            coolDownTimer.cancel();
        }
    }

    private boolean tryMove(Shape newPiece, int newX, int newY) {

        for (int i = 0; i < 4; i++) {

            int x = newX + newPiece.x(i);
            int y = newY - newPiece.y(i);

            if (x < 0 || x >= BOARD_WIDTH || y < 0 || y >= BOARD_HEIGHT) {

                return false;
            }

            if (shapeAt(x, y) != Tetrominoe.NoShape) {

                return false;
            }
        }

        curPiece = newPiece;
        curX = newX;
        curY = newY;

        repaint();

        return true;
    }

    private void removeFullLines() {

        int numFullLines = 0;

        for (int i = BOARD_HEIGHT - 1; i >= 0; i--) {

            boolean lineIsFull = true;

            for (int j = 0; j < BOARD_WIDTH; j++) {

                if (shapeAt(j, i) == Tetrominoe.NoShape) {

                    lineIsFull = false;
                    break;
                }
            }

            if (lineIsFull) {

                numFullLines++;

                for (int k = i; k < BOARD_HEIGHT - 1; k++) {
                    for (int j = 0; j < BOARD_WIDTH; j++) {
                        board[(k * BOARD_WIDTH) + j] = shapeAt(j, k + 1);
                    }
                }
            }
        }

        if (numFullLines > 0) {

            numLinesRemoved += numFullLines;

            statusbar.setText(String.valueOf(numLinesRemoved));
            isFallingFinished = true;
            curPiece.setShape(Tetrominoe.NoShape);            
        }
    }

    private void drawSquare(Graphics g, int x, int y, Tetrominoe shape) {

        Color colors[] = {new Color(0, 0, 0), new Color(204, 102, 102),
                new Color(102, 204, 102), new Color(102, 102, 204),
                new Color(204, 204, 102), new Color(204, 102, 204),
                new Color(102, 204, 204), new Color(218, 170, 0)
        };

        Color color = colors[shape.ordinal()];

        g.setColor(color);
        g.fillRect(x + 1, y + 1, squareWidth() - 2, squareHeight() - 2);

        g.setColor(color.brighter());
        g.drawLine(x, y + squareHeight() - 1, x, y);
        g.drawLine(x, y, x + squareWidth() - 1, y);

        g.setColor(color.darker());
        g.drawLine(x + 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + squareHeight() - 1);
        g.drawLine(x + squareWidth() - 1, y + squareHeight() - 1,
                x + squareWidth() - 1, y + 1);
       
        
    }
     
    private class GameCycle implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {

            doGameCycle();
        }
    }
    
    class SayCoolDown extends TimerTask {
        public void run() {
           System.out.println("Hello World!"); 
        }
    }

    private void doGameCycle() {

        update();
        repaint();
    }

    private void update() {

        if (isPaused) {

            return;
        }

        if (isFallingFinished) {

            isFallingFinished = false;
            newPiece();
        } else {

            oneLineDown();
        }
    }

    public void setIsPaused(boolean isPaused) {
        this.isPaused = isPaused;
    }
    
    
    public int getNumLinesRemoved() {
        return numLinesRemoved;
    }

    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (curPiece.getShape() == Tetrominoe.NoShape) {

                return;
            }

            int keycode = e.getKeyCode();

            // Java 12 switch expressions
            switch (keycode) {
                case KeyEvent.VK_P : pause(); break;
                case KeyEvent.VK_LEFT : tryMove(curPiece, curX - 1, curY); break;
                case KeyEvent.VK_RIGHT : tryMove(curPiece, curX + 1, curY); break;
                case KeyEvent.VK_DOWN : tryMove(curPiece.rotateRight(), curX, curY); break;
                case KeyEvent.VK_UP : tryMove(curPiece.rotateLeft(), curX, curY); break;
                case KeyEvent.VK_SPACE : dropDown(); break;
                case KeyEvent.VK_D : oneLineDown(); break;
            }
        }
    }
    
    class TAdapter_P extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            if (curPiece.getShape() == Tetrominoe.NoShape) {

                return;
            }

            int keycode = e.getKeyCode();

            // Java 12 switch expressions
            switch (keycode) {
                case KeyEvent.VK_P : pause(); break;
            }
        }
    }

    public int getScoreState() {
        return scoreState;
    }
    
}
