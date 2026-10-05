package com.antoine.modele;

import com.antoine.contracts.IEntity;
import com.antoine.geometry.Coordinates;
import com.antoine.modele.level.AbstractLevel;
import com.antoine.modele.level.Level;
import com.antoine.modele.level.Level2;
import com.antoine.modele.level.Level3;
import org.junit.Test;

import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class LevelsTest {

    /**
     * Rejoue les pas un par un, puis un pas de plus : le niveau constate la sortie au pas suivant.
     */
    private static <L extends AbstractLevel> L playToExit(Supplier<L> levels) {
        List<Direction> path= MazeSolver.solve(levels);
        L level= levels.get();
        Direction previous= null;
        for (Direction direction : path) {
            assertTrue("Le niveau ne doit pas finir avant la sortie", level.isRunning());
            // Comme dans la partie : changer de direction relâche la précédente.
            if (direction != previous)
                level.playerMovesReleased();
            MazeSolver.step(level, direction);
            previous= direction;
        }
        MazeSolver.step(level, path.get(path.size() - 1));
        return level;
    }

    @Test
    public void appleSeTermine() {
        assertTrue(playToExit(LevelFactory::apple).isFinished());
    }

    @Test
    public void raritySeTermine() {
        assertTrue(playToExit(LevelFactory::rarity).isFinished());
    }

    @Test
    public void rainbowSeTermine() {
        assertTrue(playToExit(LevelFactory::rainbow).isFinished());
    }

    @Test
    public void flutterSeTermineEtChangeDEcran() {
        Level2 level= playToExit(LevelFactory::flutter);
        assertTrue(level.isFinished());
        // Le joueur est toujours dessiné dans l'écran affiché.
        IEntity player= level.getPlayer();
        assertTrue(level.getScreen().getBeginY() <= player.getY());
        assertTrue(player.getY() < level.getScreen().getEndY());
    }

    @Test
    public void pinkySeTermineEtLEcranSuitLeJoueur() {
        Level3 level= playToExit(LevelFactory::pinky);
        assertTrue(level.isFinished());
        IEntity player= level.getPlayer();
        assertTrue(level.getScreen().getBeginX() <= player.getX() && player.getX() < level.getScreen().getEndX());
        assertTrue(level.getScreen().getBeginY() <= player.getY() && player.getY() < level.getScreen().getEndY());
    }

    @Test
    public void lesMursArretentLeJoueur() {
        Level level= LevelFactory.apple();
        IEntity player= level.getPlayer();
        for (int i= 0; i < 200; i++)
            level.playerMovesUp();
        int y= player.getY();
        level.playerMovesUp();
        assertEquals("Le joueur reste bloqué contre le mur", y, player.getY());
        assertTrue(y >= 0);
        assertTrue(level.isRunning());
    }

    @Test
    public void ecranDesPremiersNiveaux() {
        Level level= LevelFactory.apple();
        assertEquals(640, level.getScreen().getWidth());
        assertEquals(640, level.getScreen().getHeight());
        assertNull(level.getBoss());
        assertNotNull(level.getEndImageUrl());
    }

    @Test
    public void positionDeDepart() {
        Level level= LevelFactory.apple();
        assertEquals(new Coordinates(40, 50), new Coordinates(level.getPlayer().getX(), level.getPlayer().getY()));
    }
}
