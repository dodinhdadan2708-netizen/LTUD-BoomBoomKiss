package boomboomkiss.model;

import boomboomkiss.config.UiStyle;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.io.InputStream;
import java.util.Collections;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

/** Single source for UI font files, weights, sizes, roles and Swing defaults. */
final class UiFonts {
    static {
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
    }

    static final int TITLE_SIZE = 40, HEADING_SIZE = 26, BUTTON_SIZE = 20;
    static final int LABEL_SIZE = 16, BODY_SIZE = 15, CAPTION_SIZE = 13, COUNTER_SIZE = 16;
    static final String VIETNAMESE = "Người 1 đặt bánh · Hãy hôn đối phương! · ẶẬỆỘỢỰ ằẳẵ ĐđƠơƯư";
    private static final Font NUNITO_EXTRA_BOLD = load("Nunito-ExtraBold.ttf");
    private static final Font NUNITO_BOLD = load("Nunito-Bold.ttf");
    private static final Font BALOO_EXTRA_BOLD = load("Baloo2-ExtraBold.ttf");

    enum Role { TITLE, HEADING, BUTTON, LABEL, BODY, CAPTION, COUNTER }

    static final Font TITLE = derive(BALOO_EXTRA_BOLD, TITLE_SIZE, "TITLE");
    static final Font HEADING = derive(BALOO_EXTRA_BOLD, HEADING_SIZE, "HEADING");
    static final Font BUTTON = derive(BALOO_EXTRA_BOLD, BUTTON_SIZE, "BUTTON");
    static final Font LABEL = derive(NUNITO_EXTRA_BOLD, LABEL_SIZE, "LABEL");
    static final Font BODY = derive(NUNITO_BOLD, BODY_SIZE, "BODY");
    static final Font CAPTION = derive(NUNITO_BOLD, CAPTION_SIZE, "CAPTION");
    static final Font COUNTER = derive(NUNITO_EXTRA_BOLD, COUNTER_SIZE, "COUNTER");

    private UiFonts() { }

    private static Font load(String file) {
        // JAR: resources/fonts; IntelliJ source-root copying: boomboomkiss/resources/fonts.
        for (String root : new String[] {"/resources/fonts/", "/boomboomkiss/resources/fonts/"}) {
            try (InputStream input = UiFonts.class.getResourceAsStream(root + file)) {
                if (input == null) continue;
                Font font = Font.createFont(Font.TRUETYPE_FONT, input);
                GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(font);
                return font;
            } catch (Exception error) {
                System.err.println("[UiFonts] Cannot load " + file + ": " + error.getMessage());
            }
        }
        System.err.println("[UiFonts] Resource unavailable: " + file + "; using fallback.");
        return null;
    }

    private static Font derive(Font source, int size, String role) {
        if (source != null && source.canDisplayUpTo(VIETNAMESE) == -1)
            return source.deriveFont(Font.PLAIN, (float) size);
        if (NUNITO_EXTRA_BOLD != null && NUNITO_EXTRA_BOLD.canDisplayUpTo(VIETNAMESE) == -1) {
            System.err.println("[UiFonts] " + role + " uses Nunito ExtraBold fallback.");
            return NUNITO_EXTRA_BOLD.deriveFont(Font.PLAIN, (float) size);
        }
        System.err.println("[UiFonts] " + role + " uses the system weighted fallback.");
        // Only the emergency system fallback needs a weight attribute; bundled files never synthesize bold.
        return new Font("Dialog", Font.PLAIN, size).deriveFont(
                Collections.singletonMap(TextAttribute.WEIGHT, TextAttribute.WEIGHT_BOLD));
    }

    static Font forRole(Role role) {
        switch (role) {
            case TITLE: return TITLE;
            case HEADING: return HEADING;
            case BUTTON: return BUTTON;
            case LABEL: return LABEL;
            case BODY: return BODY;
            case CAPTION: return CAPTION;
            case COUNTER: return COUNTER;
            default: throw new IllegalArgumentException("Unknown font role");
        }
    }

    static void antialias(Graphics2D graphics) {
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
    }

    static void install() {
        for (String key : new String[] {"Label.font", "TitledBorder.font", "CheckBox.font", "RadioButton.font",
                "Menu.font", "MenuItem.font", "TabbedPane.font"})
            UIManager.put(key, new FontUIResource(LABEL));
        for (String key : new String[] {"Button.font", "ToggleButton.font", "OptionPane.buttonFont"})
            UIManager.put(key, new FontUIResource(BUTTON));
        for (String key : new String[] {"TextField.font", "FormattedTextField.font", "TextArea.font",
                "TextPane.font", "EditorPane.font", "ComboBox.font", "List.font", "Table.font",
                "OptionPane.font", "OptionPane.messageFont"})
            UIManager.put(key, new FontUIResource(BODY));
        UIManager.put("ToolTip.font", new FontUIResource(CAPTION));
        UIManager.put("Label.foreground", UiStyle.DARK);
        UIManager.put("OptionPane.messageForeground", UiStyle.DARK);
        UIManager.put("Button.foreground", UiStyle.DARK);
        UIManager.put("OptionPane.yesButtonText", "CÓ");
        UIManager.put("OptionPane.noButtonText", "KHÔNG");
        UIManager.put("OptionPane.okButtonText", "ĐỒNG Ý");
        UIManager.put("OptionPane.cancelButtonText", "HỦY");
    }
}
