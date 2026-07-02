package level.level1;

import config.GameConfig;
import core.AppRouter;
import entity.Door;
import level.BaseLevel;
import javafx.scene.paint.Color;


/**
 * Concrete scene implementation for Level 1.
 * 
 * Handles the specific layout, goals, and unique background path 
 * for the first playable stage while leveraging the BaseLevel framework.
 */
public final class Level1 extends BaseLevel {
    private static final String BRANCHG = "Pictures/Branch/grass_branch.png";
    private static final String BRANCHN = "Pictures/Branch/normal_branch.png";
    private static final String BOX = "Pictures/Box/box.png";
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background.png";
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final String GOAL_DOOR_IMAGE = "Pictures/Portal/door2.png"; 
    
    private final Door goalDoor = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, GOAL_DOOR_IMAGE);
    
    public Level1(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 1";
    }
    
    @Override
    protected double getSpawnX() {
        return 70;
    }

    @Override
    protected double getSpawnY() {
        return 620 - config.getPlayerHeight();
    }

    @Override
    protected String getBackgroundImagePath() {
        return "./Pictures/Backgrounds/jungle_background.png";
    }

    @Override
    protected void buildLevel() {
    	setBackgroundImage(BACKGROUND_IMAGE);
        // Ground Floor Platform
        for (int i=0; i<config.getWorldWidth();i+=160)
        	addBlock(i, config.getWorldHeight() - 80, 160, 40, BRANCHG); 
        
        // First branch (Low left)
        addBlock(180, 420, 160, 30, BRANCHN);
        addBlock(440, 380, 160, 30, BRANCHG);
        addBlock(700, 340, 160, 30, BRANCHG);
        addBlock(940, 250, 160, 30, BRANCHN);
        addPushableBlock(1000, config.getWorldHeight() - 150, 35, 35, Color.web("#8b5a2b"), BOX);

        // Level Exit Goal - placed standing on top of the last branch platform
        goalDoor.setPosition(1020, 265 - DOOR_HEIGHT);
        root.getChildren().add(goalDoor.getNode());
    }

    @Override
    protected void onAfterUpdate(double deltaSeconds) {
        // Check if the player has reached the goal door
        if (player.getBounds().intersects(goalDoor.getBounds())) {
            onGoalReached();
        }
    }

    @Override
    protected int getPreviousLevelId() {
        return 0; 
    }

    @Override
    protected int getNextLevelId() {
        return 2; 
    }
}