import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferStrategy;
import java.io.*;
import java.util.Random;

public class ConwaysLife extends JFrame implements Runnable, MouseListener, MouseMotionListener {

    //variables for buffering
    private final BufferStrategy strategy;
    private final boolean isInitialised;
    private final Graphics offscreenGraphics;

    //variable for grid of squares
    private final boolean[][][] gameState = new boolean[40][40][2];

    boolean click = false; //for buttons, so that the squares under it cant be pressed
    int prevX,prevY,curX,curY = 0; //previous x and y for mouse dragged function
    boolean moving = false; //for the dragged mouse, so that the pressed doesn't get called too
    int x,y; //variables for changing squares on mouse drag and press

    int StateOfGame = 1; //0 = playing, 1 = not playing, 2 = paused window, 3 = game over

    public ConwaysLife() {
        //setting the size of the screen
        Dimension screensize = java.awt.Toolkit.getDefaultToolkit().getScreenSize();
        int X = screensize.width / 2 - 400;
        int Y = screensize.height / 2 - 400;
        setBounds(X, Y, 800, 800);
        setVisible(true);
        this.setTitle("Conway's game of Life");

        //buffering
        createBufferStrategy(2);
        strategy = getBufferStrategy();
        offscreenGraphics = strategy.getDrawGraphics();

        //register the JFrame itself to receive mouse events
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        setFocusable(true);

        // initialise the game state
        for (int x=0;x<40;x++) {
            for (int y=0;y<40;y++) {
                gameState[x][y][0]=gameState[x][y][1]=false;
            }
        }

        Thread t = new Thread(this);
        t.start();//runs the 'run' method

        isInitialised = true; //allows the paint method to run
    }

    public void run() {
        while (true) {
            try {
                if (StateOfGame == 0) { //if the game is being played
                    updateGame(); //update the front buffer with the rules
                }
                Thread.sleep(200);
                this.repaint(); //calls the paint method in this class
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public String writeData(){
        String str = ""; //creates a string that can be returned
        //checks each square in the array
        for(int i=0;i<40;i++){
            for(int j=0;j<40;j++){
                if(gameState[j][i][0]){
                    str += "1";//if its true (i.e. white), then write a 1
                }
                else{
                    str += "0"; //if its false (i.e. black), then write a 0
                }
            }
            str += "\n"; //add a new line for each row of square
        }
        return str;
    }

    public void mouseDragged(MouseEvent e) {
        if(StateOfGame == 1) { //if the game is not playing
            x = e.getX() / 20; //get the x and y points in relation to the 2d array
            y = e.getY() / 20;

            curX = x%20; //get the current x and y remainders to represent the current co ords
            curY = y%20;
            if((curX == prevX && curY == prevY) ) { //if it's the same square as the last one, don't do anything
                return;
            }
            else{
                //otherwise change the state of it
                gameState[x][y][0] = !gameState[x][y][0];
                //change the previous x and y to the current ones, so to use them on the next call
                prevX = curX;
                prevY = curY;
                moving = true; //indicate that the mouse is being dragged so that the pressed function can't be invoked
            }
        }
        repaint();
    }

    public void mouseMoved(MouseEvent e) {

    }

    public void mouseClicked(MouseEvent e) {
        click = true; //indicates that the user cannot change a square value because a button has been pressed
        String workingDirectory = System.getProperty("user.dir"); //get the directory we're in
        String filename = workingDirectory + "/lifeGame.txt"; //create a file in the directory called lifeGame.txt

        //Save button
        if((e.getX() >=280 && e.getX() <=321) && (e.getY() >=32 && e.getY() <= 52)){
            StateOfGame = 1; //change to not playing
            try{
                BufferedWriter writer = new BufferedWriter((new FileWriter(filename)));
                String data = writeData(); //call function that writes the current states of the squares to the file
                writer.write(data); //write the data to the file
                writer.close();
            } catch (IOException p){}
        }
        //Load button
        if((e.getX() >=220 && e.getX() <=264) && (e.getY() >=32 && e.getY() <= 52)){
           StateOfGame = 1; //change to not playing
           String line;
           int count = 0; //counter for y in gameState array
            try{
                BufferedReader reader = new BufferedReader(new FileReader(filename));
                while((line=reader.readLine()) != null){ //while the next line is not empty
                    for(int i=0;i<line.length();i++){
                        gameState[i][count][0] = line.charAt(i) == '1'; //set the gameState current square to the result of "is the character a '1'
                                                                        //therefore, if it's a 1, then it will set it to true, otherwise false
                    }
                    count++;//increment count to go to the next y line
                }
                reader.close();
            }catch (IOException o){}
        }
        //start button
        if ((e.getX() >= 7 && e.getX() <= 52) && (e.getY() >= 32 && e.getY() <= 52)) {
            StateOfGame = 0; //change to playing
        }
        //Random Button
        if ((e.getX() >= 60 && e.getX() <= 130) && (e.getY() >= 32 && e.getY() <= 52)) {
            randomise(); //randomise the cells
            StateOfGame = 1; //keep at 'not playing' until user presses start
        }
        //Stop Button
        if ((e.getX() >= 143 && e.getX() <= 184) && (e.getY() >= 32 && e.getY() <= 52)) {
            StateOfGame = 2; //pause window
        }
        click = false;
    }

    public void mousePressed(MouseEvent e) {
        if (StateOfGame == 1 && !moving) { //if it's not playing let the user pick squares to change
            System.out.println(click);
            if(click){ //if any of the buttons have been pressed, then don't let the user change the squares around it
                return;
            }
            //get the x and y of the square in the array
            x = e.getX() / 20;
            y = e.getY() / 20;
            curX = x%20; //get the current x and y remainders to represent the current co ords
            curY = y%20;

            gameState[x][y][0] = !gameState[x][y][0];
            //change the previous x and y to the current ones, so to use them on the next call
            prevX = curX;
            prevY = curY;

            System.out.println("Pressed");
        } else if (StateOfGame == 2) { //paused window
            //start over button
            if ((e.getX() >= 465 && e.getX() <= 472) && (e.getY() >= 229 && e.getY() <= 241)) {
                restart();
                StateOfGame = 1;//change to not playing screen
            }
            //continue button
            if ((e.getX() >= 630 && e.getX() <= 642) && (e.getY() >= 269 && e.getY() <= 281)) {
                StateOfGame = 0;//change to playing
            }
        } else if (StateOfGame == 3) { //game over window
            if ((e.getX() >= 560 && e.getX() <= 572) && (e.getY() >= 229 && e.getY() <= 241)) {
                restart(); //restarts the game for the user
                StateOfGame = 1;//set the state to not playing
            }
        }
        repaint();
    }

    public void mouseReleased(MouseEvent e) {
        moving = false;
        System.out.println("Released");
    }

    public void mouseEntered(MouseEvent e) {

    }

    public void mouseExited(MouseEvent e) {

    }

    public void randomise() { //randomise the screen of squares
        Random random = new Random();

        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                gameState[x][y][0] = random.nextDouble() < 0.25; //give each cell a random change to be alive or dead
            }
        }
    }

    public void restart() {
        //changes all the cells in the front and back buffer to dead so the game can restart
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                gameState[x][y][0] = false;
                gameState[x][y][1] = false;
            }
        }
    }

    public void updateGame() {
        int alive = 0; //counter for how many cells are alive
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                //check the live neighbours of cell [x][y][0]
                int numNeighbours = 0;
                for (int xx = -1; xx <= 1; xx++) {
                    for (int yy = -1; yy <= 1; yy++) {
                        if ((xx != 0 || yy != 0) && gameState[(x + xx + 40) % 40][(y + yy + 40) % 40][0]) {
                            numNeighbours++;
                        }
                    }
                }
                if (gameState[x][y][0]) { //if the cell is alive
                    if (numNeighbours < 2 || numNeighbours > 3) { // if the cell has less than 2 or more then 3 neighbours
                        gameState[x][y][1] = false; //it dies, apply rule to back buffer
                    } else {
                        alive++; //alive cell counter
                    }
                } else { // if the cell is dead
                    if (numNeighbours == 3) { // and it has 3 neighbours
                        gameState[x][y][1] = true; //it becomes alive, apply rule to back buffer
                        alive++; //alive cell counter
                    }
                }
            }
        }

        //switch back and front buffers so the front buffers can be displayed on next repaint
        for (int x = 0; x < 40; x++) {
            for (int y = 0; y < 40; y++) {
                gameState[x][y][0] = gameState[x][y][1];
            }
        }
        if (alive == 0) { //if all the cells have died
            StateOfGame = 3; //game over
        }
    }

    public void paint(Graphics g) {
        if (!isInitialised) {
            return;
        }
        //double buffering
        g = offscreenGraphics;

        //set background colour
        g.setColor(Color.black);
        g.fillRect(0, 0, 800, 800);

        if (StateOfGame == 1 || StateOfGame == 0) {// if the game is not paused or over, check the squares and paint them

            //check all squares in the front buffer, if they are set to true, paint them white
            for (int x = 0; x < 40; x++) {
                for (int y = 0; y < 40; y++) {
                    if (gameState[x][y][0]) {
                        g.setColor(Color.white);
                        g.fillRect(x * 20, y * 20, 20, 20);
                    }
                }
            }

            //displaying the buttons in the top left corner
            g.setColor(Color.green);
            g.setFont(new Font("serif", Font.PLAIN, 18));
            //green rectangles
            g.fillRect(7, 32, 43, 20);
            g.fillRect(60, 32, 70, 20);
            g.fillRect(140, 32, 41, 20);
            g.fillRect(220, 32, 44, 20);
            g.fillRect(280, 32, 41, 20);

            g.setColor(Color.black); //text for the buttons
            g.drawString("Start", 10, 47);
            g.drawString("Random", 65, 47);
            g.drawString("Stop", 143, 47);
            g.drawString("Load", 223, 47);
            g.drawString("Save", 282, 47);


        } else if (StateOfGame == 2) { //if the game is paused
            g.setColor(Color.white);
            g.setFont(new Font("serif", Font.PLAIN, 30));
            g.drawString("Game Paused", 200, 200);

            g.setFont(new Font("serif", Font.PLAIN, 18));
            g.drawString("If you want to start over click here:", 200, 240); //option to start over
            g.fillRect(465, 229, 12, 12);
            g.drawString("If you want to continue with this configuration click here:", 200, 280); //option to continue
            g.fillRect(630, 269, 12, 12);

        } else { //if the game ended and all the cells died
            g.setColor(Color.white);
            g.setFont(new Font("serif", Font.PLAIN, 34));
            g.drawString("Game Over", 150, 180);

            g.setFont(new Font("serif", Font.PLAIN, 18));
            g.drawString("Your civilisation has died, try starting a new one here:", 150, 240); //option to start over

            g.fillRect(560, 229, 12, 12);
            //text for the user to understand the rules of the game
            g.drawString("Remember the rules of the Game:", 150, 280);
            g.setFont(new Font("serif", Font.PLAIN, 15));
            g.drawString("1. Any live cell with fewer than two live neighbours dies, as if caused by under-population.", 150, 320);
            g.drawString("2. Any live cell with two or three live neighbours lives on to the next generation.", 150, 360);
            g.drawString("3. Any live cell with more than three live neighbours dies, as if by overcrowding.", 150, 400);
            g.drawString("4. Any dead cell with exactly three live neighbours becomes a live cell, as if by reproduction.", 150, 440);
        }
        //double buffering
        strategy.show();
    }
    public static void main (String[]args){
        ConwaysLife CL = new ConwaysLife();
    }
}