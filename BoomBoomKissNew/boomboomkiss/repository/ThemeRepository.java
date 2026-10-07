package boomboomkiss.repository;

import boomboomkiss.model.Theme;

public interface ThemeRepository {
    Theme load();
    void save(Theme theme);
}
