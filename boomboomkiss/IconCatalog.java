package boomboomkiss;

import javax.swing.Icon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;

/** Local vector artwork: no emoji font, network, or image library required. */
final class IconCatalog {
    static final String[] SAFE = {"safe-check", "safe-cookie", "safe-flower", "safe-star", "safe-leaf", "safe-smile"};
    static final String[] BOOM = {"boom-bomb", "boom-fire", "boom-bolt", "boom-burst", "boom-dynamite", "boom-rocket"};
    static final String[] KISS = {"kiss-lips", "kiss-heart", "kiss-face", "kiss-gift", "kiss-party", "kiss-cherry"};

    private IconCatalog() { }

    static String[] choices(CellType type) {
        return type == CellType.SAFE ? SAFE : type == CellType.BOMB ? BOOM : KISS;
    }

    static String normalize(String value, CellType type) {
        for (String id : choices(type)) if (id.equals(value)) return id;
        return choices(type)[0];
    }

    static Icon icon(String id, int size) {
        return new Symbol(id, size);
    }

    static void draw(Graphics2D graphics, String id, double x, double y, double size) {
        Graphics2D g = (Graphics2D) graphics.create();
        g.translate(x, y);
        g.scale(size / 64.0, size / 64.0);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setStroke(new BasicStroke(2.8f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        Color ink = new Color(48, 40, 55);
        Color pink = new Color(236, 89, 116);
        Color green = new Color(102, 193, 129);
        if ("safe-check".equals(id)) {
            circle(g, 7, 7, 50, green, new Color(60, 138, 89));
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(6, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(path(19, 32, 28, 41, 45, 23));
        } else if ("safe-cookie".equals(id)) {
            circle(g, 7, 7, 50, new Color(236, 186, 117), new Color(172, 117, 66));
            for (int[] p : new int[][] {{20, 20}, {38, 17}, {16, 37}, {35, 34}, {39, 47}})
                circle(g, p[0], p[1], 6, new Color(123, 79, 53), null);
        } else if ("safe-flower".equals(id)) {
            for (int angle = 0; angle < 360; angle += 60) {
                double a = Math.toRadians(angle);
                circle(g, 22 + Math.cos(a) * 15, 22 + Math.sin(a) * 15, 20, new Color(247, 167, 184), null);
            }
            circle(g, 21, 21, 22, new Color(255, 214, 115), null);
        } else if ("safe-star".equals(id)) {
            shape(g, star(32, 32, 26, 13, 5), new Color(255, 210, 96), new Color(218, 159, 51));
        } else if ("safe-leaf".equals(id)) {
            Path2D leaf = new Path2D.Double();
            leaf.moveTo(13, 51); leaf.curveTo(0, 23, 21, 9, 52, 11);
            leaf.curveTo(57, 43, 38, 61, 13, 51); leaf.closePath();
            shape(g, leaf, green, new Color(60, 138, 89));
            g.setColor(new Color(60, 138, 89)); g.draw(path(11, 55, 44, 21));
        } else if ("safe-smile".equals(id) || "kiss-face".equals(id)) {
            circle(g, 6, 6, 52, new Color(255, 213, 112), new Color(213, 155, 45));
            g.setColor(ink); g.draw(path(18, 24, 24, 24)); g.draw(path(38, 24, 44, 24));
            Path2D smile = new Path2D.Double(); smile.moveTo(21, 37); smile.quadTo(32, 50, 43, 37); g.draw(smile);
            if (id.equals("kiss-face")) shape(g, heart(45, 38, 16), pink, null);
        } else if ("boom-bomb".equals(id)) {
            g.setColor(new Color(126, 97, 75)); g.draw(path(37, 15, 41, 9, 49, 12));
            shape(g, star(51, 9, 10, 4, 5), new Color(255, 193, 75), null);
            g.setColor(ink); g.fillRoundRect(26, 11, 15, 14, 5, 5);
            circle(g, 9, 20, 44, new Color(47, 40, 58), new Color(25, 22, 30));
            circle(g, 17, 27, 9, new Color(212, 206, 221), null);
        } else if ("boom-fire".equals(id)) {
            Path2D flame = new Path2D.Double(); flame.moveTo(31, 3);
            flame.curveTo(36, 19, 59, 28, 52, 48); flame.curveTo(46, 66, 15, 65, 11, 46);
            flame.curveTo(6, 29, 23, 21, 31, 3); flame.closePath();
            shape(g, flame, new Color(237, 127, 68), null);
            Path2D core = new Path2D.Double(); core.moveTo(33, 27); core.curveTo(53, 55, 16, 66, 23, 42);
            core.closePath(); shape(g, core, new Color(255, 216, 91), null);
        } else if ("boom-bolt".equals(id)) {
            shape(g, path(35, 3, 10, 37, 28, 37, 24, 62, 55, 25, 37, 25, 35, 3),
                    new Color(255, 211, 77), new Color(218, 159, 45));
        } else if ("boom-burst".equals(id)) {
            shape(g, star(32, 32, 29, 15, 9), new Color(237, 148, 66), null);
            shape(g, star(32, 32, 18, 9, 8), new Color(255, 218, 100), null);
            circle(g, 27, 27, 10, Color.WHITE, null);
        } else if ("boom-dynamite".equals(id)) {
            g.setColor(new Color(201, 70, 85)); g.fillRoundRect(12, 22, 12, 35, 5, 5);
            g.fillRoundRect(26, 19, 12, 38, 5, 5); g.fillRoundRect(40, 22, 12, 35, 5, 5);
            g.setColor(new Color(91, 61, 74)); g.fillRect(10, 35, 44, 10);
            g.setColor(ink); g.draw(path(32, 18, 37, 8, 48, 10));
            shape(g, star(51, 10, 9, 4, 5), new Color(255, 203, 86), null);
        } else if ("boom-rocket".equals(id)) {
            shape(g, path(18, 44, 9, 59, 26, 52, 18, 44), new Color(255, 196, 79), null);
            shape(g, path(18, 33, 9, 33, 5, 47, 20, 45, 18, 33), pink, null);
            shape(g, path(29, 46, 31, 60, 44, 53, 41, 43, 29, 46), pink, null);
            Path2D rocket = new Path2D.Double(); rocket.moveTo(17, 39); rocket.curveTo(26, 15, 44, 5, 58, 6);
            rocket.curveTo(58, 25, 45, 43, 26, 51); rocket.closePath();
            shape(g, rocket, new Color(230, 233, 245), new Color(87, 106, 147));
            circle(g, 33, 18, 14, new Color(115, 169, 206), null);
        } else if ("kiss-lips".equals(id)) {
            Path2D lips = new Path2D.Double(); lips.moveTo(4, 31);
            lips.curveTo(17, 10, 27, 12, 32, 19); lips.curveTo(39, 10, 51, 14, 60, 31);
            lips.curveTo(44, 62, 20, 60, 4, 31); lips.closePath();
            shape(g, lips, pink, new Color(135, 39, 61));
            Path2D middle = new Path2D.Double(); middle.moveTo(4, 31); middle.quadTo(31, 42, 60, 31);
            g.setColor(new Color(135, 39, 61)); g.draw(middle);
            g.setColor(new Color(255, 179, 194)); g.draw(path(20, 20, 25, 18));
        } else if ("kiss-heart".equals(id)) {
            shape(g, heart(32, 33, 52), pink, new Color(181, 59, 82));
        } else if ("kiss-gift".equals(id)) {
            g.setColor(new Color(236, 129, 153)); g.fillRoundRect(9, 23, 46, 34, 6, 6);
            g.setColor(pink); g.fillRoundRect(6, 21, 52, 12, 4, 4);
            g.setColor(new Color(255, 221, 122)); g.fillRect(28, 21, 9, 36);
            g.setStroke(new BasicStroke(5)); g.drawOval(17, 7, 14, 13); g.drawOval(33, 7, 14, 13);
        } else if ("kiss-party".equals(id)) {
            shape(g, path(7, 57, 19, 21, 45, 47, 7, 57), new Color(255, 201, 87), null);
            g.setColor(new Color(235, 115, 145)); g.draw(path(13, 42, 25, 52)); g.draw(path(17, 31, 35, 49));
            Color[] colors = {pink, new Color(100, 179, 209), new Color(134, 115, 200)};
            int[][] dots = {{23, 7}, {42, 9}, {54, 27}};
            for (int i = 0; i < dots.length; i++) circle(g, dots[i][0], dots[i][1], 6, colors[i], null);
            g.setColor(colors[1]); g.draw(path(32, 29, 41, 21));
            g.setColor(colors[2]); g.draw(path(47, 43, 58, 40));
        } else if ("kiss-cherry".equals(id)) {
            g.setColor(new Color(75, 149, 101)); g.draw(path(19, 38, 31, 10, 44, 36));
            circle(g, 5, 32, 26, pink, new Color(168, 53, 80));
            circle(g, 32, 33, 26, pink, new Color(168, 53, 80));
            circle(g, 11, 37, 6, new Color(255, 182, 192), null);
        } else {
            // Legacy symbol fallback is kept for existing theme files.
            g.setColor(pink); g.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 45));
            g.drawString(id, 8, 48);
        }
        g.dispose();
    }

    private static void circle(Graphics2D g, double x, double y, double size, Color fill, Color stroke) {
        shape(g, new Ellipse2D.Double(x, y, size, size), fill, stroke);
    }

    private static void shape(Graphics2D g, java.awt.Shape shape, Color fill, Color stroke) {
        if (fill != null) { g.setColor(fill); g.fill(shape); }
        if (stroke != null) { g.setColor(stroke); g.draw(shape); }
    }

    private static Path2D path(double... coordinates) {
        Path2D p = new Path2D.Double(); p.moveTo(coordinates[0], coordinates[1]);
        for (int i = 2; i < coordinates.length; i += 2) p.lineTo(coordinates[i], coordinates[i + 1]);
        return p;
    }

    private static Path2D star(double x, double y, double outer, double inner, int points) {
        Path2D p = new Path2D.Double();
        for (int i = 0; i < points * 2; i++) {
            double a = -Math.PI / 2 + Math.PI * i / points;
            double r = i % 2 == 0 ? outer : inner;
            double px = x + Math.cos(a) * r, py = y + Math.sin(a) * r;
            if (i == 0) p.moveTo(px, py); else p.lineTo(px, py);
        }
        p.closePath(); return p;
    }

    private static Path2D heart(double x, double y, double size) {
        double r = size / 2;
        Path2D p = new Path2D.Double(); p.moveTo(x, y + r);
        p.curveTo(x - r * 2, y - r * .2, x - r, y - r * 1.6, x, y - r * .65);
        p.curveTo(x + r, y - r * 1.6, x + r * 2, y - r * .2, x, y + r); p.closePath();
        return p;
    }

    private static final class Symbol implements Icon {
        private final String id;
        private final int size;
        Symbol(String id, int size) { this.id = id; this.size = size; }
        public int getIconWidth() { return size; }
        public int getIconHeight() { return size; }
        public void paintIcon(Component component, Graphics graphics, int x, int y) {
            draw((Graphics2D) graphics, id, x, y, size);
        }
    }
}
