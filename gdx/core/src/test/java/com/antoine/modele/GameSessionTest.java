package com.antoine.modele;

import com.antoine.modele.level.AbstractLevel;
import com.antoine.modele.level.Level4;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static org.junit.Assert.*;

public class GameSessionTest {

    private final List<String> musics= new ArrayList<>();
    private int steps;

    private GameSession newSession() {
        GameSession session= new GameSession();
        session.setListener(new GameSession.Listener() {
            @Override public void levelChanged(AbstractLevel level) {musics.add(level.getName());}
            @Override public void step() {steps++;}
        });
        return session;
    }

    /**
     * Joue le niveau en cours comme un joueur : maintient chaque direction le temps d'un pas
     * (un nouvel appui fait un pas tout de suite, puis un pas par intervalle).
     */
    private static void play(GameSession session, Supplier<? extends AbstractLevel> levels) {
        List<Direction> path= MazeSolver.solve(levels);
        for (Direction direction : path) {
            if (direction != session.getHeldDirection()) {
                session.setHeldDirection(direction);
                session.update(0);
            } else {
                session.update(GameSession.STEP);
            }
        }
        session.update(GameSession.STEP);
        session.setHeldDirection(null);
    }

    @Test
    public void partieComplete() {
        GameSession session= newSession();
        List<AbstractLevel> first= session.getFirstLevels();
        assertSame(first.get(0), session.getCurrentLevel());
        assertEquals("apple", musics.get(0));
        assertFalse(session.canSelect(first.get(0)));
        assertTrue(session.canSelect(first.get(1)));

        play(session, LevelFactory::apple);
        assertTrue(first.get(0).isFinished());
        assertEquals(1, session.getFinishedLevels());
        assertFalse("Il reste des niveaux à choisir", session.isWaitingForNextLevel());
        assertTrue(steps > 0);

        session.select(first.get(1));
        assertEquals("rarity", musics.get(musics.size() - 1));
        play(session, LevelFactory::rarity);
        assertFalse(session.canSelect(first.get(0)));
        session.select(first.get(2));
        play(session, LevelFactory::rainbow);
        assertEquals(3, session.getFinishedLevels());

        // Les trois premiers niveaux finis : une direction lance Fluttershy.
        assertTrue(session.isWaitingForNextLevel());
        session.setHeldDirection(Direction.UP);
        assertEquals("flutter", session.getCurrentLevel().getName());
        assertFalse(session.isChoosingFirstLevels());
        int y= session.getCurrentLevel().getPlayer().getY();
        session.update(1);
        assertEquals("Il faut relâcher avant de bouger", y, session.getCurrentLevel().getPlayer().getY());
        session.setHeldDirection(null);

        play(session, LevelFactory::flutter);
        assertTrue(session.isWaitingForNextLevel());
        session.continueToNextLevel();
        assertEquals("pinky", session.getCurrentLevel().getName());

        play(session, LevelFactory::pinky);
        session.continueToNextLevel();
        assertTrue(session.getCurrentLevel() instanceof Level4);
        assertEquals(5, session.getFinishedLevels());
        assertFalse(session.isGameFinished());
        assertEquals(List.of("apple", "rarity", "rainbow", "flutter", "pinky", "twilight"), musics);

        // Le niveau final se déroule tout seul : la présentation commence.
        for (int i= 0; i < 60 * 30 && !((Level4) session.getCurrentLevel()).isPlaying(); i++)
            session.update(1f / 60f);
        assertTrue(((Level4) session.getCurrentLevel()).isPlaying());
    }

    @Test
    public void unNiveauFiniNePeutPlusEtreChoisi() {
        GameSession session= newSession();
        play(session, LevelFactory::apple);
        session.select(session.getFirstLevels().get(0));
        assertSame(session.getFirstLevels().get(0), session.getCurrentLevel());
        session.select(session.getFirstLevels().get(2));
        assertSame(session.getFirstLevels().get(2), session.getCurrentLevel());
        assertFalse(session.canSelect(session.getFirstLevels().get(0)));
    }

    @Test
    public void relacherArreteLeJoueur() {
        GameSession session= newSession();
        AbstractLevel level= session.getCurrentLevel();
        session.setHeldDirection(Direction.RIGHT);
        session.update(0.5f);
        int x= level.getPlayer().getX();
        assertTrue(x > 40);
        session.setHeldDirection(null);
        session.update(0.5f);
        assertEquals(x, level.getPlayer().getX());
    }
}
