package ui;

import config.GameConfig;
import core.AppRouter;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

/*
 * Main menu view.
 *
 * Architectural role:
 * - This class is responsible only for constructing the menu user interface.
 * - It must not contain gameplay rules, movement code, collision logic, or direct
 *   stage navigation logic.
 *
 * Interaction model:
 * - The router passes itself into this class.
 * - Each button directly calls a simple navigation method on AppRouter.
 * - The class therefore remains a view layer rather than becoming a controller for
 *   the whole application.
 *
 * Extension guidance:
 * - A future revision can add additional buttons such as Credits, Instructions, or
 *   Continue without changing the core architecture.
 */
public final class MenuView {

    private ImageView makeImageButton(String imagePath, double width, double height, Runnable onClick) {
        Image img = new Image(AppRouter.resourceUri("Pictures/UI/" + imagePath));
        ImageView iv = new ImageView(img);
        iv.setFitWidth(width);
        iv.setFitHeight(height);
        iv.setPreserveRatio(true);
        iv.setStyle("-fx-cursor: hand;");
        iv.setOnMouseEntered(e  -> { iv.setScaleX(1.08); iv.setScaleY(1.08); });
        iv.setOnMouseExited(e   -> { iv.setScaleX(1.0);  iv.setScaleY(1.0);  });
        iv.setOnMousePressed(e  -> { iv.setScaleX(0.96); iv.setScaleY(0.96); });
        iv.setOnMouseReleased(e -> { iv.setScaleX(1.08); iv.setScaleY(1.08); });
        iv.setOnMouseClicked(e  -> onClick.run());
        return iv;
    }

    public Scene createScene(GameConfig config, AppRouter router) {

        Image bgImage = new Image(AppRouter.resourceUri("Pictures/Backgrounds/main_background.png"));
        ImageView bgView = new ImageView(bgImage);
        bgView.setFitWidth(config.getWorldWidth());
        bgView.setFitHeight(config.getWorldHeight());
        bgView.setPreserveRatio(false);

        Image cardImage = new Image(AppRouter.resourceUri("Pictures/UI/menu_map2.png"));
        ImageView cardView = new ImageView(cardImage);
        cardView.setPreserveRatio(true);   
        cardView.setFitWidth(530);         

        // ── BUTTONS ───────────────────────────────────────────────────────────
        Label title = new Label("Mirror Steps");
        title.getStyleClass().add("app-title");

        ImageView startButton    = makeImageButton("start_button.png",    220, 60, () -> router.showLevel(1));
        ImageView level1Button = makeImageButton("levels_button.png", 220, 60, () -> router.showLevels());
        ImageView settingsButton = makeImageButton("settings_button.png", 220, 60, () -> router.showSettings());
        ImageView exitButton     = makeImageButton("exit_button.png",     220, 60, () -> router.closeApp());

        // ── BUTTONS ON TOP OF CARD IMAGE ──────────────────────────────────────
        VBox buttons = new VBox(14, title, startButton, level1Button, settingsButton, exitButton);
        buttons.setAlignment(Pos.CENTER);

        // card image behind buttons, both centered together
        StackPane cardStack = new StackPane(cardView, buttons);
        cardStack.setAlignment(Pos.CENTER);

        // ── FULL LAYOUT ───────────────────────────────────────────────────────
        // background behind everything, cardStack centered on top
        StackPane root = new StackPane(bgView, cardStack);
        root.setAlignment(Pos.CENTER);
        root.setMinSize(config.getWorldWidth(), config.getWorldHeight());
        root.setPrefSize(config.getWorldWidth(), config.getWorldHeight());

        return new Scene(root, config.getWorldWidth(), config.getWorldHeight());
    }
}
