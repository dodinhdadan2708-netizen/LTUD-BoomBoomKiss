package boomboomkiss;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class ThemeTest {
    public static void main(String[] args) throws Exception {
        Path temporary = Files.createTempFile("boom-kiss-theme-test-", ".properties");
        try {
            ThemeRepository repository = new FileThemeRepository(temporary);
            Theme theme = new Theme();
            theme.setIcon(CellType.SAFE, "safe-flower");
            theme.setIcon(CellType.BOMB, "boom-bomb");
            theme.setIcon(CellType.KISS, "kiss-lips");
            theme.loseText = "Thua rồi! Hôn đối phương một cái \uD83D\uDE18";
            repository.save(theme);
            Theme loaded = repository.load();
            check(theme.loseText.equals(loaded.loseText), "Unicode punishment round trip");
            for (CellType type : CellType.values()) {
                check(loaded.iconFor(type).equals(theme.iconFor(type)), "icon persistence " + type);
                check(IconCatalog.choices(type).length == 6, "six icon options");
                check(Arrays.asList(IconCatalog.choices(type)).contains(loaded.iconFor(type)), "valid icon");
            }
            String preserved = loaded.loseText;
            loaded.setIcon(CellType.BOMB, "boom-rocket");
            check(preserved.equals(loaded.loseText), "changing icons preserves punishment");
            check(IconCatalog.normalize("B", CellType.BOMB).equals("boom-bomb"), "migrate old letter icon");
            Files.delete(temporary);
            check(new FileThemeRepository(temporary).load().boomIcon.equals("boom-bomb"), "missing file default");
            System.out.println("Theme tests passed");
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
