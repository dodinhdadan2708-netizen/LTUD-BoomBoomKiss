package boomboomkiss.model;

final class Position {
    private final int row;
    private final int column;

    Position(int row, int column) {
        this.row = row;
        this.column = column;
    }

    int getRow() { return row; }
    int getColumn() { return column; }

    @Override public boolean equals(Object other) {
        if (!(other instanceof Position)) return false;
        Position position = (Position) other;
        return row == position.row && column == position.column;
    }

    @Override public int hashCode() { return 31 * row + column; }
}
