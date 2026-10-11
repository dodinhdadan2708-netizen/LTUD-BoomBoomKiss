package boomboomkiss.repository;

import boomboomkiss.model.Theme;
import boomboomkiss.model.CellType;
import boomboomkiss.model.IconCatalog;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class FileThemeRepository implements ThemeRepository {
    private final Path path;

    public FileThemeRepository(Path path) {
        if (path == null) throw new IllegalArgumentException("Path must not be null");
        this.path = path;
    }

    public Theme load() {
        Theme theme = new Theme();
        // A missing file is the normal first-run state; actual read failures are reported.
        if (Files.notExists(path)) return theme;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            theme.setIcon(CellType.SAFE, IconCatalog.normalize(properties.getProperty("safeIcon"), CellType.SAFE));
            theme.setIcon(CellType.BOOM, IconCatalog.normalize(properties.getProperty("boomIcon"), CellType.BOOM));
            theme.setIcon(CellType.KISS, IconCatalog.normalize(properties.getProperty("kissIcon"), CellType.KISS));
            theme.setLoseText(properties.getProperty("loseText", theme.getLoseText()));
        } catch (IOException | IllegalArgumentException exception) {
            throw new IllegalStateException("Cannot read theme file: " + path, exception);
        }
        return theme;
    }

    public void save(Theme theme) {
        if (theme == null) throw new IllegalArgumentException("Theme must not be null");
        Properties properties = new Properties();
        properties.setProperty("safeIcon", theme.iconFor(CellType.SAFE));
        properties.setProperty("boomIcon", theme.iconFor(CellType.BOOM));
        properties.setProperty("kissIcon", theme.iconFor(CellType.KISS));
        properties.setProperty("loseText", theme.getLoseText());
        try (OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, "Boom Boom Kiss theme");
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot save theme file: " + path, exception);
        }
    }
}
