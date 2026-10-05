package com.antoine.modele;

import com.antoine.modele.level.AbstractLevel;
import com.antoine.modele.level.Level4;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * <b>Le déroulement d'une partie.</b>
 * <p>Les trois premiers niveaux (Applejack, Rarity, Rainbow Dash) se jouent dans l'ordre voulu.
 * Une fois finis, viennent Fluttershy, Pinkie Pie, puis le niveau final de Twilight.</p>
 * <p>Quand un niveau est fini, son image de fin s'affiche ; le niveau suivant se lance
 * à la prochaine action du joueur ({@link #continueToNextLevel()}).</p>
 *
 * @author Antoine
 */
public class GameSession {

    /**Nombre total de niveaux*/
    public static final int LEVEL_COUNT= 6;

    /**Durée d'un pas du joueur quand une direction est maintenue*/
    static final float STEP= 1f / 30f;

    /**Prévient l'affichage et le son des événements de la partie.*/
    public interface Listener {
        /**Le niveau en cours a changé : la musique doit suivre.*/
        void levelChanged(AbstractLevel level);

        /**Le joueur a fait un pas.*/
        void step();
    }

    private enum Stage {FIRST_LEVELS, FLUTTER, PINKY, TWILIGHT}

    private final List<AbstractLevel> firstLevels;
    private AbstractLevel flutter, pinky, twilight;
    private AbstractLevel current;
    private Stage stage= Stage.FIRST_LEVELS;

    /**Direction maintenue par le joueur, null si aucune*/
    private Direction held;

    /**Après un changement de niveau, la direction doit être relâchée avant de bouger*/
    private boolean waitRelease;

    private float accumulator;

    private Listener listener= new Listener() {
        @Override public void levelChanged(AbstractLevel level) {}
        @Override public void step() {}
    };

    public GameSession() {
        this(LevelFactory.apple(), LevelFactory.rarity(), LevelFactory.rainbow());
    }

    GameSession(AbstractLevel... firstLevels) {
        this.firstLevels= Collections.unmodifiableList(Arrays.asList(firstLevels));
        current= firstLevels[0];
    }

    public void setListener(Listener listener) {
        this.listener= listener;
        listener.levelChanged(current);
    }

    public AbstractLevel getCurrentLevel() {return current;}

    public List<AbstractLevel> getFirstLevels() {return firstLevels;}

    /**
     * @return true tant que l'on joue les trois premiers niveaux (les boutons de choix sont affichés).
     */
    public boolean isChoosingFirstLevels() {return stage == Stage.FIRST_LEVELS;}

    /**
     * @return le nombre de niveaux terminés.
     */
    public int getFinishedLevels() {
        int finished= 0;
        for (AbstractLevel level : firstLevels)
            if (level.isFinished()) finished++;
        for (AbstractLevel level : Arrays.asList(flutter, pinky, twilight))
            if (level != null && level.isFinished()) finished++;
        return finished;
    }

    /**
     * @return true si le jeu est entièrement terminé.
     */
    public boolean isGameFinished() {
        return twilight != null && twilight.isFinished();
    }

    /**
     * <p>Un des trois premiers niveaux peut être choisi s'il n'est ni en cours, ni fini.</p>
     */
    public boolean canSelect(AbstractLevel level) {
        return stage == Stage.FIRST_LEVELS && level != current && !level.isFinished();
    }

    public void select(AbstractLevel level) {
        if (!canSelect(level))
            return;
        changeLevel(level);
    }

    /**
     * @return true si le niveau en cours est fini et qu'un niveau suivant attend le joueur.
     */
    public boolean isWaitingForNextLevel() {
        if (!current.isFinished())
            return false;
        switch (stage) {
            case FIRST_LEVELS:
                for (AbstractLevel level : firstLevels)
                    if (!level.isFinished()) return false;
                return true;
            case FLUTTER:
            case PINKY:
                return true;
            default:
                return false;
        }
    }

    /**
     * <p>Lance le niveau suivant, si le niveau en cours est fini.</p>
     */
    public void continueToNextLevel() {
        if (!isWaitingForNextLevel())
            return;
        switch (stage) {
            case FIRST_LEVELS:
                stage= Stage.FLUTTER;
                flutter= LevelFactory.flutter();
                changeLevel(flutter);
                break;
            case FLUTTER:
                stage= Stage.PINKY;
                pinky= LevelFactory.pinky();
                changeLevel(pinky);
                break;
            case PINKY:
                stage= Stage.TWILIGHT;
                twilight= LevelFactory.twilight();
                changeLevel(twilight);
                twilight.start();
                break;
            default:
                break;
        }
    }

    private void changeLevel(AbstractLevel level) {
        if (held != null)
            current.playerMovesReleased();
        current= level;
        waitRelease= held != null;
        accumulator= 0;
        listener.levelChanged(level);
    }

    /**
     * <p>Indique la direction que le joueur maintient (null quand il relâche).</p>
     * Appuyer sur une direction quand un niveau est fini lance le niveau suivant.
     */
    public void setHeldDirection(Direction direction) {
        if (direction == held)
            return;
        Direction previous= held;
        held= direction;

        // Relâcher la direction précédente, même en passant directement à une autre :
        // un déplacement ne remet à zéro que son propre axe.
        if (previous != null)
            current.playerMovesReleased();
        if (direction == null) {
            waitRelease= false;
            return;
        }
        if (previous == null && isWaitingForNextLevel()) {
            continueToNextLevel();
            return;
        }
        if (!waitRelease && current.isRunning())
            accumulator= STEP; // le premier pas dans une nouvelle direction est immédiat
    }

    public Direction getHeldDirection() {return held;}

    /**
     * <p>Fait avancer la partie.</p>
     * @param delta temps écoulé depuis le dernier appel, en secondes.
     */
    public void update(float delta) {
        AbstractLevel level= current;
        if (held != null && !waitRelease && level.isRunning()) {
            if (level instanceof Level4) {
                // Le niveau final déplace lui-même le joueur à chaque tour.
                if (((Level4) level).isPlaying()) {
                    move(level, held);
                    accumulator+= delta;
                    while (accumulator >= STEP) {
                        accumulator-= STEP;
                        listener.step();
                    }
                }
            } else {
                accumulator+= delta;
                while (accumulator >= STEP && level.isRunning()) {
                    accumulator-= STEP;
                    move(level, held);
                    listener.step();
                }
            }
        }
        level.update(delta);
    }

    private static void move(AbstractLevel level, Direction direction) {
        switch (direction) {
            case LEFT: level.playerMovesLeft(); break;
            case RIGHT: level.playerMovesRight(); break;
            case UP: level.playerMovesUp(); break;
            case DOWN: level.playerMovesDown(); break;
        }
    }
}
