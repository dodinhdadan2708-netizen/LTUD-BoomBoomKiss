package boomboomkiss;

abstract class Cell {
    Position position;
    private boolean revealed;

    Cell(Position position) {
        this.position = position;
    }

    boolean isRevealed() {
        return revealed;
    }

    void reveal() {
        revealed = true;
    }

    abstract CellType getType();
}

enum CellType { SAFE, BOMB, KISS }

class SafeCell extends Cell {
    SafeCell(Position position) { super(position); }
    CellType getType() { return CellType.SAFE; }
}

class BombCell extends Cell {
    BombCell(Position position) { super(position); }
    CellType getType() { return CellType.BOMB; }
}

class KissCell extends Cell {
    KissCell(Position position) { super(position); }
    CellType getType() { return CellType.KISS; }
}
