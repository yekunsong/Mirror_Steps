package core;

import config.GameConfig;
import javafx.application.Application;
import javafx.stage.Stage;

/*
 * Single JavaFX entry point for the whole project.
 *
 * This class replaces the older multi-step startup chain.
 * The startup logic is now intentionally short:
 * 1. create one shared GameConfig
 * 2. create one AppRouter
 * 3. open the Menu scene
 *
 * Relationship notes:
 * - Main is not a parent class of AppRouter
 * - Main only creates shared objects, then hands control to AppRouter
 *
 * Future extension directions:
 * - add icon loading
 * - add save file loading
 * - add splash screen support
 *
 * If you want to change the fixed window size, edit the numbers below or move them
 * into constants if your team prefers.
 */
public final class Main extends Application {

    private static final double FIXED_STAGE_WIDTH = 1280;
    private static final double FIXED_STAGE_HEIGHT = 720;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        GameConfig config = new GameConfig(FIXED_STAGE_WIDTH, FIXED_STAGE_HEIGHT);
        AppRouter router = new AppRouter(stage, config);
        router.showMenu();
    }
}
