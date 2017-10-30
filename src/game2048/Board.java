/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game2048;

import java.util.Random;

/**
 *
 * @author Fernando
 */
class Board {
    enum State {
        start, running, over
    }
    
    enum Direction {
        
    }
    
    Tile[][] tiles;
    int increment;
    int side;
    private boolean checkingAvailableMoves;
    static int highest;
    static int score;
    private State gamestate = State.start;
    private final Random rand = new Random();
    
    public Board() {
        this.side = 4;
    }
    
    public Board (int side) {
        this.side = side;
    }
    
    public int getScore() {
        return this.score;
    }
    
    public int getNumberOfEmptyCells(){
        int number = 0;
        
        for (int i = 0; i < side; i++) {
            for (int j = 0; j < side; j++) {
                number += (tiles[i][j]==null)?1:0;
            }
        }
        
        return number;
    }
    
    public Board clone() {
        Board c = new Board(this.side);
        c.checkingAvailableMoves = checkingAvailableMoves;
        c.gamestate = gamestate;
        c.increment = increment;
        c.side = side;
        c.tiles = tiles;
        
        return c;
    }
    
    public int[][] getBoardArray() {
        int [][] a = new int[side][side];
        
        for (int i = 0; i < side; i++) {
            a[i] = new int [side];
            for (int j = 0; j < side; j++) {
                a[i][j] = tiles[i][j].getValue();
            }
        }
        
        return a;
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
    
    void clearMerged() {
        for (Tile[] row : tiles)
            for (Tile tile : row)
                if (tile != null)
                    tile.setMerged(false);
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
    
    boolean movesAvailable() {
        checkingAvailableMoves = true;
        boolean hasMoves = moveUp() || moveDown() || moveLeft() || moveRight();
        checkingAvailableMoves = false;
        return hasMoves;
    }
    
    boolean isGameTerminated () {
        return !movesAvailable();
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
}