package boomboomkiss.model;

import boomboomkiss.repository.ThemeRepository;

import java.util.Random;

enum GameState { SETUP_PLAYER_1, SETUP_PLAYER_2, PLAYING, GAME_OVER }

class Player {
    private final String name;
    private final Board board;

    Player(String name, Random random) {
        this.name = name;
        this.board = new Board(random);
    }

    String getName() { return name; }
    Board getBoard() { return board; }
}

class Game {
    private Player player1;
    private Player player2;
    private Player currentPlayer;
    private Player loser;
    private GameState state;
    private final Random random;
    private final ThemeRepository themeRepository;
    private final Theme theme;

    Game(ThemeRepository repository) { this(new Random(), repository); }

    Game(Random random, ThemeRepository repository) {
        if (random == null || repository == null) throw new IllegalArgumentException("Missing game dependency");
        this.random = random;
        this.themeRepository = repository;
        this.theme = repository.load();
        if (theme == null) throw new IllegalStateException("Repository returned no theme");
        restart();
    }

    Player getPlayer1() { return player1; }
    Player getPlayer2() { return player2; }
    Player getCurrentPlayer() { return currentPlayer; }
    Player getLoser() { return loser; }
    GameState getState() { return state; }
    Theme getTheme() { return theme; }
    void saveTheme() { themeRepository.save(theme); }

    void restart() {
        if (player1 != null) player1.getBoard().endPlaying();
        if (player2 != null) player2.getBoard().endPlaying();
        player1 = new Player("Người 1", random);
        player2 = new Player("Người 2", random);
        currentPlayer = player1;
        loser = null;
        state = GameState.SETUP_PLAYER_1;
    }

    Player setupPlayer() {
        if (state != GameState.SETUP_PLAYER_1 && state != GameState.SETUP_PLAYER_2)
            throw new IllegalStateException("Setup already finished");
        return state == GameState.SETUP_PLAYER_1 ? player1 : player2;
    }

    void confirmSetup() {
        setupPlayer().getBoard().confirmSetup();
        state = state == GameState.SETUP_PLAYER_1 ? GameState.SETUP_PLAYER_2 : GameState.PLAYING;
        if (state == GameState.PLAYING) {
            player1.getBoard().startPlaying();
            player2.getBoard().startPlaying();
        }
    }

    Player opponent() { return currentPlayer == player1 ? player2 : player1; }

    AttackResult attack(Position position) {
        if (state != GameState.PLAYING) throw new IllegalStateException("Game not playing");
        AttackResult result = opponent().getBoard().open(position);
        if (result.isKissHit()) {
            loser = currentPlayer;
            state = GameState.GAME_OVER;
            player1.getBoard().endPlaying();
            player2.getBoard().endPlaying();
        }
        return result;
    }

    void switchTurn() {
        if (state != GameState.PLAYING) throw new IllegalStateException("Game not playing");
        currentPlayer = opponent();
    }

    Player getWinner() { return loser == null ? null : (loser == player1 ? player2 : player1); }
}
