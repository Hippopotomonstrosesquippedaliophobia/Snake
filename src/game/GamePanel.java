/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import javax.swing.JPanel;

/**
 *
 * @author Bronj
 */
@SuppressWarnings("serial")
public class GamePanel extends JPanel implements Runnable, KeyListener {
    public static final int WIDTH = 400;
    public static final int HEIGHT = 400;
    
    //Rendering variables
    private Graphics2D g2d;
    private BufferedImage image;
    
    //Game logistical Variables
    private Thread thread;
    private boolean running;
    private long targetTime;
    
    //Game Stuff
    private final int SIZE = 10;
    private Entity head, apple;
    private ArrayList<Entity> snake;
    private int score;
    private int highscore;
    private int level;
    private boolean gameover;
    
    //Movement stuff 
    private int dx, dy;
    
    //Input from keyboard
    private boolean up, down, left, right, start;
    
    public GamePanel() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        addKeyListener(this);
    }   
    
    private void setFPS(int fps){
        targetTime = 1000 / fps;
    }
    
    public void addNotify(){
        super.addNotify();
        thread = new Thread(this);
        thread.start();
    }
    @Override
    public void keyPressed(KeyEvent e){
        int k = e.getKeyCode();
        
        switch (k){
            case KeyEvent.VK_UP: 
                up = true;
                break;
            case KeyEvent.VK_DOWN: 
                down = true;
                break;
            case KeyEvent.VK_LEFT: 
                left = true;
                break;
            case KeyEvent.VK_RIGHT: 
                right = true;
                break;
            case KeyEvent.VK_ENTER: 
                start = true;
                break;
            default:
                System.out.println("Key Not Recognized");
            
        }
    }
    @Override
    public void keyReleased(KeyEvent e){
        int k = e.getKeyCode();
        
        switch (k){
            case KeyEvent.VK_UP: 
                up = false;
                break;
            case KeyEvent.VK_DOWN: 
                down = false;
                break;
            case KeyEvent.VK_LEFT: 
                left= false;
                break;
            case KeyEvent.VK_RIGHT: 
                right= false;
                break;
            case KeyEvent.VK_ENTER: 
                start = true;
                break;
            default:
                System.out.println("Key Not Recognized");
            
        }
    }
    @Override
    public void keyTyped(KeyEvent arg0){
        
    }
    
    @Override
    public void run(){
        if (running) return;
        init();
        
        long startTime;
        long elapsed;
        long wait;
        
        while (running){
            startTime = System.nanoTime();
            
            update();
            requestRender();
            
            elapsed = System.nanoTime() - startTime;
            wait = targetTime - elapsed / 1000000;
            
            if (wait > 0){
                try{
                    Thread.sleep(wait);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }    
    
    private void init(){
        image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g2d = image.createGraphics();
        
        running = true;
        setUpLevel();
    }
    
    public void setUpLevel(){
        snake = new ArrayList<Entity>();
        head = new Entity(SIZE);
        
        head.setPosition(WIDTH/2, HEIGHT/2);
        snake.add(head);
        
        for (int i = 1; i < 3; i++){
            Entity e = new Entity(SIZE);
            e.setPosition(head.getX() + (i * SIZE), head.getY());
            snake.add(e);
        }
        
        apple = new Entity(SIZE);
        setApple();
        score = 0;
        gameover = false;
        level = 1;
        dx = dy = 0;
        setFPS(level * 10);
    }
    
    public void setApple(){
        int x = (int)(Math.random() * (WIDTH - SIZE));
        int y = (int)(Math.random() * (HEIGHT - SIZE));
        
        x = x - (x % SIZE);
        y = y - (y % SIZE);
        
        apple.setPosition(x, y);
    }
    private void update(){
        if (gameover){
            if(start){
                setUpLevel();
            }
            return;
        }
        if (up && dy == 0){
            dy = -SIZE;
            dx = 0;
        }
        if (down && dy == 0){
            dy = SIZE;
            dx = 0;
        }
        if (left && dx == 0){
            dy = 0;
            dx = -SIZE;
        }
        if (right && dx == 0 && dy != 0){
            dy = 0;
            dx = SIZE;
        }
        
        if (dx != 0 || dy != 0){                
            for (int i = snake.size() - 1; i > 0; i--){
                snake.get(i).setPosition(
                        snake.get(i - 1).getX(),
                        snake.get(i - 1).getY()
                    );
            }
            head.move(dx, dy);
        }
        
        for(Entity e : snake){
            if (e.isCollision(head)){
                gameover = true;
                start = false;
                break;
            }
        }
        
        if (apple.isCollision(head)){
            score++;
            
            if (score > highscore){
                highscore = score;
            }
            //Can add sound for pickup too
            
            Entity e = new Entity(SIZE);
            e.setPosition(-100, -100);
            snake.add(e);
            
            setApple();
            
            if (score % 10 == 0){
                level++;
                if ( level > 10) level = 10;
                setFPS(level * 10);
            }
        }
        if (head.getX() < 0) head.setX(WIDTH);
        if (head.getY() < 0) head.setY(HEIGHT);
        
        if (head.getX() > WIDTH) head.setX(0);
        if (head.getY() > HEIGHT) head.setY(0);
        
    }
    private void requestRender(){
        render(g2d);
        Graphics g = getGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();
    }
    private void render(Graphics2D g2d){
        g2d.clearRect(0, 0, WIDTH, HEIGHT);
        g2d.setColor(Color.GREEN);
        
        for(Entity e : snake){
            e.render(g2d);
        }
        
        g2d.setColor(Color.RED);
        apple.render(g2d);
        
        if (gameover){
            g2d.drawString("GAME OVER!", 160,200);
        }
        
        g2d.setColor(Color.WHITE);
        g2d.drawString("Score: "+ score + "   Level: "+ level, 10,20);
        g2d.drawString("Highscore: "+ highscore, 10, 40);
        
        if (dx == 0 && dy == 0){
            g2d.drawString("Ready!", 180,200);
        }
        if (gameover){
            g2d.drawString("Press ENTER to START!", 130,220);
        }
    }
}
