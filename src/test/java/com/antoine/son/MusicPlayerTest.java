package com.antoine.son;

import org.junit.Test;

import static org.junit.Assert.*;

public class MusicPlayerTest {

    String path= "/ressources/sons/bruitage/trotDur.wav";

    @Test
    public void adjustVolumeMoitie() {
        MusicPlayer player= new MusicPlayer(path, 0.5f);
        // 1000 en little-endian : 0xE8 0x03
        byte[] samples= {(byte) 0xE8, 0x03};

        byte[] result= player.adjustVolume(samples);

        assertEquals(500, (short) ((result[1] << 8) | (result[0] & 0xff)));
    }

    @Test
    public void adjustVolumeMuet() {
        MusicPlayer player= new MusicPlayer(path, 0f);
        byte[] result= player.adjustVolume(new byte[]{(byte) 0xE8, 0x03});

        assertArrayEquals(new byte[]{0, 0}, result);
    }

    @Test
    public void setVolume() {
        MusicPlayer player= new MusicPlayer(path, 0.5f);
        player.setVolume(0.2f);

        assertEquals(0.2f, player.volume, 0f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void volumeTropFort() {
        new MusicPlayer(path, 2f);
    }

    @Test(expected = IllegalArgumentException.class)
    public void volumeNegatif() {
        new MusicPlayer(path, 0.5f).setVolume(-1f);
    }

    @Test
    public void arretSansLecture() {
        MusicPlayer player= new MusicPlayer(path, 0.5f);
        player.pause();
        player.arret();

        assertFalse(player.using);
    }
}
