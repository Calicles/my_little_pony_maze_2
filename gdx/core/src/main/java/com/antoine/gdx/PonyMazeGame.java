package com.antoine.gdx;

import com.antoine.contracts.IEntity;
import com.antoine.contracts.IMap;
import com.antoine.geometry.Tile;
import com.antoine.modele.Direction;
import com.antoine.modele.GameSession;
import com.antoine.modele.level.AbstractLevel;
import com.antoine.services.Resources;
import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

import java.util.List;

/**
 * <b>Le jeu libGDX : affichage, commandes et son autour de la partie ({@link GameSession}).</b>
 */
public class PonyMazeGame extends ApplicationAdapter implements GameInput.Actions {

    public static final String TITLE= "My Little Pony - Le Labyrinthe de Discord";

    /**Le rose de la fenêtre d'origine (Color.PINK de Swing)*/
    private static final Color PINK= new Color(1f, 175 / 255f, 175 / 255f, 1f);
    private static final Color DARK_PINK= new Color(0.85f, 0.45f, 0.5f, 1f);
    private static final Color TEXT= new Color(0.35f, 0.1f, 0.2f, 1f);
    private static final Color GREY= new Color(0.35f, 0.35f, 0.35f, 1f);

    private static final String BUTTONS= "/ressources/images/boutons/";
    private static final String MUSIC_KNOB= "/ressources/images/slide/celestiaSlide.png";
    private static final String SOUND_KNOB= "/ressources/images/slide/lunaSlide.png";

    private GameSession session;
    private GameAudio audio;
    private Textures textures;
    private GameInput input;

    private SpriteBatch batch;
    private ShapeRenderer shapes;
    private BitmapFont font;
    private final GlyphLayout glyphs= new GlyphLayout();
    private OrthographicCamera camera;
    private FitViewport viewport;
    private Layout layout;

    /**Position des boutons des trois premiers niveaux (calculée à l'affichage)*/
    private final Rectangle[] levelButtons= {new Rectangle(), new Rectangle(), new Rectangle()};
    private final Rectangle scissor= new Rectangle();
    private float time;
    private boolean touchScreen;

    @Override
    public void create() {
        Resources.setLoader(path -> Gdx.files.internal(path.startsWith("/") ? path.substring(1) : path).read());
        touchScreen= Gdx.app.getType() == Application.ApplicationType.Android
                || Gdx.app.getType() == Application.ApplicationType.iOS;

        batch= new SpriteBatch();
        shapes= new ShapeRenderer();
        font= new BitmapFont();
        font.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        font.setUseIntegerPositions(false);
        camera= new OrthographicCamera();
        viewport= new FitViewport(1, 1, camera);
        textures= new Textures();

        audio= new GameAudio();
        session= new GameSession();
        session.setListener(audio);

        input= new GameInput(viewport, this);
        Gdx.input.setInputProcessor(input);
        resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void resize(int width, int height) {
        if (width <= 0 || height <= 0)
            return;
        layout= Layout.forScreen(width, height);
        viewport.setWorldSize(layout.width, layout.height);
        viewport.update(width, height, true);
        input.setLayout(layout);
        font.getData().setScale(layout.fontScale);
    }

    @Override
    public void render() {
        // Après une pause (téléphone en veille), on ne rattrape pas tout le temps écoulé.
        float delta= Math.min(Gdx.graphics.getDeltaTime(), 0.1f);
        time+= delta;
        session.setHeldDirection(input.current());
        session.update(delta);

        ScreenUtils.clear(PINK);
        viewport.apply();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        drawGame();
        drawMessage();
        drawProgressBar();
        if (session.isChoosingFirstLevels())
            drawLevelButtons();
        else
            drawMiniMap();
        drawSlider(layout.musicSlider, audio.getMusicVolume(), MUSIC_KNOB, "musique");
        drawSlider(layout.soundSlider, audio.getSoundVolume(), SOUND_KNOB, "bruitage");
        if (layout.dpad != null)
            drawDpad();
    }

    //=============================   Le jeu   =============================

    private void drawGame() {
        Rectangle area= layout.gameArea;
        AbstractLevel level= session.getCurrentLevel();

        shapes.begin(ShapeType.Filled);
        shapes.setColor(Color.BLACK);
        shapes.rect(area.x - 3, area.y - 3, area.width + 6, area.height + 6);
        shapes.end();

        batch.begin();
        ScissorStack.calculateScissors(camera, batch.getTransformMatrix(), area, scissor);
        if (ScissorStack.pushScissors(scissor)) {
            if (level.isRunning())
                drawLevel(level, area);
            else
                batch.draw(textures.endImage(level.getEndImageUrl()), area.x, area.y, area.width, area.height);
            batch.flush();
            ScissorStack.popScissors();
        }
        batch.end();
    }

    private void drawLevel(AbstractLevel level, Rectangle area) {
        com.antoine.geometry.Rectangle screen= level.getScreen();
        IMap map= level.getMap();
        Tile[][] tiles= map.getMap();
        int tileWidth= map.getTile_width(), tileHeight= map.getTile_height();
        float scale= layout.gameScale();

        int firstRow= Math.max(0, screen.getBeginY() / tileHeight);
        int lastRow= Math.min(map.getHeightInTile() - 1, (screen.getEndY() - 1) / tileHeight);
        int firstCol= Math.max(0, screen.getBeginX() / tileWidth);
        int lastCol= Math.min(map.getWidthInTile() - 1, (screen.getEndX() - 1) / tileWidth);

        for (int row= firstRow; row <= lastRow; row++) {
            for (int col= firstCol; col <= lastCol; col++) {
                Tile tile= tiles[row][col];
                String image= map.getTileSet().get(tile.getTile_num());
                if (image == null)
                    continue;
                batch.draw(textures.get(image),
                        area.x + (tile.getX() - screen.getBeginX()) * scale,
                        area.y + area.height - (tile.getY() - screen.getBeginY() + tileHeight) * scale,
                        tileWidth * scale, tileHeight * scale);
            }
        }

        drawEntity(level.getPlayer(), screen, area, scale);
        if (level.getBoss() != null)
            drawEntity(level.getBoss(), screen, area, scale);
    }

    private void drawEntity(IEntity entity, com.antoine.geometry.Rectangle screen, Rectangle area, float scale) {
        Texture texture= textures.get(entity.getImage());
        batch.draw(texture,
                area.x + (entity.getX() - screen.getBeginX()) * scale,
                area.y + area.height - (entity.getY() - screen.getBeginY() + texture.getHeight()) * scale,
                texture.getWidth() * scale, texture.getHeight() * scale);
    }

    /**
     * <p>Indique quoi faire quand un niveau est fini.</p>
     */
    private void drawMessage() {
        String message= null;
        if (session.isWaitingForNextLevel())
            message= touchScreen ? "Touche l'écran pour continuer" : "Appuie sur une flèche pour continuer";
        else if (session.isChoosingFirstLevels() && session.getCurrentLevel().isFinished())
            message= touchScreen ? "Choisis un autre poney en bas" : "Clique sur un autre poney en bas";
        if (message == null)
            return;

        Rectangle area= layout.gameArea;
        float bandHeight= 40 * layout.fontScale;
        shapes.begin(ShapeType.Filled);
        shapes.setColor(0, 0, 0, 0.6f);
        shapes.rect(area.x, area.y, area.width, bandHeight);
        shapes.end();

        batch.begin();
        font.setColor(Color.WHITE);
        glyphs.setText(font, message, Color.WHITE, area.width, Align.center, true);
        font.draw(batch, glyphs, area.x, area.y + (bandHeight + glyphs.height) / 2);
        batch.end();
    }

    //=============================   Interface   =============================

    private void drawProgressBar() {
        Rectangle bar= layout.progressBar;
        float progress= (float) session.getFinishedLevels() / GameSession.LEVEL_COUNT;

        shapes.begin(ShapeType.Filled);
        shapes.setColor(DARK_PINK);
        shapes.rect(bar.x - 2, bar.y - 2, bar.width + 4, bar.height + 4);
        shapes.setColor(PINK);
        shapes.rect(bar.x, bar.y, bar.width, bar.height);
        shapes.setColor(Color.RED);
        shapes.rect(bar.x, bar.y, bar.width * progress, bar.height);
        shapes.end();

        batch.begin();
        font.setColor(TEXT);
        String text= session.getFinishedLevels() + " / " + GameSession.LEVEL_COUNT;
        glyphs.setText(font, text, TEXT, bar.width, Align.center, false);
        font.draw(batch, glyphs, bar.x, bar.y + (bar.height + glyphs.height) / 2);
        batch.end();
    }

    private void drawLevelButtons() {
        Rectangle panel= layout.bottomPanel;
        List<AbstractLevel> levels= session.getFirstLevels();
        float size= Math.min(panel.height - 10, 100), gap= size / 3;
        float x= panel.x + (panel.width - levels.size() * size - (levels.size() - 1) * gap) / 2;
        float y= panel.y + (panel.height - size) / 2;

        batch.begin();
        for (int i= 0; i < levels.size(); i++) {
            AbstractLevel level= levels.get(i);
            levelButtons[i].set(x + i * (size + gap), y, size, size);
            batch.setColor(level.isFinished() ? GREY : Color.WHITE);
            batch.draw(textures.get(BUTTONS + level.getName() + ".png"),
                    levelButtons[i].x, levelButtons[i].y, size, size);
        }
        batch.setColor(Color.WHITE);
        batch.end();

        // Le niveau en cours est encadré.
        for (int i= 0; i < levels.size(); i++) {
            if (levels.get(i) != session.getCurrentLevel())
                continue;
            Rectangle button= levelButtons[i];
            shapes.begin(ShapeType.Filled);
            shapes.setColor(Color.RED);
            float border= 4;
            shapes.rect(button.x - border, button.y - border, button.width + 2 * border, border);
            shapes.rect(button.x - border, button.y + button.height, button.width + 2 * border, border);
            shapes.rect(button.x - border, button.y, border, button.height);
            shapes.rect(button.x + button.width, button.y, border, button.height);
            shapes.end();
        }
    }

    /**
     * <p>Mini-carte des derniers niveaux : murs, sortie qui clignote, écran, joueur et boss.</p>
     */
    private void drawMiniMap() {
        AbstractLevel level= session.getCurrentLevel();
        IMap map= level.getMap();
        Rectangle panel= layout.bottomPanel;
        float scale= Math.min(panel.width / level.getMapWidth(), (panel.height - 6) / level.getMapHeight());
        float width= level.getMapWidth() * scale, height= level.getMapHeight() * scale;
        float x0= panel.x + (panel.width - width) / 2, top= panel.y + (panel.height + height) / 2;
        float tileWidth= map.getTile_width() * scale, tileHeight= map.getTile_height() * scale;

        shapes.begin(ShapeType.Filled);
        shapes.setColor(Color.GREEN);
        shapes.rect(x0, top - height, width, height);
        shapes.setColor(Color.ORANGE);
        Tile exit= null;
        for (Tile[] row : map.getMap())
            for (Tile tile : row) {
                if (tile.getTile_num() > Tile.getSolidNum())
                    shapes.rect(x0 + tile.getX() * scale, top - (tile.getY() + map.getTile_height()) * scale, tileWidth, tileHeight);
                else if (tile.isExit())
                    exit= tile;
            }
        if (exit != null && (time * 2) % 1 < 0.7f) {
            shapes.setColor(Color.BLUE);
            shapes.rect(x0 + exit.getX() * scale, top - (exit.getY() + 2 * map.getTile_height()) * scale, 2 * tileWidth, 2 * tileHeight);
        }
        drawMiniEntity(level.getPlayer(), Color.RED, x0, top, scale);
        drawMiniEntity(level.getBoss(), Color.BLACK, x0, top, scale);
        shapes.end();

        com.antoine.geometry.Rectangle screen= level.getScreen();
        shapes.begin(ShapeType.Line);
        shapes.setColor(Color.YELLOW);
        shapes.rect(x0 + screen.getBeginX() * scale, top - screen.getEndY() * scale,
                screen.getWidth() * scale, screen.getHeight() * scale);
        shapes.end();
    }

    private void drawMiniEntity(IEntity entity, Color color, float x0, float top, float scale) {
        if (entity == null)
            return;
        float size= Math.max(4, entity.getPosition().getWidth() * scale);
        shapes.setColor(color);
        shapes.rect(x0 + entity.getX() * scale, top - entity.getY() * scale - size, size, size);
    }

    /**
     * <p>Curseur de volume vertical, de 0 à 50, avec l'image de Celestia ou de Luna.</p>
     */
    private void drawSlider(Rectangle slider, float volume, String knob, String label) {
        float bottom= trackBottom(slider), top= trackTop(slider);
        float centerX= slider.x + slider.width * 0.6f;

        shapes.begin(ShapeType.Filled);
        shapes.setColor(TEXT);
        shapes.rectLine(centerX, bottom, centerX, top, 3);
        for (int value= 0; value <= 50; value++) {
            float y= bottom + (top - bottom) * value / 50f;
            float length= value % 10 == 0 ? 12 : 5;
            shapes.rectLine(centerX - 8 - length, y, centerX - 8, y, 1.5f);
        }
        shapes.end();

        Texture texture= textures.get(knob);
        float knobScale= slider.width / 80f;
        float knobY= bottom + (top - bottom) * volume / GameAudio.MAX_VOLUME;
        batch.begin();
        batch.draw(texture, centerX - texture.getWidth() * knobScale / 2, knobY - texture.getHeight() * knobScale / 2,
                texture.getWidth() * knobScale, texture.getHeight() * knobScale);
        font.setColor(TEXT);
        glyphs.setText(font, label, TEXT, slider.width + 30, Align.center, false);
        font.draw(batch, glyphs, slider.x - 15, slider.y + glyphs.height + 4);
        batch.end();
    }

    private float trackBottom(Rectangle slider) {
        return slider.y + 30 * layout.fontScale;
    }

    private float trackTop(Rectangle slider) {
        return slider.y + slider.height - 25;
    }

    @Override
    public void slide(boolean music, float y) {
        Rectangle slider= music ? layout.musicSlider : layout.soundSlider;
        float ratio= (y - trackBottom(slider)) / (trackTop(slider) - trackBottom(slider));
        float volume= Math.max(0, Math.min(1, ratio)) * GameAudio.MAX_VOLUME;
        if (music)
            audio.setMusicVolume(volume);
        else
            audio.setSoundVolume(volume);
    }

    /**
     * <p>Croix directionnelle tactile : quatre flèches autour d'un centre.</p>
     */
    private void drawDpad() {
        Rectangle pad= layout.dpad;
        float cx= pad.x + pad.width / 2, cy= pad.y + pad.height / 2;
        float radius= Math.min(pad.width, pad.height) / 2;
        float arm= radius * 0.36f;

        shapes.begin(ShapeType.Filled);
        shapes.setColor(DARK_PINK);
        shapes.circle(cx, cy, radius, 48);
        shapes.setColor(PINK);
        shapes.rect(cx - arm, cy - radius * 0.92f, 2 * arm, radius * 1.84f);
        shapes.rect(cx - radius * 0.92f, cy - arm, radius * 1.84f, 2 * arm);
        drawArrow(Direction.UP, cx, cy + radius * 0.6f, arm);
        drawArrow(Direction.DOWN, cx, cy - radius * 0.6f, arm);
        drawArrow(Direction.LEFT, cx - radius * 0.6f, cy, arm);
        drawArrow(Direction.RIGHT, cx + radius * 0.6f, cy, arm);
        shapes.end();
    }

    private void drawArrow(Direction direction, float x, float y, float size) {
        shapes.setColor(input.isDpadPressed(direction) ? Color.RED : Color.WHITE);
        float s= size * 0.8f;
        switch (direction) {
            case UP: shapes.triangle(x - s, y - s / 2, x + s, y - s / 2, x, y + s * 0.7f); break;
            case DOWN: shapes.triangle(x - s, y + s / 2, x + s, y + s / 2, x, y - s * 0.7f); break;
            case LEFT: shapes.triangle(x + s / 2, y - s, x + s / 2, y + s, x - s * 0.7f, y); break;
            case RIGHT: shapes.triangle(x - s / 2, y - s, x - s / 2, y + s, x + s * 0.7f, y); break;
        }
    }

    @Override
    public boolean tap(float x, float y) {
        if (session.isChoosingFirstLevels()) {
            List<AbstractLevel> levels= session.getFirstLevels();
            for (int i= 0; i < levels.size(); i++)
                if (levelButtons[i].contains(x, y)) {
                    session.select(levels.get(i));
                    return true;
                }
        }
        if (layout.gameArea.contains(x, y) && session.isWaitingForNextLevel()) {
            session.continueToNextLevel();
            return true;
        }
        return false;
    }

    //=============================   Cycle de vie   =============================

    @Override
    public void pause() {
        input.reset();
        session.setHeldDirection(null);
        audio.save();
    }

    @Override
    public void dispose() {
        audio.save();
        audio.dispose();
        textures.dispose();
        batch.dispose();
        shapes.dispose();
        font.dispose();
    }
}
