package com.antoine.gdx;

import com.badlogic.gdx.math.Rectangle;

/**
 * <b>Disposition des éléments de l'écran, en coordonnées virtuelles (origine en bas à gauche).</b>
 * <p>En paysage (PC), elle reprend la fenêtre d'origine : curseurs de volume sur les côtés,
 * boutons des niveaux en bas. En portrait (téléphone), le jeu est en haut et les commandes
 * tactiles en bas.</p>
 */
final class Layout {

    /**Taille du jeu en pixels d'origine (20 tuiles de 32 pixels)*/
    static final float GAME_SIZE= 640;

    final boolean portrait;
    final float width, height;

    final Rectangle progressBar;
    final Rectangle gameArea;
    /**Boutons des trois premiers niveaux, ou mini-carte*/
    final Rectangle bottomPanel;
    final Rectangle musicSlider, soundSlider;
    /**Zone du D-pad, null en paysage (on joue au clavier)*/
    final Rectangle dpad;
    final float fontScale;

    private Layout(boolean portrait, float width, float height, Rectangle progressBar, Rectangle gameArea,
                   Rectangle bottomPanel, Rectangle musicSlider, Rectangle soundSlider, Rectangle dpad, float fontScale) {
        this.portrait= portrait;
        this.width= width;
        this.height= height;
        this.progressBar= progressBar;
        this.gameArea= gameArea;
        this.bottomPanel= bottomPanel;
        this.musicSlider= musicSlider;
        this.soundSlider= soundSlider;
        this.dpad= dpad;
        this.fontScale= fontScale;
    }

    static Layout forScreen(int screenWidth, int screenHeight) {
        return screenHeight > screenWidth ? portrait() : landscape();
    }

    private static Layout landscape() {
        return new Layout(false, 860, 800,
                new Rectangle(110, 768, 640, 26),
                new Rectangle(110, 120, GAME_SIZE, GAME_SIZE),
                new Rectangle(110, 6, 640, 108),
                new Rectangle(15, 150, 80, 600),
                new Rectangle(765, 150, 80, 600),
                null, 1.3f);
    }

    private static Layout portrait() {
        return new Layout(true, 720, 1280,
                new Rectangle(20, 1228, 680, 36),
                new Rectangle(10, 512, 700, 700),
                new Rectangle(10, 392, 700, 110),
                new Rectangle(455, 70, 110, 300),
                new Rectangle(595, 70, 110, 300),
                new Rectangle(20, 10, 380, 370), 2f);
    }

    /**Facteur entre les pixels du jeu et la zone de jeu à l'écran*/
    float gameScale() {
        return gameArea.width / GAME_SIZE;
    }
}
