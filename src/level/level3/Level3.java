package level.level3;

import config.GameConfig;
import core.AppRouter;
import entity.FloorButton;
import entity.MovePlatform;
import entity.Portal;
import javafx.scene.paint.Color;
import level.BaseLevel;
import entity.Door;

/*
 * Level 3 - Vertical Elevator Tower + Horizontal Shuttle + Portal + Door Puzzle
 */
public final class Level3 extends BaseLevel {

    private static final String PORTAL_LEFT = "Pictures/Portal/portal_left.png";
    private static final String PORTAL_RIGHT = "Pictures/Portal/portal_right.png";
    private static final String PLATFORMB = "Pictures/Platforms/blue.png";
    private static final String PLATFORMG = "Pictures/Platforms/green.png";
    private static final String PLATFORMR = "Pictures/Platforms/red.png";
    private static final String PLATFORMP = "Pictures/Platforms/purple.png";
    private static final String BRANCHB = "Pictures/Branch/black_branch.png";
    private static final String BRANCHG = "Pictures/Branch/grass_branch.png";
    private static final String BRANCHN = "Pictures/Branch/normal_branch.png";
    private static final String BUTTONG = "Pictures/buttons/green_btn.png";
    private static final String BUTTONP = "Pictures/buttons/purple_btn.png";
    private static final String BUTTONB = "Pictures/buttons/blue_btn.png";
    private static final String BUTTONR = "Pictures/buttons/red_btn.png";
    private static final String GOLDV = "Pictures/Platforms/goldV.png";
    private static final String GOLD = "Pictures/Platforms/gold.png";
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background.png";
    private static final String BOX = "Pictures/Box/box.png";
    
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final String GOAL_DOOR_IMAGE = "Pictures/Portal/door2.png"; 
    private final Door goalDoor = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, GOAL_DOOR_IMAGE);

    // Floor heights & dimensions
    private static final double GROUND_Y = 680;

    private static final double ELEVATOR_W = 110;
    private static final double PLATFORM_SPEED = 80;

    // ==========================================
    // PLATFORM MOVING POINTS (DIRECT VALUES)
    // ==========================================
    // Shared horizontal bounds for BOTH Elevator A and Elevator B
    // (they move the same way, just at different heights)
    private static final double PLATFORM_REST_X = 1150;
    private static final double PLATFORM_ACTIVE_X = 1050;

    // Elevator A height
    private static final double ELEVATOR_A_Y = 450;

    // Elevator B height (placed LOWER than A, i.e. a bigger Y value)
    private static final double ELEVATOR_B_Y = 300;
    
    private static final double ELEVATOR_C_Y = 140;

    // Platform 3 (Elevator Door) Bounds
    private static final double PLATFORM3_REST_Y = 680;
    private static final double PLATFORM3_ACTIVE_Y = 180;

    // Buttons
    private FloorButton button1;
    private FloorButton button2;
    private FloorButton button3;
    private FloorButton button4;

    // Elevators / platforms
    private MovePlatform elevatorA;
    private MovePlatform elevatorB;
    private MovePlatform elevatorC;
    private MovePlatform elevatorDoor;

    public Level3(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getLevelTitle() {
        return "Level 3";
    }
    
    @Override
    protected double getSpawnX() {
        return 630;
    }

    @Override
    protected double getSpawnY() {
        return 550 - config.getPlayerHeight();
    }

    @Override
    protected String getBackgroundImagePath() {
        return "./Pictures/Backgrounds/jungle_background.png";
    }

    @Override
    protected void buildLevel() {
    	setBackgroundImage(BACKGROUND_IMAGE);

        Portal portalA = addPortal(20, GROUND_Y - 130, 70, 100, PORTAL_LEFT);
        Portal portalB = addPortal(600, 80, 70, 100, PORTAL_RIGHT);

        portalA.linkTo(portalB);
        portalB.linkTo(portalA);

        button1 = addFloorButton(50, 490, 40, 20, BUTTONB);
        button2 = addFloorButton(600, 290, 40, 20, BUTTONG);
        button3 = addFloorButton(900, GROUND_Y-50, 40, 20, BUTTONR);

        button4 = addFloorButton(config.getWorldWidth() - 90, GROUND_Y - 50, 40, 20, BUTTONP);

        addPushableBlock(config.getWorldWidth() - 90, 50, 35, 35, Color.web("#8b5a2b"), BOX);

        elevatorA = new MovePlatform(
                PLATFORM_REST_X, ELEVATOR_A_Y, ELEVATOR_W, 24,
                config.getBlockColor(), PLATFORMB,
                MovePlatform.Direction.HORIZONTAL,
                PLATFORM_ACTIVE_X, PLATFORM_REST_X, PLATFORM_SPEED);
        elevatorA.setManualControl(true);
        movePlatforms.add(elevatorA);
        root.getChildren().add(elevatorA.getNode());

        elevatorB = new MovePlatform(
                PLATFORM_REST_X, ELEVATOR_B_Y, ELEVATOR_W, 24,
                config.getBlockColor(), PLATFORMG,
                MovePlatform.Direction.HORIZONTAL,
                PLATFORM_ACTIVE_X, PLATFORM_REST_X, PLATFORM_SPEED);
        elevatorB.setManualControl(true);
        movePlatforms.add(elevatorB);
        root.getChildren().add(elevatorB.getNode());
        
        elevatorC = new MovePlatform(
                PLATFORM_REST_X, ELEVATOR_C_Y, ELEVATOR_W, 24,
                config.getBlockColor(), PLATFORMR,
                MovePlatform.Direction.HORIZONTAL,
                PLATFORM_ACTIVE_X, PLATFORM_REST_X, PLATFORM_SPEED);
        elevatorC.setManualControl(true);
        movePlatforms.add(elevatorC);
        root.getChildren().add(elevatorC.getNode());

        elevatorDoor = new MovePlatform(
                350, GROUND_Y, ELEVATOR_W, 24,
                config.getBlockColor(), PLATFORMP,
                MovePlatform.Direction.VERTICAL,
                PLATFORM3_ACTIVE_Y, PLATFORM3_REST_Y, PLATFORM_SPEED);
        elevatorDoor.setManualControl(true);
        movePlatforms.add(elevatorDoor);
        root.getChildren().add(elevatorDoor.getNode());

        for (int i = 0; i <=720; i += 140) {
            addSolidBlock(1150, i, 24, 140, GOLDV);
            addSolidBlock(1260, i , 24, 140, GOLDV);
        }
        	
        for (int i=0; i<config.getWorldWidth();i+=160)
        	addBlock(i, config.getWorldHeight() - 80, 160, 40, BRANCHG);  
        		
        addSolidBlock(1150, 0 , 140, 24, GOLD);

        
        addBlock(0, 130, 160, 30, BRANCHG);
        addBlock(0, 500, 160, 30, BRANCHN);
        addBlock(550, 300, 160, 30, BRANCHB);

        goalDoor.setPosition(50, 145 - DOOR_HEIGHT);
        root.getChildren().add(goalDoor.getNode());
    }

    @Override
    protected void onAfterUpdate(double deltaSeconds) {
        if (player.getBounds().intersects(goalDoor.getBounds())) 
            onGoalReached();
            
        // Elevator A response (HORIZONTAL -> use moveToX)
        double targetA = button1.isPressed() ? PLATFORM_ACTIVE_X : PLATFORM_REST_X;
        elevatorA.moveToX(targetA, deltaSeconds);

        // Elevator B response (HORIZONTAL -> use moveToX)
        double targetB = button2.isPressed() ? PLATFORM_ACTIVE_X : PLATFORM_REST_X;
        elevatorB.moveToX(targetB, deltaSeconds);
        
        double targetC = button3.isPressed() ? PLATFORM_ACTIVE_X : PLATFORM_REST_X;
        elevatorC.moveToX(targetC, deltaSeconds);

        // Elevator Door response (VERTICAL -> use moveToY)
        double targetDoor = button4.isPressed() ? PLATFORM3_ACTIVE_Y : PLATFORM3_REST_Y;
        elevatorDoor.moveToY(targetDoor, deltaSeconds);
    }

    @Override
    protected int getPreviousLevelId() {
        return 2;
    }

    @Override
    protected int getNextLevelId() {
        return 4;
    }
}