package com.antoine.services;

import org.junit.Test;

import java.awt.image.BufferedImage;
import java.util.HashMap;

import static org.junit.Assert.*;

public class Map_readerTest {

    String mapPath= "/ressources/maps/levelApple/map.txt";
    String tileSetPath= "/ressources/maps/levelApple/tileSet.txt";


    /**
     * test de non nullité pour map
     */
    @Test
    public void readMap() {
        int[][] map= Map_reader.readMap(mapPath);
        assertNotNull(map);
    }


    /**
     * test de non nullité pour tileSet
     */
    @Test
    public void readTileSet() {
       HashMap<Integer, BufferedImage> set= Map_reader.readTileSet(tileSetPath);
       assertNotNull(set);
       assertFalse(set.isEmpty());
    }


    /**
     * test sur nombre de lignes
     */
    @Test
    public void readMap2() {
        int[][] map= Map_reader.readMap(mapPath);
        assertEquals(20, map.length);
    }


    /**
     * test sur nombre de colonnes
     */
    @Test
    public void readMap3() {
        int[][] map= Map_reader.readMap(mapPath);
        assertEquals(20, map[0].length);
    }


    /**
     * test de valeur d'une cellule
     */
    @Test
    public void readMap4() {
        int[][] map= Map_reader.readMap(mapPath);
        assertEquals(7, map[1][0]);
    }
}
