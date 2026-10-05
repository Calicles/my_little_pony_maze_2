package com.antoine.gdx;

import com.antoine.modele.Direction;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.List;

/**
 * <b>Commandes du joueur : clavier, D-pad tactile, curseurs et boutons.</b>
 * <p>Plusieurs touches ou doigts peuvent être posés : la direction retenue est la dernière appuyée.</p>
 */
final class GameInput extends InputAdapter {

    /**Actions déclenchées par un appui hors du D-pad.*/
    interface Actions {
        /**Appui sur un point de l'écran (coordonnées virtuelles) ; true s'il a été utilisé.*/
        boolean tap(float x, float y);

        /**Déplacement sur un curseur de volume.*/
        void slide(boolean music, float y);
    }

    /**Rayon central du D-pad où aucune direction n'est donnée*/
    private static final float DEAD_ZONE= 25;
    private static final int MAX_POINTERS= 10;

    private final Viewport viewport;
    private final Actions actions;
    private Layout layout;

    /**Directions dans l'ordre d'appui : la dernière gagne*/
    private final List<Direction> pressed= new ArrayList<>();
    private final Direction[] keyDirections= new Direction[256];
    private final Direction[] pointerDirections= new Direction[MAX_POINTERS];
    /**Curseur tenu par chaque doigt : 1 musique, 2 bruitage, 0 aucun*/
    private final int[] pointerSliders= new int[MAX_POINTERS];
    private final boolean[] pointerOnDpad= new boolean[MAX_POINTERS];
    private final Vector2 point= new Vector2();

    GameInput(Viewport viewport, Actions actions) {
        this.viewport= viewport;
        this.actions= actions;
    }

    void setLayout(Layout layout) {
        this.layout= layout;
    }

    /**
     * @return la direction maintenue, null si aucune.
     */
    Direction current() {
        return pressed.isEmpty() ? null : pressed.get(pressed.size() - 1);
    }

    /**
     * @return true si cette direction est donnée par le D-pad (pour l'afficher enfoncée).
     */
    boolean isDpadPressed(Direction direction) {
        for (Direction d : pointerDirections)
            if (d == direction) return true;
        return false;
    }

    private static Direction keyDirection(int keycode) {
        switch (keycode) {
            case Keys.LEFT: case Keys.A: case Keys.Q: return Direction.LEFT;
            case Keys.RIGHT: case Keys.D: return Direction.RIGHT;
            case Keys.UP: case Keys.W: case Keys.Z: return Direction.UP;
            case Keys.DOWN: case Keys.S: return Direction.DOWN;
            default: return null;
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        Direction direction= keyDirection(keycode);
        if (direction == null || keycode >= keyDirections.length)
            return false;
        release(keyDirections[keycode]);
        keyDirections[keycode]= direction;
        pressed.add(direction);
        return true;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode >= keyDirections.length || keyDirections[keycode] == null)
            return false;
        release(keyDirections[keycode]);
        keyDirections[keycode]= null;
        return true;
    }

    private void release(Direction direction) {
        if (direction != null)
            pressed.remove(direction);
    }

    private Vector2 toWorld(int screenX, int screenY) {
        return viewport.unproject(point.set(screenX, screenY));
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        if (pointer >= MAX_POINTERS || layout == null)
            return false;
        Vector2 p= toWorld(screenX, screenY);
        if (layout.dpad != null && layout.dpad.contains(p)) {
            pointerOnDpad[pointer]= true;
            setPointerDirection(pointer, dpadDirection(p));
            return true;
        }
        if (layout.musicSlider.contains(p)) {
            pointerSliders[pointer]= 1;
            actions.slide(true, p.y);
            return true;
        }
        if (layout.soundSlider.contains(p)) {
            pointerSliders[pointer]= 2;
            actions.slide(false, p.y);
            return true;
        }
        return actions.tap(p.x, p.y);
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        if (pointer >= MAX_POINTERS)
            return false;
        Vector2 p= toWorld(screenX, screenY);
        if (pointerOnDpad[pointer]) {
            // Le doigt peut glisser d'une flèche à l'autre, même en sortant un peu du D-pad.
            setPointerDirection(pointer, dpadDirection(p));
            return true;
        }
        if (pointerSliders[pointer] != 0) {
            actions.slide(pointerSliders[pointer] == 1, p.y);
            return true;
        }
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        if (pointer >= MAX_POINTERS)
            return false;
        setPointerDirection(pointer, null);
        pointerOnDpad[pointer]= false;
        pointerSliders[pointer]= 0;
        return true;
    }

    @Override
    public boolean touchCancelled(int screenX, int screenY, int pointer, int button) {
        return touchUp(screenX, screenY, pointer, button);
    }

    private void setPointerDirection(int pointer, Direction direction) {
        if (pointerDirections[pointer] == direction)
            return;
        release(pointerDirections[pointer]);
        pointerDirections[pointer]= direction;
        if (direction != null)
            pressed.add(direction);
    }

    /**
     * <p>Direction donnée par un point du D-pad : l'axe le plus marqué depuis le centre.</p>
     */
    private Direction dpadDirection(Vector2 p) {
        float dx= p.x - layout.dpad.x - layout.dpad.width / 2;
        float dy= p.y - layout.dpad.y - layout.dpad.height / 2;
        if (dx * dx + dy * dy < DEAD_ZONE * DEAD_ZONE)
            return null;
        if (Math.abs(dx) > Math.abs(dy))
            return dx > 0 ? Direction.RIGHT : Direction.LEFT;
        return dy > 0 ? Direction.UP : Direction.DOWN;
    }

    /**
     * <p>Oublie les appuis en cours (quand l'application passe en arrière-plan).</p>
     */
    void reset() {
        pressed.clear();
        java.util.Arrays.fill(keyDirections, null);
        java.util.Arrays.fill(pointerDirections, null);
        java.util.Arrays.fill(pointerSliders, 0);
        java.util.Arrays.fill(pointerOnDpad, false);
    }
}
