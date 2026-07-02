package level.level4;

import config.GameConfig;
import core.AppRouter;
import entity.FloorButton;
import entity.MovePlatform;
import entity.Portal;
import javafx.scene.paint.Color;
import level.BaseLevel;
import entity.Door;

/*
 * Level 4 - Two-Layer Portal & Button Puzzle
 *
 * Goal:
 * - Player spawns on the bottom layer.
 * - Push bottom box onto bottom button -> Raises Platform 1.
 * - Take the portal to the top layer.
 * - Push top box onto top button -> Lowers Platform 2.
 * - Cross the completed bridge to the goal!
 */

public final class Level4 extends BaseLevel {

    private static final String PORTAL_LEFT = "Pictures/Portal/portal_left.png";
    private static final String PORTAL_RIGHT = "Pictures/Portal/portal_right.png";
    private static final String PLATFORMB = "Pictures/Platforms/blue.png";
    private static final String PLATFORMG = "Pictures/Platforms/green.png";
    private static final String BRANCHG = "Pictures/Branch/grass_branch.png";
    private static final String BRANCHN = "Pictures/Branch/normal_branch.png";
    private static final String BRANCHB = "Pictures/Branch/black_branch.png";
    private static final String BUTTONG = "Pictures/buttons/green_btn.png";
    private static final String BUTTONB = "Pictures/buttons/blue_btn.png";
    private static final String BOX = "Pictures/Box/box.png";
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background.png";
    
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final String GOAL_DOOR_IMAGE = "Pictures/Portal/door2.png"; 
    private final Door goalDoor = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, GOAL_DOOR_IMAGE);

    private static final double PLATFORM1_REST_Y = 330;
    private static final double PLATFORM1_ACTIVE_Y = 650;
    private static final double PLATFORM2_REST_Y = 330; 
    private static final double PLATFORM2_ACTIVE_Y = 650; 
    private static final double PLATFORM_SPEED = 180;

    // Level objects
    private FloorButton button1;
    private FloorButton button2;
    private MovePlatform triggeredPlatform1;
    private MovePlatform triggeredPlatform2;

    public Level4(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected double getSpawnX() {
        return 630;
    }

    @Override
    protected double getSpawnY() {
        return 330 - config.getPlayerHeight();
    }

    @Override
    protected String getLevelTitle() {
        return "Level 4";
    }

    @Override
    protected String getBackgroundImagePath() {
        return "./Pictures/Backgrounds/jungle_background.png";
    }


    @Override
    protected void buildLevel() {
    	setBackgroundImage(BACKGROUND_IMAGE);

        addSolidBlock(0, 650, 160, 30, BRANCHG);
        addSolidBlock(160, 650, 160, 30, BRANCHG);
        addSolidBlock(config.getWorldWidth() - 160, 650, 160, 30, BRANCHG);
        addSolidBlock(config.getWorldWidth() - 320, 650, 160, 30, BRANCHG);
        
        //top layer 
        for (int i = 0; i < config.getWorldWidth(); i += 160) {
            addSolidBlock(i, 200, 160, 30, BRANCHN);
        }
        
        //second layer
        for (int i = 0; i < config.getWorldWidth(); i += 160) {
            addSolidBlock(i, 420, 160, 30, BRANCHB);
        }

        Portal bottomPortal = addPortal(50, 550, 70, 100, PORTAL_LEFT);
        Portal centralPortalR = addPortal(config.getWorldWidth()-150, 310, 70, 100, PORTAL_RIGHT);
        
        Portal centralPortalL = addPortal(50, 310, 70, 100, PORTAL_LEFT);
        Portal topPortal = addPortal(config.getWorldWidth()-150, 100, 70, 100, PORTAL_RIGHT);

        bottomPortal.linkTo(centralPortalR);
        centralPortalR.linkTo(bottomPortal);
        
        centralPortalL.linkTo(topPortal);
        topPortal.linkTo(centralPortalL);
        
        button1 = addFloorButton(820, 190, 40, 20, BUTTONG);
        addPushableBlock(230, 600, 35, 35, Color.web("#8b5a2b"), BOX); // Box 1

        button2 = addFloorButton(400, 190, 40, 20, BUTTONB);
        addPushableBlock(230, 310, 35, 35, Color.web("#8b5a2b"), BOX); // Box 2

        triggeredPlatform1 = new MovePlatform(450, PLATFORM2_REST_Y, 100, 24, config.getBlockColor(), PLATFORMG, MovePlatform.Direction.VERTICAL, PLATFORM2_REST_Y, PLATFORM2_ACTIVE_Y, PLATFORM_SPEED);
        triggeredPlatform1.setManualControl(true);
        movePlatforms.add(triggeredPlatform1);
        root.getChildren().add(triggeredPlatform1.getNode());

        triggeredPlatform2 = new MovePlatform(750, PLATFORM2_REST_Y, 100, 24, config.getBlockColor(), PLATFORMB, MovePlatform.Direction.VERTICAL, PLATFORM2_REST_Y, PLATFORM2_ACTIVE_Y, PLATFORM_SPEED);
        triggeredPlatform2.setManualControl(true);
        movePlatforms.add(triggeredPlatform2);
        root.getChildren().add(triggeredPlatform2.getNode());

        goalDoor.setPosition(1200, 660 - DOOR_HEIGHT);
        root.getChildren().add(goalDoor.getNode());
    }

    @Override
    protected void onAfterUpdate(double deltaSeconds) {
        if (player.getBounds().intersects(goalDoor.getBounds())) 
            onGoalReached();
            
        double targetY1 = button1.isPressed() ? PLATFORM1_ACTIVE_Y : PLATFORM1_REST_Y;
        triggeredPlatform1.moveToY(targetY1, deltaSeconds);

        double targetY2 = button2.isPressed() ? PLATFORM2_ACTIVE_Y : PLATFORM2_REST_Y;
        triggeredPlatform2.moveToY(targetY2, deltaSeconds);
    }

    @Override
    protected int getPreviousLevelId() {
        return 3;
    }

    @Override
    protected int getNextLevelId() {
        return 5;
    }
}