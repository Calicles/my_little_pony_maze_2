package com.antoine.gdx.lwjgl3;

import com.antoine.gdx.PonyMazeGame;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

/**
 * <b>Lanceur de la version PC.</b>
 * <p>Avec l'argument "portrait", la fenêtre prend le format d'un téléphone.</p>
 */
public class Lwjgl3Launcher {

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config= new Lwjgl3ApplicationConfiguration();
        config.setTitle(PonyMazeGame.TITLE);
        // "portrait" ouvre une fenêtre au format d'un téléphone, avec les commandes tactiles.
        if (args.length > 0 && args[0].equals("portrait"))
            config.setWindowedMode(450, 800);
        else
            config.setWindowedMode(860, 800);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setWindowIcon("ressources/images/boutons/apple.png");
        new Lwjgl3Application(new PonyMazeGame(), config);
    }
}
