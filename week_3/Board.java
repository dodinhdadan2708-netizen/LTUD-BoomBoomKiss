package week_3;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;

final class Board {
    private final Cell[][] cells = new Cell[GameConfig.BOARD_SIZE][GameConfig.BOARD_SIZE];

    Board() {
        clear();
    }

    void clear() {
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                cells[row][col] = new SafeCell(new Position(row, col));
            }
        }
    }

    Cell getCell(Position position) {
        if (position.row < 0 || position.row >= GameConfig.BOARD_SIZE ||
                position.column < 0 || position.column >= GameConfig.BOARD_SIZE) {
            throw new IllegalArgumentException("Position outside board");
        }
        return cells[position.row][position.column];
    }

    int count(CellType type) {
        int result = 0;
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell.getType() == type) result++;
            }
        }
        return result;
    }

    int remaining(CellType type) {
        int result = 0;
        for (Cell[] row : cells) {
            for (Cell cell : row) {
                if (cell.getType() == type && !cell.isRevealed()) result++;
            }
        }
        return result;
    }

    boolean place(Position position, CellType type) {
        if (type == CellType.SAFE || getCell(position).getType() != CellType.SAFE ||
                count(type) >= GameConfig.limitFor(type)) {
            return false;
        }
        cells[position.row][position.column] = type == CellType.BOMB
                ? new BombCell(position) : new KissCell(position);
        return true;
    }

    boolean setType(Position position, CellType type) {
        CellType current = getCell(position).getType();
        if (current == type) return true;
        if (type != CellType.SAFE && count(type) >= GameConfig.limitFor(type)) return false;
        remove(position);
        return type == CellType.SAFE || place(position, type);
    }

    void remove(Position position) {
        cells[position.row][position.column] = new SafeCell(position);
    }

    boolean isSetupComplete() {
        return count(CellType.BOMB) == GameConfig.BOMB_COUNT &&
                count(CellType.KISS) == GameConfig.KISS_COUNT;
    }

    void randomize(Random random) {
        clear();
        int[] slots = new int[GameConfig.BOARD_SIZE * GameConfig.BOARD_SIZE];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        for (int i = 0; i < GameConfig.BOMB_COUNT + GameConfig.KISS_COUNT; i++) {
            int pick = i + random.nextInt(slots.length - i);
            int temp = slots[i];
            slots[i] = slots[pick];
            slots[pick] = temp;
            place(new Position(slots[i] / GameConfig.BOARD_SIZE,
                    slots[i] % GameConfig.BOARD_SIZE), i < GameConfig.BOMB_COUNT ? CellType.BOMB : CellType.KISS);
        }
    }

    AttackResult reveal(Position target, Random random) {
        Cell first = getCell(target);
        if (first.isRevealed()) throw new IllegalArgumentException("Cell already revealed");
        ArrayDeque<Position> queue = new ArrayDeque<Position>();
        queue.add(target);
        List<Position> opened = new ArrayList<Position>();
        boolean kissHit = false;
        while (!queue.isEmpty()) {
            Position position = queue.remove();
            Cell cell = getCell(position);
            if (cell.isRevealed()) continue;
            cell.reveal();
            opened.add(position);
            kissHit |= cell.onOpen(this, queue, random);
        }
        return new AttackResult(opened, kissHit);
    }

    void explode(Position center, Queue<Position> pending, Random random) {
        boolean horizontal = random.nextBoolean();
        for (int offset = -GameConfig.BLAST_RADIUS; offset <= GameConfig.BLAST_RADIUS; offset++) {
            int row = center.row + (horizontal ? 0 : offset);
            int col = center.column + (horizontal ? offset : 0);
            if (row >= 0 && row < GameConfig.BOARD_SIZE &&
                    col >= 0 && col < GameConfig.BOARD_SIZE &&
                    !cells[row][col].isRevealed()) {
                pending.add(new Position(row, col));
            }
        }
    }
}

final class AttackResult {
    final List<Position> opened;
    final boolean kissHit;

    AttackResult(List<Position> opened, boolean kissHit) {
        this.opened = opened;
        this.kissHit = kissHit;
    }
}
