package com.antoine.gdx.android;

import android.os.Bundle;

import com.antoine.gdx.PonyMazeGame;
import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

/**
 * <b>Lanceur de la version Android.</b>
 */
public class AndroidLauncher extends AndroidApplication {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AndroidApplicationConfiguration config= new AndroidApplicationConfiguration();
        config.useImmersiveMode= true;
        config.useAccelerometer= false;
        config.useCompass= false;
        initialize(new PonyMazeGame(), config);
    }
}
