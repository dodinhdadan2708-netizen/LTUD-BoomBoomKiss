package boomboomkiss;

import java.util.Random;

public final class GameTest {
    public static void main(String[] args) {
        testSetup();
        testHorizontalThenVerticalChain();
        testCornerAndAlreadyOpenedCell();
        testTurnAndLoser();
        testBlastLoserAndLastKiss();
        System.out.println("Game tests passed");
    }

    private static void testSetup() {
        Board board = new Board();
        check(board.place(new Position(2, 2), CellType.BOMB), "first bomb");
        check(!board.place(new Position(2, 2), CellType.KISS), "occupied cell rejected");
        check(!board.isSetupComplete(), "incomplete setup rejected");
        check(board.place(new Position(2, 3), CellType.BOMB), "second bomb");
        check(board.place(new Position(3, 3), CellType.KISS), "kiss");
        check(board.isSetupComplete(), "setup complete");
        board.remove(new Position(3, 3));
        check(board.getCell(new Position(3, 3)).getType() == CellType.SAFE, "clear selected cell");
        check(board.place(new Position(1, 1), CellType.KISS), "replace with kiss");
        check(board.isSetupComplete(), "setup remains complete after edit");
        check(!board.place(new Position(1, 1), CellType.BOMB), "occupied cell replacement blocked");
        check(board.getCell(new Position(1, 1)).getType() == CellType.KISS, "failed edit preserves original");
        check(!board.place(new Position(0, 0), CellType.BOMB), "bomb limit");
        check(board.remaining(CellType.SAFE) == 22 && board.remaining(CellType.BOMB) == 2 &&
                board.remaining(CellType.KISS) == 1, "initial counters");
        Game game = new Game();
        boolean incompleteRejected = false;
        try {
            game.confirmSetup();
        } catch (IllegalStateException expected) {
            incompleteRejected = true;
        }
        check(incompleteRejected, "game rejects incomplete setup");
    }

    private static void testHorizontalThenVerticalChain() {
        CountingRandom random = new CountingRandom(true, false);
        Board board = new Board(random);
        board.place(new Position(2, 2), CellType.BOMB);
        board.place(new Position(2, 3), CellType.BOMB);
        board.place(new Position(3, 3), CellType.KISS);
        AttackResult blast = board.reveal(new Position(2, 2));
        check(blast.kissHit, "chain blast reaches kiss");
        check(blast.opened.size() == 5, "only horizontal then vertical cells opened");
        check(!board.getCell(new Position(1, 2)).isRevealed(), "first boom did not blast vertically");
        check(board.getCell(new Position(3, 3)).isRevealed(), "kiss revealed by second boom");
        check(board.remaining(CellType.BOMB) == 0 && board.remaining(CellType.KISS) == 0 &&
                board.remaining(CellType.SAFE) == 20, "counters after chain blast");
        check(random.calls == 2, "each boom explodes once");
        boolean repeatedAttackRejected = false;
        try {
            board.reveal(new Position(2, 2));
        } catch (IllegalArgumentException expected) {
            repeatedAttackRejected = true;
        }
        check(repeatedAttackRejected, "repeated attack rejected");
    }

    private static void testCornerAndAlreadyOpenedCell() {
        Board board = new Board(new CountingRandom(true));
        board.place(new Position(0, 0), CellType.BOMB);
        board.place(new Position(4, 4), CellType.BOMB);
        board.place(new Position(3, 3), CellType.KISS);
        board.reveal(new Position(0, 1));
        AttackResult corner = board.reveal(new Position(0, 0));
        check(corner.opened.size() == 1, "opened neighbor skipped at corner");
        check(!corner.kissHit, "corner blast stays inside board");
        check(board.getCell(new Position(0, 1)).isRevealed(), "previously opened cell remains open");

        Board edge = new Board(new CountingRandom(false));
        edge.place(new Position(0, 2), CellType.BOMB);
        edge.place(new Position(4, 4), CellType.BOMB);
        edge.place(new Position(3, 3), CellType.KISS);
        AttackResult vertical = edge.reveal(new Position(0, 2));
        check(vertical.opened.size() == 2 && edge.getCell(new Position(1, 2)).isRevealed(),
                "vertical edge blast clipped correctly");
    }

    private static void testTurnAndLoser() {
        Game game = new Game(new CountingRandom(true));
        game.player1.board.randomize(new Random(1));
        game.confirmSetup();
        check(game.state == GameState.SETUP_PLAYER_2, "player 2 setup");
        game.player2.board.place(new Position(4, 4), CellType.BOMB);
        game.player2.board.place(new Position(4, 3), CellType.BOMB);
        game.player2.board.place(new Position(0, 0), CellType.KISS);
        game.confirmSetup();
        game.attack(new Position(2, 2));
        game.switchTurn();
        check(game.currentPlayer == game.player2, "safe attack changes turn");
        game.switchTurn();
        game.attack(new Position(0, 0));
        check(game.state == GameState.GAME_OVER, "game over on kiss");
        check(game.loser == game.player1 && game.getWinner() == game.player2, "winner and loser");
        game.restart();
        check(game.state == GameState.SETUP_PLAYER_1 && game.loser == null, "restart");
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }

    private static void testBlastLoserAndLastKiss() {
        Game game = new Game(new CountingRandom(true, false));
        game.player1.board.randomize(new Random(10));
        game.confirmSetup();
        game.player2.board.place(new Position(2, 2), CellType.BOMB);
        game.player2.board.place(new Position(2, 3), CellType.BOMB);
        game.player2.board.place(new Position(3, 3), CellType.KISS);
        game.confirmSetup();
        game.attack(new Position(2, 2));
        check(game.loser == game.player1 && game.getWinner() == game.player2, "chain initiator loses");
        game.switchTurn();
        check(game.currentPlayer == game.player1, "no turn change after game over");
        boolean rejected = false;
        try { game.attack(new Position(0, 0)); }
        catch (IllegalStateException expected) { rejected = true; }
        check(rejected, "no further attacks after game over");

        Random horizontal = new Random() { @Override public boolean nextBoolean() { return true; } };
        Board last = new Board(horizontal);
        last.place(new Position(0, 0), CellType.BOMB);
        last.place(new Position(4, 4), CellType.BOMB);
        last.place(new Position(2, 2), CellType.KISS);
        for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++) {
            Position p = new Position(row, col);
            if ((row != 2 || col != 2) && !last.getCell(p).isRevealed()) last.reveal(p);
        }
        check(last.remaining(CellType.SAFE) == 0 && last.remaining(CellType.BOMB) == 0, "only kiss left");
        check(last.reveal(new Position(2, 2)).kissHit, "last remaining kiss loses");
    }

    private static final class CountingRandom extends Random {
        private final boolean[] horizontal;
        int calls;

        CountingRandom(boolean... horizontal) {
            this.horizontal = horizontal;
        }

        @Override
        public boolean nextBoolean() {
            if (calls >= horizontal.length) throw new AssertionError("unexpected explosion");
            return horizontal[calls++];
        }
    }
}
