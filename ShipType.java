
/*
    ShipType.java
    - Defines the 5 ships in the Battleship game
    - Each ship has a name and length (Determine squares occupied)
*/

public enum ShipType
{
    CARRIER("Carrier", 5),
    BATTLESHIP("Battleship", 4),
    CRUISER("Cruiser", 3),
    SUBMARINE("Submarine", 3),
    DESTROYER("Destroyer", 2);

    private final String name;
    private final int length;

    ShipType(String name, int length)
    {
        this.name = name;
        this.length = length;
    }

    // Getters for ship info
    public String getName()
    {
        return name;
    }

    public int getLength()
    {
        return length;
    }
}
