package boomboomkiss.model;

import boomboomkiss.repository.ThemeRepository;
import boomboomkiss.repository.FileThemeRepository;

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
            theme.setIcon(CellType.BOOM, "boom-bomb");
            theme.setIcon(CellType.KISS, "kiss-lips");
            theme.setLoseText("Thua rồi! Hôn đối phương một cái \uD83D\uDE18");
            repository.save(theme);
            Theme loaded = repository.load();
            check(theme.getLoseText().equals(loaded.getLoseText()), "Unicode punishment round trip");
            for (CellType type : CellType.values()) {
                check(loaded.iconFor(type).equals(theme.iconFor(type)), "icon persistence " + type);
                check(IconCatalog.choices(type).length == 6, "six icon options");
                check(Arrays.asList(IconCatalog.choices(type)).contains(loaded.iconFor(type)), "valid icon");
            }
            String preserved = loaded.getLoseText();
            loaded.setIcon(CellType.BOOM, "boom-rocket");
            check(preserved.equals(loaded.getLoseText()), "changing icons preserves punishment");
            GameTest.expect(IllegalArgumentException.class, () -> loaded.setIcon(CellType.SAFE, "boom-bomb"));
            GameTest.expect(IllegalArgumentException.class, () -> loaded.setLoseText(null));
            check(IconCatalog.normalize("B", CellType.BOOM).equals("boom-bomb"), "migrate old letter icon");
            final int[] repositoryCalls = {0, 0};
            ThemeRepository spy = new ThemeRepository() {
                public Theme load() { repositoryCalls[0]++; return loaded.copy(); }
                public void save(Theme value) {
                    repositoryCalls[1]++;
                    check(value.getLoseText().equals(loaded.getLoseText()), "Game saves current theme");
                }
            };
            Game game = new Game(spy);
            game.saveTheme();
            game.restart();
            check(repositoryCalls[0] == 1 && repositoryCalls[1] == 1, "Game uses repository abstraction");
            check(game.getTheme().getLoseText().equals(loaded.getLoseText()), "restart preserves punishment");
            Files.write(temporary, java.util.Collections.singletonList("loseText=\\uZZZZ"),
                    java.nio.charset.StandardCharsets.US_ASCII);
            GameTest.expect(IllegalStateException.class, repository::load);
            GameTest.expect(IllegalStateException.class, () -> new Game(repository));
            Files.delete(temporary);
            check(new FileThemeRepository(temporary).load().iconFor(CellType.BOOM).equals("boom-bomb"), "missing file default");
            Path directory = Files.createTempDirectory("boom-kiss-repository-test-");
            try {
                ThemeRepository unwritable = new FileThemeRepository(directory);
                GameTest.expect(IllegalStateException.class, unwritable::load);
                GameTest.expect(IllegalStateException.class, () -> unwritable.save(theme));
            } finally { Files.delete(directory); }
            System.out.println("Theme tests passed");
        } finally {
            Files.deleteIfExists(temporary);
        }
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
