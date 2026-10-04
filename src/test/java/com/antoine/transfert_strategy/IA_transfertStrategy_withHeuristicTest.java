package com.antoine.transfert_strategy;

import com.antoine.entity.Boss;
import com.antoine.modele.level.Level4;
import com.antoine.services.Assembler;
import org.junit.Test;

import static org.junit.Assert.*;

public class IA_transfertStrategy_withHeuristicTest {

    private String path= String.valueOf(getClass().getResource("/config/conf.xml"));

    private double distance(Boss boss, Level4 level) {
        int dx= boss.getX() - level.getPlayerX();
        int dy= boss.getY() - level.getPlayerY();
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Le boss doit se rapprocher du joueur (immobile) en suivant le chemin calculé.
     */
    @Test
    public void bossSeRapprocheDuJoueur() {
        Assembler assembler= new Assembler(path);
        Level4 level= (Level4) assembler.newInstance("levelTwilight");
        Boss boss= level.getBoss();
        double start= distance(boss, level);

        for (int i= 0; i < 200; i++) {
            boss.memorizeMoves();
            boss.think();
        }

        assertTrue(distance(boss, level) < start);
    }
}
