package boomboomkiss.model;

import javax.swing.JButton;
import java.awt.*;

class RoundedButton extends JButton {
    private final Color fill;
    private Color outline;
    private final int radius;
    RoundedButton(String text, Color fill, Color foreground, int radius) {
        super(text); this.fill = fill; this.radius = radius;
        setForeground(foreground); setFont(UiFonts.BUTTON);
        setFocusPainted(false); setContentAreaFilled(false); setBorderPainted(false); setOpaque(false);
        setRolloverEnabled(true); setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        setMargin(new Insets(4, 12, 7, 12)); setPreferredSize(new Dimension(200, 51));
    }
    @Override public void setText(String text) {
        super.setText(text == null ? null : text.toUpperCase(java.util.Locale.ROOT));
    }
    void setOutline(Color value) { outline = value; repaint(); }
    @Override protected void paintComponent(Graphics graphics) {
        Graphics2D g = GameUi.smooth(graphics);
        boolean pressed = isEnabled() && getModel().isPressed();
        int offset = pressed ? 3 : 0;
        g.setColor(new Color(30, 40, 50, isEnabled() ? 38 : 12));
        g.fillRoundRect(2, 6, getWidth()-4, getHeight()-7, radius, radius);
        Color color = !isEnabled() ? GameUi.blend(fill, Color.WHITE, .6) : pressed ? fill.darker() :
                getModel().isRollover() ? GameUi.blend(fill, Color.WHITE, .14) : fill;
        g.setColor(color); g.fillRoundRect(2, 2+offset, getWidth()-4, getHeight()-8, radius, radius);
        if (outline != null || isFocusOwner()) {
            g.setColor(outline == null ? GameUi.INK : outline); g.setStroke(new BasicStroke(2.2f));
            g.drawRoundRect(2, 2+offset, getWidth()-5, getHeight()-9, radius, radius);
        }
        g.translate(0, offset); if (!isEnabled()) g.setComposite(AlphaComposite.SrcOver.derive(.5f));
        super.paintComponent(g); g.dispose();
    }
}
