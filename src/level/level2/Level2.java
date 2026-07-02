package level.level2;

import config.GameConfig;
import core.AppRouter;
import entity.Door;
import entity.FloorButton;
import entity.GameObject;
import entity.MovePlatform;
import entity.PushableBlock;
import javafx.scene.paint.Color;
import level.BaseLevel;
import entity.Door;

/*
 * Level 2 - Button puzzle level.
 *
 * Goal:
 * - The player pushes a block onto a floor button.
 * - When the button is pressed (by the player OR the block),
 *   a moving platform rises to help the player reach the goal.
 */

public final class Level2 extends BaseLevel {

    private static final double PLATFORM_REST_Y = 500;
    private static final double PLATFORM_ACTIVE_Y = 250;
    private static final double PLATFORM_SPEED = 180;

    private static final String BRANCHG = "Pictures/Branch/grass_branch.png";
    private static final String BRANCHN = "Pictures/Branch/normal_branch.png";
    private static final String BRANCHB = "Pictures/Branch/black_branch.png";
    private static final String PLATFORMB = "Pictures/Platforms/blue.png";
    private static final String BUTTONB = "Pictures/buttons/blue_btn.png";
    private static final String BOX = "Pictures/Box/box.png";
    private static final String BACKGROUND_IMAGE = "Pictures/Backgrounds/jungle_background.png";
    
    private static final double DOOR_WIDTH = 52;
    private static final double DOOR_HEIGHT = 86;
    private static final String GOAL_DOOR_IMAGE = "Pictures/Portal/door2.png"; 
    private final Door goalDoor = new Door(0, 0, DOOR_WIDTH, DOOR_HEIGHT, GOAL_DOOR_IMAGE);

    private FloorButton triggerButton;
    private PushableBlock pushableBlock;
    private MovePlatform triggeredPlatform;

    public Level2(GameConfig config, AppRouter router) {
        super(config, router);
    }

    @Override
    protected String getBackgroundImagePath() {
        return "./Pictures/Backgrounds/jungle_background.png";
    }

    @Override
    protected String getLevelTitle() {
        return "Level 2";
    }

    @Override
    protected void buildLevel() {
    	setBackgroundImage(BACKGROUND_IMAGE);

        // Branch platforms
        addBlock(0, 500, 140, 24, BRANCHN);
        addBlock(200, 350, 140, 24, BRANCHN);

        // Floor button
        triggerButton = new FloorButton(350, config.getWorldHeight() - 95, 40, 20, BUTTONB);
        root.getChildren().add(triggerButton.getNode());

        // Pushable block (managed by BaseLevel)
        addPushableBlock(80, config.getWorldHeight() - 300, 35, 35, Color.web("#8b5a2b"), BOX);
        pushableBlock = pushableBlocks.get(pushableBlocks.size() - 1);

        // Button-controlled moving platform
        triggeredPlatform = new MovePlatform(550, PLATFORM_REST_Y, 140, 24, config.getBlockColor(), PLATFORMB, MovePlatform.Direction.VERTICAL,PLATFORM_ACTIVE_Y, PLATFORM_REST_Y,PLATFORM_SPEED);
       
        triggeredPlatform.setManualControl(true);
        movePlatforms.add(triggeredPlatform);
        root.getChildren().add(triggeredPlatform.getNode());  
        
        addBlock(900, 130, 160, 30, BRANCHB);
        
        for (int i=0; i<config.getWorldWidth();i+=160)
        	addBlock(i, config.getWorldHeight() - 80, 160, 40, BRANCHG); 
        
        goalDoor.setPosition(1000, 145 - DOOR_HEIGHT);
        root.getChildren().add(goalDoor.getNode());
    }
    
    @Override
    protected void onAfterUpdate(double deltaSeconds) {
        if (player.getBounds().intersects(goalDoor.getBounds())) 
            onGoalReached();
        
        boolean pressed =
                isOnButton(player, triggerButton)
                || isOnButton(pushableBlock, triggerButton);

        triggerButton.setPressed(pressed);

        double targetY = pressed ? PLATFORM_ACTIVE_Y : PLATFORM_REST_Y;
        triggeredPlatform.moveToY(targetY, deltaSeconds);
    }

    private boolean isOnButton(GameObject obj, FloorButton button) {
        if (obj == null || button == null) {
            return false;
        }

        double objLeft = obj.getX();
        double objRight = obj.getX() + obj.getWidth();
        double objBottom = obj.getY() + obj.getHeight();

        double buttonLeft = button.getX();
        double buttonRight = button.getX() + button.getWidth();
        double buttonTop = button.getY();
        double buttonBottom = button.getY() + button.getHeight();

        boolean horizontallyOverlapping =
                objRight > buttonLeft && objLeft < buttonRight;

        boolean standingOnButton =
                objBottom >= buttonTop && objBottom <= buttonBottom + 12;

        return horizontallyOverlapping && standingOnButton;
    }
    

    @Override
    protected int getPreviousLevelId() {
        return 1;
    }

    @Override
    protected int getNextLevelId() {
        return 3;
    }
}