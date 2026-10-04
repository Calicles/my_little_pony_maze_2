package com.antoine.manager.musique;

import org.junit.Test;

import static org.junit.Assert.*;

public class JukeboxTest {

    @Test
    public void volumesParDefaut() {
        Jukebox jukebox= new Jukebox();

        assertEquals(0.1f, jukebox.getMusicVolume(), 0f);
        assertEquals(0.5f, jukebox.getSoundVolume(), 0f);
    }

    @Test
    public void changementDeVolume() {
        Jukebox jukebox= new Jukebox();
        jukebox.setMusic("apple,/ressources/sons/bruitage/trotDur.wav");
        jukebox.setSound("apple,/ressources/sons/bruitage/stepSnow.wav");

        jukebox.setMusicVolume(0.3f);
        jukebox.setSoundVolume(0.7f);

        assertEquals(0.3f, jukebox.getMusicVolume(), 0f);
        assertEquals(0.7f, jukebox.getSoundVolume(), 0f);
    }
}
