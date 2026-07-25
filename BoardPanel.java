
/*
    BoardPanel.java
    - Creating of a single Battleship game board
    - (Drawing of board, displaying current states of each cell, mouse clicks,
      updates)
*/

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import javax.swing.JPanel;

public class BoardPanel extends JPanel
{
    // Size due to 10 rows and 10 cols
    private static final int BOARD_SIZE = 10;

    // 34 pixels for row and col labels
    private static final int LABEL_SPACE = 34;


    // Storing of player ships location
    private final CellState[][] shipCells;

    // Storing of firing (HIT or MISS)
    private final CellState[][] shotCells;
    
    // Drawing information for mouse clicks
    private int startX;
    private int startY;
    private int cellSize;


    // Ship placement info
    private boolean placementMode;
    private boolean horizontal;
    private boolean showShips;
    private ShipType currentShip;
    private boolean firingMode;

    // Starting square for placement preview
    private int previewRow;
    private int previewCol;

    // Used for BattleShipFrame that ship is placed
    private ShipPlacementListener shipPlacementListener;
    
    // Used for BattleshipFrame that shot was fired 
    private ShotListener shotListener;

    public interface ShipPlacementListener
    {
        void shipPlaced(ShipType ship);
    }
    
    public interface ShotListener
    {
        void shotSelected(int row, int col);
    }


    // Start both boards as hidden and set to light blue, board is visible
    public BoardPanel()
    {
        shipCells = new CellState[BOARD_SIZE][BOARD_SIZE];
        shotCells = new CellState[BOARD_SIZE][BOARD_SIZE];
 
        // Start cells as EMPTY
        for(int i = 0; i < BOARD_SIZE; i++)
        {
            for(int j = 0; j < BOARD_SIZE; j++)
            {
                shipCells[i][j] = CellState.EMPTY;
                shotCells[i][j] = CellState.EMPTY;
            }
        }

        // Initial setup
        placementMode = false;
        firingMode = false;
        horizontal = true;
        showShips = false;
        currentShip = null;
        previewRow = -1;
        previewCol = -1;
        setBackground(new Color(235, 242, 248));

        // Mouse handling for board
        addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent event)
            {
                Point cell = getCellAt(event.getX(), event.getY());
                if(cell == null)
                {
                    return;
                }

                int col = cell.x;
                int row = cell.y;
            
                if(placementMode && canPlaceShip(row, col))
                {
                    placeShip(row, col);
                }
                else if(firingMode && shotListener != null)
                {
                    shotListener.shotSelected(row, col);
                }
            }

            public void mouseExited(MouseEvent event)
            {
                previewRow = -1;
                previewCol = -1;
                repaint();
            }
        });

        // Updating preview for mouse moving
        addMouseMotionListener(new MouseMotionAdapter()
        {
            public void mouseMoved(MouseEvent event)
            {
                if(!placementMode && !firingMode)
                {
                    return;
                }

                Point cell = getCellAt(event.getX(), event.getY());
                if(cell == null)
                {
                    previewRow = -1;
                    previewCol = -1;
                }
                else
                {
                    previewCol = cell.x;
                    previewRow = cell.y;
                }
                repaint();
            }
        });
    }

    // Listener that runs after ship is successfully placed
    public void setShipPlacementListener(ShipPlacementListener listener)
    {
        shipPlacementListener = listener;
    }

    // Listener that shot fired
    public void setShotListener(ShotListener listener)
    {
        shotListener = listener;
    }

    // Start of placing the given ship
    public void startShipPlacement(ShipType ship)
    {
        currentShip = ship;
        placementMode = true;
        firingMode = false;
        showShips = true;
        previewRow = -1;
        previewCol = -1;
        repaint();
    }

    // Changing of current ship between horizontal and vertical
    public void rotateShip()
    {
        if(placementMode)
        {
            horizontal = !horizontal;
            repaint();
        }
    }

    // Hiding ship layer and show shot overlay
    // Ships stoared in shipCells, but not drawn on the screen
    public void showShotOverlay()
    {
        placementMode = false;
        firingMode = false;
        showShips = false;
        currentShip = null;
        previewRow = -1;
        previewCol = -1;
        repaint();
    }

    // Setting selection on or off for firing
    public void setFiringMode(boolean firingMode)
    {
        this.firingMode = firingMode;
        placementMode = false;
        showShips = false;
        currentShip = null;
        previewRow = -1;
        previewCol = -1;
        repaint();
    }

    // Drawing of board
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);

        // Calcuate available space, counts if window is resized
        int usableWidth = getWidth() - LABEL_SPACE - 20;
        int usableHeight = getHeight() - LABEL_SPACE - 20;
        cellSize = Math.max(1, Math.min(usableWidth, usableHeight) / BOARD_SIZE);

        // Grid size is based off window size previosuly calculated
        int gridSize = cellSize * BOARD_SIZE;
        startX = (getWidth() - gridSize + LABEL_SPACE) / 2;
        startY = (getHeight() - gridSize + LABEL_SPACE) / 2;

        // Initial setup for board/cells/labels
        drawLabels(g);
        drawGrid(g);

        if(placementMode)
        {
            drawShipPreview(g);
        }
        else if(firingMode)
        {
            drawShotPreview(g);
        }
    }


    private void drawGrid(Graphics g)
    {
        for(int row = 0; row < BOARD_SIZE; row++)
        {
            for(int col = 0; col < BOARD_SIZE; col++)
            {
                int x =startX + col * cellSize;
                int y =startY + row * cellSize;

                if(shotCells[row][col] == CellState.HIT)
                {
                    g.setColor(Color.RED);
                }
                else if(shotCells[row][col] ==CellState.MISS)
                {
                    g.setColor(Color.WHITE);
                }
                else if(showShips && shipCells[row][col] == CellState.SHIP)
                {
                    g.setColor(Color.DARK_GRAY);
                }
                else
                {
                    g.setColor(new Color(185,220,240));
                }

                g.fillRect(x,y,cellSize,cellSize);
                g.setColor(new Color(25,70,105));
                g.drawRect(x,y,cellSize,cellSize);
            }
        }
    }
    
    // Labels (A-J, 1 - 10)
    private void drawLabels(Graphics g)
    {
        // Dark color + Sizing
        g.setColor(new Color(20, 45, 65));
        g.setFont(new Font("SansSerif", Font.BOLD,Math.max(12, cellSize / 3)));
        
        // Return label in pixels for sizing
        FontMetrics metrics = g.getFontMetrics();

        // A - J
        for (int col = 0; col < BOARD_SIZE; col++)
        {
            String label = String.valueOf((char)('A' + col));
            int x = startX + col * cellSize + (cellSize - metrics.stringWidth(label)) / 2;
            int y = startY - 8;
            g.drawString(label, x, y);
        }

        // 1 - 10
        for (int row = 0; row < BOARD_SIZE; row++)
        {
            String label = String.valueOf(row + 1);
            int x = startX - metrics.stringWidth(label) - 8;
            int y = startY + row * cellSize + (cellSize + metrics.getAscent()) / 2 - 2;
            g.drawString(label, x, y);
        }
    }


    // Drawing ship preview (under mouse on current square)
    private void drawShipPreview(Graphics g)
    {
        if(currentShip == null || previewRow == -1 || previewCol == -1)
        {
            return;
        }

        boolean valid = canPlaceShip(previewRow, previewCol);

        for(int i = 0; i < currentShip.getLength(); i++)
        {
            int row = previewRow;
            int col = previewCol;

            if(horizontal)
            {
                col += i;
            }
            else
            {
                row += i;
            }

            // Checking for previewing cells inside the board
            if(row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE)
            {
                int x = startX + col * cellSize;
                int y = startY + row * cellSize;

                if(valid)
                {
                    g.setColor(new Color(80, 200, 100));
                }
                else
                {
                    g.setColor(new Color(230, 80, 80));
                }

                g.fillRect(x + 2, y + 2, cellSize - 3, cellSize - 3);
                g.setColor(new Color(25, 70, 105));
                g.drawRect(x, y, cellSize, cellSize);
            }
        }
    }


    // Highlighting one square during firing mode
    private void drawShotPreview(Graphics g)
    {
        if(previewRow == -1 || previewCol == -1)
        {
            return;
        }

        // Do not highlight squares that were already fired upon
        if(shotCells[previewRow][previewCol] != CellState.EMPTY)
        {
            return;
        }

        int x = startX + previewCol * cellSize;
        int y = startY + previewRow * cellSize;

        g.setColor(new Color(255, 220, 90));
        g.fillRect(x + 2, y + 2, cellSize - 3, cellSize - 3);

        g.setColor(new Color(25, 70, 105));
        g.drawRect(x, y, cellSize, cellSize);
    }


    // Conveting mouse position to a square
    private Point getCellAt(int mouseX, int mouseY)
    {
        int gridSize = cellSize * BOARD_SIZE;

        if(mouseX < startX || mouseX >= startX + gridSize)
        {
            return null;
        }

        if(mouseY < startY || mouseY >= startY + gridSize)
        {
            return null;
        }

        int col = (mouseX - startX) / cellSize;
        int row = (mouseY - startY) / cellSize;
        return new Point(col, row);
    }
    

    // Checking if current ship fits and does not overlap another ship
    private boolean canPlaceShip(int startingRow, int startingCol)
    {
        if(currentShip == null)
        {
            return false;
        }

        for(int i = 0; i < currentShip.getLength(); i++)
        {
            int row = startingRow;
            int col = startingCol;

            if(horizontal)
            {
                col += i;;
            }
            else
            {
                row += i;
            }

            // If ship leaves board
            if(row < 0 || row >= BOARD_SIZE ||
               col < 0 || col >= BOARD_SIZE)
            {
                return false;
            }

            // If overlapping
            if(shipCells[row][col] == CellState.SHIP)
            {
                return false;
            }
        }
        return true;
    }

    private void placeShip(int startingRow, int startingCol)
    {
        ShipType placedShip = currentShip;

        for(int i = 0; i < placedShip.getLength(); i++)
        {
            int row = startingRow;
            int col = startingCol;

            if(horizontal)
            {
                col += i;
            }
            else
            {
                row += i;
            }

            shipCells[row][col] = CellState.SHIP;
        }

        placementMode = false;
        currentShip = null;
        previewRow = -1;
        previewCol = -1;
        repaint();

        if(shipPlacementListener != null)
        {
            shipPlacementListener.shipPlaced(placedShip);
        }
    }
    public CellState fireAt(int row, int col)
    {
        // Reject invalid coordinates
        if(row < 0 || row >= BOARD_SIZE ||
        col < 0 || col >= BOARD_SIZE)
        {
            return null;
        }

        // Reject a square that was already selected
        if(shotCells[row][col] != CellState.EMPTY)
        {
            return null;
        }

        // Compare the shot against the parallel ship array
        if(shipCells[row][col] == CellState.SHIP)
        {
            shotCells[row][col] = CellState.HIT;
        }
        else
        {
            shotCells[row][col] = CellState.MISS;
        }

        repaint();

        return shotCells[row][col];
    }
}
