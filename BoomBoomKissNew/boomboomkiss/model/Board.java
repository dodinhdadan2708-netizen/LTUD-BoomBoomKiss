package boomboomkiss.model;

import boomboomkiss.config.GameConfig;

import java.util.Random;

class Board {
    private final Cell[][] cells = new Cell[GameConfig.BOARD_SIZE][GameConfig.BOARD_SIZE];
    private final Random random;
    private boolean setupLocked;
    private boolean playing;
    private AttackResult activeResult;

    Board() { this(new Random()); }

    Board(Random random) {
        if (random == null) throw new IllegalArgumentException("Random must not be null");
        this.random = random;
        clear();
    }

    boolean isInside(Position position) {
        return position != null && position.getRow() >= 0 && position.getRow() < GameConfig.BOARD_SIZE &&
                position.getColumn() >= 0 && position.getColumn() < GameConfig.BOARD_SIZE;
    }

    Cell getCell(Position position) {
        if (!isInside(position)) throw new IllegalArgumentException("Position outside board");
        return cells[position.getRow()][position.getColumn()];
    }

    private void requireSetup() {
        if (setupLocked) throw new IllegalStateException("Cannot edit a confirmed board");
    }

    private void requireType(CellType type) {
        if (type == null) throw new IllegalArgumentException("Cell type must not be null");
    }

    void clear() {
        requireSetup();
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++)
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++)
                cells[row][col] = new SafeCell(new Position(row, col));
    }

    int count(CellType type) {
        requireType(type);
        int count = 0;
        for (Cell[] row : cells) for (Cell cell : row) if (cell.getType().equals(type)) count++;
        return count;
    }

    int remaining(CellType type) {
        requireType(type);
        int count = 0;
        for (Cell[] row : cells) for (Cell cell : row)
            if (cell.getType().equals(type) && !cell.isOpened()) count++;
        return count;
    }

    void place(Position position, CellType type) {
        requireSetup(); requireType(type);
        Cell current = getCell(position);
        if (!type.isPlaceable()) throw new IllegalArgumentException("Use remove to clear a cell");
        if (current.getType().isPlaceable()) throw new IllegalArgumentException("Cell is occupied");
        if (count(type) >= type.getLimit()) throw new IllegalStateException("Piece limit reached");
        cells[position.getRow()][position.getColumn()] = type.create(position);
    }

    void remove(Position position) {
        requireSetup(); getCell(position);
        cells[position.getRow()][position.getColumn()] = new SafeCell(position);
    }

    // Atomic edit for the icon picker: validate before removing the original piece.
    void edit(Position position, CellType type) {
        requireSetup(); requireType(type);
        Cell current = getCell(position);
        if (current.getType().equals(type)) return;
        if (type.isPlaceable() && count(type) >= type.getLimit())
            throw new IllegalStateException("Piece limit reached");
        remove(position);
        if (type.isPlaceable()) place(position, type);
    }

    boolean isSetupComplete() {
        return count(CellType.BOOM) == GameConfig.BOOM_COUNT && count(CellType.KISS) == GameConfig.KISS_COUNT;
    }

    void confirmSetup() {
        requireSetup();
        if (!isSetupComplete()) throw new IllegalStateException("Place 2 boom and 1 kiss first");
        setupLocked = true;
    }

    void startPlaying() {
        if (!setupLocked || playing) throw new IllegalStateException("Board not ready to start");
        playing = true;
    }

    void endPlaying() { playing = false; }

    void randomize(Random placementRandom) {
        requireSetup();
        if (placementRandom == null) throw new IllegalArgumentException("Random must not be null");
        clear();
        int[] slots = new int[GameConfig.BOARD_SIZE * GameConfig.BOARD_SIZE];
        for (int i = 0; i < slots.length; i++) slots[i] = i;
        for (int i = 0; i < GameConfig.BOOM_COUNT + GameConfig.KISS_COUNT; i++) {
            int pick = i + placementRandom.nextInt(slots.length - i);
            int temp = slots[i]; slots[i] = slots[pick]; slots[pick] = temp;
            place(new Position(slots[i] / GameConfig.BOARD_SIZE, slots[i] % GameConfig.BOARD_SIZE),
                    i < GameConfig.BOOM_COUNT ? CellType.BOOM : CellType.KISS);
        }
    }

    AttackResult open(Position target) {
        if (!playing) throw new IllegalStateException("Board not playing");
        if (activeResult != null) throw new IllegalStateException("Another attack is being resolved");
        if (getCell(target).isOpened()) throw new IllegalStateException("Cell already opened");
        AttackResult result = new AttackResult();
        activeResult = result;
        try {
            result.enqueue(target);
            while (result.hasPending()) {
                Cell cell = getCell(result.nextPending());
                cell.open(this, result); // No type branch: the cell dispatches its own onOpen().
            }
            result.finish();
            return result;
        } finally {
            activeResult = null;
        }
    }

    void validateOpening(Cell cell, AttackResult result) {
        if (!playing || activeResult == null || activeResult != result)
            throw new IllegalStateException("Cell must be opened through Board.open");
        if (getCell(cell.getPosition()) != cell) throw new IllegalArgumentException("Cell belongs to another board");
    }

    void explode(Position center, AttackResult result) {
        if (activeResult == null || activeResult != result)
            throw new IllegalStateException("Explosion must belong to the active attack");
        getCell(center);
        boolean horizontal = random.nextBoolean();
        for (int offset = -GameConfig.BLAST_RADIUS; offset <= GameConfig.BLAST_RADIUS; offset++) {
            Position neighbor = new Position(center.getRow() + (horizontal ? 0 : offset),
                    center.getColumn() + (horizontal ? offset : 0));
            if (isInside(neighbor) && !getCell(neighbor).isOpened()) result.enqueue(neighbor);
        }
    }
}
