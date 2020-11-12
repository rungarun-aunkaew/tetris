package com.tetris;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.Icon;
import javax.swing.JPanel;


public class Tetris extends JFrame {

    private JLabel statusbar;
    private JLabel coolDownbar;
    private Image image;
    public Tetris() {

        initUI();
        URL imageURL;
        Image image;
    }

    private void initUI() {

        statusbar = new JLabel(" 0   Point - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
        add(statusbar, BorderLayout.SOUTH);
        
        coolDownbar = new JLabel();
        add(coolDownbar, BorderLayout.NORTH);

        Board board = new Board(this);
        add(board);
        board.start();

        setTitle("Tetris Game");
        setSize(300, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        
        
        
    }

    JLabel getStatusBar() {
        return statusbar;
    }
    
    JLabel getCoolDownBar() {
        return coolDownbar;
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(() -> {

            Tetris game = new Tetris();
            game.setVisible(true);
        });
    }
    /* public class bg extends JPanel{
                private static final long serialVersionUID = 1L;
                private ImageIcon bg   = new ImageIcon("bg.png"); 
        public bg(){
            setLayout(null);
        }
        public void paintComponent(Graphics g){
            super.paintComponent(g);
            g.drawImage(bg.getImage(),0,0,300,600,this);     
        }  
    } */

}
