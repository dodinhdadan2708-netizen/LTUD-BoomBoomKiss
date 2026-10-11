package boomboomkiss.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.GlyphVector;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.imageio.ImageIO;
import javax.swing.UIManager;

public final class VisualTest {
    public static void main(String[] args) throws Exception {
        UiFonts.install();
        if (args.length > 0 && "--fallback".equals(args[0])) {
            for (UiFonts.Role role : UiFonts.Role.values()) {
                Font font = UiFonts.forRole(role);
                if (font == null || font.canDisplayUpTo(UiFonts.VIETNAMESE) != -1)
                    throw new AssertionError("Fallback unusable: " + role);
            }
            System.out.println("Missing resource font fallback passed; application does not crash");
            return;
        }
        for (String name : new String[] {"Baloo2-ExtraBold.ttf", "Nunito-Bold.ttf", "Nunito-ExtraBold.ttf"})
            checkStaticFont(name);
        BufferedImage sample = new BufferedImage(1800, 550, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = GameUi.smooth(sample.getGraphics());
        g.setColor(Color.WHITE); g.fillRect(0, 0, sample.getWidth(), sample.getHeight());
        g.setColor(GameUi.INK);
        int y = 45;
        int[] expectedSizes = {40,26,20,16,15,13,16};
        for (UiFonts.Role role : UiFonts.Role.values()) {
            Font font = UiFonts.forRole(role);
            if (font.canDisplayUpTo(UiFonts.VIETNAMESE) != -1) throw new AssertionError("Missing Vietnamese glyph " + role);
            if (font.getSize() != expectedSizes[role.ordinal()] || font.getStyle() != Font.PLAIN)
                throw new AssertionError("Wrong role size/style " + role);
            String family = role.ordinal() <= 2 ? "Baloo 2" : "Nunito";
            if (!font.getFamily().contains(family)) throw new AssertionError("Unexpected font/fallback: " + role + " " + font);
            if (!font.getFontName().contains(role == UiFonts.Role.BODY || role == UiFonts.Role.CAPTION ? "Bold" : "ExtraBold"))
                throw new AssertionError("Wrong font file weight " + font.getFontName());
            g.setFont(font);
            g.drawString(role + " · " + UiFonts.VIETNAMESE, 15, y);
            y += 72;
            System.out.println(role + ": " + font.getFontName() + " " + font.getSize() + " (PLAIN instance)");
        }
        if (!RenderingHints.VALUE_FRACTIONALMETRICS_ON.equals(g.getRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS)))
            throw new AssertionError("Fractional metrics disabled");
        verifyVerticalFit(g, UiFonts.BUTTON, "NGẪU NHIÊN ẶẬỆỘỢỰ ằẳẵ", 49, 4, 7);
        verifyVerticalFit(g, UiFonts.BODY, UiFonts.VIETNAMESE, 49, 11, 11);
        if (!UiFonts.BUTTON.equals(UIManager.getFont("OptionPane.buttonFont")) ||
                !UiFonts.BODY.equals(UIManager.getFont("OptionPane.messageFont")))
            throw new AssertionError("Dialog fonts not installed");
        GameUi.Tile hidden = new GameUi.Tile(null, GameUi.NAVY, false, false);
        hidden.setSize(60,60);
        for (float phase : new float[] {0,.25f,.5f,1}) {
            hidden.animate(phase,true);
            if (hidden.hasSymbol()) throw new AssertionError("Unopened tile contains symbol");
        }
        g.dispose(); Files.createDirectories(Paths.get("boomboomkiss/build/previews"));
        ImageIO.write(sample, "png", Paths.get("boomboomkiss/build/previews/09-fonts.png").toFile());
        System.out.println("Font roles, static TTF, Vietnamese, vertical fit and dialog tests passed");
    }

    private static void verifyVerticalFit(Graphics2D graphics, Font font, String text, int height, int top, int bottom) {
        graphics.setFont(font);
        java.awt.FontMetrics metrics = graphics.getFontMetrics();
        float baseline = top + (height-top-bottom-metrics.getHeight())/2f + metrics.getAscent();
        GlyphVector glyphs = font.createGlyphVector(graphics.getFontRenderContext(), text);
        Rectangle2D bounds = glyphs.getVisualBounds();
        if (baseline+bounds.getMinY() < top || baseline+bounds.getMaxY() > height-bottom)
            throw new AssertionError("Glyph clipping for " + font + ": " + bounds);
    }

    private static void checkStaticFont(String file) throws Exception {
        InputStream stream = UiFonts.class.getResourceAsStream("/resources/fonts/"+file);
        if (stream == null) throw new AssertionError("Resource missing: " + file);
        try (DataInputStream input = new DataInputStream(stream)) {
            input.readInt(); int tables = input.readUnsignedShort(); input.skipBytes(6);
            for (int i=0; i<tables; i++) {
                byte[] tag = new byte[4]; input.readFully(tag); input.skipBytes(12);
                if ("fvar".equals(new String(tag, "US-ASCII"))) throw new AssertionError("Variable font: " + file);
            }
        }
    }
}
