package week_3;

import java.util.Queue;
import java.util.Random;

abstract class Cell {
    final Position position;
    private boolean revealed;

    Cell(Position position) {
        this.position = position;
    }

    final boolean isRevealed() {
        return revealed;
    }

    final void reveal() {
        revealed = true;
    }

    abstract CellType getType();
    abstract boolean onOpen(Board board, Queue<Position> pending, Random random);
}

enum CellType { SAFE, BOMB, KISS }

final class SafeCell extends Cell {
    SafeCell(Position position) { super(position); }
    CellType getType() { return CellType.SAFE; }
    boolean onOpen(Board board, Queue<Position> pending, Random random) { return false; }
}

final class BombCell extends Cell {
    BombCell(Position position) { super(position); }
    CellType getType() { return CellType.BOMB; }
    boolean onOpen(Board board, Queue<Position> pending, Random random) {
        board.explode(position, pending, random);
        return false;
    }
}

final class KissCell extends Cell {
    KissCell(Position position) { super(position); }
    CellType getType() { return CellType.KISS; }
    boolean onOpen(Board board, Queue<Position> pending, Random random) { return true; }
}
