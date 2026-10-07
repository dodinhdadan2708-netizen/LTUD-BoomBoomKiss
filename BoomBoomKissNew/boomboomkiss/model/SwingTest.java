package boomboomkiss.model;

import boomboomkiss.repository.ThemeRepository;
import boomboomkiss.repository.MemoryThemeRepository;

import javax.imageio.ImageIO;
import javax.swing.AbstractButton;
import javax.swing.JDialog;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/** Tests Swing components inside the app; no mouse/keyboard or desktop capture. */
public final class SwingTest {
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(() -> {
            Main frame = null;
            try {
                Path previews = Paths.get("boomboomkiss", "build", "previews");
                Files.createDirectories(previews);
                Theme saved = new Theme();
                saved.setLoseText("Thua rồi! Hôn đối phương một cái.");
                ThemeRepository memory = new ThemeRepository() {
                    public Theme load() { return saved; }
                    public void save(Theme theme) { }
                };
                Game game = new Game(memory);
                frame = new Main(game);
                frame.setSize(480, 850);
                layout(frame);
                check(find(frame.getContentPane(), JTextField.class).size() == 1, "only punishment is editable text");
                check(saved.getLoseText().equals(find(frame.getContentPane(), JTextField.class).get(0).getText()),
                        "existing punishment visible");
                Method themePicker = Main.class.getDeclaredMethod("themePicker", CellType.class, Consumer.class);
                themePicker.setAccessible(true);
                for (CellType type : CellType.values()) {
                    JDialog palette = (JDialog) themePicker.invoke(frame, type, (Consumer<String>) id -> saved.setIcon(type, id));
                    layout(palette);
                    List<AbstractButton> icons = find(palette.getContentPane(), AbstractButton.class);
                    check(icons.size() == 6 && find(palette.getContentPane(), JTextField.class).isEmpty(), "icon-only theme picker");
                    for (AbstractButton option : icons) check(option.getText().isEmpty() && option.getIcon() != null, "no letters in picker");
                    if (type == CellType.KISS) snapshot((JComponent) palette.getContentPane(), previews.resolve("07-theme-picker.png"));
                    icons.get(0).doClick(); palette.dispose();
                }
                check(saved.getLoseText().equals("Thua rồi! Hôn đối phương một cái."), "icon picker preserves punishment");
                snapshot(frame, previews.resolve("01-menu.png"));

                call(frame, "showSetup"); layout(frame);
                List<GameUi.Tile> setup = find(frame.getContentPane(), GameUi.Tile.class);
                check(setup.size() == 25, "25 setup cells");
                check(setup.get(0).isEnabled() && setup.get(0).getActionListeners().length == 1, "cell opens picker");
                check(!button(frame, "LƯU BÀN").isEnabled(), "save disabled until all pieces placed");
                game.getPlayer1().getBoard().randomize(new java.util.Random(12));
                call(frame, "showSetup"); layout(frame);
                check(button(frame, "LƯU BÀN").isEnabled(), "complete setup can save");
                Method cellPicker = Main.class.getDeclaredMethod("cellPicker", Board.class, Position.class);
                cellPicker.setAccessible(true);
                Position occupied = null;
                for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++) {
                    Position position = new Position(row, col);
                    if (game.getPlayer1().getBoard().getCell(position).getType() == CellType.BOOM) occupied = position;
                }
                JDialog picker = (JDialog) cellPicker.invoke(frame, game.getPlayer1().getBoard(), occupied);
                layout(picker);
                List<AbstractButton> symbols = find(picker.getContentPane(), AbstractButton.class);
                check(symbols.size() == 3 && symbols.get(0).isEnabled() && symbols.get(1).isEnabled() &&
                        !symbols.get(2).isEnabled(), "cell picker respects piece limits");
                snapshot((JComponent) picker.getContentPane(), previews.resolve("08-cell-picker.png"));
                picker.dispose();
                snapshot(frame, previews.resolve("02-setup.png"));
                button(frame, "LƯU BÀN").doClick(); layout(frame);
                check(find(frame.getContentPane(), GameUi.Tile.class).isEmpty(), "handoff hides all cells");
                snapshot(frame, previews.resolve("03-handoff.png"));
                button(frame, "TIẾP TỤC").doClick();
                game.getPlayer2().getBoard().randomize(new java.util.Random(13));
                call(frame, "showSetup");
                button(frame, "LƯU BÀN").doClick();
                button(frame, "BẮT ĐẦU").doClick(); layout(frame);

                List<GameUi.Tile> tiles = find(frame.getContentPane(), GameUi.Tile.class);
                check(tiles.size() == 50, "both 5x5 boards visible");
                check(enabled(tiles) == 25, "only opponent board selectable");
                check(tiles.get(0).getAccessibleContext().getAccessibleName().startsWith("P2"), "P2 is always top");
                check(tiles.get(25).getAccessibleContext().getAccessibleName().startsWith("P1"), "P1 is always bottom");
                for (GameUi.Tile tile : tiles) check(tile.getIcon() == null && tile.getText().isEmpty(), "no leaked text/icon");
                snapshot(frame, previews.resolve("04-play.png"));
                List<GameUi.SquareBoard> firstBoards = find(frame.getContentPane(), GameUi.SquareBoard.class);
                check(firstBoards.get(0).getHeight() >= 160 && firstBoards.get(1).getHeight() >= 160, "two readable boards");

                game.switchTurn(); call(frame, "showPlay"); layout(frame);
                tiles = find(frame.getContentPane(), GameUi.Tile.class);
                check(!tiles.get(0).isEnabled() && tiles.get(25).isEnabled(), "target switches without flipping boards");
                frame.setSize(400, 660); layout(frame);
                snapshot(frame, previews.resolve("05-play-small.png"));
                for (GameUi.SquareBoard board : find(frame.getContentPane(), GameUi.SquareBoard.class))
                    check(board.getHeight() >= 140, "board fits minimum window");

                game.switchTurn();
                Position kiss = null;
                for (int row = 0; row < 5; row++) for (int col = 0; col < 5; col++) {
                    Position position = new Position(row, col);
                    if (game.getPlayer2().getBoard().getCell(position).getType() == CellType.KISS) kiss = position;
                }
                game.attack(kiss);
                frame.setSize(480, 850); call(frame, "showGameOver"); layout(frame);
                snapshot(frame, previews.resolve("06-result.png"));
                check(saved.getLoseText().equals("Thua rồi! Hôn đối phương một cái."), "punishment preserved throughout game");
                button(frame, "CHƠI LẠI").doClick();
                check(game.getState() == GameState.SETUP_PLAYER_1, "play again returns to setup");
                System.out.println("Swing tests passed; previews saved to " + previews.toAbsolutePath());
            } catch (Exception exception) {
                throw new RuntimeException(exception);
            } finally {
                if (frame != null) frame.dispose();
            }
        });
        testAutomaticTurns();
    }

    private static void testAutomaticTurns() throws Exception {
        AtomicReference<Main> frame = new AtomicReference<Main>();
        Game game = new Game(new MemoryThemeRepository());
        CountDownLatch completed = new CountDownLatch(1);
        AtomicReference<Throwable> failure = new AtomicReference<Throwable>();
        SwingUtilities.invokeAndWait(() -> {
            try {
                for (Player player : new Player[] {game.getPlayer1(), game.getPlayer2()}) {
                    player.getBoard().place(new Position(4, 4), CellType.BOOM);
                    player.getBoard().place(new Position(4, 3), CellType.BOOM);
                    player.getBoard().place(new Position(0, 0), CellType.KISS);
                }
                game.confirmSetup(); game.confirmSetup();
                Main app = new Main(game);
                frame.set(app); call(app, "showPlay"); layout(app);
                List<GameUi.Tile> tiles = find(app.getContentPane(), GameUi.Tile.class);
                tiles.get(12).doClick(); // Safe cell on P2.
                check(game.getCurrentPlayer() == game.getPlayer1() && game.getPlayer2().getBoard().getCell(new Position(2, 2)).isOpened(),
                        "clicked cell revealed before changing turn");
                check(enabled(find(app.getContentPane(), GameUi.Tile.class)) == 0, "double clicks blocked during reveal");
                javax.swing.Timer afterSafe = new javax.swing.Timer(600, event -> {
                    try {
                        check(game.getCurrentPlayer() == game.getPlayer2(), "turn changed automatically");
                        List<GameUi.Tile> next = find(app.getContentPane(), GameUi.Tile.class);
                        check(!next.get(0).isEnabled() && next.get(25).isEnabled(), "P2 selects fixed bottom board");
                        next.get(25).doClick(); // Kiss on P1: P2 loses.
                        javax.swing.Timer afterKiss = new javax.swing.Timer(600, ignored -> {
                            try {
                                check(game.getLoser() == game.getPlayer2() && game.getState() == GameState.GAME_OVER,
                                        "kiss click loses for current attacker");
                                check(button(app, "CHƠI LẠI") != null, "result shown automatically");
                            } catch (Throwable error) { failure.set(error); }
                            finally { app.dispose(); completed.countDown(); }
                        });
                        afterKiss.setRepeats(false); afterKiss.start();
                    } catch (Throwable error) { failure.set(error); app.dispose(); completed.countDown(); }
                });
                afterSafe.setRepeats(false); afterSafe.start();
            } catch (Throwable error) {
                failure.set(error); if (frame.get() != null) frame.get().dispose(); completed.countDown();
            }
        });
        if (!completed.await(8, TimeUnit.SECONDS)) {
            SwingUtilities.invokeAndWait(() -> { if (frame.get() != null) frame.get().dispose(); });
            throw new AssertionError("Automatic turn test timed out");
        }
        if (failure.get() != null) throw new AssertionError("Automatic turn test failed", failure.get());
        System.out.println("Automatic turn and game-over tests passed");
    }

    private static void call(Main frame, String name) throws Exception {
        Method method = Main.class.getDeclaredMethod(name);
        method.setAccessible(true); method.invoke(frame);
    }

    private static int enabled(List<GameUi.Tile> tiles) {
        int count = 0; for (GameUi.Tile tile : tiles) if (tile.isEnabled()) count++; return count;
    }

    private static AbstractButton button(Main frame, String text) {
        for (AbstractButton button : find(frame.getContentPane(), AbstractButton.class))
            if (text.equals(button.getText())) return button;
        throw new AssertionError("Missing button: " + text);
    }

    private static <T> List<T> find(Container parent, Class<T> type) {
        List<T> result = new ArrayList<T>();
        for (Component child : parent.getComponents()) {
            if (type.isInstance(child)) result.add(type.cast(child));
            if (child instanceof Container) result.addAll(find((Container) child, type));
        }
        return result;
    }

    private static void layout(Container container) {
        container.doLayout();
        for (Component child : container.getComponents()) if (child instanceof Container) layout((Container) child);
    }

    private static void snapshot(Main frame, Path output) throws Exception {
        JComponent content = (JComponent) frame.getContentPane();
        snapshot(content, output);
    }

    private static void snapshot(JComponent content, Path output) throws Exception {
        BufferedImage image = new BufferedImage(content.getWidth(), content.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics(); content.printAll(graphics); graphics.dispose();
        ImageIO.write(image, "png", output.toFile());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
