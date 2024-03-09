import org.jbox2d.collision.shapes.CircleShape;
import org.jbox2d.common.Vec2;
import org.jbox2d.dynamics.*;

import javax.sound.sampled.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;

/**
 * Mainframe class represents a JFrame application utilizing physics simulations.
 */




public class Mainframe {
    /** The main window of the application. */
    private JFrame frame;
    private int maxHealth = 100; // Feel free to adjust
    private int currentHealth = maxHealth;
    private int healthBarX = 10; // Position on the frame
    private int healthBarY = 10; // Position on the frame
    private int healthBarWidth = 200;
    private int healthBarHeight = 20;
    /** Custom panel for rendering game objects. */
    /** Represents the physical body of a circle in the world. */
    private DrawingPanel drawingPanel;
    private Body circleBody;
    /** Represents the physics world. */
    private World world;
    /** Constant for converting pixel values to meter values. */
    public static final float PIXELS_TO_METERS = 0.01f;
    /** Flag indicating if the circle body is jumping. */
    private boolean isJumping = false;
    /** Flag indicating if the game is over. */
    private boolean isGameOver = false;
    /** Flag indicating if the game is won. */
    private boolean isGameWin = false;
    /** Background audio clip for the game. */
    private Clip backgroundAudioClip;
    /** Flag indicating if the game is in level 1. */
    boolean level1=true;
    /** Flag indicating if the game is in level 2. */
    boolean level2=false;
    /** Flag indicating if the game is in level 3. */
    boolean level3=false;
    /** Represents another physical object in the game. */
    private Body rectangularBody;

    public Mainframe(int frameWidth, int frameHeight) {
        // Initializing main application window
        frame = new JFrame("Game");
        // Setting up the drawing panel
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(frameWidth, frameHeight);
        // Setting up the drawing panel
        drawingPanel = new DrawingPanel();



        frame.getContentPane().add(drawingPanel);
        frame.setVisible(true);
        // Setting up world with gravitational force
        world = new World(new Vec2(0, -9.81f * PIXELS_TO_METERS));

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyType.DYNAMIC;
        bodyDef.position.set(10 * PIXELS_TO_METERS, frameHeight * PIXELS_TO_METERS / 2);
        circleBody = world.createBody(bodyDef);
        // Defining and attaching a circular shape to the circle body
        org.jbox2d.collision.shapes.CircleShape circleShape = new CircleShape();
        circleShape.setRadius(30 * PIXELS_TO_METERS);
        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 1.0f;
        circleBody.createFixture(fixtureDef);




        // Load and play background audio
        /**
         Initialize and start looping the background audio clip.
         */
        try {
            URL audioUrl = getClass().getResource("background.wav"); // Change to your audio file path
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioUrl);
            backgroundAudioClip = AudioSystem.getClip();
            backgroundAudioClip.open(audioInputStream);
            backgroundAudioClip.loop(Clip.LOOP_CONTINUOUSLY); // Loop the audio

        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }

        /**
         Set up key listeners for user interaction.
         */
        frame.addKeyListener(new KeyAdapter() {
            @Override

            public void keyPressed(KeyEvent e) {
                if (e.getKeyChar() == 'w' && !isJumping) { // Change 's' to 'w'
                    circleBody.setLinearVelocity(new Vec2(2, 0));
                    isJumping = true;
                    circleBody.applyLinearImpulse(new Vec2(0, -200 * PIXELS_TO_METERS), circleBody.getWorldCenter()); // Change impulse to -200
                    playJumpSound();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyChar() == 'w') { // Change 's' to 'w'
                    Vec2 velocity = circleBody.getLinearVelocity();
                    if (velocity.y < 0) { // Change to check if y velocity is negative
                        circleBody.setLinearVelocity(new Vec2(velocity.x, velocity.y * -0.2f));
                    }
                    isJumping = false;
                }
            }
        });
        /**
         Start the main game loop, updating the physics world and rendering the game objects.
         */
        Timer timer = new Timer(16, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!isGameOver) {
                    world.step(1 / 60f, 8, 3);
                    if(level1) {
                        checkGameOver(190, 205);
                    }else if(level2){
                        checkGameOver(170, 215);
                    }
                    else{
                        level3=true;
                        checkGameOver(240, 280);
                    }
                    checkGameWin();
                    drawingPanel.repaint();
                }
            }
        });
        timer.start();
    }

    /**
     Draw a background image on the game window.
     */
    private void drawBackground(Graphics g, String imagePath) {
        ImageIcon backgroundImage = new ImageIcon(getClass().getResource(imagePath));
        g.drawImage(backgroundImage.getImage(), 0, 0, frame.getWidth(), frame.getHeight(), null);
    }
    /**
     * Render the circle on the game window.
     */
    private void drawCircle(Graphics g) {
        int radius = (int) (0.1 / PIXELS_TO_METERS);
        Vec2 position = circleBody.getPosition();
        int x = (int) (position.x / PIXELS_TO_METERS) - radius;
        int y = (int) (position.y / PIXELS_TO_METERS) - radius;
        g.setColor(Color.BLUE);
        g.fillOval(x, y, radius * 2, radius * 2);
    }



    /**
     Play a sound effect when the circle jumps.
     */
    private void playJumpSound() {
        try {
            URL audioUrl = getClass().getResource("jump.wav"); // Adjust the path accordingly
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioUrl);
            Clip jumpAudioClip = AudioSystem.getClip();
            jumpAudioClip.open(audioInputStream);
            jumpAudioClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    /**
     Plays the collision sound effect.
     */
    private void playCollisionSound() {
        try {
            URL audioUrl = getClass().getResource("col.wav"); // Adjust the path accordingly
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioUrl);
            Clip collisionAudioClip = AudioSystem.getClip();
            collisionAudioClip.open(audioInputStream);
            collisionAudioClip.start();
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
        }
    }
    /**
     Checks if the player has won the game (i.e., if the circle has reached the right edge of the frame).
     */
    private void checkGameWin() {
        Vec2 position = circleBody.getPosition();
        int radius = (int) (0.1 / PIXELS_TO_METERS);
        int rightBoundary = frame.getWidth() - radius;
        if (position.x / PIXELS_TO_METERS >= rightBoundary) {
            isGameWin = true;
            backgroundAudioClip.stop();
            showGameWinDialog();
        }
    }
    /**Checks if the player has lost the game based on the circle's position and boundaries provided.
     Top boundary
     Distance from the bottom of the frame to set the bottom boundary
     */
    private void checkGameOver(int i,int j) {
        Vec2 position = circleBody.getPosition();


        int radius = (int) (0.1 / PIXELS_TO_METERS);
        int topBoundary =i;
        int bottomBoundary = frame.getHeight() -j;


        if (position.y / PIXELS_TO_METERS - radius <= topBoundary || position.y / PIXELS_TO_METERS + radius >= bottomBoundary) {
            isGameOver = true;
            backgroundAudioClip.stop();
            playCollisionSound();
            showGameOverDialog();
        }


    }

    /**
     Displays a dialog box indicating the game is over and offers an option to restart.
     */
    private void showGameOverDialog() {
        int option = JOptionPane.showConfirmDialog(frame, "Game Over!\nDo you want to start again?", "Game Over", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            resetGame();
        } else {
            System.exit(0);
        }
    }
    /**
     Resets the game to its initial state.
     */
    private void resetGame() {
        isGameOver = false;
        circleBody.setTransform(new Vec2(10 * PIXELS_TO_METERS, frame.getHeight() * PIXELS_TO_METERS / 2), 0);
        circleBody.setLinearVelocity(new Vec2(0, 0));
        backgroundAudioClip.loop(Clip.LOOP_CONTINUOUSLY);
    }
    /**
     Displays a dialog box congratulating the player on winning and offers an option to proceed to the next level.
     */
    private void showGameWinDialog() {
        int option = JOptionPane.showConfirmDialog(frame, "You win!\n Next Level?", "You win!", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            if (level1) {
                goToNextLevel();
            } else if (level2) {
                goToNextNextLevel();
            } else if (level3) {
                // Handle winning the game (if there's no next level)
                System.exit(0);
            }
        } else {
            System.exit(0);
        }
    }
    /**
     Progresses the game to the third level.
     */
    private void goToNextNextLevel() {
        isGameWin = false;
        isGameOver = false;
        level1=false;
        level2=false;
        level3=true;

        resetGame();



/**
 DrawingPanel class is responsible for rendering the game's background and objects.
 */
        drawingPanel.loadImage("/level3.jpg"); // Load the new background image
        drawingPanel.repaint();

        backgroundAudioClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    private void goToNextLevel() {
        isGameWin = false;
        isGameOver = false;
        level1=false;
        level2=true;
        resetGame();
        drawingPanel.loadImage("/level2.jpeg"); // Load the new background image
        drawingPanel.repaint();
        backgroundAudioClip.loop(Clip.LOOP_CONTINUOUSLY);
    }

    /**
     Initializes the DrawingPanel with a default background image.
     */
    private class DrawingPanel extends JPanel {
        private Image backgroundImage; // Add this line

        public DrawingPanel() {
            loadImage("/level1.jpeg"); // Load the default background image

        }


        public void loadImage(String imagePath) {
            backgroundImage = new ImageIcon(getClass().getResource(imagePath)).getImage();

        }


        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);

            drawCircle(g);
            //drawRectangularObject(g);
        }
    }

    public void paint(Graphics g) {

        // ... Rendering other game components ...

        // Draw background of health bar
        g.setColor(Color.GRAY);
        g.fillRect(healthBarX, healthBarY, healthBarWidth, healthBarHeight);

        // Calculate width of the actual health
        int actualHealthWidth = (int) ((currentHealth / (float) maxHealth) * healthBarWidth);

        // Draw the actual health
        g.setColor(Color.RED); // Red color or any other desired color for health representation
        g.fillRect(healthBarX, healthBarY, actualHealthWidth, healthBarHeight);
    }


    /**
     Entry point for the game. Initializes the main frame.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Mainframe(800, 600);
        });
    }
}



