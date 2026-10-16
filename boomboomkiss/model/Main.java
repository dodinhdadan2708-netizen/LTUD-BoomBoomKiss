package boomboomkiss.model;

import boomboomkiss.config.GameConfig;
import boomboomkiss.config.UiStyle;
import boomboomkiss.repository.MemoryThemeRepository;
import boomboomkiss.repository.FileThemeRepository;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
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

import static boomboomkiss.model.GameUi.*;

public class Main extends JFrame {
    static { UiFonts.install(); }
    private enum BoardMode { SETUP, TARGET, OWN }

    private final Game game;
    private final Theme theme;
    private final Random placementRandom = new Random();
    private boolean busy;
    private Timer revealTimer;
    private Timer fadeTimer;
    private Board animationBoard;
    private java.util.List<Position> animationPositions = java.util.Collections.emptyList();
    private final java.util.List<GameUi.Tile> animatedTiles = new java.util.ArrayList<GameUi.Tile>();
    private JPanel resultOverlay;
    private final java.util.Map<CellType, String> sessionLabels = new java.util.EnumMap<CellType, String>(CellType.class);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { new Main().setVisible(true); }
            catch (IllegalStateException error) {
                JOptionPane.showMessageDialog(null, "Không đọc được theme đã lưu. Game sẽ dùng theme mặc định.\n" +
                        error.getMessage(), "Đọc theme", JOptionPane.WARNING_MESSAGE);
                new Main(new Game(new MemoryThemeRepository())).setVisible(true);
            }
        });
    }

    Main() {
        this(new Game(new FileThemeRepository(Paths.get("boom-boom-kiss-theme.properties"))));
    }

    Main(Game game) {
        super("Boom Boom Kiss");
        this.game = game;
        this.theme = game.getTheme();
        sessionLabels.put(CellType.SAFE, "Safe");
        sessionLabels.put(CellType.BOOM, "Boom");
        sessionLabels.put(CellType.KISS, "Kiss");
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        setMinimumSize(new Dimension(400, 660));
        Rectangle screen = getGraphicsConfiguration().getBounds();
        Insets insets = java.awt.Toolkit.getDefaultToolkit().getScreenInsets(getGraphicsConfiguration());
        int usableHeight = screen.height - insets.top - insets.bottom;
        setSize(480, Math.min(880, usableHeight - 30));
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override public void windowClosing(WindowEvent event) { confirmExit(); }
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
        JPanel panel = new GameUi.FadePanel();
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(UiStyle.PADDING, UiStyle.PADDING, UiStyle.PADDING, UiStyle.PADDING));
        return panel;
    }

    private void display(JPanel content) {
        if (fadeTimer != null) fadeTimer.stop();
        getGlassPane().setVisible(false);
        resultOverlay = null;
        setContentPane(content);
        revalidate();
        repaint();
        if (isShowing() && !busy && content instanceof GameUi.FadePanel) {
            GameUi.FadePanel fading = (GameUi.FadePanel) content;
            fading.setProgress(.2f);
            long start = System.nanoTime();
            fadeTimer = new Timer(16, event -> {
                float progress = Math.min(1, (System.nanoTime()-start)/1_000_000f/UiStyle.FADE_MS);
                fading.setProgress(.2f + .8f * progress);
                if (progress >= 1) ((Timer)event.getSource()).stop();
            });
            fadeTimer.start();
        }
    }

    private void confirmExit() {
        if (JOptionPane.showConfirmDialog(this, "Thoát trò chơi?", "Boom Boom Kiss",
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) == JOptionPane.YES_OPTION) {
            saveTheme(); dispose();
        }
    }

    private JLabel label(String text, UiFonts.Role role, Color color) {
        JLabel label = new JLabel(text, SwingConstants.CENTER);
        label.setFont(UiFonts.forRole(role));
        label.setForeground(color);
        label.setAlignmentX(CENTER_ALIGNMENT);
        return label;
    }

    private JLabel wrapped(String text, int width, UiFonts.Role role, Color color) {
        return label("<html><div style='width:" + width + "pt;text-align:center'>" +
                escapeHtml(text) + "</div></html>", role, color);
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
        text.add(label(title, UiFonts.Role.HEADING, textColor));
        gap(text, 4);
        if (!subtitle.isEmpty()) text.add(label(subtitle, UiFonts.Role.CAPTION, textColor));
        panel.add(text, BorderLayout.CENTER);
        GameUi.Button exit = new GameUi.Button("\u00d7", new Color(255, 255, 255, 180), INK, 20);
        exit.setFont(UiFonts.BUTTON);
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
        for (String id : new String[] {theme.iconFor(CellType.BOOM), theme.iconFor(CellType.KISS), theme.iconFor(CellType.BOOM)}) {
            JLabel icon = new JLabel(IconCatalog.icon(id, size));
            panel.add(icon);
        }
        panel.setMaximumSize(new Dimension(360, size + 6));
        return panel;
    }

    private void stopAnimation() {
        if (revealTimer != null) revealTimer.stop();
        if (fadeTimer != null) fadeTimer.stop();
        animationBoard = null; animationPositions = java.util.Collections.emptyList(); animatedTiles.clear();
        busy = false;
    }

    private void showMenu() {
        stopAnimation();
        JPanel root = page(PAPER);
        JPanel homeTop = new JPanel(new BorderLayout()); homeTop.setOpaque(false);
        GameUi.Button exit = new GameUi.Button("×", PAPER, INK, UiStyle.RADIUS);
        exit.setMargin(new Insets(0,0,0,0)); exit.setPreferredSize(new Dimension(28,28));
        exit.getAccessibleContext().setAccessibleName("Thoát trò chơi");
        exit.addActionListener(event -> confirmExit()); homeTop.add(exit, BorderLayout.EAST);
        root.add(homeTop, BorderLayout.NORTH);

        Card hero = new Card(UiStyle.CREAM, 32);
        hero.setLayout(new BorderLayout(0, 8));
        hero.setBorder(BorderFactory.createEmptyBorder(6, 14, 10, 14));
        JPanel branding = stack();
        JPanel logo = logo(58); branding.add(logo);
        JPanel wordmark = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0)); wordmark.setOpaque(false);
        wordmark.add(label("BOOM BOOM", UiFonts.Role.HEADING, INK));
        wordmark.add(label("KISS", UiFonts.Role.HEADING, RED));
        branding.add(wordmark);
        hero.add(branding, BorderLayout.NORTH);

        Card settings = new Card(Color.WHITE, 26);
        settings.setLayout(new BoxLayout(settings, BoxLayout.Y_AXIS));
        settings.setBorder(BorderFactory.createEmptyBorder(8, 12, 10, 12));
        settings.add(label("Chọn biểu tượng theo ý bạn", UiFonts.Role.LABEL, INK));
        gap(settings, 3);
        settings.add(label("Bấm vào ô để đổi biểu tượng", UiFonts.Role.CAPTION, INK));
        gap(settings, 7);
        JPanel choices = new JPanel(new GridLayout(1, 3, 8, 0));
        choices.setOpaque(false);
        choices.setMaximumSize(new Dimension(Integer.MAX_VALUE, 95));
        choices.setPreferredSize(new Dimension(320,95));
        JPanel rules = rulesCard();
        GameUi.Button[] iconChoices = new GameUi.Button[3];
        JTextField[] captions = new JTextField[3];
        for (CellType type : CellType.values()) {
            GameUi.Button choice = iconButton(theme.iconFor(type), "Đổi icon " + sessionLabels.get(type), 41, PAPER);
            choice.setPreferredSize(new Dimension(80,62));
            JTextField caption = new JTextField(sessionLabels.get(type));
            caption.setFont(UiFonts.CAPTION); caption.setForeground(INK); caption.setOpaque(false);
            caption.setHorizontalAlignment(SwingConstants.CENTER);
            caption.setBorder(BorderFactory.createEmptyBorder(2,2,2,2));
            caption.getAccessibleContext().setAccessibleName("Nhãn " + type.name());
            iconChoices[type.ordinal()] = choice; captions[type.ordinal()] = caption;
            Runnable markSelected = () -> {
                for (CellType item : CellType.values()) {
                    boolean selected = item == type;
                    iconChoices[item.ordinal()].setOutline(selected ? RED : null);
                    captions[item.ordinal()].setFont(selected ? UiFonts.LABEL : UiFonts.CAPTION);
                }
            };
            caption.addFocusListener(new java.awt.event.FocusAdapter() {
                @Override public void focusGained(java.awt.event.FocusEvent event) { markSelected.run(); }
                @Override public void focusLost(java.awt.event.FocusEvent event) {
                    if (caption.getText().trim().isEmpty()) caption.setText(defaultLabel(type));
                }
            });
            caption.getDocument().addDocumentListener(new DocumentListener() {
                private void update() {
                    String text = caption.getText().trim();
                    sessionLabels.put(type, text.isEmpty() ? defaultLabel(type) : text);
                    choice.getAccessibleContext().setAccessibleName("Đổi icon " + sessionLabels.get(type));
                    refreshRules(rules);
                }
                public void insertUpdate(DocumentEvent event) { update(); }
                public void removeUpdate(DocumentEvent event) { update(); }
                public void changedUpdate(DocumentEvent event) { update(); }
            });
            choice.addActionListener(event -> {
                markSelected.run();
                showIconPicker(type, icon -> {
                theme.setIcon(type, icon);
                choice.setIcon(IconCatalog.icon(icon, 41));
                choice.setDisabledIcon(choice.getIcon());
                JPanel newLogo = logo(58);
                // Replace the first row so text entered in the punishment field stays intact.
                branding.remove(0);
                branding.add(newLogo, 0);
                branding.revalidate(); branding.repaint();
                refreshRules(rules);
                saveTheme();
                });
            });
            JPanel iconBlock = new JPanel(new BorderLayout(0,3)); iconBlock.setOpaque(false);
            iconBlock.add(choice,BorderLayout.CENTER); iconBlock.add(caption,BorderLayout.SOUTH);
            choices.add(iconBlock);
        }
        settings.add(choices);
        gap(settings, 8);
        settings.add(label("Hành động khi thua", UiFonts.Role.LABEL, INK));
        gap(settings, 5);
        JTextField punishment = new PlaceholderField(theme.getLoseText(), "Nhập hành động cho người thua…");
        punishment.setFont(UiFonts.BODY);
        punishment.setForeground(INK);
        punishment.setBackground(PAPER);
        punishment.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(PAPER, 1, true),
                BorderFactory.createEmptyBorder(11, 10, 11, 10)));
        punishment.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49));
        punishment.getAccessibleContext().setAccessibleName("Punishment text");
        punishment.getDocument().addDocumentListener(new DocumentListener() {
            private void update() { theme.setLoseText(punishment.getText()); }
            public void insertUpdate(DocumentEvent event) { update(); }
            public void removeUpdate(DocumentEvent event) { update(); }
            public void changedUpdate(DocumentEvent event) { update(); }
        });
        settings.add(punishment);
        gap(settings, 8);
        GameUi.Button play = button("CHƠI", GREEN, () -> {
            if (theme.getLoseText().trim().isEmpty()) theme.setLoseText(Theme.DEFAULT_LOSE_TEXT);
            saveTheme();
            game.restart();
            showSetup();
        });
        play.setIcon(new javax.swing.Icon() {
            public int getIconWidth() { return 16; }
            public int getIconHeight() { return 18; }
            public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y) {
                java.awt.Graphics2D copy = smooth(g); copy.setColor(Color.WHITE);
                copy.fillPolygon(new int[] {x+2,x+2,x+14}, new int[] {y+2,y+16,y+9},3); copy.dispose();
            }
        });
        settings.add(play);
        JPanel cards = new JPanel(new java.awt.GridBagLayout()); cards.setOpaque(false);
        java.awt.GridBagConstraints constraints = new java.awt.GridBagConstraints();
        constraints.gridx=0; constraints.gridy=0; constraints.weightx=1; constraints.fill=java.awt.GridBagConstraints.HORIZONTAL;
        cards.add(settings,constraints);
        constraints.gridy=1; constraints.insets=new Insets(8,0,0,0); cards.add(rules,constraints);
        constraints.gridy=2; constraints.weighty=1; constraints.fill=java.awt.GridBagConstraints.BOTH;
        constraints.insets=new Insets(0,0,0,0); cards.add(Box.createVerticalGlue(),constraints);
        hero.add(cards, BorderLayout.CENTER);
        root.add(hero, BorderLayout.CENTER);
        display(root);
    }

    private String defaultLabel(CellType type) {
        switch (type) { case SAFE: return "Safe"; case BOOM: return "Boom"; case KISS: return "Kiss";
            default: throw new IllegalArgumentException("Unknown cell type"); }
    }

    private JPanel rulesCard() {
        Card card = new Card(blend(UiStyle.CREAM, UiStyle.PLAYER_ONE, .22), UiStyle.RADIUS);
        card.setLayout(new BoxLayout(card,BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(8,10,8,10));
        refreshRules(card); return card;
    }

    private void refreshRules(JPanel card) {
        card.removeAll();
        JLabel title = label("Luật chơi",UiFonts.Role.LABEL,INK);
        title.setIcon(IconCatalog.icon(theme.iconFor(CellType.SAFE),18)); title.setIconTextGap(6);
        title.setAlignmentX(LEFT_ALIGNMENT); card.add(title); gap(card,6);
        card.add(ruleLine(new CellType[] {CellType.BOOM,CellType.BOOM,CellType.KISS},
                "Mỗi bàn giấu 2 " + ruleLabel(CellType.BOOM) + " và 1 " + ruleLabel(CellType.KISS)));
        gap(card,6);
        card.add(ruleLine(new CellType[] {CellType.KISS}, "Mở trúng " + ruleLabel(CellType.KISS) +
                ", trực tiếp hoặc do nổ lan, là thua"));
        card.revalidate(); card.repaint();
    }

    private String ruleLabel(CellType type) {
        String value = sessionLabels.get(type);
        // Bound the summary only; the editable session label retains its full value.
        int count = value.codePointCount(0, value.length());
        return count <= 10 ? value : value.substring(0, value.offsetByCodePoints(0, 9)) + "…";
    }

    private JPanel ruleLine(CellType[] icons, String text) {
        JPanel row = new JPanel(new BorderLayout(6,0)); row.setOpaque(false); row.setAlignmentX(LEFT_ALIGNMENT);
        JPanel symbols = new JPanel(new FlowLayout(FlowLayout.LEFT,1,0)); symbols.setOpaque(false);
        symbols.setPreferredSize(new Dimension(65,24));
        for(CellType type:icons) symbols.add(new JLabel(IconCatalog.icon(theme.iconFor(type),20)));
        JLabel message = label("<html><div style='width:190pt;text-align:left'>"+escapeHtml(text)+"</div></html>",UiFonts.Role.CAPTION,INK);
        message.setHorizontalAlignment(SwingConstants.LEFT);
        row.add(symbols,BorderLayout.WEST); row.add(message,BorderLayout.CENTER); return row;
    }

    private void saveTheme() {
        try { game.saveTheme(); }
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
        Board board = player.getBoard();
        Color color = player == game.getPlayer1() ? PEACH : BLUE;
        JPanel root = page(color);
        Card title = new Card(player == game.getPlayer1() ? INK : new Color(55, 97, 141), UiStyle.RADIUS);
        title.setLayout(new BorderLayout()); title.setBorder(BorderFactory.createEmptyBorder(14,12,14,12));
        title.add(heading("Người " + (player == game.getPlayer1() ? "1" : "2") + " đặt bánh",
                "Bấm một ô để chọn biểu tượng", Color.WHITE)); root.add(title, BorderLayout.NORTH);
        root.add(boardPanel(board, BoardMode.SETUP, player), BorderLayout.CENTER);
        JPanel footer = stack();
        Card countCard = new Card(new Color(255, 255, 255, 120), 22);
        countCard.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 6));
        for (int index = 0; index < 3; index++) {
            CellType type = index < 2 ? CellType.BOOM : CellType.KISS;
            boolean placed = type == CellType.BOOM ? index < board.count(type) : board.count(type) == 1;
            final boolean dim = placed;
            JLabel piece = new JLabel(IconCatalog.icon(theme.iconFor(type), 42)) {
                @Override public void paint(java.awt.Graphics graphics) {
                    java.awt.Graphics2D g = smooth(graphics);
                    if (dim) g.setComposite(java.awt.AlphaComposite.SrcOver.derive(.25f));
                    super.paint(g); g.dispose();
                }
            };
            piece.getAccessibleContext().setAccessibleName(type.name() + (placed ? " đã đặt" : " còn lại"));
            countCard.add(piece);
        }
        countCard.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        footer.add(countCard); gap(footer, 12);
        JPanel tools = new JPanel(new GridLayout(1, 2, 9, 0)); tools.setOpaque(false);
        tools.add(button("NGẪU NHIÊN", NAVY, () -> { board.randomize(placementRandom); showSetup(); }));
        tools.add(button("XÓA HẾT", INK, () -> { board.clear(); showSetup(); }));
        tools.setMaximumSize(new Dimension(Integer.MAX_VALUE, 49)); footer.add(tools);
        gap(footer, 10);
        GameUi.Button save = button("LƯU BÀN", GREEN, () -> {
            game.confirmSetup();
            if (game.getState() == GameState.SETUP_PLAYER_2) {
                showHandoff("Người 1 đã sẵn sàng!", "Đưa máy cho Người 2. Đừng nhìn nhé!",
                        "TIẾP TỤC", this::showSetup);
            } else {
                showHandoff("Đã đặt bánh xong!", "Đưa máy lại cho Người 1 để bắt đầu.", "BẮT ĐẦU", this::showPlay);
            }
        });
        save.setEnabled(board.isSetupComplete()); footer.add(save);
        gap(footer, 8);
        footer.add(label(board.isSetupComplete() ? "Đủ bánh. Bấm lưu để che bàn." :
                "Đặt đủ 2 boom và 1 kiss để lưu.", UiFonts.Role.CAPTION, INK));
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
                    board.count(type) < (type == CellType.BOOM ? GameConfig.BOOM_COUNT : GameConfig.KISS_COUNT);
            choice.setEnabled(available);
            if (current == type) choice.setOutline(RED);
            choice.addActionListener(event -> {
                try { board.edit(position, type); }
                catch (IllegalArgumentException | IllegalStateException error) {
                    JOptionPane.showMessageDialog(this, "Không thể đặt bánh: " + error.getMessage(),
                            "Đặt bánh", JOptionPane.WARNING_MESSAGE);
                } finally { dialog.dispose(); }
                if (game.getState() == GameState.SETUP_PLAYER_1 || game.getState() == GameState.SETUP_PLAYER_2)
                    showSetup();
            });
            options.add(choice);
        }
        dialog.pack(); dialog.setLocationRelativeTo(this); return dialog;
    }

    private JPanel boardPanel(Board board, BoardMode mode, Player owner) {
        boolean blue = owner == game.getPlayer2();
        Color well = blue ? new Color(82, 128, 172) : new Color(212, 145, 115);
        Color hidden = blue ? new Color(61, 98, 135) : new Color(132, 93, 77);
        boolean target = mode == BoardMode.TARGET;
        GameUi.SquareBoard grid = new GameUi.SquareBoard(well, target || mode == BoardMode.SETUP);
        grid.setDimmed(mode == BoardMode.OWN);
        for (int row = 0; row < GameConfig.BOARD_SIZE; row++) {
            for (int col = 0; col < GameConfig.BOARD_SIZE; col++) {
                Position position = new Position(row, col);
                Cell cell = board.getCell(position);
                boolean visible = mode == BoardMode.SETUP ? cell.getType() != CellType.SAFE : cell.isOpened();
                boolean clickable = mode == BoardMode.SETUP || target && !busy &&
                        game.getState() == GameState.PLAYING && !cell.isOpened();
                Color tileColor = hidden;
                if (visible && cell.getType() == CellType.KISS) tileColor = new Color(252, 222, 230);
                else if (visible && cell.getType() == CellType.BOOM) tileColor = new Color(255, 229, 185);
                GameUi.Tile tile = new GameUi.Tile(visible ? theme.iconFor(cell.getType()) : null,
                        tileColor, cell.isOpened(), clickable);
                tile.setBackground(tileColor);
                if (busy && animationBoard == board && animationPositions.contains(position)) {
                    tile.animate(0, cell.getType() == CellType.BOOM);
                    tile.putClientProperty("explosion", cell.getType() == CellType.BOOM);
                    animatedTiles.add(tile);
                }
                tile.getAccessibleContext().setAccessibleName((blue ? "P2" : "P1") + " ô " +
                        (row + 1) + "," + (col + 1) + (cell.isOpened() ? " đã mở" : ""));
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
        center.add(label(title, UiFonts.Role.HEADING, INK)); gap(center, 18);
        center.add(wrapped(message, 290, UiFonts.Role.BODY, INK)); gap(center, 36);
        center.add(button(command, GREEN, next));
        center.add(Box.createVerticalGlue());
        root.add(center, BorderLayout.CENTER);
        display(root);
    }

    private void showPlay() {
        animatedTiles.clear();
        JPanel root = page(PAPER);
        JPanel top = stack();
        top.add(heading("Lượt Người " + (game.getCurrentPlayer() == game.getPlayer1() ? "1" : "2"),
                "Chọn một ô trên bàn đối thủ", INK));
        gap(top, 8); top.add(scoreboard());
        root.add(top, BorderLayout.NORTH);
        JPanel boards = new JPanel(new GridLayout(2, 1, 0, 10)); boards.setOpaque(false);
        boards.add(boardSection(game.getPlayer2(), BLUE));
        boards.add(boardSection(game.getPlayer1(), PEACH));
        root.add(boards, BorderLayout.CENTER);
        display(root);
    }

    private JPanel boardSection(Player owner, Color color) {
        boolean target = owner == game.opponent();
        Card section = new Card(color, 25);
        section.setBorder(BorderFactory.createEmptyBorder(7, 6, 6, 6));
        section.setLayout(new BorderLayout(0, 3));
        JLabel title = label("Bàn Người " + (owner == game.getPlayer1() ? "1" : "2") +
                (target ? "  \u00b7  Chọn tại đây" : ""), UiFonts.Role.CAPTION, Color.WHITE);
        section.add(title, BorderLayout.NORTH);
        section.add(boardPanel(owner.getBoard(), target ? BoardMode.TARGET : BoardMode.OWN, owner), BorderLayout.CENTER);
        JPanel badgeRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0)); badgeRow.setOpaque(false);
        JLabel badge = label(owner == game.getCurrentPlayer() ? "Lượt Người " +
                (owner == game.getPlayer1() ? "1" : "2") : " ", UiFonts.Role.LABEL, INK);
        badge.setBorder(BorderFactory.createEmptyBorder(2,12,2,12));
        Card pill = new Card(owner == game.getCurrentPlayer() ? Color.WHITE : new Color(255,255,255,0), 22);
        pill.setLayout(new BorderLayout()); pill.add(badge);
        badgeRow.add(pill); section.add(badgeRow, BorderLayout.SOUTH);
        return section;
    }

    private JPanel scoreboard() {
        JPanel scores = new JPanel(new GridLayout(2, 1, 0, 3));
        scores.setOpaque(false); scores.setMaximumSize(new Dimension(Integer.MAX_VALUE, 64));
        scores.add(scoreRow(game.getPlayer1(), "P1")); scores.add(scoreRow(game.getPlayer2(), "P2"));
        return scores;
    }

    private JPanel scoreRow(Player player, String name) {
        boolean active = player == game.getCurrentPlayer();
        Card row = new Card(active ? Color.WHITE : new Color(224, 230, 239), 14);
        row.setLayout(new GridLayout(1, 4, 0, 0));
        row.setPreferredSize(new Dimension(380, 31));
        row.add(label(name + (active ? " \u2022" : ""), UiFonts.Role.COUNTER, INK));
        for (CellType type : CellType.values())
            row.add(counter(theme.iconFor(type), "\u00d7" + player.getBoard().remaining(type), 20, active ? INK : MUTED));
        return row;
    }

    private JLabel counter(String icon, String text, int size, Color color) {
        JLabel label = label(text, UiFonts.Role.COUNTER, color);
        label.setIcon(IconCatalog.icon(icon, size)); label.setIconTextGap(7);
        return label;
    }

    private void attack(Position position) {
        if (busy || game.getState() != GameState.PLAYING) return;
        busy = true;
        AttackResult result;
        try { result = game.attack(position); }
        catch (IllegalArgumentException | IllegalStateException error) {
            busy = false;
            JOptionPane.showMessageDialog(this, "Không thể mở ô: " + error.getMessage(),
                    "Mở bánh", JOptionPane.WARNING_MESSAGE);
            showPlay(); return;
        }
        animationBoard = game.opponent().getBoard(); animationPositions = result.getOpened();
        showPlay();
        long started = System.nanoTime();
        revealTimer = new Timer(16, event -> {
            float progress = Math.min(1, (System.nanoTime()-started)/1_000_000f/UiStyle.TURN_MS);
            for (GameUi.Tile tile : animatedTiles) tile.animate(progress, Boolean.TRUE.equals(tile.getClientProperty("explosion")));
            if (progress < 1) return;
            ((Timer)event.getSource()).stop(); busy = false;
            animationBoard = null; animationPositions = java.util.Collections.emptyList();
            if (result.isKissHit()) showGameOver();
            else { game.switchTurn(); showPlay(); }
        });
        revealTimer.start();
    }

    private void showGameOver() {
        stopAnimation();
        showPlay();
        if (fadeTimer != null) fadeTimer.stop();
        if (getContentPane() instanceof GameUi.FadePanel) ((GameUi.FadePanel)getContentPane()).setProgress(1);
        disableBehind(getContentPane());
        JPanel root = new JPanel(new java.awt.GridBagLayout()) {
            @Override protected void paintComponent(java.awt.Graphics graphics) {
                java.awt.Graphics2D g = smooth(graphics); g.setColor(new Color(23, 29, 43, 170));
                g.fillRect(0,0,getWidth(),getHeight()); g.dispose();
            }
        };
        root.setOpaque(false);
        root.addMouseListener(new java.awt.event.MouseAdapter() { });
        root.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() { });
        root.setFocusCycleRoot(true);
        Card result = new Card(Color.WHITE, 32);
        result.setLayout(new BoxLayout(result, BoxLayout.Y_AXIS));
        boolean compact = getHeight() < 740;
        result.setBorder(BorderFactory.createEmptyBorder(compact ? 16 : 24, 26, compact ? 16 : 24, 26));
        Card circle = new Card(new Color(251,228,235), 120);
        circle.setLayout(new FlowLayout(FlowLayout.CENTER, 12,12));
        circle.add(new JLabel(IconCatalog.icon(theme.iconFor(CellType.KISS), compact ? 52 : 76)));
        int circleSize = compact ? 80 : 105;
        circle.setMaximumSize(new Dimension(circleSize,circleSize)); circle.setPreferredSize(new Dimension(circleSize,circleSize));
        circle.setAlignmentX(CENTER_ALIGNMENT); result.add(circle); gap(result, compact ? 10 : 16);
        result.add(wrapped("BOOM! Bạn đã trúng Kiss!", 245, UiFonts.Role.HEADING, RED)); gap(result, compact ? 6 : 10);
        result.add(label("Người " + (game.getLoser() == game.getPlayer1() ? "1" : "2") + " thua.", UiFonts.Role.LABEL, INK));
        gap(result, 6);
        result.add(label("Người " + (game.getWinner() == game.getPlayer1() ? "1" : "2") + " thắng!", UiFonts.Role.BODY, INK));
        gap(result, compact ? 12 : 25);
        Card action = new Card(new Color(255, 244, 211), 21);
        action.setLayout(new BoxLayout(action, BoxLayout.Y_AXIS));
        action.setBorder(BorderFactory.createEmptyBorder(16, 10, 16, 10));
        action.add(label("HÌNH PHẠT", UiFonts.Role.CAPTION, INK));
        gap(action, 9);
        JTextArea punishment = new JTextArea(theme.getLoseText());
        punishment.setFont(UiFonts.BODY); punishment.setForeground(INK);
        punishment.setEditable(false); punishment.setLineWrap(true); punishment.setWrapStyleWord(true);
        punishment.setOpaque(false); punishment.setBorder(null); punishment.setCaretPosition(0);
        punishment.getAccessibleContext().setAccessibleName("Nội dung hình phạt");
        JScrollPane punishmentScroll = new JScrollPane(punishment,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        punishmentScroll.setBorder(null); punishmentScroll.setOpaque(false);
        punishmentScroll.getViewport().setOpaque(false);
        punishmentScroll.setPreferredSize(new Dimension(250, theme.getLoseText().length() > 80 ? 72 : 36));
        punishmentScroll.setMaximumSize(new Dimension(Integer.MAX_VALUE, 72));
        punishmentScroll.setAlignmentX(CENTER_ALIGNMENT); action.add(punishmentScroll);
        action.setMaximumSize(new Dimension(Integer.MAX_VALUE, 190)); result.add(action);
        gap(result, compact ? 12 : 20);
        GameUi.Button again = button("Chơi lại", GREEN, () -> { stopAnimation(); game.restart(); showSetup(); });
        result.add(again);
        gap(result, 10); result.add(button("Về trang chủ", INK, this::showMenu));
        java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        c.insets = new Insets(12,18,12,18); c.fill = java.awt.GridBagConstraints.HORIZONTAL; c.weightx = 1;
        root.add(result,c); resultOverlay = root; setGlassPane(root); root.setVisible(true);
        again.requestFocusInWindow(); repaint();
    }

    private void disableBehind(java.awt.Container parent) {
        for (java.awt.Component child : parent.getComponents()) {
            child.setEnabled(false);
            if (child instanceof java.awt.Container) disableBehind((java.awt.Container)child);
        }
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }
}
