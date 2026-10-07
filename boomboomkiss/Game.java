package boomboomkiss;

import java.util.Random;

enum GameState { SETUP_PLAYER_1, SETUP_PLAYER_2, PLAYING, GAME_OVER }

class Player {
    String name;
    Board board;

    Player(String name) {
        this(name, new Random());
    }

    Player(String name, Random random) {
        this.name = name;
        this.board = new Board(random);
    }
}

class Game {
    Player player1;
    Player player2;
    Player currentPlayer;
    Player loser;
    GameState state = GameState.SETUP_PLAYER_1;

    Game() {
        this(new Random());
    }

    Game(Random random) {
        player1 = new Player("Người 1", random);
        player2 = new Player("Người 2", random);
        currentPlayer = player1;
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
        AttackResult result = opponent().board.reveal(position);
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
