package week_3;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/** Reusable Swing painting; the board never decides game rules. */
final class GameUi {
    static final Color INK = new Color(66, 72, 94);
    static final Color MUTED = new Color(130, 139, 158);
    static final Color PAPER = new Color(239, 242, 247);
    static final Color PEACH = new Color(238, 163, 132);
    static final Color BLUE = new Color(110, 172, 204);
    static final Color NAVY = new Color(79, 119, 163);
    static final Color GREEN = new Color(91, 168, 101);
    static final Color RED = new Color(205, 66, 87);

    private GameUi() { }

    static Font font(int size, boolean bold) {
        return new Font("Segoe UI", bold ? Font.BOLD : Font.PLAIN, size);
    }

    static Graphics2D smooth(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        return g;
    }

    static class Card extends JPanel {
        private final Color fill;
        private final int radius;
        Card(Color fill, int radius) {
            this.fill = fill;
            this.radius = radius;
            setOpaque(false);
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = smooth(graphics);
            g.setColor(fill);
            g.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    static class Button extends JButton {
        private Color fill;
        private Color outline;
        private final int radius;

        Button(String text, Color fill, Color foreground, int radius) {
            super(text);
            this.fill = fill;
            this.radius = radius;
            setForeground(foreground);
            setFont(font(17, true));
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setOpaque(false);
            setRolloverEnabled(true);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setMargin(new java.awt.Insets(4, 12, 4, 12));
            setPreferredSize(new Dimension(200, 49));
        }

        void setOutline(Color color) { outline = color; repaint(); }

        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = smooth(graphics);
            Color color = !isEnabled() ? blend(fill, Color.WHITE, .55) :
                    getModel().isPressed() ? fill.darker() :
                            getModel().isRollover() ? blend(fill, Color.WHITE, .12) : fill;
            g.setColor(color);
            g.fillRoundRect(2, 2, getWidth() - 4, getHeight() - 5, radius, radius);
            if (outline != null || isFocusOwner()) {
                g.setColor(outline != null ? outline : INK);
                g.setStroke(new BasicStroke(2.5f));
                g.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 6, radius, radius);
            }
            g.dispose();
            super.paintComponent(graphics);
        }
    }

    static final class Tile extends Button {
        private final String symbol;
        private final boolean opened;
        Tile(String symbol, Color fill, boolean opened, boolean clickable) {
            super("", fill, Color.WHITE, 15);
            this.symbol = symbol;
            this.opened = opened;
            setEnabled(clickable);
            setCursor(Cursor.getPredefinedCursor(clickable ? Cursor.HAND_CURSOR : Cursor.DEFAULT_CURSOR));
            // Keep disabled revealed icons in full color, unlike JButton's default gray icon.
        }
        @Override protected void paintComponent(Graphics graphics) {
            Graphics2D g = smooth(graphics);
            Color fill = getBackground();
            g.setColor(fill);
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            if (isEnabled() && (getModel().isRollover() || isFocusOwner())) {
                g.setColor(new Color(255, 255, 255, 75));
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            }
            if (symbol != null) {
                double size = Math.min(getWidth(), getHeight()) * .66;
                IconCatalog.draw(g, symbol, (getWidth() - size) / 2, (getHeight() - size) / 2, size);
            }
            if (opened) {
                g.setColor(new Color(255, 255, 255, 110));
                g.setStroke(new BasicStroke(1.3f));
                g.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, 15, 15);
            }
            g.dispose();
        }
    }

    /** Centers a square grid in the available space, fitting both boards without scrolling. */
    static final class SquareBoard extends JPanel {
        private final JPanel grid = new JPanel(new GridLayout(GameConfig.BOARD_SIZE, GameConfig.BOARD_SIZE, 5, 5));
        private final Color fill;
        private final boolean active;
        SquareBoard(Color fill, boolean active) {
            this.fill = fill;
            this.active = active;
            setOpaque(false);
            setLayout(null);
            grid.setOpaque(false);
            add(grid);
            setPreferredSize(new Dimension(330, 330));
            setMinimumSize(new Dimension(160, 160));
            setAlignmentX(CENTER_ALIGNMENT);
        }
        void addTile(Tile tile) { grid.add(tile); }
        @Override public void doLayout() {
            int size = Math.max(10, Math.min(getWidth(), getHeight()) - 12);
            grid.setBounds((getWidth() - size) / 2 + 10, (getHeight() - size) / 2 + 10, size - 20, size - 20);
        }
        @Override protected void paintComponent(Graphics graphics) {
            int size = Math.max(10, Math.min(getWidth(), getHeight()) - 12);
            int x = (getWidth() - size) / 2, y = (getHeight() - size) / 2;
            Graphics2D g = smooth(graphics);
            g.setColor(fill); g.fillRoundRect(x, y, size, size, 25, 25);
            if (active) {
                g.setColor(Color.WHITE); g.setStroke(new BasicStroke(3));
                g.drawRoundRect(x, y, size, size, 25, 25);
            }
            g.dispose();
        }
    }

    static Color blend(Color a, Color b, double weight) {
        return new Color((int) (a.getRed() * (1 - weight) + b.getRed() * weight),
                (int) (a.getGreen() * (1 - weight) + b.getGreen() * weight),
                (int) (a.getBlue() * (1 - weight) + b.getBlue() * weight));
    }
}
