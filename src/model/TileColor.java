package model;

public enum TileColor {
    BLACK, BLUE, YELLOW, RED;

    @Override
    // I want to return the following strings for each color: "B" for BLACK, "b" for BLUE, "Y" for YELLOW, "R" for RED
    public String toString() {
        return switch (this) {
            case BLACK -> "B";
            case BLUE -> "b";
            case YELLOW -> "Y";
            case RED -> "R";
        };
    }
}
