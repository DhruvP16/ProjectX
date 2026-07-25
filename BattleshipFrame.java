/*
    BattelshipFrame.java
    - Creates main window for Battleship game
    - Manages the layout of the GUI
      (Both boards, game relay, status messages, buttons, player switching)
*/



import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.io.File;

// Audio imports
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;


public class BattleshipFrame extends JFrame
{
    // Two boards
    private final BoardPanel player1Board;
    private final BoardPanel player2Board;

    private final JLabel player1Title;
    private final JLabel player2Title;
    private final JLabel statusLabel;

    private final JTextArea gameRelay;
    private final JButton rotateShipButton;
    private final JButton randomFleetButton;

    // Contains the 5 ships in placement order
    private final ShipType[] fleet;
    
    private int placementPlayer;
    private int currentPlayer;
    private int currentShipIndex;
    private boolean placementComplete;

    public BattleshipFrame()
    {
        super("Battleship Layout");

        fleet = ShipType.values();
        placementPlayer = 1;
        currentShipIndex = 0;
        currentPlayer = 1;
        placementComplete = false;

        player1Board = new BoardPanel();
        player2Board = new BoardPanel();

        // Listening for Player placements
        player1Board.setShipPlacementListener(ship -> handleShipPlacement(1, ship));
        player2Board.setShipPlacementListener(ship -> handleShipPlacement(2, ship));

        // Listening for Firing selections
        player1Board.setShotListener((row, col) -> handleShotSelection(1, row,
        col));
        player2Board.setShotListener((row, col) -> handleShotSelection(2, row,
        col));

        player1Title = createTitle("PLAYER 1");
        player2Title = createTitle("PLAYER 2");

        // Status of Turns
        statusLabel = new JLabel("", SwingConstants.CENTER);
        statusLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        statusLabel.setBorder(BorderFactory.createEmptyBorder(12, 5, 12, 5));

        // Game Relay
        gameRelay = new JTextArea();
        gameRelay.setEditable(false);
        gameRelay.setLineWrap(true);
        gameRelay.setWrapStyleWord(true);
        gameRelay.setFont(new Font("Monospaced", Font.PLAIN, 15));
        gameRelay.setBackground(new Color(245,248,250));
        gameRelay.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));


        // Buttons
        rotateShipButton = new JButton("Rotate Ship");
        JButton rulesButton = new JButton("Rules");
        randomFleetButton = new JButton("Random Fleet");

        // Events
        rotateShipButton.addActionListener(event -> 
        {
            if(placementComplete)
            {
                return;
            }
            BoardPanel board = getPlacementBoard(); 
            board.rotateShip();
        });

        rulesButton.addActionListener(event -> new RulesWindow());

        // Randomly place 5 ships for current player
        randomFleetButton.addActionListener(event ->
        {
            if(placementComplete)
            {
                return;
            }

            // Warning message
            int choice = JOptionPane.showConfirmDialog
            (
                this,
                "This will remove any ships already placed and randomly place all five ships.\n\nContinue?",
                "Random Fleet",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE
            );

            if(choice != JOptionPane.YES_OPTION)
            {
                return;
            }

            BoardPanel board = getPlacementBoard();
            board.placeFleetRandomly();

            // 5 ships placed
            currentShipIndex = fleet.length;
            addGameMessage("Player " + placementPlayer + " randomly placed all five ships.");
            finishPlayerPlacement();
        });

        // Placing two boards next to each other
        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 14, 0));
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));
        boardsPanel.add(createBoardContainer(player1Title, player1Board));
        boardsPanel.add(createBoardContainer(player2Title, player2Board));

        // Right Side of the panel
        JPanel sidePanel = new JPanel(new BorderLayout(0, 12));
        sidePanel.setPreferredSize(new Dimension(280, 0));
        sidePanel.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 14));

        // Relay title
        JLabel relayTitle = new JLabel("GAME RELAY", SwingConstants.CENTER);
        relayTitle.setFont(new Font("SansSerif", Font.BOLD, 20));
        relayTitle.setForeground(new Color(20,60,90));
        relayTitle.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        JScrollPane relayScrollPane = new JScrollPane(gameRelay);

        // Relay panel layout
        JPanel relayPanel = new JPanel(new BorderLayout());
        relayPanel.setBorder(BorderFactory.createLineBorder(new Color(70,90,105), 2));
        relayPanel.add(relayTitle, BorderLayout.NORTH);
        relayPanel.add(relayScrollPane, BorderLayout.CENTER);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridLayout(0,1,0,8));
        buttonPanel.setBorder(BorderFactory.createTitledBorder("Controls"));

        buttonPanel.add(rotateShipButton);
        buttonPanel.add(randomFleetButton);
        buttonPanel.add(rulesButton);

        // Adding relay and buttons to right side
        sidePanel.add(relayPanel, BorderLayout.CENTER);
        sidePanel.add(buttonPanel, BorderLayout.SOUTH);

        // Initial layout 
        setLayout(new BorderLayout());
        add(statusLabel, BorderLayout.NORTH);
        add(boardsPanel, BorderLayout.CENTER);
        add(sidePanel, BorderLayout.EAST);

        // Player 1 is current and intial setup of boards
        startPlayerPlacement(1);

        // Frame properties
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 600));
        setSize(1350, 750);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    // PlLAYER 1 or PLAYER 2 (title)
    private JLabel createTitle(String text)
    {
        JLabel title = new JLabel(text, SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setForeground(new Color(20, 60, 90));
        title.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
        return title;
    }

    // Method for making one title and one board into a panel
    private JPanel createBoardContainer(JLabel title, BoardPanel board)
    {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(Color.WHITE);
        container.setBorder(BorderFactory.createLineBorder(new Color(70, 90, 105), 2));
        container.add(title, BorderLayout.NORTH);
        container.add(board, BorderLayout.CENTER);
        return container;
    }

    // Player 1 ship placement phase
    private void startPlayerPlacement(int playerNumber)
    {
        placementPlayer = playerNumber;
        currentShipIndex = 0;

        // Hide any ships that should not currently be visible
        player1Board.showShotOverlay();
        player2Board.showShotOverlay();
        
        if(playerNumber == 1)
        {
            player1Title.setText("PLAYER 1 - PLACE YOUR SHIPS");
            player2Title.setText("PLAYER 2 - SHOT GRID");
        }
        else
        {
            player1Title.setText("PLAYER 1 - SHOT GRID");
            player2Title.setText("PLAYER 2 - PLACE YOUR SHIPS");
        }

        addGameMessage("Player " + playerNumber + " begins ship placement.");
        startNextShip();
    }

    // Placement of next ship in fleet
    private void startNextShip()
    {
        ShipType ship = fleet[currentShipIndex];
        BoardPanel board = getPlacementBoard(); 
        board.startShipPlacement(ship);
        statusLabel.setText("Player " + placementPlayer + ": Place the " + ship.getName() + " (" + ship.getLength() + " squares)");

        addGameMessage("Place the " + ship.getName() + " using " + ship.getLength() + " squares.");
    }

    // Return the board of the player currently placing ships
     private BoardPanel getPlacementBoard()
    {
        if(placementPlayer == 1)
        {
            return player1Board;
        }
        else
        {
            return player2Board;
        }
    }


    // Handling of successfully placed ship
    private void handleShipPlacement(int playerNumber, ShipType ship)
    {
        addGameMessage("Player " + playerNumber + " placed the " + ship.getName() + ".");

        currentShipIndex++;

        if(currentShipIndex < fleet.length)
        {
            startNextShip();
        }
        else
        {
            finishPlayerPlacement();
        }
    }

    // Runing after player places all 5 ships
    private void finishPlayerPlacement()
    {
        // Hide the ships that were just placed
        getPlacementBoard().showShotOverlay();

        addGameMessage("Player " + placementPlayer + " placed all five ships.");

        // Player 1 finished, so pass the computer to Player 2
        if(placementPlayer == 1)
        {
            statusLabel.setText("Pass the computer to Player 2");

            JOptionPane.showMessageDialog(this,
                "Player 1 has placed all five ships.\n\nPass the computer to Player 2.\nPlayer 2 should click OK when ready.",
                "Player 2 Ship Placement",
                JOptionPane.INFORMATION_MESSAGE);

            startPlayerPlacement(2);
        }
        // Player 2 finished, so prepare for Player 1's first shot
        else
        {
            placementComplete = true;
            currentPlayer = 1;
            player1Board.showShotOverlay();
            player2Board.showShotOverlay();
            player1Board.setFiringMode(false);
            player2Board.setFiringMode(true);
            player1Title.setText("PLAYER 1 BOARD");
            player2Title.setText("PLAYER 2 TARGET GRID");

            // Rotation is no longer needed after placement
            rotateShipButton.setEnabled(false);
            randomFleetButton.setEnabled(false);

            statusLabel.setText("Player 1: Select a square on Player 2's board");
            addGameMessage("Both players placed all five ships.");
            addGameMessage("All ships are now hidden beneath the shot grids.");

            JOptionPane.showMessageDialog(this,
                "Player 2 has placed all five ships.\n\nPass the computer back to Player 1.\nPlayer 1 will take the first shot.",
                "Begin Battleship",
                JOptionPane.INFORMATION_MESSAGE);

            addGameMessage("Player 1 should select a square on Player 2's board.");
        }
    }


    // Recive the square selected during firing
    private void handleShotSelection(int targetPlayer, int row, int col)
    {
        if(!placementComplete)
        {
            return;
        }

        // Player 1 must fire at Player 2's board
        if(currentPlayer == 1 && targetPlayer != 2)
        {
            return;
        }

        // Player 2 must fire at Player 1's board
        if(currentPlayer == 2 && targetPlayer != 1)
        {
            return;
        }

        BoardPanel targetBoard;

        if(targetPlayer == 1)
        {
            targetBoard = player1Board;
        }
        else
        {
            targetBoard = player2Board;
        }

        CellState result = targetBoard.fireAt(row, col);

        if(result == null)
        {
            JOptionPane.showMessageDialog(
                this,
                "That square has already been selected.\nChoose another square.",
                "Invalid Shot",
                JOptionPane.WARNING_MESSAGE
            );

            return;
        }

        char columnLetter = (char)('A' + col);
        String coordinate = "" + columnLetter + (row + 1);

        if(result == CellState.HIT)
        {
            addGameMessage(
                "Player " + currentPlayer +
                " fired at " + coordinate + ": HIT!"
            );

            playSound("missileHit.wav");
        }
        else
        {
            addGameMessage(
                "Player " + currentPlayer +
                " fired at " + coordinate + ": MISS."
            );

            playSound("WaterSplash.wav");
        }

        if(targetBoard.areAllShipsSunk())
        {
            handleGameOver(currentPlayer);
            return;
        }

        // Switch to the other player
        if(currentPlayer == 1)
        {
            currentPlayer = 2;

            player2Board.setFiringMode(false);
            player1Board.setFiringMode(true);

            player1Title.setText("PLAYER 1 TARGET GRID");
            player2Title.setText("PLAYER 2 BOARD");

            statusLabel.setText(
                "Player 2: Select a square on Player 1's board"
            );
        }
        else
        {
            currentPlayer = 1;

            player1Board.setFiringMode(false);
            player2Board.setFiringMode(true);

            player1Title.setText("PLAYER 1 BOARD");
            player2Title.setText("PLAYER 2 TARGET GRID");

            statusLabel.setText(
                "Player 1: Select a square on Player 2's board"
            );
        }
    }

    // Audio Testing
    // Loads and plays one sound file
    private void playSound(String fileName)
    {
        try
        {
            // Create a File object using the file path
            File soundFile = new File(fileName);

            // Open the file as an audio stream
            AudioInputStream audio =
                AudioSystem.getAudioInputStream(soundFile);

            // Create an object that can play a short sound
            Clip clip = AudioSystem.getClip();

            // ADDED: Load the sound into the Clip
            clip.open(audio);

            // Start playing the sound
            clip.start();
        }
        catch(Exception exception)
        {
            // ADDED: Show an error instead of crashing
            JOptionPane.showMessageDialog(
                this,
                "Could not play " + fileName +
                ".\nMake sure the file is a WAV file", "Sound Error",
                JOptionPane.ERROR_MESSAGE
            );
        }
    }

    // Passing of message for the game relay
    private void addGameMessage(String message)
    {
        gameRelay.append(message + "\n");
        gameRelay.setCaretPosition(gameRelay.getDocument().getLength());
    }

    //Game over + new game logic
    private void handleGameOver(int winningPlayer)
    {
        player1Board.setFiringMode(false);
        player2Board.setFiringMode(false);

        String winMessage =
            "Player " + winningPlayer + " wins the game!";

        addGameMessage(winMessage);
        statusLabel.setText(winMessage);

        int choice = JOptionPane.showConfirmDialog(
            this,
            winMessage + "\n\nWould you like to play again?",
            "Game Over",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.INFORMATION_MESSAGE
        );

        if(choice == JOptionPane.YES_OPTION)
        {
            dispose();
            new BattleshipFrame();
        }
    }
}
