package game2048;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import static java.lang.Math.min;
import java.util.ArrayList;
import java.util.Random;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public class Game2048 extends JPanel {
 
    enum State {
        start, running, over
    }
    
    enum Direction {
        up, down, left, right
    }
    
    private boolean stileColor;
 
    final Color[] colorTable = {
        new Color(0xF9F6F2), new Color(0x776E65), new Color(0xEEE4DA),
        new Color(0xEDE0C8), new Color(0xF2B179), new Color(0xF59563),
        new Color(0xF67C5F), new Color(0xF65E3B), new Color(0xbe7e56),
        new Color(0xbe5e56), new Color(0x9c3931), new Color(0x701710),
        new Color(0xEDC22E)};
    
    final Color alternateColorTable = new Color(0x000000);
    
    final Color colorFinal = new Color(0x3A382F);

    static int target = 2048;
 
    static int highest;
    static int score;
    static final int depth = 4;
    static Timer timer;
 
    private final Color gridColor = new Color(0xBBADA0);
    private final Color alternateGridColor = new Color(0x2B377B);
    private final Color emptyColor = new Color(0xCDC1B4);
    private final Color alternateEmptyColor = new Color(0x5AAED5);
    private final Color backgroundColor = new Color(0xFAF8EF);
    private final Color alternatebackgroundColor = new Color(0x030707);
    private final Color startColor = new Color(0xFFEBCD);
    private final Color alternateStartColor = new Color(0x4B85D0);
 
    private final Random rand = new Random();
 
    private Tile[][] tiles;
    private final int side = 4;
    private State gamestate = State.start;
    private boolean checkingAvailableMoves;
 
    public Game2048() {
        this.stileColor = true;
        
        setPreferredSize(new Dimension(900, 700));
        setBackground(backgroundColor);
        setFont(new Font("SansSerif", Font.BOLD, 48));
        setFocusable(true);
 
        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                startGame();
                repaint();
            }
        });
        
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_C) {
                    stileColor = !stileColor;
                    if (stileColor) {
                        setBackground(backgroundColor);
                    } else {
                        setBackground(alternatebackgroundColor);
                    }
                }
                
                if (gamestate == State.start) {
                    if (e.getKeyCode() != KeyEvent.VK_C)
                        startGame();
                } else {                 
                    switch (e.getKeyCode()) {
                        case KeyEvent.VK_UP:
                            moveUp();
                            break;
                        case KeyEvent.VK_DOWN:
                            moveDown();
                            break;
                        case KeyEvent.VK_LEFT:
                            moveLeft();
                            break;
                        case KeyEvent.VK_RIGHT:
                            moveRight();
                            break;
                    }
                }
                repaint();
            }
            
            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_I && gamestate == State.running) {
                    iaPlaying();
                    repaint();
                }
            }
        });
    }
    
    @Override
    public String toString() {
        String text = "";
        
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                if (tiles[i][j] != null) {
                    text += tiles[i][j].getValue() + " ";
                } else {
                    text += "0 ";
                }
            }
            text += "\n";
        }
        
        return text;
    }
 
    @Override
    public void paintComponent(Graphics gg) {
        super.paintComponent(gg);
        Graphics2D g = (Graphics2D) gg;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
 
        drawGrid(g);
    }
 
    void iaPlaying(){
        timer = new Timer(300, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                iaPlay();
                repaint();
                if (gamestate != State.running) {
                    timer.stop();
                }
            }
        });
        
        timer.start();
    }
    
    boolean iaPlay(){
        Tile[][] tilesCopy = copyTiles(tiles);
        int dir[] = new int [4];
        if (moveUp()) {
            dir[0] = iaMove(tilesCopy,0, -1, 0); // Up
            tilesCopy = copyTiles(tiles);
        } else {
            dir[0] = -1;
        }
        if (moveDown()) {
            dir[1] = iaMove(tilesCopy,side * side - 1, 1, 0); // Down
            tilesCopy = copyTiles(tiles);
        } else {
            dir[1] = -1;
        }
        if (moveLeft()) {
            dir[2] = iaMove(tilesCopy,0, 0, -1); // Left
            tilesCopy = copyTiles(tiles);
        } else {
            dir[2] = -1;
        }
        if (moveRight()) {
            dir[3] = iaMove(tilesCopy,side * side - 1, 0, 1); // Right
        }else {
            dir[3] = -1;
        }
        
        if (dir[0] >= dir [1] && dir[0] >= dir [2] && dir[0] >= dir[3]) {
            return move(0,-1,0);
        } else if (dir[1] >= dir [2] && dir[1] >= dir[3]) {
            return move(side*side -1,1,0);
        } else if (dir[2] >= dir[3]) {
            return move(0,0,-1);
        } else {
            return move(side*side -1,0,1);
        }
    }
    
    int iaPlay(int depth, ArrayList<Direction> steps, Board currentTable) {        
        if (depth == this.depth)
            return 0;
        
        int dir[] = new int[4];
        if (moveUp(currentTable.tiles)) {
            dir[0] = iaPlayUp(depth,steps,currentTable);
        } else {
            dir[0] = -1;
        }
        
        if (moveDown(currentTable.tiles)) {
            dir[1] = iaPlayUp(depth,steps,currentTable);
        } else {
            dir[1] = -1;
        }
        
        if (moveLeft(currentTable.tiles)) {
            dir[2] = iaPlayUp(depth,steps,currentTable);
        } else {
            dir[2] = -1;
        }
        
        if (moveRight(currentTable.tiles)) {
            dir[3] = iaPlayUp(depth,steps,currentTable);
        } else {
            dir[3] = -1;
        }

        return 0;
    }
    
    int iaPlayUp(int depth, ArrayList<Direction> steps, Board currentTable) {
        Board t = new Board ();
        t.tiles = copyTiles(currentTable.tiles);
        t.increment = iaMove(t.tiles,0, -1, 0); // Up

        steps.add(Direction.up);
        return iaPlay(depth+1,steps,t) + t.increment;
    }
    
    int iaPlayDown(int depth, ArrayList<Direction> steps, Board currentTable) {
        Board t = new Board ();
        t.tiles = copyTiles(currentTable.tiles);
        t.increment = iaMove(t.tiles,side * side - 1, 1, 0);

        steps.add(Direction.down);
        return iaPlay(depth+1,steps,t) + t.increment;
    }
    
    int iaPlayLeft(int depth, ArrayList<Direction> steps, Board currentTable) {
        Board t = new Board ();
        t.tiles = copyTiles(currentTable.tiles);
        t.increment = iaMove(t.tiles,0, 0, -1); // Left

        steps.add(Direction.left);
        return iaPlay(depth+1,steps,t) + t.increment;
    }
    
    int iaPlayRight(int depth, ArrayList<Direction> steps, Board currentTable) {
        Board t = new Board ();
        t.tiles = copyTiles(currentTable.tiles);
        t.increment = iaMove(t.tiles,side * side - 1, 0, 1); // Right

        steps.add(Direction.right);
        return iaPlay(depth+1,steps,t) + t.increment;
    }
    
    /*Tile[][] simularUp(Tile[][] tilesAnterior, int increment) {       
        Tile[][] actual = copyTiles(tilesAnterior);
        increment = (moveUp())?iaMove(actual,0, -1, 0):-1;
        return actual;
    }*/
    
    int iaMove(Tile[][] tilesCopy, int countDownFrom, int yIncr, int xIncr) {
        int increment = 0;
 
        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);
 
            int r = j / side;
            int c = j % side;
 
            if (tilesCopy[r][c] == null)
                continue;
 
            int nextR = r + yIncr;
            int nextC = c + xIncr;
 
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
 
                Tile next = tilesCopy[nextR][nextC];
                Tile curr = tilesCopy[r][c];
 
                if (next == null) { 
                    tilesCopy[nextR][nextC] = curr;
                    tilesCopy[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
 
                } else if (next.canMergeWith(curr)) { 
                    int value = next.mergeWith(curr);
                    increment += value;
                    tilesCopy[r][c] = null;
                    break;
                } else
                    break;
            }
        }
        
        return increment;
    }
    
    Tile[][] copyTiles(Tile[][] t){
        Tile[][] tableroCopia = new Tile[side][side];
        for (int i = 0; i < side; i++) {
            System.arraycopy(t[i], 0, tableroCopia[i], 0, side);
        }
        
        return tableroCopia;
    }
    
    void startGame() {
        if (gamestate != State.running) {
            score = 0;
            highest = 0;
            gamestate = State.running;
            tiles = new Tile[side][side];
            addRandomTile();
            addRandomTile();
        }
    }
 
    void drawGrid(Graphics2D g) {
        if (this.stileColor) {
            g.setColor(gridColor);
        } else {
            g.setColor(alternateGridColor);
        }
        g.fillRoundRect((int)(this.getWidth()/4.5), (int)(this.getHeight()/7.0), (int)(this.getWidth()/1.8), (int)(this.getHeight()/1.4), 15, 15);

        if (gamestate == State.running) {
            if (this.stileColor) {
                g.setColor(gridColor.darker());
            } else {
                g.setColor(alternateGridColor.darker());
            }
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/22.5),(int)(this.getHeight()/17.5))));
            
            g.drawString("Score: " + this.score, (int)(this.getWidth()/4.5), (int)(this.getHeight()/8.75));
            
            for (int r = 0; r < side; r++) {
                for (int c = 0; c < side; c++) {
                    if (tiles[r][c] == null) {
                        if (this.stileColor) {
                            g.setColor(emptyColor);
                        } else {
                            g.setColor(alternateEmptyColor);
                        }
                        g.fillRoundRect((int)(this.getWidth()/4.186) + c * (int)(this.getWidth()/7.438), (int)(this.getHeight()/6.0869565) + r * (int)(this.getHeight()/5.7851239), (int)(this.getWidth()/8.490566), (int)(this.getHeight()/6.603773584), 7, 7);
                    } else {
                        if (!this.stileColor) {
                            g.setColor(alternateEmptyColor);
                            g.fillRoundRect((int)(this.getWidth()/4.186) + c * (int)(this.getWidth()/7.438), (int)(this.getHeight()/6.0869565) + r * (int)(this.getHeight()/5.7851239), (int)(this.getWidth()/8.490566), (int)(this.getHeight()/6.603773584), 7, 7);
                        }
                        drawTile(g, r, c);
                    }
                }
            }
        } else {
            if (this.stileColor) {
                g.setColor(startColor);
            } else {
                g.setColor(alternateStartColor);
            }
            
            g.fillRoundRect((int)(this.getWidth()/4.186), (int)(this.getHeight()/6.0869565), (int)(this.getWidth()/1.91897654), (int)(this.getHeight()/1.4925373), 7, 7);
 
            if (this.stileColor) {
                g.setColor(gridColor.darker());
            } else {
                g.setColor(alternateGridColor.darker());
            }
            
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/7.03125),(int)(this.getHeight()/5.46875))));
            
            FontMetrics metrics = g.getFontMetrics();
            int widthString = metrics.charsWidth(("2048").toCharArray(), 0, ("2048").length());
            
            g.drawString("2048", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/2.59259259259));
 
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/45),(int)(this.getHeight()/35))));
            metrics = g.getFontMetrics();
            
            /*if (gamestate == State.won) {
                g.drawString("you made it!", 390, 350);
 
            } else*/ if (gamestate == State.over) {
                widthString = metrics.charsWidth(("game over").toCharArray(), 0, ("game over").length());
                g.drawString("game over", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/2));
            }
            
            if (this.stileColor) {
                g.setColor(gridColor.darker());
            } else {
                g.setColor(alternateGridColor.darker());
            }
            
            widthString = metrics.charsWidth(("Click to start a new game").toCharArray(), 0, ("Click to start a new game").length());
            g.drawString("Click to start a new game", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.4893617));
            widthString = metrics.charsWidth(("(use arrow keys to move tiles)").toCharArray(), 0, ("(use arrow keys to move tiles)").length());
            g.drawString("(use arrow keys to move tiles)", (int)(this.getWidth()/2 - widthString/2), (int)(this.getHeight()/1.32075471));
        }
    }
 
    void drawTile(Graphics2D g, int r, int c) {
        int value = tiles[r][c].getValue();
        
        if (this.stileColor) {
            if (value <= 2048) {
                g.setColor(colorTable[((int) (Math.log(value) / Math.log(2)) + 1 )]);
            } else {
                g.setColor(colorFinal);
            }
        } else {
            double a = Math.log10(value)/Math.log10(2);
            double b = Math.log10(Math.max(2,highest))/Math.log10(2);
            g.setColor(new Color(0,0,0,(float)(a/b)));
        }
        //g.fillRoundRect(215 + c * 121, 115 + r * 121, 106, 106, 7, 7);
        g.fillRoundRect((int)(this.getWidth()/4.186) + c * (int)(this.getWidth()/7.438), (int)(this.getHeight()/6.0869565) + r * (int)(this.getHeight()/5.7851239), (int)(this.getWidth()/8.490566), (int)(this.getHeight()/6.603773584), 7, 7);
        String s = String.valueOf(value);
        
        if (this.stileColor) {
            g.setColor(value <= 4 ? colorTable[1] : colorTable[0]);
        } else {
            g.setColor(new Color(0xFFFFFF));
        }

        if (value <= 8192) {
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/18.75),(int)(this.getHeight()/14.58333333))));
        } else if (value <= 65536){
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/23.6842105),(int)(this.getHeight()/14.42105263))));
        } else {
            g.setFont(new Font("SansSerif", Font.BOLD, min((int)(this.getWidth()/32.14285714),(int)(this.getHeight()/25))));
        }
        
        FontMetrics fm = g.getFontMetrics();
        int asc = fm.getAscent();
        int dec = fm.getDescent();
 
        int x = (int)(this.getWidth()/4.186) + c * (int)(this.getWidth()/7.438) + ((int)(this.getWidth()/8.490566) - fm.stringWidth(s)) / 2;
        int y = (int)(this.getHeight()/6.0869565) + r * (int)(this.getHeight()/5.7851239) + (asc + ((int)(this.getHeight()/6.603773584) - (asc + dec)) / 2);
 
        g.drawString(s, x, y);
    }

    private void addRandomTile() {
        int pos = rand.nextInt(side * side);
        int row, col;
        do {
            pos = (pos + 1) % (side * side);
            row = pos / side;
            col = pos % side;
        } while (tiles[row][col] != null);
 
        int val = rand.nextInt(10) == 0 ? 4 : 2;

        tiles[row][col] = new Tile(val);
    }
 
    private boolean move(int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;
        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);
 
            int r = j / side;
            int c = j % side;
 
            if (tiles[r][c] == null)
                continue;
 
            int nextR = r + yIncr;
            int nextC = c + xIncr;
 
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
 
                Tile next = tiles[nextR][nextC];
                Tile curr = tiles[r][c];
 
                if (next == null) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    tiles[nextR][nextC] = curr;
                    tiles[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;
 
                } else if (next.canMergeWith(curr)) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    int value = next.mergeWith(curr);
                    if (value > highest)
                        highest = value;
                    score += value;
                    tiles[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }
 
        if (moved) {
            /*if (highest < target)*/ {
                clearMerged();
                addRandomTile();
                if (!movesAvailable()) {
                    gamestate = State.over;
                }
            }/* else if (highest == target)
                gamestate = State.won;*/
        }
 
        return moved;
    }
    
    private boolean move(Tile[][] t, int countDownFrom, int yIncr, int xIncr) {
        boolean moved = false;
 
        for (int i = 0; i < side * side; i++) {
            int j = Math.abs(countDownFrom - i);
 
            int r = j / side;
            int c = j % side;
 
            if (t[r][c] == null)
                continue;
 
            int nextR = r + yIncr;
            int nextC = c + xIncr;
 
            while (nextR >= 0 && nextR < side && nextC >= 0 && nextC < side) {
 
                Tile next = t[nextR][nextC];
                Tile curr = t[r][c];
 
                if (next == null) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    t[nextR][nextC] = curr;
                    t[r][c] = null;
                    r = nextR;
                    c = nextC;
                    nextR += yIncr;
                    nextC += xIncr;
                    moved = true;
 
                } else if (next.canMergeWith(curr)) {
 
                    if (checkingAvailableMoves)
                        return true;
 
                    int value = next.mergeWith(curr);
                    if (value > highest)
                        highest = value;
                    score += value;
                    t[r][c] = null;
                    moved = true;
                    break;
                } else
                    break;
            }
        }
 
        if (moved) {
            /*if (highest < target)*/ {
                clearMerged();
                addRandomTile();
                if (!movesAvailable()) {
                    gamestate = State.over;
                }
            }/* else if (highest == target)
                gamestate = State.won;*/
        }
 
        return moved;
    }
    
    boolean moveUp() {
        return move(0, -1, 0);
    }
 
    boolean moveDown() {
        return move(side * side - 1, 1, 0);
    }
 
    boolean moveLeft() {
        return move(0, 0, -1);
    }
 
    boolean moveRight() {
        return move(side * side - 1, 0, 1);
    }
    
    boolean moveUp(Tile[][] t) {
        return move(t,0, -1, 0);
    }
 
    boolean moveDown(Tile[][] t) {
        return move(t,side * side - 1, 1, 0);
    }
 
    boolean moveLeft(Tile[][] t) {
        return move(t,0, 0, -1);
    }
 
    boolean moveRight(Tile[][] t) {
        return move(t,side * side - 1, 0, 1);
    }
 
    void clearMerged() {
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile != null)
                    tile.setMerged(false);
    }
 
    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }
 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame f = new JFrame();
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setTitle("2048");
            f.setResizable(true);
            f.add(new Game2048(), BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
        });
    }
}

class Tile {
    private boolean merged;
    private int value;
 
    Tile(int val) {
        value = val;
    }
 
    int getValue() {
        return value;
    }
 
    void setMerged(boolean m) {
        merged = m;
    }
 
    boolean canMergeWith(Tile other) {
        return !merged && other != null && !other.merged && value == other.getValue();
    }
 
    int mergeWith(Tile other) {
        if (canMergeWith(other)) {
            value *= 2;
            merged = true;
            return value;
        }
        return -1;
    }
}