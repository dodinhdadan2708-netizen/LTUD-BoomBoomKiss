package week_3;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

final class Theme {
    String safeIcon = IconCatalog.SAFE[0];
    String boomIcon = IconCatalog.BOOM[0];
    String kissIcon = IconCatalog.KISS[0];
    String loseText = "Kiss your opponent!";

    String iconFor(CellType type) {
        return type == CellType.SAFE ? safeIcon : type == CellType.BOMB ? boomIcon : kissIcon;
    }

    void setIcon(CellType type, String icon) {
        String valid = IconCatalog.normalize(icon, type);
        if (type == CellType.SAFE) safeIcon = valid;
        else if (type == CellType.BOMB) boomIcon = valid;
        else kissIcon = valid;
    }
}

interface ThemeRepository {
    Theme load();
    void save(Theme theme);
}

final class FileThemeRepository implements ThemeRepository {
    private final Path path;

    FileThemeRepository(Path path) {
        this.path = path;
    }

    public Theme load() {
        Theme theme = new Theme();
        if (!Files.exists(path)) return theme;
        Properties properties = new Properties();
        try (InputStream input = Files.newInputStream(path)) {
            properties.load(input);
            theme.safeIcon = IconCatalog.normalize(properties.getProperty("safeIcon"), CellType.SAFE);
            theme.boomIcon = IconCatalog.normalize(properties.getProperty("boomIcon"), CellType.BOMB);
            theme.kissIcon = IconCatalog.normalize(properties.getProperty("kissIcon"), CellType.KISS);
            theme.loseText = properties.getProperty("loseText", theme.loseText);
        } catch (IOException ignored) {
            // The default theme remains playable if the file cannot be read.
        }
        return theme;
    }

    public void save(Theme theme) {
        Properties properties = new Properties();
        properties.setProperty("safeIcon", theme.safeIcon);
        properties.setProperty("boomIcon", theme.boomIcon);
        properties.setProperty("kissIcon", theme.kissIcon);
        properties.setProperty("loseText", theme.loseText);
        try (OutputStream output = Files.newOutputStream(path)) {
            properties.store(output, "Boom Boom Kiss theme");
        } catch (IOException exception) {
            throw new IllegalStateException("Could not save theme: " + exception.getMessage(), exception);
        }
    }
}
