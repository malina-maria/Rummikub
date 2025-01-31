package model;

public enum TileColor {
    BLACK, BLUE, YELLOW, RED;

    /**
     * Returns a single-character representation of the TileColor.
     *
     * @return "B" for BLACK, "b" for BLUE, "Y" for YELLOW, "R" for RED.
     */
    @Override
    public String toString() {
        return switch (this) {
            case BLACK -> "B";
            case BLUE -> "b";
            case YELLOW -> "Y";
            case RED -> "R";
        };
    }

    /**
     * Returns the TileColor corresponding to the given abbreviation.
     *
     * @param abbreviation the single-character abbreviation of the color ("B", "b", "Y", or "R").
     * @return the TileColor matching the abbreviation.
     * @throws IllegalArgumentException if the abbreviation is invalid.
     */
    public static TileColor fromAbbreviation(String abbreviation) {
        return switch (abbreviation) {
            case "B" -> BLACK;
            case "b" -> BLUE;
            case "Y" -> YELLOW;
            case "R" -> RED;
            default -> throw new IllegalArgumentException("Invalid TileColor abbreviation: " + abbreviation);
        };
    }
}
