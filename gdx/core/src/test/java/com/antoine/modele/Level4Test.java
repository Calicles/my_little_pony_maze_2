package com.antoine.modele;

import com.antoine.contracts.IEntity;
import com.antoine.contracts.IMap;
import com.antoine.geometry.Pythagore;
import com.antoine.geometry.Coordinates;
import com.antoine.geometry.Rectangle;
import com.antoine.geometry.Tile;
import com.antoine.services.Resources;
import com.antoine.modele.level.Level4;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class Level4Test {

    private static final float FRAME= 1f / 60f;

    /**Fait tourner le niveau jusqu'à ce que la partie commence (présentation finie).*/
    private static void untilPlaying(Level4 level) {
        for (int i= 0; i < 60 * 30 && !level.isPlaying(); i++)
            level.update(FRAME);
        assertTrue("La présentation doit se terminer", level.isPlaying());
    }

    /**
     * @return une position libre (2 tuiles sur 2 sans mur) à moins de radius pixels de l'entité,
     * mais sans la toucher.
     */
    private static Coordinates freeSpotNear(Level4 level, IEntity entity, int radius) {
        IMap map= level.getMap();
        for (int row= 0; row < map.getHeightInTile() - 1; row++)
            for (int col= 0; col < map.getWidthInTile() - 1; col++) {
                if (map.isSolideTile(col, row) || map.isSolideTile(col + 1, row)
                        || map.isSolideTile(col, row + 1) || map.isSolideTile(col + 1, row + 1))
                    continue;
                Coordinates spot= new Coordinates(col * Tile.getWidth(), row * Tile.getHeight());
                Rectangle area= new Rectangle(spot, spot.getX() + 2 * Tile.getWidth(), spot.getY() + 2 * Tile.getHeight());
                int d= Pythagore.calculDistance(Rectangle.findMiddleCoor(area), Rectangle.findMiddleCoor(entity.getPosition()));
                if (d < radius && d > 150)
                    return new Coordinates(spot.getX() + 1, spot.getY() + 1);
            }
        throw new AssertionError("Pas de case libre près du boss");
    }

    private static int distance(IEntity a, IEntity b) {
        return Pythagore.calculDistance(Rectangle.findMiddleCoor(a.getPosition()), Rectangle.findMiddleCoor(b.getPosition()));
    }

    @Test
    public void presentationPuisPartie() {
        Level4 level= LevelFactory.twilight();
        level.start();
        Rectangle screen= level.getScreen();
        int startX= screen.getBeginX();
        assertFalse(level.isPlaying());

        // Pendant la présentation, l'écran glisse jusqu'au coin de la carte.
        boolean reachedCorner= false;
        for (int i= 0; i < 60 * 30 && !level.isPlaying(); i++) {
            level.update(FRAME);
            reachedCorner|= screen.getBeginX() == 0 && screen.getBeginY() == 0;
        }
        assertTrue(reachedCorner);
        assertTrue(level.isPlaying());
        assertEquals("L'écran revient à sa place", startX, screen.getBeginX());
        assertTrue("Le boss est entré", level.getBoss().getY() >= 12);
    }

    @Test
    public void leBossAttrapeLeJoueurImmobile() {
        Level4 level= LevelFactory.twilight();
        level.start();
        untilPlaying(level);
        IEntity boss= level.getBoss(), player= level.getPlayer();
        // Le boss ne chasse que dans un rayon de 300 pixels : on place le joueur à portée, sur une case libre.
        player.translateTo(freeSpotNear(level, boss, 250));
        int startDistance= distance(boss, player);

        // Le boss doit rejoindre le joueur qui ne bouge pas.
        for (int i= 0; i < 60 * 120 && level.isRunning(); i++)
            level.update(FRAME);
        assertFalse("Le joueur est attrapé", level.isRunning());
        assertFalse("Être attrapé ne termine pas le niveau", level.isFinished());
        assertTrue(level.getEndImageUrl().endsWith("discord3.png"));
        assertTrue(startDistance > 0);

        // Après l'image de défaite, tout recommence.
        for (int i= 0; i < 60 * 30 && !level.isPlaying(); i++)
            level.update(FRAME);
        assertTrue(level.isPlaying());
        assertTrue(level.isRunning());
        assertEquals(880, player.getX());
        assertEquals(300, player.getY());
    }

    @Test
    public void laSortieDuNiveauFinalEstAccessible() {
        assertFalse(MazeSolver.solve(LevelFactory::twilight).isEmpty());
    }

    @Test
    public void victoirePuisAnimationDeFin() {
        Level4 level= LevelFactory.twilight();
        level.start();
        untilPlaying(level);

        // Le boss attend près de la sortie : on l'éloigne pour que le joueur puisse y entrer.
        level.getBoss().translateTo(new Coordinates(880, 300));
        Tile exit= level.getMap().findExit();
        level.getPlayer().translateTo(new Coordinates(exit.getX() + 1, exit.getY() + 1));
        level.update(1f / 24f + 0.001f);
        assertTrue(level.isFinished());
        assertTrue(level.getEndImageUrl().endsWith("final/fin0.jpg"));

        Set<String> images= new HashSet<>();
        for (int i= 0; i < 60 * 5; i++) {
            level.update(FRAME);
            images.add(level.getEndImageUrl());
        }
        assertTrue(level.getEndImageUrl().endsWith("final/fin.jpg"));
        assertEquals("fin0, fin32 à fin1, puis fin", 34, images.size());
        for (String image : images)
            assertNotNull(image, Resources.class.getResource(image));
    }
}
