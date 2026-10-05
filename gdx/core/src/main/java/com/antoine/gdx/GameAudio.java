package com.antoine.gdx;

import com.antoine.modele.GameSession;
import com.antoine.modele.level.AbstractLevel;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Musique de chaque niveau et bruits de pas.</b>
 * <p>Chaque niveau a sa musique ; changer de niveau met la précédente en pause, pour la
 * reprendre où elle en était. Les volumes réglés sont mémorisés.</p>
 */
final class GameAudio implements GameSession.Listener, Disposable {

    /**Volume maximal des curseurs, comme dans la version d'origine (0 à 50 %)*/
    static final float MAX_VOLUME= 0.5f;

    /**Durée du bruit de pas : il n'est pas rejoué tant qu'il n'est pas fini*/
    private static final long STEP_SOUND_MILLIS= 850;

    private static final String[] FIRST_LEVELS= {"apple", "rarity", "rainbow"};

    private final Preferences preferences;
    private final Map<String, Music> musics= new HashMap<>();
    private final Sound stepSound;
    private Music playing;
    private String playingName;
    private long lastStep;
    private float musicVolume, soundVolume;

    GameAudio() {
        preferences= Gdx.app.getPreferences("ponymaze");
        musicVolume= preferences.getFloat("musicVolume", 0.1f);
        soundVolume= preferences.getFloat("soundVolume", 0.5f);
        stepSound= Gdx.audio.newSound(Gdx.files.internal("ressources/sons/bruitage/stepSnow.wav"));
    }

    @Override
    public void levelChanged(AbstractLevel level) {
        String name= level.getName();
        if (name.equals(playingName))
            return;
        if (playing != null)
            playing.pause();

        // Après les trois premiers niveaux, leurs musiques ne servent plus.
        if (!isFirstLevel(name))
            for (String first : FIRST_LEVELS) {
                Music music= musics.remove(first);
                if (music != null) music.dispose();
            }

        Music music= musics.get(name);
        if (music == null) {
            music= Gdx.audio.newMusic(Gdx.files.internal("ressources/sons/musics/" + name + ".ogg"));
            music.setLooping(true);
            musics.put(name, music);
        }
        music.setVolume(musicVolume);
        music.play();
        playing= music;
        playingName= name;
    }

    private static boolean isFirstLevel(String name) {
        for (String first : FIRST_LEVELS)
            if (first.equals(name)) return true;
        return false;
    }

    @Override
    public void step() {
        long now= TimeUtils.millis();
        if (now - lastStep >= STEP_SOUND_MILLIS) {
            lastStep= now;
            stepSound.play(soundVolume);
        }
    }

    float getMusicVolume() {return musicVolume;}

    float getSoundVolume() {return soundVolume;}

    void setMusicVolume(float volume) {
        musicVolume= clamp(volume);
        if (playing != null)
            playing.setVolume(musicVolume);
        preferences.putFloat("musicVolume", musicVolume);
    }

    void setSoundVolume(float volume) {
        soundVolume= clamp(volume);
        preferences.putFloat("soundVolume", soundVolume);
    }

    /**Enregistre les volumes (appelé quand l'application passe en arrière-plan).*/
    void save() {
        preferences.flush();
    }

    private static float clamp(float volume) {
        return Math.max(0, Math.min(MAX_VOLUME, volume));
    }

    @Override
    public void dispose() {
        for (Music music : musics.values())
            music.dispose();
        musics.clear();
        stepSound.dispose();
    }
}
