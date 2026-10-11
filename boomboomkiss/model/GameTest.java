package boomboomkiss.model;

import boomboomkiss.config.GameConfig;
import boomboomkiss.repository.MemoryThemeRepository;
import boomboomkiss.repository.ThemeRepository;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Random;

public final class GameTest {
    public static void main(String[] args) throws Exception {
        testExceptionContracts();
        testHorizontalThenVerticalChain();
        testCornerAndPreviouslyOpened();
        testStateAndLoser();
        testLastKiss();
        testPolymorphicOpening();
        testEncapsulation();
        testPackagesAndFactories();
        System.out.println("Game tests passed (Template Method, encapsulation, exceptions and rules)");
    }

    private static void testExceptionContracts() {
        Board board = new Board();
        Position a = new Position(0, 0), b = new Position(0, 1), kiss = new Position(3, 3);
        expect(IllegalStateException.class, () -> board.open(a));
        expect(IllegalArgumentException.class, () -> board.place(new Position(-1, 0), CellType.BOOM));
        expect(IllegalArgumentException.class, () -> board.remove(new Position(5, 0)));
        expect(IllegalArgumentException.class, () -> board.place(null, CellType.BOOM));
        expect(IllegalArgumentException.class, () -> board.place(a, null));
        expect(IllegalArgumentException.class, () -> board.place(a, CellType.SAFE));
        board.place(a, CellType.BOOM);
        expect(IllegalArgumentException.class, () -> board.place(a, CellType.KISS));
        expect(IllegalStateException.class, board::confirmSetup);
        board.place(b, CellType.BOOM);
        expect(IllegalStateException.class, () -> board.place(new Position(0, 2), CellType.BOOM));
        board.place(kiss, CellType.KISS);
        expect(IllegalStateException.class, () -> board.edit(kiss, CellType.BOOM));
        check(board.getCell(kiss).getType() == CellType.KISS, "failed edit preserves original");
        board.edit(kiss, CellType.SAFE);
        board.place(new Position(4, 4), CellType.KISS);
        check(board.isSetupComplete(), "edit and placement complete setup");
        board.confirmSetup();
        expect(IllegalStateException.class, () -> board.open(new Position(2, 2)));
        board.startPlaying();
        expect(IllegalStateException.class, () -> board.place(new Position(1, 1), CellType.BOOM));
        expect(IllegalStateException.class, () -> board.remove(a));
        expect(IllegalStateException.class, board::clear);
        expect(IllegalStateException.class, () -> board.randomize(new Random()));
        expect(IllegalArgumentException.class, () -> board.open(new Position(0, 5)));
        board.open(new Position(2, 2));
        expect(IllegalStateException.class, () -> board.open(new Position(2, 2)));
        expect(IllegalStateException.class, () -> board.getCell(a).open(board, new AttackResult()));
    }

    private static void testHorizontalThenVerticalChain() {
        CountingRandom random = new CountingRandom(true, false);
        Board board = new Board(random);
        board.place(new Position(2, 2), CellType.BOOM);
        board.place(new Position(2, 3), CellType.BOOM);
        board.place(new Position(3, 3), CellType.KISS);
        ready(board);
        AttackResult result = board.open(new Position(2, 2));
        check(result.isKissHit(), "second boom opens kiss");
        check(result.getOpened().size() == 5, "random horizontal then vertical blast opens five cells");
        check(new HashSet<Position>(result.getOpened()).size() == 5, "no duplicate openings");
        check(random.calls == 2, "each boom picks a direction exactly once");
        check(!board.getCell(new Position(1, 2)).isOpened(), "first blast is not a four-direction cross");
        check(board.remaining(CellType.SAFE) == 20 && board.remaining(CellType.BOOM) == 0 &&
                board.remaining(CellType.KISS) == 0, "counters after chain");
        expect(UnsupportedOperationException.class, () -> result.getOpened().clear());
        expect(IllegalStateException.class, result::markKissHit);
        CountingRandom sameDirection = new CountingRandom(true, true);
        Board overlap = new Board(sameDirection);
        overlap.place(new Position(2, 2), CellType.BOOM);
        overlap.place(new Position(2, 3), CellType.BOOM);
        overlap.place(new Position(4, 4), CellType.KISS); ready(overlap);
        AttackResult chain = overlap.open(new Position(2, 2));
        check(chain.getOpened().size() == 4 && sameDirection.calls == 2 && !chain.isKissHit(),
                "overlapping blasts do not reopen or retrigger adjacent boom");
    }

    private static void testCornerAndPreviouslyOpened() {
        Board corner = new Board(new CountingRandom(true));
        corner.place(new Position(0, 0), CellType.BOOM);
        corner.place(new Position(4, 4), CellType.BOOM);
        corner.place(new Position(3, 3), CellType.KISS);
        ready(corner);
        corner.open(new Position(0, 1));
        AttackResult result = corner.open(new Position(0, 0));
        check(result.getOpened().size() == 1 && !result.isKissHit(), "corner clips and skips opened cell");
        Board edge = new Board(new CountingRandom(false));
        edge.place(new Position(0, 2), CellType.BOOM);
        edge.place(new Position(4, 4), CellType.BOOM);
        edge.place(new Position(3, 3), CellType.KISS);
        ready(edge);
        check(edge.open(new Position(0, 2)).getOpened().size() == 2, "vertical edge clips");
        check(edge.getCell(new Position(1, 2)).isOpened(), "vertical neighbor opened");
    }

    private static void testStateAndLoser() {
        Game game = new Game(new CountingRandom(true, false), new MemoryThemeRepository());
        expect(IllegalStateException.class, () -> game.attack(new Position(0, 0)));
        expect(IllegalStateException.class, game::switchTurn);
        expect(IllegalStateException.class, game::confirmSetup);
        game.getPlayer1().getBoard().randomize(new Random(10));
        game.confirmSetup();
        expect(IllegalStateException.class, () -> game.getPlayer1().getBoard().open(new Position(0, 0)));
        Board target = game.getPlayer2().getBoard();
        target.place(new Position(2, 2), CellType.BOOM);
        target.place(new Position(2, 3), CellType.BOOM);
        target.place(new Position(3, 3), CellType.KISS);
        game.confirmSetup();
        expect(IllegalStateException.class, game::confirmSetup);
        expect(IllegalStateException.class, game::setupPlayer);
        game.attack(new Position(0, 0));
        game.switchTurn(); check(game.getCurrentPlayer() == game.getPlayer2(), "safe attack allows next turn");
        game.switchTurn();
        game.attack(new Position(2, 2));
        check(game.getLoser() == game.getPlayer1() && game.getWinner() == game.getPlayer2(), "chain initiator loses");
        expect(IllegalStateException.class, game::switchTurn);
        expect(IllegalStateException.class, () -> game.attack(new Position(1, 1)));
        expect(IllegalStateException.class, () -> target.open(new Position(1, 1)));
        game.restart();
        check(game.getState() == GameState.SETUP_PLAYER_1 && game.getLoser() == null, "restart clears result");
        check(game.getPlayer1().getBoard().remaining(CellType.SAFE) == 25, "restart creates blank boards");
    }

    private static void testLastKiss() {
        Board board = new Board(new Random() { @Override public boolean nextBoolean() { return true; } });
        board.place(new Position(0, 0), CellType.BOOM);
        board.place(new Position(4, 4), CellType.BOOM);
        board.place(new Position(2, 2), CellType.KISS); ready(board);
        for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++) {
            Position p = new Position(row, col);
            if ((row != 2 || col != 2) && !board.getCell(p).isOpened()) board.open(p);
        }
        check(board.remaining(CellType.SAFE) == 0 && board.remaining(CellType.BOOM) == 0, "only kiss remains");
        check(board.open(new Position(2, 2)).isKissHit(), "opening last kiss loses");
    }

    private static void testPolymorphicOpening() throws Exception {
        Board board = new Board();
        board.randomize(new Random(6)); ready(board);
        Position position = new Position(0, 0);
        // Substitute a new cell behavior; opening must dispatch it without asking getType().
        Cell probe = new Cell(position) {
            @Override CellType getType() { throw new AssertionError("Board.open branched on cell type"); }
            @Override protected void onOpen(Board owner, AttackResult result) { result.markKissHit(); }
        };
        Field grid = Board.class.getDeclaredField("cells"); grid.setAccessible(true);
        ((Cell[][]) grid.get(board))[0][0] = probe;
        check(board.open(position).isKissHit(), "new subclass onOpen dispatched polymorphically");
        check(probe.isOpened(), "template marks cell opened");
    }

    private static void testEncapsulation() throws Exception {
        for (Class<?> type : new Class<?>[] {Cell.class, Board.class, Position.class, AttackResult.class})
            for (Field field : type.getDeclaredFields())
                if (!field.isSynthetic()) check(Modifier.isPrivate(field.getModifiers()), "private field " + type + "." + field.getName());
        for (Field field : Position.class.getDeclaredFields())
            check(Modifier.isFinal(field.getModifiers()), "immutable position field");
        Method open = Cell.class.getDeclaredMethod("open", Board.class, AttackResult.class);
        check(Modifier.isFinal(open.getModifiers()), "template method cannot be overridden");
        Method hook = Cell.class.getDeclaredMethod("onOpen", Board.class, AttackResult.class);
        check(Modifier.isProtected(hook.getModifiers()) && Modifier.isAbstract(hook.getModifiers()), "protected abstract hook");
    }

    private static void ready(Board board) { board.confirmSetup(); board.startPlaying(); }

    private static void testPackagesAndFactories() throws Exception {
        check(Game.class.getPackage().getName().equals("boomboomkiss.model"), "model package");
        check(GameConfig.class.getPackage().getName().equals("boomboomkiss.config"), "config package");
        check(ThemeRepository.class.getPackage().getName().equals("boomboomkiss.repository"), "repository package");
        check(Game.class.getDeclaredField("themeRepository").getType() == ThemeRepository.class,
                "Game depends on repository interface");
        check(CellType.BOOM.create(new Position(0, 0)) instanceof BoomCell, "BOOM factory creates BoomCell");
        check(GameConfig.BLAST_RADIUS == 1, "blast radius from config");
        String[] icons = IconCatalog.choices(CellType.KISS);
        String first = icons[0]; icons[0] = "modified";
        check(IconCatalog.choices(CellType.KISS)[0].equals(first), "icon choices are a defensive copy");
    }

    static void expect(Class<? extends Throwable> type, Runnable operation) {
        try { operation.run(); }
        catch (Throwable error) {
            if (type.isInstance(error)) return;
            throw new AssertionError("Expected " + type.getSimpleName() + " but got " + error, error);
        }
        throw new AssertionError("Expected " + type.getSimpleName());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static final class CountingRandom extends Random {
        private final boolean[] directions;
        private int calls;
        CountingRandom(boolean... directions) { this.directions = directions; }
        @Override public boolean nextBoolean() {
            if (calls >= directions.length) throw new AssertionError("Boom exploded twice");
            return directions[calls++];
        }
    }
}
