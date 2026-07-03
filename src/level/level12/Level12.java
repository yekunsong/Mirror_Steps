package level.level12;

import config.GameConfig;
import core.AppRouter;
import entity.Portal;
import entity.SolidBlock;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import level.BaseLevel;

/*
 * Level 12 — The Nine Chambers.
 *
 * The screen is divided into a 3x3 grid of sealed rooms, numbered 1 (bottom-left)
 * through 9 (top-right), reading left-to-right then bottom-to-top. Rooms 1-8 each
 * have a top-right and a bottom-right portal; exactly one of the two leads onward
 * to the next room's bottom-left arrival portal, the other resets the player back
 * to room 1. Room 9 only has an exit door. All portals share the same blue skin,
 * so the correct path through rooms 1-8 must be memorized rather than seen.
 */
public final class Level12 extends BaseLevel {

    //////// CONSTANTS ////////

    private static final String CURRENT_SECTION_BACKGROUND = "Pictures/Backgrounds/jungle_background.png";
    private static final String HIDDEN_SECTION_BACKGROUND = "Pictures/Backgrounds/brick_background.png";
    private static final String WALL_IMAGE = "Pictures/Platforms/grey_wall.png";
    private static final String PORTAL_IMAGE = "Pictures/Portal/portal_left.png";
    private static final String GOAL_IMAGE = "Pictures/Portal/door2.png";

    private static final double BACKGROUND_TILE_SIZE = 256.0;
    private static final double PLATFORM_TILE_WIDTH = 300.0;
    private static final double PLATFORM_TILE_HEIGHT = 169.0;

    private static final double WALL_THICKNESS = 30.0;
    private static final double PORTAL_WIDTH = 48.0;
    private static final double PORTAL_HEIGHT = 64.0;
    private static final double PORTAL_INSET = 20.0;
    private static final double GOAL_WIDTH = 36.0;
    private static final double GOAL_HEIGHT = 72.0;

    // Correct exit for each room 1-8, 'b' = bottom-right portal, 't' = top-right portal
    private static final char[] CORRECT_SEQUENCE = { 'b', 't', 'b', 'b', 't', 't', 'b', 'b' };

    //////// FIELDS ////////

    private final Rectangle topStrip = new Rectangle();
    private final Rectangle bottomStrip = new Rectangle();
    private final Rectangle leftStrip = new Rectangle();
    private final Rectangle rightStrip = new Rectangle();

    private final Canvas visionCanvas = new Canvas();

    //////// CONSTRUCTOR ////////

    public Level12(GameConfig config, AppRouter router) {
        super(config, router);
    }

    //////// ABSTRACT OVERRIDES ////////

    @Override
    protected String getLevelTitle() {
        return "Level 12";
    }

    @Override
    protected int getPreviousLevelId() {
        return 11;
    }

    @Override
    protected int getNextLevelId() {
        return 13;
    }

    //////// LEVEL LAYOUT ////////

    @Override
    protected void buildLevel() {
        setBackgroundImage(CURRENT_SECTION_BACKGROUND);

        buildGrid();
        buildPortals();
        buildSectionOverlay();
        configureVisionLayer();
    }

    private void configureVisionLayer() {
        visionCanvas.setWidth(config.getWorldWidth());
        visionCanvas.setHeight(config.getWorldHeight());
        visionCanvas.setMouseTransparent(true);
        root.getChildren().add(visionCanvas);
        updateVisionLayer();
    }

    private void buildGrid() {
        double worldWidth = config.getWorldWidth();
        double worldHeight = config.getWorldHeight();
        double sectionWidth = worldWidth / 3.0;
        double sectionHeight = worldHeight / 3.0;

        // Outer border
        addSolidBlockTiled(0, 0, worldWidth, WALL_THICKNESS, WALL_IMAGE);
        addSolidBlockTiled(0, worldHeight - WALL_THICKNESS, worldWidth, WALL_THICKNESS, WALL_IMAGE);
        addSolidBlockTiled(0, 0, WALL_THICKNESS, worldHeight, WALL_IMAGE);
        addSolidBlockTiled(worldWidth - WALL_THICKNESS, 0, WALL_THICKNESS, worldHeight, WALL_IMAGE);

        // Internal dividers, forming the 3x3 grid of sealed rooms
        addSolidBlockTiled(sectionWidth - WALL_THICKNESS / 2.0, 0, WALL_THICKNESS, worldHeight, WALL_IMAGE);
        addSolidBlockTiled(2 * sectionWidth - WALL_THICKNESS / 2.0, 0, WALL_THICKNESS, worldHeight, WALL_IMAGE);
        addSolidBlockTiled(0, sectionHeight - WALL_THICKNESS / 2.0, worldWidth, WALL_THICKNESS, WALL_IMAGE);
        addSolidBlockTiled(0, 2 * sectionHeight - WALL_THICKNESS / 2.0, worldWidth, WALL_THICKNESS, WALL_IMAGE);
    }

    private void addSolidBlockTiled(double x, double y, double width, double height, String imagePath) {
        SolidBlock block = new SolidBlock(x, y, width, height, config.getBlockColor());

        try {
            Image tileImage = new Image(new java.io.File(imagePath).toURI().toString());
            ImagePattern pattern = new ImagePattern(
                    tileImage,
                    0, 0,
                    PLATFORM_TILE_WIDTH, PLATFORM_TILE_HEIGHT,
                    false);

            ((Rectangle) block.getNode()).setFill(pattern);
        } catch (Exception e) {
            System.err.println("Failed to load tiled texture: " + e.getMessage());
        }

        solidBlocks.add(block);
        root.getChildren().add(block.getNode());
    }

    private void buildPortals() {
        Portal[] arrivals = new Portal[10];

        for (int i = 2; i <= 9; i++) {
            double[] box = sectionInterior(i);
            double x = box[0] + PORTAL_INSET;
            double y = box[3] - PORTAL_INSET - PORTAL_HEIGHT;
            arrivals[i] = addPortal(x, y, PORTAL_WIDTH, PORTAL_HEIGHT, PORTAL_IMAGE);
        }

        for (int i = 1; i <= 8; i++) {
            double[] box = sectionInterior(i);
            double exitX = box[2] - PORTAL_INSET - PORTAL_WIDTH;
            double topY = box[1] + PORTAL_INSET;
            double bottomY = box[3] - PORTAL_INSET - PORTAL_HEIGHT;

            Portal topPortal = addPortal(exitX, topY, PORTAL_WIDTH, PORTAL_HEIGHT, PORTAL_IMAGE);
            Portal bottomPortal = addPortal(exitX, bottomY, PORTAL_WIDTH, PORTAL_HEIGHT, PORTAL_IMAGE);

            Portal correctPortal = CORRECT_SEQUENCE[i - 1] == 't' ? topPortal : bottomPortal;
            Portal nextArrival = arrivals[i + 1];

            correctPortal.linkTo(nextArrival);
            nextArrival.linkTo(correctPortal);
        }

        double[] doorBox = sectionInterior(9);
        double doorX = doorBox[2] - PORTAL_INSET - GOAL_WIDTH;
        double doorY = doorBox[3] - PORTAL_INSET - GOAL_HEIGHT;
        setGoal(doorX, doorY);
        applyImageToGoal(GOAL_IMAGE);
    }

    private void buildSectionOverlay() {
        ImagePattern hiddenPattern;
        try {
            Image hiddenImage = new Image(new java.io.File(HIDDEN_SECTION_BACKGROUND).toURI().toString());
            hiddenPattern = new ImagePattern(hiddenImage, 0, 0, BACKGROUND_TILE_SIZE, 144, false);
        } catch (Exception e) {
            System.err.println("Failed to load hidden section overlay texture: " + e.getMessage());
            hiddenPattern = null;
        }

        for (Rectangle strip : new Rectangle[] { topStrip, bottomStrip, leftStrip, rightStrip }) {
            strip.setFill(hiddenPattern != null ? hiddenPattern : Color.DARKSLATEGRAY);
            strip.setMouseTransparent(true);
            root.getChildren().add(strip);
        }

        updateSectionOverlay();
    }

    // Returns { left, top, right, bottom } of the given room's walkable interior (1-9)
    private double[] sectionInterior(int index) {
        double worldWidth = config.getWorldWidth();
        double worldHeight = config.getWorldHeight();
        double sectionWidth = worldWidth / 3.0;
        double sectionHeight = worldHeight / 3.0;

        int col = (index - 1) % 3;
        int rowFromBottom = (index - 1) / 3;
        int row = 2 - rowFromBottom;

        double left = col == 0 ? WALL_THICKNESS : col * sectionWidth + WALL_THICKNESS / 2.0;
        double right = col == 2 ? worldWidth - WALL_THICKNESS : (col + 1) * sectionWidth - WALL_THICKNESS / 2.0;
        double top = row == 0 ? WALL_THICKNESS : row * sectionHeight + WALL_THICKNESS / 2.0;
        double bottom = row == 2 ? worldHeight - WALL_THICKNESS : (row + 1) * sectionHeight - WALL_THICKNESS / 2.0;

        return new double[] { left, top, right, bottom };
    }

    private void applyImageToGoal(String imagePath) {
        if (goal == null || imagePath == null) {
            return;
        }

        try {
            Image img = new Image(new java.io.File(imagePath).toURI().toString());

            goal.setFill(new ImagePattern(img));
        } catch (Exception e) {
            System.err.println("Failed to load goal image: " + e.getMessage());
        }
    }

    //////// SPAWN ////////

    @Override
    protected double getSpawnX() {
        double[] box = sectionInterior(1);
        return box[0] + PORTAL_INSET;
    }

    @Override
    protected double getSpawnY() {
        double[] box = sectionInterior(1);
        return box[3] - config.getPlayerHeight();
    }

    //////// UPDATE LOOP ////////

    @Override
    protected void onAfterUpdate(double deltaSeconds) {
        for (Portal portal : portals) {
            if (portal.getLinkedPortal() == null && !portal.isOnCooldown()
                    && player.getBounds().intersects(portal.getBounds())) {
                portal.startCooldown();
                onSolidPlayerOutOfWorld();
            }
        }

        updateSectionOverlay();
        updateVisionLayer();
    }

    private void updateVisionLayer() {
        double width = visionCanvas.getWidth();
        double height = visionCanvas.getHeight();
        double radius = config.getWorldWidth() / 3.0;

        GraphicsContext gc = visionCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        double centerX = player.getX() + player.getWidth() / 2.0;
        double centerY = player.getY() + player.getHeight() / 2.0;

        RadialGradient gradient = new RadialGradient(
                0, 0, centerX, centerY, radius, false, CycleMethod.NO_CYCLE,
                new Stop(0.0, Color.rgb(0, 0, 0, 0.0)),
                new Stop(0.9, Color.rgb(0, 0, 0, 0.0)),
                new Stop(1.0, Color.rgb(0, 0, 0, 1.0)));

        gc.setFill(gradient);
        gc.fillRect(0, 0, width, height);
    }

    private void updateSectionOverlay() {
        double worldWidth = config.getWorldWidth();
        double worldHeight = config.getWorldHeight();
        double sectionWidth = worldWidth / 3.0;
        double sectionHeight = worldHeight / 3.0;

        double centerX = player.getX() + player.getWidth() / 2.0;
        double centerY = player.getY() + player.getHeight() / 2.0;

        int col = (int) Math.max(0, Math.min(2, Math.floor(centerX / sectionWidth)));
        int row = (int) Math.max(0, Math.min(2, Math.floor(centerY / sectionHeight)));

        // An outer border wall sits entirely on this side (full thickness already visible),
        // but an internal divider is centered on the boundary, so only half its thickness
        // falls within the raw grid cell. Extend the reveal past internal boundaries by the
        // other half so every boundary wall shows its full, even thickness.
        double visLeft = col == 0 ? 0 : col * sectionWidth - WALL_THICKNESS / 2.0;
        double visTop = row == 0 ? 0 : row * sectionHeight - WALL_THICKNESS / 2.0;
        double visRight = col == 2 ? worldWidth : (col + 1) * sectionWidth + WALL_THICKNESS / 2.0;
        double visBottom = row == 2 ? worldHeight : (row + 1) * sectionHeight + WALL_THICKNESS / 2.0;

        topStrip.setX(0);
        topStrip.setY(0);
        topStrip.setWidth(worldWidth);
        topStrip.setHeight(visTop);

        bottomStrip.setX(0);
        bottomStrip.setY(visBottom);
        bottomStrip.setWidth(worldWidth);
        bottomStrip.setHeight(worldHeight - visBottom);

        leftStrip.setX(0);
        leftStrip.setY(visTop);
        leftStrip.setWidth(visLeft);
        leftStrip.setHeight(visBottom - visTop);

        rightStrip.setX(visRight);
        rightStrip.setY(visTop);
        rightStrip.setWidth(worldWidth - visRight);
        rightStrip.setHeight(visBottom - visTop);
    }

}
