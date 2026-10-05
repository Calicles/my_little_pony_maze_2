package com.antoine.gdx;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Disposable;

import java.util.HashMap;
import java.util.Map;

/**
 * <b>Charge les images du jeu à la demande et les garde en mémoire.</b>
 * <p>Les grandes images de fin ne sont gardées qu'une à la fois : l'animation finale
 * en compte 34.</p>
 */
final class Textures implements Disposable {

    private final Map<String, Texture> cache= new HashMap<>();
    private String endImagePath;
    private Texture endImage;

    /**
     * @param path chemin au format du jeu ("/ressources/...").
     */
    Texture get(String path) {
        Texture texture= cache.get(path);
        if (texture == null) {
            texture= load(path);
            cache.put(path, texture);
        }
        return texture;
    }

    /**
     * @return l'image de fin demandée ; la précédente est libérée.
     */
    Texture endImage(String path) {
        if (!path.equals(endImagePath)) {
            if (endImage != null)
                endImage.dispose();
            endImage= load(path);
            endImagePath= path;
        }
        return endImage;
    }

    private static Texture load(String path) {
        Texture texture= new Texture(Gdx.files.internal(path.startsWith("/") ? path.substring(1) : path));
        texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        return texture;
    }

    @Override
    public void dispose() {
        for (Texture texture : cache.values())
            texture.dispose();
        cache.clear();
        if (endImage != null)
            endImage.dispose();
        endImage= null;
        endImagePath= null;
    }
}
