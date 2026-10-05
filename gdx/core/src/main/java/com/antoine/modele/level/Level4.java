package com.antoine.modele.level;

import com.antoine.contracts.IEnnemi;
import com.antoine.contracts.IEntity;
import com.antoine.geometry.Coordinates;
import com.antoine.geometry.DoubleBoxes;
import com.antoine.geometry.Rectangle;

/**
 * <b>Niveau final : la carte défile et un boss poursuit le joueur.</b>
 * <p>Le niveau a son propre déroulement, avancé par {@link #update(float)} :</p>
 * <ol>
 *     <li>une présentation : l'écran glisse vers le coin de la carte, le boss entre, l'écran revient ;</li>
 *     <li>la partie : à chaque tour le joueur et le boss se déplacent ;</li>
 *     <li>si le boss attrape le joueur, l'image de défaite s'affiche puis tout recommence ;</li>
 *     <li>si le joueur sort, l'animation de fin est jouée.</li>
 * </ol>
 *
 * @author Antoine
 */
public class Level4 extends Level3 {

    /**Durée d'un tour de la boucle de jeu (24 tours par seconde)*/
    private static final float LOOP_STEP= 1f / 24f;

    /**Attente avant la première présentation*/
    private static final float START_DELAY= 2f;

    /**Durée d'un pixel de glissement de l'écran, à l'aller puis au retour*/
    private static final float PAN_STEP= 0.008f, PAN_BACK_STEP= 0.003f;

    /**Durée d'un pas du boss pendant son entrée*/
    private static final float BOSS_STEP= 0.05f;

    /**Hauteur à laquelle le boss s'arrête en entrant*/
    private static final int BOSS_ENTRY_Y= 12;

    /**Durée d'affichage de l'image de défaite*/
    private static final float LOSE_DELAY= 2.5f;

    /**Attente avant l'animation de fin, puis durée d'une image de l'animation*/
    private static final float END_DELAY= 2f, END_FRAME= 0.05f;

    private enum State {NOT_STARTED, WAITING, PAN_TO_CORNER, BOSS_ENTERS, PAN_BACK, PLAYING, CAUGHT, WON, END_ANIMATION, FINISHED}

    private IEnnemi boss;

    private Coordinates startPlayerPosition, startBossPosition, startScreenPosition, startScrollPosition;

    /**Position de l'écran à retrouver après la présentation*/
    private Coordinates panReturn;

    private State state= State.NOT_STARTED;

    /**Temps accumulé dans l'état courant*/
    private float timer;

    /**Le boss a attrapé le joueur*/
    private boolean over;

    private String loseImagePath, winImagePath;

    /**Format du chemin des images de l'animation de fin (avec leur numéro), et nombre d'images*/
    private String endAnimationFormat;
    private int numberEndAnimationImages, endFrame;

    public Level4(String name){
        super(name);
    }

    public void setBoss(IEnnemi boss){
        this.boss= boss;
        this.boss.setAttributes(player.getPosition(), map);
        startBossPosition= new Coordinates(boss.getX(), boss.getY());
        startPlayerPosition= new Coordinates(player.getX(), player.getY());
        startScreenPosition= new Coordinates(boxes.getScreenBeginX(), boxes.getScreenBeginY());
        startScrollPosition= new Coordinates(boxes.getScrollBeginX(), boxes.getScrollBeginY());
        boss.startThinking();
    }

    @Override
    public IEntity getBoss(){return boss;}

    public void setLoseImagePath(String loseImagePath) {
        this.loseImagePath= loseImagePath;
    }

    /**
     * @param format le chemin des images de fin, avec %d pour leur numéro (0 : première image).
     * @param numberOfImages le nombre d'images de l'animation.
     * @param finalImage l'image affichée à la toute fin.
     */
    public void setEndAnimation(String format, int numberOfImages, String finalImage) {
        this.endAnimationFormat= format;
        this.numberEndAnimationImages= numberOfImages;
        this.winImagePath= finalImage;
        this.endImageUrl= String.format(format, 0);
    }

    @Override
    protected void initBoxes() {
        Rectangle screen= new Rectangle(10 * tile_width, 30*tile_width, 0,
                20* tile_height);
        Rectangle scrollBox= new Rectangle(858, 950,
                290, 350);
        this.boxes= new DoubleBoxes(screen, scrollBox);
    }

    @Override
    public void start(){
        if (state == State.NOT_STARTED)
            enter(State.WAITING);
    }

    /**
     * @return true pendant que le joueur peut jouer (ni présentation, ni défaite, ni fin).
     */
    public boolean isPlaying() {
        return state == State.PLAYING;
    }

    @Override
    public void update(float delta) {
        timer += delta;
        switch (state) {
            case WAITING:
                if (timer >= START_DELAY)
                    startPresentation();
                break;
            case PAN_TO_CORNER:
                while (timer >= PAN_STEP && state == State.PAN_TO_CORNER) {
                    timer -= PAN_STEP;
                    if (panTowards(0, 0))
                        enter(State.BOSS_ENTERS);
                }
                break;
            case BOSS_ENTERS:
                while (timer >= BOSS_STEP && state == State.BOSS_ENTERS) {
                    timer -= BOSS_STEP;
                    if (boss.getY() < BOSS_ENTRY_Y)
                        boss.movesDown();
                    else
                        enter(State.PAN_BACK);
                }
                break;
            case PAN_BACK:
                while (timer >= PAN_BACK_STEP && state == State.PAN_BACK) {
                    timer -= PAN_BACK_STEP;
                    if (panTowards(panReturn.getX(), panReturn.getY()))
                        enter(State.PLAYING);
                }
                break;
            case PLAYING:
                while (timer >= LOOP_STEP && state == State.PLAYING) {
                    timer -= LOOP_STEP;
                    loop();
                }
                break;
            case CAUGHT:
                if (timer >= LOSE_DELAY) {
                    endImageUrl= String.format(endAnimationFormat, 0);
                    startPresentation();
                }
                break;
            case WON:
                if (timer >= END_DELAY) {
                    endFrame= numberEndAnimationImages;
                    enter(State.END_ANIMATION);
                }
                break;
            case END_ANIMATION:
                while (timer >= END_FRAME && state == State.END_ANIMATION) {
                    timer -= END_FRAME;
                    if (endFrame > 0) {
                        endImageUrl= String.format(endAnimationFormat, endFrame);
                        endFrame--;
                    } else {
                        endImageUrl= winImagePath;
                        enter(State.FINISHED);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void enter(State newState) {
        state= newState;
        timer= 0;
    }

    /**
     * <p>Replace tout le monde au départ et lance la présentation du niveau.</p>
     */
    private void startPresentation() {
        boss.translateTo(startBossPosition);
        player.translateTo(startPlayerPosition);
        boxes.getScreen().setCoordinates(startScreenPosition);
        boxes.getScroll().setCoordinates(startScrollPosition);
        over= false;
        panReturn= new Coordinates(boxes.getScreenBeginX(), boxes.getScreenBeginY());
        enter(State.PAN_TO_CORNER);
    }

    /**
     * <p>Fait glisser l'écran d'un pixel vers une position, d'abord en largeur puis en hauteur.</p>
     * @return true quand l'écran est arrivé.
     */
    private boolean panTowards(int x, int y) {
        Rectangle screen= boxes.getScreen();
        if (x != screen.getBeginX())
            screen.translate(x < screen.getBeginX() ? -1 : 1, 0);
        else if (y != screen.getBeginY())
            screen.translate(0, y < screen.getBeginY() ? -1 : 1);
        return screen.getBeginX() == x && screen.getBeginY() == y;
    }

    /**
     * <p>Un tour de jeu.</p>
     * On vérifie les collisions entre joueur et boss, on déplace le joueur et l'écran,
     * puis le boss applique sa direction et calcule la suivante.
     */
    private void loop() {
        if (Rectangle.isTouching(boss.getPosition(), player.getPosition())) {
            over= true;
            endImageUrl= loseImagePath;
            enter(State.CAUGHT);
            return;
        }
        scroll(player.memorizeMoves(map));
        boss.memorizeMoves();
        boss.think();
        checkRunning();
        if (!running)
            enter(State.WON);
    }

    private void scroll(Coordinates vector) {
        if (vector.getX() < 0){
            scrollLeft(vector);
        }else if (vector.getX() > 0){
            scrollRight(vector);
        }else if (vector.getY() < 0){
            scrollUp(vector);
        }else if (vector.getY() > 0){
            scrollDown(vector);
        }
    }

    //Dans ce niveau, les touches donnent seulement la direction : la boucle de jeu déplace le joueur.

    @Override
    public void playerMovesLeft() {
        player.movesLeft();
    }

    @Override
    public void playerMovesRight() {
        player.movesRight();
    }

    @Override
    public void playerMovesUp() {
        player.movesUp();
    }

    @Override
    public void playerMovesDown() {
        player.movesDown();
    }

    @Override
    public boolean isRunning(){
        return running && !over;
    }
}
