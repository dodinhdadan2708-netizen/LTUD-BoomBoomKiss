package boomboomkiss.model;

import javax.swing.JTextField;
import java.awt.*;

final class PlaceholderField extends JTextField {
    private final String placeholder;
    PlaceholderField(String text, String placeholder) { super(text); this.placeholder = placeholder; }
    @Override protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        if (!getText().isEmpty()) return;
        Graphics2D g = GameUi.smooth(graphics); g.setFont(getFont()); g.setColor(GameUi.MUTED);
        g.drawString(placeholder, getInsets().left, (getHeight()-g.getFontMetrics().getHeight())/2+g.getFontMetrics().getAscent());
        g.dispose();
    }
}
