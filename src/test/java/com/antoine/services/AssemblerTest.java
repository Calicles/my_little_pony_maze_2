package com.antoine.services;

import com.antoine.modele.level.Level;
import com.antoine.modele.level.Level4;
import org.junit.Test;

import static org.junit.Assert.*;

public class AssemblerTest {

    private String path= String.valueOf(getClass().getResource("/config/conf.xml"));

    @Test(expected = RuntimeException.class)
    public void newInstanceIdInconnue() {
        Assembler ass= new Assembler(path);
        ass.newInstance("taratata");
    }

    @Test
    public void newInstance() {
        Assembler ass= new Assembler(path);
        Level level= (Level) ass.newInstance("levelApple");
        assertEquals(40, level.getPlayerX());
        assertEquals(50, level.getPlayerY());
        assertNotNull(level.getMap());
        assertEquals(34, level.getPlayer().getImage().getWidth());
    }

    @Test
    public void newInstance2(){
        Assembler assembler= new Assembler(path);
        Level4 level= (Level4) assembler.newInstance("levelTwilight");
        assertNotNull(level.getBoss());
    }
}
