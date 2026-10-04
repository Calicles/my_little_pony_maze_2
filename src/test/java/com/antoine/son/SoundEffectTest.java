package com.antoine.son;

import org.junit.Test;

import static org.junit.Assert.*;

public class SoundEffectTest {

    String path= "/ressources/sons/bruitage/stepSnow.wav";

    @Test
    public void adjustVolumeLongueur() {
        SoundEffect effect= new SoundEffect(path, 0.5f);

        assertEquals(128, effect.adjustVolume(0, 128).length);
    }

    @Test(expected = RuntimeException.class)
    public void fichierAbsent() {
        new SoundEffect("/ressources/sons/taratata.wav", 0.5f);
    }
}
