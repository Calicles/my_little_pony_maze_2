package com.antoine.modele;

import com.antoine.contracts.IEntity;
import com.antoine.geometry.Coordinates;
import com.antoine.geometry.Rectangle;
import com.antoine.geometry.Tile;
import com.antoine.modele.level.AbstractLevel;
import com.antoine.modele.level.Level4;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**
 * Trouve une suite de pas qui mène le joueur à la sortie, en essayant les déplacements
 * du jeu lui-même (collisions comprises) depuis chaque position atteinte.
 */
final class MazeSolver {

    private MazeSolver() {}

    static List<Direction> solve(Supplier<? extends AbstractLevel> levels) {
        AbstractLevel probe= levels.get();
        IEntity player= probe.getPlayer();
        Tile exitTile= probe.getMap().findExit();
        Rectangle exit= new Rectangle(exitTile.getX(), exitTile.getX() + 2 * Tile.getWidth(),
                exitTile.getY(), exitTile.getY() + 2 * Tile.getHeight());

        long start= key(player.getX(), player.getY());
        Map<Long, Long> parent= new HashMap<>();
        Map<Long, Direction> via= new HashMap<>();
        parent.put(start, start);
        ArrayDeque<Long> queue= new ArrayDeque<>();
        queue.add(start);

        while (!queue.isEmpty()) {
            long current= queue.poll();
            int x= (int) (current >> 32), y= (int) current;
            if (Rectangle.isInBox(exit, new Rectangle(x, x + player.getPosition().getWidth(),
                    y, y + player.getPosition().getHeight())))
                return path(parent, via, start, current);

            for (Direction direction : Direction.values()) {
                player.translateTo(new Coordinates(x, y));
                probe.playerMovesReleased();
                step(probe, direction);
                long next= key(player.getX(), player.getY());
                if (!parent.containsKey(next)) {
                    parent.put(next, current);
                    via.put(next, direction);
                    queue.add(next);
                }
            }
        }
        throw new AssertionError("Aucun chemin vers la sortie");
    }

    static void step(AbstractLevel level, Direction direction) {
        switch (direction) {
            case LEFT: level.playerMovesLeft(); break;
            case RIGHT: level.playerMovesRight(); break;
            case UP: level.playerMovesUp(); break;
            case DOWN: level.playerMovesDown(); break;
        }
        // Dans le niveau final, la direction ne fait que s'enregistrer : on applique le pas ici.
        if (level instanceof Level4)
            level.getPlayer().memorizeMoves(level.getMap());
    }

    private static List<Direction> path(Map<Long, Long> parent, Map<Long, Direction> via, long start, long end) {
        List<Direction> path= new ArrayList<>();
        for (long node= end; node != start; node= parent.get(node))
            path.add(via.get(node));
        Collections.reverse(path);
        return path;
    }

    private static long key(int x, int y) {
        return ((long) x << 32) | (y & 0xFFFFFFFFL);
    }
}
