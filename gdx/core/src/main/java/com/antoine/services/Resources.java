package com.antoine.services;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>Accès aux fichiers du jeu (cartes, animations, images).</b>
 * <p>Par défaut les fichiers sont lus dans le classpath ; le jeu libGDX remplace la source
 * pour lire les assets de la plateforme (Android, desktop).</p>
 * Les chemins gardent le format d'origine : "/ressources/...".
 *
 * @author Antoine
 */
public final class Resources {

    /**Source des fichiers.*/
    public interface Loader {
        InputStream open(String path) throws IOException;
    }

    private static Loader loader = path -> {
        InputStream stream = Resources.class.getResourceAsStream(path);
        if (stream == null)
            throw new IOException("Ressource introuvable : " + path);
        return stream;
    };

    /**Dimensions des images déjà lues, par chemin.*/
    private static final Map<String, int[]> sizes = new HashMap<>();

    private Resources() {}

    public static void setLoader(Loader newLoader) {
        loader = newLoader;
        synchronized (sizes) {
            sizes.clear();
        }
    }

    public static InputStream open(String path) throws IOException {
        return loader.open(path);
    }

    public static BufferedReader reader(String path) throws IOException {
        return new BufferedReader(new InputStreamReader(open(path), StandardCharsets.UTF_8));
    }

    /**
     * <p>Lit la largeur et la hauteur d'une image PNG dans son en-tête, sans la décoder.</p>
     * @param path le chemin de l'image.
     * @return {largeur, hauteur}.
     * @throws RuntimeException si l'image est introuvable ou n'est pas un PNG.
     */
    public static int[] imageSize(String path) {
        synchronized (sizes) {
            int[] size = sizes.get(path);
            if (size != null)
                return size;

            try (DataInputStream in = new DataInputStream(open(path))) {
                byte[] header = new byte[16];
                in.readFully(header);
                // Signature PNG (8 octets), longueur (4) et type "IHDR" (4), puis largeur et hauteur.
                if ((header[0] & 0xFF) != 0x89 || header[1] != 'P' || header[2] != 'N' || header[3] != 'G'
                        || header[12] != 'I' || header[13] != 'H' || header[14] != 'D' || header[15] != 'R')
                    throw new IOException("Pas une image PNG : " + path);
                size = new int[]{in.readInt(), in.readInt()};
            } catch (IOException e) {
                throw new RuntimeException("Erreur de lecture de l'image : " + path, e);
            }
            sizes.put(path, size);
            return size;
        }
    }
}
