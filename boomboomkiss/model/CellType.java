package boomboomkiss.model;

import boomboomkiss.config.GameConfig;
import java.util.function.Function;

public enum CellType {
    SAFE(GameConfig.BOARD_SIZE * GameConfig.BOARD_SIZE, SafeCell::new),
    BOOM(GameConfig.BOOM_COUNT, BoomCell::new),
    KISS(GameConfig.KISS_COUNT, KissCell::new);

    private final int limit;
    private final Function<Position, Cell> factory;

    CellType(int limit, Function<Position, Cell> factory) {
        this.limit = limit;
        this.factory = factory;
    }

    int getLimit() { return limit; }
    boolean isPlaceable() { return this != SAFE; }
    Cell create(Position position) { return factory.apply(position); }
}
