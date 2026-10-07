package boomboomkiss.model;

abstract class Cell {
    private final Position position;
    private boolean opened;

    Cell(Position position) {
        if (position == null) throw new IllegalArgumentException("Position must not be null");
        this.position = position;
    }

    final Position getPosition() { return position; }
    final boolean isOpened() { return opened; }

    // Template Method: all cell kinds share validation and the once-only opening rule.
    final void open(Board board, AttackResult result) {
        if (board == null || result == null) throw new IllegalArgumentException("Missing opening context");
        board.validateOpening(this, result);
        if (opened) return; // Blast overlaps and chain reactions skip already opened cells.
        opened = true; // Mark before dispatch, so an adjacent Boom cannot activate this one again.
        result.recordOpened(position);
        onOpen(board, result);
    }

    abstract CellType getType();
    protected abstract void onOpen(Board board, AttackResult result);
}

class SafeCell extends Cell {
    SafeCell(Position position) { super(position); }
    @Override CellType getType() { return CellType.SAFE; }
    @Override protected void onOpen(Board board, AttackResult result) {
        // Safe only participates in the shared opening steps.
    }
}

class BoomCell extends Cell {
    BoomCell(Position position) { super(position); }
    @Override CellType getType() { return CellType.BOOM; }
    @Override protected void onOpen(Board board, AttackResult result) {
        board.explode(getPosition(), result);
    }
}

class KissCell extends Cell {
    KissCell(Position position) { super(position); }
    @Override CellType getType() { return CellType.KISS; }
    @Override protected void onOpen(Board board, AttackResult result) {
        result.markKissHit();
    }
}
