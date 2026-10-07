package boomboomkiss.repository;

import boomboomkiss.model.Theme;

// Default for logic tests and embedding the game without file I/O.
public final class MemoryThemeRepository implements ThemeRepository {
    private Theme saved = new Theme();
    public Theme load() { return saved.copy(); }
    public void save(Theme theme) {
        if (theme == null) throw new IllegalArgumentException("Theme must not be null");
        saved = theme.copy();
    }
}
