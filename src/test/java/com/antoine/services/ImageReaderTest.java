package com.antoine.services;

import org.junit.Test;

import java.awt.image.BufferedImage;

import static org.junit.Assert.*;

public class ImageReaderTest {

    String path= "/ressources/images/tapis1.png";


    /**
     * test de lancement d'exception si le fichier n'existe pas
     */
    @Test (expected = RuntimeException.class)
    public void lireImage() {
        ImageReader.lireImage("taratata");
    }


    /**
     * test de valeur de longueur d'une image
     * vérifie le chargement
     */
    @Test
    public void lireImage1() {
        BufferedImage image= ImageReader.lireImage(path);

        assertEquals(32, image.getWidth());
    }
}
