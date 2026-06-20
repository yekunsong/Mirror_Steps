package config;

import javafx.scene.paint.Color;

/*
 * Per-level configuration template.
 * This class belongs to the shared config layer, but each level owner only edits
 * their own LevelConfig instance inside the matching level package.
 * Use this class to define level title, colors, world size, physics, and the music tag
 * without touching the shared runtime engine.
 */
public final class LevelConfig {

    private final int id;
    private final String title;
    private final String subtitle;
    private final String description;
    private final String mechanicLabel;
    private final int previousLevelId;
    private final int nextLevelId;
    private final double worldWidth;
    private final double worldHeight;
    private final double gravity;
    private final double moveSpeed;
    private final double jumpVelocity;
    private final Color backgroundColor;
    private final Color panelColor;
    private final Color playerColor;
    private final Color platformColor;
    private final Color accentColor;
    private final Color doorColor;
    private final String musicTrackId;

    private LevelConfig(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.subtitle = builder.subtitle;
        this.description = builder.description;
        this.mechanicLabel = builder.mechanicLabel;
        this.previousLevelId = builder.previousLevelId;
        this.nextLevelId = builder.nextLevelId;
        this.worldWidth = builder.worldWidth;
        this.worldHeight = builder.worldHeight;
        this.gravity = builder.gravity;
        this.moveSpeed = builder.moveSpeed;
        this.jumpVelocity = builder.jumpVelocity;
        this.backgroundColor = builder.backgroundColor;
        this.panelColor = builder.panelColor;
        this.playerColor = builder.playerColor;
        this.platformColor = builder.platformColor;
        this.accentColor = builder.accentColor;
        this.doorColor = builder.doorColor;
        this.musicTrackId = builder.musicTrackId;
    }

    public static Builder builder(int id, String title) {
        return new Builder(id, title);
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getMechanicLabel() {
        return mechanicLabel;
    }

    public int getPreviousLevelId() {
        return previousLevelId;
    }

    public int getNextLevelId() {
        return nextLevelId;
    }

    public double getWorldWidth() {
        return worldWidth;
    }

    public double getWorldHeight() {
        return worldHeight;
    }

    public double getGravity() {
        return gravity;
    }

    public double getMoveSpeed() {
        return moveSpeed;
    }

    public double getJumpVelocity() {
        return jumpVelocity;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public Color getPanelColor() {
        return panelColor;
    }

    public Color getPlayerColor() {
        return playerColor;
    }

    public Color getPlatformColor() {
        return platformColor;
    }

    public Color getAccentColor() {
        return accentColor;
    }

    public Color getDoorColor() {
        return doorColor;
    }

    public String getMusicTrackId() {
        return musicTrackId;
    }

    public static final class Builder {

        private final int id;
        private final String title;
        private String subtitle = "";
        private String description = "";
        private String mechanicLabel = "";
        private int previousLevelId = 0;
        private int nextLevelId = 0;
        private double worldWidth = 960;
        private double worldHeight = 540;
        private double gravity = 700;
        private double moveSpeed = 220;
        private double jumpVelocity = -360;
        private Color backgroundColor = Color.web("#0f172a");
        private Color panelColor = Color.web("#1e293b");
        private Color playerColor = Color.web("#22c55e");
        private Color platformColor = Color.web("#94a3b8");
        private Color accentColor = Color.web("#38bdf8");
        private Color doorColor = Color.web("#f59e0b");
        private String musicTrackId = "menu";

        private Builder(int id, String title) {
            this.id = id;
            this.title = title;
        }

        public Builder subtitle(String subtitle) {
            this.subtitle = subtitle;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public Builder mechanicLabel(String mechanicLabel) {
            this.mechanicLabel = mechanicLabel;
            return this;
        }

        public Builder previousLevelId(int previousLevelId) {
            this.previousLevelId = previousLevelId;
            return this;
        }

        public Builder nextLevelId(int nextLevelId) {
            this.nextLevelId = nextLevelId;
            return this;
        }

        public Builder worldSize(double worldWidth, double worldHeight) {
            this.worldWidth = worldWidth;
            this.worldHeight = worldHeight;
            return this;
        }

        public Builder gravity(double gravity) {
            this.gravity = gravity;
            return this;
        }

        public Builder moveSpeed(double moveSpeed) {
            this.moveSpeed = moveSpeed;
            return this;
        }

        public Builder jumpVelocity(double jumpVelocity) {
            this.jumpVelocity = jumpVelocity;
            return this;
        }

        public Builder backgroundColor(Color backgroundColor) {
            this.backgroundColor = backgroundColor;
            return this;
        }

        public Builder panelColor(Color panelColor) {
            this.panelColor = panelColor;
            return this;
        }

        public Builder playerColor(Color playerColor) {
            this.playerColor = playerColor;
            return this;
        }

        public Builder platformColor(Color platformColor) {
            this.platformColor = platformColor;
            return this;
        }

        public Builder accentColor(Color accentColor) {
            this.accentColor = accentColor;
            return this;
        }

        public Builder doorColor(Color doorColor) {
            this.doorColor = doorColor;
            return this;
        }

        public Builder musicTrackId(String musicTrackId) {
            this.musicTrackId = musicTrackId;
            return this;
        }

        public LevelConfig build() {
            return new LevelConfig(this);
        }
    }
}
