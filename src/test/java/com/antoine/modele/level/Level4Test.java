package com.antoine.modele.level;

import com.antoine.entity.Boss;
import com.antoine.services.Assembler;
import org.junit.Test;

import static org.junit.Assert.*;

public class Level4Test {

    private String path= String.valueOf(getClass().getResource("/config/conf.xml"));

    /**
     * Le boss doit se déplacer quand il réfléchit.
     */
    @Test
    public void bossBouge() {
        Assembler assembleur= new Assembler(path);
        Level4 level= (Level4) assembleur.newInstance("levelTwilight");
        Boss boss= level.getBoss();
        int startX= boss.getX();
        int startY= boss.getY();

        for(int i= 0; i < 15; i++) {
            boss.memorizeMoves();
            boss.think();
        }

        assertTrue(boss.getX() != startX || boss.getY() != startY);
        assertNotNull(level.getPlayer());
    }
}
