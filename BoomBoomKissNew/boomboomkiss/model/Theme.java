package boomboomkiss.model;

public final class Theme {
    private String safeIcon = IconCatalog.choices(CellType.SAFE)[0];
    private String boomIcon = IconCatalog.choices(CellType.BOOM)[0];
    private String kissIcon = IconCatalog.choices(CellType.KISS)[0];
    private String loseText = "Kiss your opponent!";

    public String getLoseText() { return loseText; }
    public void setLoseText(String value) {
        if (value == null) throw new IllegalArgumentException("Punishment must not be null");
        loseText = value;
    }

    public String iconFor(CellType type) {
        if (type == null) throw new IllegalArgumentException("Cell type must not be null");
        return type == CellType.SAFE ? safeIcon : type == CellType.BOOM ? boomIcon : kissIcon;
    }

    public void setIcon(CellType type, String icon) {
        if (type == null || icon == null) throw new IllegalArgumentException("Missing icon selection");
        String valid = IconCatalog.normalize(icon, type);
        if (!valid.equals(icon)) throw new IllegalArgumentException("Icon not available for this cell type");
        if (type == CellType.SAFE) safeIcon = valid;
        else if (type == CellType.BOOM) boomIcon = valid;
        else kissIcon = valid;
    }

    public Theme copy() {
        Theme result = new Theme();
        result.safeIcon = safeIcon;
        result.boomIcon = boomIcon;
        result.kissIcon = kissIcon;
        result.loseText = loseText;
        return result;
    }
}
