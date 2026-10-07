package boomboomkiss;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.WindowConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Paths;
import java.util.Random;
import java.util.function.Consumer;

import static boomboomkiss.GameUi.*;

public class Main extends JFrame {
    private enum BoardMode { SETUP, TARGET, OWN }

    private final Game game;
    private final ThemeRepository themeRepository;
    private final Theme theme;
    private final Random placementRandom = new Random();
    private boolean busy;
    private Timer revealTimer;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    Main() {
        this(new Game(), new FileThemeRepository(Paths.get("boom-boom-kiss-theme.properties")));
    }

    Main(Game game, ThemeRepository repository) {
        super("Boom Boom Kiss");
        this.game = game;
        this.themeRepository = repository;
        this.theme = repository.load();
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setMinimumSize(new Dimension(400, 660));
        Rectangle screen = getGraphicsConfiguration().getBounds();
        Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int usableHeight = screen.height - insets.top - insets.bottom;
        setSize(480, Math.min(880, usableHeight - 30));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosed(WindowEvent event) { stopAnimation(); }
        });
        showMenu();
    }

    private JPanel stack() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        return panel;
    }

    private JPanel page(Color background) {
        JPanel panel = new JPanel(new BorderLayout(0, 12));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        return panel;
    }

    private void display(JPanel content) {
        setContentPane(content);
        revalidate();
        repaint();
    }

    private JLabel label(String text, int size, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(font(size, true));
        label.setForeground(color);
        label.setAlignmentX(CENTER_ALIGNMENT);
        return label;
    }

    private JLabel wrapped(String text, int width, int size, Color color) {
        return label("<html><div style='width:" + width + "px;text-align:center'>" +
                escapeHtml(text) + "</div></html>", size, color);
    }

    private void gap(JPanel panel, int size) { panel.add(Box.createVerticalStrut(size)); }

    private GameUi.Button button(String text, Color color, Runnable action) {
        GameUi.Button button = new GameUi.Button(text, color, Color.WHITE, 24);
        button.setAlignmentX(CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 51));
        button.addActionListener(event -> action.run());
        return button;
    }

    private GameUi.Button iconButton(String icon, String accessibleName, int size, Color color) {
        GameUi.Button button = new GameUi.Button("", color, INK, 22);
        button.setIcon(IconCatalog.icon(icon, size));
        button.setDisabledIcon(IconCatalog.icon(icon, size));
        button.getAccessibleContext().setAccessibleName(accessibleName);
        button.setPreferredSize(new Dimension(80, 70));
        return button;
    }

    private JPanel heading(String title, String subtitle, Color textColor) {
        JPanel panel = new JPanel(new BorderLayout(10, 6));
        panel.setOpaque(false);
        JPanel text = stack();
        text.add(label(title, 25, textColor));
        gap(text, 4);
        if (!subtitle.isEmpty()) text.add(label(subtitle, 12, textColor));
        panel.add(text, BorderLayout.CENTER);
        GameUi.Button exit = new GameUi.Button("\u00d7", new Color(255, 255, 255, 180), INK, 20);
        exit.setFont(font(24, true));
        exit.setMargin(new Insets(0, 0, 0, 0));
        exit.setPreferredSize(new Dimension(36, 36));
        exit.getAccessibleContext().setAccessibleName("Về trang chủ");
        exit.addActionListener(event -> showMenu());
        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        right.setOpaque(false); right.add(exit);
        panel.add(right, BorderLayout.EAST);
        return panel;
    }

    private JPanel logo(int size) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        panel.setOpaque(false);
        for (String id : new String[] {theme.boomIcon, theme.kissIcon, theme.boomIcon}) {
            JLabel icon = new JLabel(IconCatalog.icon(id, size));
            panel.add(icon);
        }
        panel.setMaximumSize(new Dimension(360, size + 6));
        return panel;
    }

    private void stopAnimation() {
        if (revealTimer != null) revealTimer.stop();
        busy = false;
    }

    private void showMenu() {
        stopAnimation();
        JPanel root = page(PAPER);
        JPanel header = stack();
        header.add(label("Boom Boom Kiss", 31, INK));
        gap(header, 5);
        header.add(label("2 người cùng máy  \u00b7  Lưới 5 \u00d7 5", 13, MUTED));
        root.add(header, BorderLayout.NORTH);

        Card hero = new Card(new Color(250, 222, 204), 32);
        hero.setLayout(new BorderLayout(0, 15));
        hero.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        JPanel branding = stack();
        JPanel logo = logo(53); branding.add(logo); gap(branding, 8);
        branding.add(label("BOOM BOOM", 28, INK));
        branding.add(label("KISS", 32, RED));
        hero.add(branding, BorderLayout.NORTH);

        Card settings = new Card(Color.WHITE, 26);
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        settings.add(label("Theme của bạn", 23, INK));
        gap(settings, 7);
        settings.add(label("Bấm biểu tượng để chọn", 13, MUTED));
        gap(settings, 16);
        JPanel choices = new JPanel(new GridLayout(1, 3, 8, 0));
        choices.setOpaque(false);
        choices.setMaximumSize(new Dimension(Integer.MAX_VALUE, 76));
        Color[] colors = {new Color(232, 246, 237), new Color(255, 241, 218), new Color(253, 231, 236)};
        for (CellType type : CellType.values()) {
            GameUi.Button choice = iconButton(theme.iconFor(type), "Đổi icon " + type.name(), 41, colors[type.ordinal()]);
            choice.addActionListener(event -> showIconPicker(type, icon -> {
                theme.setIcon(type, icon);
                choice.setIcon(IconCatalog.icon(icon, 41));
                choice.setDisabledIcon(choice.getIcon());
                JPanel newLogo = logo(53);
                // Replace the first row so text entered in the punishment field stays intact.
                branding.remove(0);
                branding.add(newLogo, 0);
                branding.revalidate(); branding.repaint();
                saveTheme();
            }));
            choices.add(choice);
        }
        settings.add(choices);
        gap(settings, 23);
        settings.add(label("Hành động khi thua", 15, INK));
        gap(settings, 10);
        JTextField punishment = new JTextField(theme.loseText);
        punishment.setFont(font(15, false));
        punishment.setForeground(INK);
        punishment.setBackground(PAPER);
        punishment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PAPER, 1, true),
                BorderFactory.createEmptyBorder(11, 10, 11, 10)));
        punishment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));
        punishment.getAccessibleContext().setAccessibleName("Punishment text");
        punishment.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { theme.loseText = punishment.getText(); }
            public void insertUpdate(DocumentEvent event) { update(); }
            public void removeUpdate(DocumentEvent event) { update(); }
            public void changedUpdate(DocumentEvent event) { update(); }
        });
        settings.add(punishment);
        gap(settings, 22);
        settings.add(button("CHƠI", GREEN, () -> {
            if (theme.loseText.trim().isEmpty()) theme.loseText = "Kiss your opponent!";
            saveTheme();
            game.restart();
            showSetup();
        }));
        gap(settings, 12);
        settings.add(label("2 boom + 1 kiss trên mỗi bàn", 12, MUTED));
        hero.add(settings, BorderLayout.CENTER);
        JScrollPane scroll = new JScrollPane(hero);
        scroll.setBorder(null); scroll.setOpaque(false); scroll.getViewport().setOpaque(false);
        scroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scroll.getVerticalScrollBar().setUnitIncrement(20);
        root.add(scroll, BorderLayout.CENTER);
        root.add(label("Mở trúng kiss trực tiếp hoặc do nổ lan là thua.", 11, MUTED), BorderLayout.SOUTH);
        display(root);
    }

    private void saveTheme() {
        try { themeRepository.save(theme); }
        catch (IllegalStateException exception) {
            JOptionPane.showMessageDialog(this, "Chưa lưu được theme. Bạn vẫn có thể chơi ván này.",
                    "Lưu theme", JOptionPane.WARNING_MESSAGE);
        }
    }

    private JDialog picker(JPanel content) {
        JDialog dialog = new JDialog(this, false);
        dialog.setUndecorated(true);
        content.setBorder(BorderFactory.createEmptyBorder(16, 16, 16, 16));
        dialog.setContentPane(content);
        dialog.getRootPane().registerKeyboardAction(event -> dialog.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);
        dialog.addWindowFocusListener(new WindowAdapter() {
            @Override public void windowLostFocus(WindowEvent event) { dialog.dispose(); }
        });
        dialog.pack();
        dialog.setLocationRelativeTo(this);
        return dialog;
    }

    private void showIconPicker(CellType type, Consumer<String> onSelect) {
        JDialog dialog = themePicker(type, onSelect);
        dialog.setVisible(true);
    }

    private JDialog themePicker(CellType type, Consumer<String> onSelect) {
        JPanel palette = new JPanel(new GridLayout(2, 3, 10, 10));
        palette.setBackground(Color.WHITE);
        JDialog dialog = picker(palette);
        for (String icon : IconCatalog.choices(type)) {
            GameUi.Button choice = iconButton(icon, "Chọn " + icon, 45, PAPER);
            if (icon.equals(theme.iconFor(type))) choice.setOutline(RED);
            choice.addActionListener(event -> { dialog.dispose(); onSelect.accept(icon); });
            palette.add(choice);
        }
        dialog.pack(); dialog.setLocationRelativeTo(this); return dialog;
    }

    private void showSetup() {
        Player player = game.setupPlayer();
        Board board = player.board;
        Color color = player == game.player1 ? PEACH : BLUE;
        JPanel root = page(color);
        root.add(heading("Người " + (player == game.player1 ? "1" : "2") + " đặt bánh",
                "Bấm một ô để chọn biểu tượng", Color.WHITE), BorderLayout.NORTH);
        root.add(boardPanel(board, BoardMode.SETUP, player), BorderLayout.CENTER);
        JPanel footer = stack();
        Card countCard = new Card(new Color(255, 255, 255, 120), 22);
        countCard.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 6));
        countCard.add(counter(theme.boomIcon, board.count(CellType.BOMB) + "/2", 27, INK));
        countCard.add(counter(theme.kissIcon, board.count(CellType.KISS) + "/1", 27, INK));
        countCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 48));
        footer.add(countCard); gap(footer, 12);
        JPanel tools = new JPanel(new GridLayout(1, 2, 9, 0)); tools.setOpaque(false);
        tools.add(button("NGẪU NHIÊN", NAVY, () -> { board.randomize(placementRandom); showSetup(); }));
        tools.add(button("XÓA HẾT", INK, () -> { board.clear(); showSetup(); }));
        tools.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49)); footer.add(tools);
        gap(footer, 10);
        GameUi.Button save = button("LƯU BÀN", GREEN, () -> {
            game.confirmSetup();
            if (game.state == GameState.SETUP_PLAYER_2) {
                showHandoff("Người 1 đã sẵn sàng!", "Đưa máy cho Người 2. Đừng nhìn nhé!",
                        "TIẾP TỤC", this::showSetup);
            } else {
                showHandoff("Đã đặt bánh xong!", "Đưa máy lại cho Người 1 để bắt đầu.", "BẮT ĐẦU", this::showPlay);
            }
        });
        save.setEnabled(board.isSetupComplete()); footer.add(save);
        gap(footer, 8);
        footer.add(label(board.isSetupComplete() ? "Đủ bánh. Bấm lưu để che bàn." :
                "Đặt đủ 2 boom và 1 kiss để lưu.", 12, INK));
        root.add(footer, BorderLayout.SOUTH);
        display(root);
    }

    private void showCellPicker(Board board, Position position) {
        JDialog dialog = cellPicker(board, position);
        dialog.setVisible(true);
    }

    private JDialog cellPicker(Board board, Position position) {
        JPanel options = new JPanel(new GridLayout(1, 3, 10, 0));
        options.setBackground(Color.WHITE);
        JDialog dialog = picker(options);
        CellType current = board.getCell(position).getType();
        for (CellType type : CellType.values()) {
            GameUi.Button choice = iconButton(theme.iconFor(type), "Đặt " + type.name(), 42, PAPER);
            boolean available = current == type || type == CellType.SAFE ||
                    board.count(type) < (type == CellType.BOMB ? GameConfig.BOMB_COUNT : GameConfig.KISS_COUNT);
            choice.setEnabled(available);
            if (current == type) choice.setOutline(RED);
            choice.addActionListener(event -> {
                CellType existing = board.getCell(position).getType();
                // Recheck because the modeless picker can outlive an earlier board edit.
                if (type != CellType.SAFE && existing != type &&
                        board.count(type) >= (type == CellType.BOMB ? GameConfig.BOMB_COUNT : GameConfig.KISS_COUNT)) {
                    dialog.dispose(); showSetup(); return;
                }
                if (board.getCell(position).getType() != type) {
                    board.remove(position);
                    if (type != CellType.SAFE) board.place(position, type);
                }
                dialog.dispose(); showSetup();
            });
            options.add(choice);
        }
        dialog.pack(); dialog.setLocationRelativeTo(this); return dialog;
    }

    private JPanel boardPanel(Board board, BoardMode mode, Player owner) {
        boolean blue = owner == game.player2;
        Color well = blue ? new Color(82, 128, 172) : new Color(212, 145, 115);
        Color hidden = blue ? new Color(61, 98, 135) : new Color(132, 93, 77);
        boolean target = mode == BoardMode.TARGET;
        GameUi.SquareBoard grid = new GameUi.SquareBoard(well, target || mode == BoardMode.SETUP);
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                Position position = new Position(row, col);
                Cell cell = board.getCell(position);
                boolean visible = mode == BoardMode.SETUP ? cell.getType() != CellType.SAFE : cell.isRevealed();
                boolean clickable = mode == BoardMode.SETUP || target && !busy &&
                        game.state == GameState.PLAYING && !cell.isRevealed();
                Color tileColor = hidden;
                if (visible && cell.getType() == CellType.KISS) tileColor = new Color(252, 222, 230);
                else if (visible && cell.getType() == CellType.BOMB) tileColor = new Color(255, 229, 185);
                GameUi.Tile tile = new GameUi.Tile(visible ? theme.iconFor(cell.getType()) : null,
                        tileColor, cell.isRevealed(), clickable);
                tile.setBackground(tileColor);
                tile.getAccessibleContext().setAccessibleName((blue ? "P2" : "P1") + " ô " +
                        (row + 1) + "," + (col + 1) + (cell.isRevealed() ? " đã mở" : ""));
                if (mode == BoardMode.SETUP) tile.addActionListener(event -> showCellPicker(board, position));
                else if (clickable) tile.addActionListener(event -> attack(position));
                grid.addTile(tile);
            }
        }
        return grid;
    }

    private void showHandoff(String title, String message, String command, Runnable next) {
        JPanel root = page(Color.WHITE);
        JPanel center = stack();
        center.add(Box.createVerticalGlue());
        center.add(label(title, 26, INK)); gap(center, 18);
        center.add(wrapped(message, 290, 18, MUTED)); gap(center, 36);
        center.add(button(command, GREEN, next));
        center.add(Box.createVerticalGlue());
        root.add(center, BorderLayout.CENTER);
        display(root);
    }

    private void showPlay() {
        JPanel root = page(PAPER);
        JPanel top = stack();
        top.add(heading("Lượt Người " + (game.currentPlayer == game.player1 ? "1" : "2"),
                "Chọn một ô trên bàn đối thủ", INK));
        gap(top, 8); top.add(scoreboard());
        root.add(top, BorderLayout.NORTH);
        JPanel boards = new JPanel(new GridLayout(2, 1, 0, 10)); boards.setOpaque(false);
        boards.add(boardSection(game.player2, BLUE));
        boards.add(boardSection(game.player1, PEACH));
        root.add(boards, BorderLayout.CENTER);
        display(root);
    }

    private JPanel boardSection(Player owner, Color color) {
        boolean target = owner == game.opponent();
        Card section = new Card(color, 25);
        section.setBorder(BorderFactory.createEmptyBorder(7, 6, 6, 6));
        section.setLayout(new BorderLayout(0, 3));
        JLabel title = label("Bàn Người " + (owner == game.player1 ? "1" : "2") +
                (target ? "  \u00b7  Chọn tại đây" : ""), 13, Color.WHITE);
        section.add(title, BorderLayout.NORTH);
        section.add(boardPanel(owner.board, target ? BoardMode.TARGET : BoardMode.OWN, owner), BorderLayout.CENTER);
        return section;
    }

    private JPanel scoreboard() {
        JPanel scores = new JPanel(new GridLayout(2, 1, 0, 3));
        scores.setOpaque(false); scores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        scores.add(scoreRow(game.player1, "P1")); scores.add(scoreRow(game.player2, "P2"));
        return scores;
    }

    private JPanel scoreRow(Player player, String name) {
        boolean active = player == game.currentPlayer;
        Card row = new Card(active ? Color.WHITE : new Color(224, 230, 239), 14);
        row.setLayout(new GridLayout(1, 4, 0, 0));
        row.setPreferredSize(new Dimension(380, 31));
        row.add(label(name + (active ? " \u2022" : ""), 12, active ? INK : MUTED));
        for (CellType type : CellType.values())
            row.add(counter(theme.iconFor(type), "\u00d7" + player.board.remaining(type), 20, active ? INK : MUTED));
        return row;
    }

    private JLabel counter(String icon, String text, int size, Color color) {
        JLabel label = label(text, 14, color);
        label.setIcon(IconCatalog.icon(icon, size)); label.setIconTextGap(7);
        return label;
    }

    private void attack(Position position) {
        if (busy || game.state != GameState.PLAYING) return;
        busy = true;
        AttackResult result = game.attack(position);
        showPlay();
        revealTimer = new Timer(330, event -> {
            busy = false;
            if (result.kissHit) showGameOver();
            else { game.switchTurn(); showPlay(); }
        });
        revealTimer.setRepeats(false); revealTimer.start();
    }

    private void showGameOver() {
        JPanel root = page(new Color(49, 55, 71));
        Card result = new Card(Color.WHITE, 32);
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        result.setBorder(BorderFactory.createEmptyBorder(24, 26, 24, 26));
        result.add(Box.createVerticalGlue());
        JLabel kiss = new JLabel(IconCatalog.icon(theme.kissIcon, 94)); kiss.setAlignmentX(CENTER_ALIGNMENT);
        result.add(kiss); gap(result, 20);
        result.add(label("BOOM! Trúng kiss rồi!", 25, RED)); gap(result, 10);
        result.add(label("Người " + (game.loser == game.player1 ? "1" : "2") + " thua.", 20, INK));
        gap(result, 6);
        result.add(label("Người " + (game.getWinner() == game.player1 ? "1" : "2") + " thắng!", 15, MUTED));
        gap(result, 25);
        Card action = new Card(new Color(255, 244, 211), 21);
        action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));
        action.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
        action.add(label("HÀNH ĐỘNG KHI THUA", 12, new Color(163, 121, 48)));
        gap(action, 9); action.add(wrapped(theme.loseText, 250, 18, INK));
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190)); result.add(action);
        gap(result, 30);
        result.add(button("CHƠI LẠI", GREEN, () -> { stopAnimation(); game.restart(); showSetup(); }));
        gap(result, 10); result.add(button("TRANG CHỦ", NAVY, this::showMenu));
        result.add(Box.createVerticalGlue());
        root.add(result, BorderLayout.CENTER); display(root);
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
