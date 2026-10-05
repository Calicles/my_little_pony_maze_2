package com.antoine.modele;

import com.antoine.entity.Boss;
import com.antoine.entity.Player;
import com.antoine.geometry.Coordinates;
import com.antoine.modele.level.AbstractLevel;
import com.antoine.modele.level.Level;
import com.antoine.modele.level.Level2;
import com.antoine.modele.level.Level3;
import com.antoine.modele.level.Level4;
import com.antoine.modele.map.Map;
import com.antoine.transfert_strategy.IA_transfertStrategy_withHeuristic;
import com.antoine.transfert_strategy.Player_transferStrategy_std;

/**
 * <b>Construit les niveaux du jeu.</b>
 * <p>Reprend le paramétrage de la version d'origine (fichier conf.xml) : cartes, personnages,
 * positions de départ, vitesses et images de fin.</p>
 *
 * @author Antoine
 */
public final class LevelFactory {

    private static final String MAPS= "/ressources/maps/";
    private static final String ANIMATIONS= "/ressources/setPonyAnimation/";
    private static final String END_IMAGES= "/ressources/images/fin/";

    /**Vitesse du joueur et du boss, en pixels par pas*/
    private static final int PLAYER_SPEED= 4, BOSS_SPEED= 3;

    private LevelFactory() {}

    public static Level apple() {
        return build(new Level("apple"), "levelApple", "apple.png", "setApple.txt", 40, 50);
    }

    public static Level rarity() {
        return build(new Level("rarity"), "levelRarity", "all4.jpg", "setRarity.txt", 572, 570);
    }

    public static Level rainbow() {
        return build(new Level("rainbow"), "levelRainbow", "my-little-pony2.jpg", "setRainbow.txt", 40, 570);
    }

    public static Level2 flutter() {
        return build(new Level2("flutter"), "levelFlutter", "transition6.png", "setFlutter.txt", 40, 690);
    }

    public static Level3 pinky() {
        return build(new Level3("pinky"), "levelPinky", "transition5.png", "setPinky.txt", 315, 960);
    }

    public static Level4 twilight() {
        Level4 level= build(new Level4("twilight"), "levelTwilight", null, "setTwilight.txt", 880, 300);
        level.setLoseImagePath(END_IMAGES + "discord3.png");
        level.setEndAnimation(END_IMAGES + "final/fin%d.jpg", 32, END_IMAGES + "final/fin.jpg");

        Boss boss= new Boss();
        boss.setAnimation(ANIMATIONS + "setDiscord.txt");
        boss.setPosition(new Coordinates(110, -150));
        IA_transfertStrategy_withHeuristic deplacement= new IA_transfertStrategy_withHeuristic();
        deplacement.setVector(new Coordinates(BOSS_SPEED, BOSS_SPEED));
        boss.setDeplacement(deplacement);
        level.setBoss(boss);
        return level;
    }

    private static <L extends AbstractLevel> L build(L level, String mapDir, String endImage,
                                                     String animation, int x, int y) {
        if (endImage != null)
            level.setEndImageUrl(END_IMAGES + endImage);

        Player player= new Player();
        player.setAnimation(ANIMATIONS + animation);
        player.setPosition(new Coordinates(x, y));
        Player_transferStrategy_std deplacement= new Player_transferStrategy_std();
        deplacement.setVector(new Coordinates(PLAYER_SPEED, PLAYER_SPEED));
        player.setDeplacement(deplacement);
        level.setPlayer(player);

        Map map= new Map();
        map.setTileSet(MAPS + mapDir + "/tileSet.txt");
        map.setMap(MAPS + mapDir + "/map.txt");
        level.setMap(map);
        return level;
    }
}
