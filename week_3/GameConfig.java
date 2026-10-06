package week_3;

final class GameConfig {
    static final int BOARD_SIZE = 5;
    static final int BOMB_COUNT = 2;
    static final int KISS_COUNT = 1;
    static final int BLAST_RADIUS = 1;

    static int limitFor(CellType type) {
        if (type == CellType.BOMB) return BOMB_COUNT;
        if (type == CellType.KISS) return KISS_COUNT;
        return BOARD_SIZE * BOARD_SIZE;
    }

    private GameConfig() { }
}
