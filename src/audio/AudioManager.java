package audio;

/*
 * Shared audio gateway for the whole game.
 * This package belongs to the framework layer and should hide any future media
 * implementation details from the menu and level packages.
 * Teammates can replace the placeholder hooks with real music and sound effects later.
 */
public final class AudioManager {

    private String currentTrackId = "";
    private double masterVolume = 0.75;

    public void playMenuMusic() {
        playTrack("menu");
    }

    public void playLevelMusic(String trackId) {
        playTrack(trackId);
    }

    public void stopMusic() {
        currentTrackId = "";
    }

    public void playEffect(String effectId) {
    }

    public String getCurrentTrackId() {
        return currentTrackId;
    }

    public double getMasterVolume() {
        return masterVolume;
    }

    public void setMasterVolume(double masterVolume) {
        this.masterVolume = Math.max(0.0, Math.min(1.0, masterVolume));
    }

    private void playTrack(String trackId) {
        currentTrackId = trackId == null ? "" : trackId;
    }
}
