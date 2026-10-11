package boomboomkiss.model;

import boomboomkiss.config.GameConfig;
import boomboomkiss.config.UiStyle;
import java.awt.AlphaComposite;

import javax.swing.JButton;
import javax.swing.JPanel;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;

/** Reusable Swing painting; the board never decides game rules. */
final class GameUi {
    static final Color INK = UiStyle.DARK;
    static final Color MUTED = UiStyle.DARK;
    static final Color PAPER = UiStyle.PAPER;
    static final Color PEACH = UiStyle.PLAYER_ONE;
    static final Color BLUE = UiStyle.PLAYER_TWO;
    static final Color NAVY = UiStyle.BLUE;
    static final Color GREEN = UiStyle.GREEN;
    static final Color RED = UiStyle.RED;

    private GameUi() { }

    static Graphics2D smooth(Graphics graphics) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        UiFonts.antialias(g);
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

    static class Button extends RoundedButton {
        Button(String text, Color fill, Color foreground, int radius) { super(text, fill, foreground, radius); }
    }

    static final class FadePanel extends JPanel {
        private float progress = 1;
        FadePanel() { super(new java.awt.BorderLayout()); }
        void setProgress(float value) { progress = value; repaint(); }
        @Override public void paint(Graphics graphics) {
            Graphics2D g = smooth(graphics);
            g.setComposite(AlphaComposite.SrcOver.derive(progress));
            super.paint(g); g.dispose();
        }
    }

    static final class Tile extends Button {
        private final String symbol;
        private final boolean opened;
        private float progress = 1;
        private boolean explosion;
        void animate(float progress, boolean explosion) { this.progress = progress; this.explosion = explosion; repaint(); }
        boolean hasSymbol() { return symbol != null; }
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
            float flip = Math.min(1f, progress * UiStyle.TURN_MS / UiStyle.FLIP_MS);
            double scale = Math.max(.05, Math.abs(Math.cos(Math.PI * flip)));
            if (flip < 1) { g.translate(getWidth()/2.0, 0); g.scale(scale, 1); g.translate(-getWidth()/2.0, 0); }
            Color fill = getBackground();
            g.setColor(fill);
            g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            if (isEnabled() && (getModel().isRollover() || isFocusOwner())) {
                g.setColor(new Color(255, 255, 255, 75));
                g.fillRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 15, 15);
            }
            if (symbol != null && flip >= .5f) {
                double size = Math.min(getWidth(), getHeight()) * .66;
                String painted = explosion && progress * UiStyle.TURN_MS < UiStyle.BLAST_MS ? "boom-burst" : symbol;
                double shake = explosion && progress < .65f ? Math.sin(progress * 80) * 2 : 0;
                IconCatalog.draw(g, painted, (getWidth() - size) / 2 + shake, (getHeight() - size) / 2, size);
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
        private boolean dimmed;
        void setDimmed(boolean value) { dimmed = value; }
        @Override public void paint(Graphics graphics) {
            Graphics2D g = smooth(graphics);
            if (dimmed) g.setComposite(AlphaComposite.SrcOver.derive(.65f));
            super.paint(g); g.dispose();
        }
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
