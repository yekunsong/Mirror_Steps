package core;

import audio.AudioManager;
import config.GameConfig;
import level.LevelRuntime;

/*
 * Shared session state container.
 * This core-layer class stores data that must survive scene switches, such as the
 * global config, the audio service, and the currently active level runtime.
 * Teammates should keep level-specific state out of this class.
 */
public final class GameManager {

    /*
     * High-level game state enumeration.
     * This nested enum stays with the session state container because it is only
     * used to describe which screen is currently active.
     */
    public enum GameState {
        MENU,
        LEVEL_SELECT,
        PLAYING,
        PAUSED
    }

    private final GameConfig gameConfig;
    private final AudioManager audioManager;
    private GameState state = GameState.MENU;
    private LevelRuntime currentLevel;

    public GameManager(GameConfig gameConfig, AudioManager audioManager) {
        this.gameConfig = gameConfig;
        this.audioManager = audioManager;
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public AudioManager getAudioManager() {
        return audioManager;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public LevelRuntime getCurrentLevel() {
        return currentLevel;
    }

    public void setCurrentLevel(LevelRuntime currentLevel) {
        this.currentLevel = currentLevel;
    }
}
