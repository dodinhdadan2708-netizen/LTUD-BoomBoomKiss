package week_3;

import java.util.Random;

enum GameState { SETUP_PLAYER_1, SETUP_PLAYER_2, PLAYING, GAME_OVER }

final class Player {
    final String name;
    final Board board = new Board();

    Player(String name) {
        this.name = name;
    }
}

final class Game {
    final Player player1 = new Player("Player 1");
    final Player player2 = new Player("Player 2");
    Player currentPlayer = player1;
    Player loser;
    GameState state = GameState.SETUP_PLAYER_1;
    private final Random random;

    Game() {
        this(new Random());
    }

    Game(Random random) {
        this.random = random;
    }

    void restart() {
        player1.board.clear();
        player2.board.clear();
        currentPlayer = player1;
        loser = null;
        state = GameState.SETUP_PLAYER_1;
    }

    Player setupPlayer() {
        return state == GameState.SETUP_PLAYER_1 ? player1 : player2;
    }

    void confirmSetup() {
        if (state != GameState.SETUP_PLAYER_1 && state != GameState.SETUP_PLAYER_2) {
            throw new IllegalStateException("Setup already finished");
        }
        if (!setupPlayer().board.isSetupComplete()) {
            throw new IllegalStateException("Place 2 bombs and 1 kiss first");
        }
        state = state == GameState.SETUP_PLAYER_1 ? GameState.SETUP_PLAYER_2 : GameState.PLAYING;
    }

    Player opponent() {
        return currentPlayer == player1 ? player2 : player1;
    }

    AttackResult attack(Position position) {
        if (state != GameState.PLAYING) throw new IllegalStateException("Game not started");
        AttackResult result = opponent().board.reveal(position, random);
        if (result.kissHit) {
            loser = currentPlayer;
            state = GameState.GAME_OVER;
        }
        return result;
    }

    void switchTurn() {
        if (state == GameState.PLAYING) currentPlayer = opponent();
    }

    Player getWinner() {
        return loser == null ? null : (loser == player1 ? player2 : player1);
    }
}
