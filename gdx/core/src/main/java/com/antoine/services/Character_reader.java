package com.antoine.services;

import java.io.BufferedReader;
import java.util.HashMap;

/**
 * <b>Classe de service, lit le fichier de configuration d'un personnage (sprites d'animation).</b>
 * Fournit le tableau association entre un entier (représente une des 4 directions) et un tableau
 * de chemins d'images (représente l'animation dans une direction).
 *
 * Le nombre de direction contenu dans le fichier et le nombre d'image d'animation par direction
 * est dans le header du ficher.
 *
 * @author Antoine
 */
public class Character_reader {

	public static HashMap<Integer, String[]> readCharactereAnimation(String url){
		HashMap<Integer, String[]> map= new HashMap<>();

		try(BufferedReader reader= Resources.reader(url)) {

			String[] bounds= reader.readLine().split(" ");
			int directionNumber= Integer.parseInt(bounds[0]);
			int imagePerDirection= Integer.parseInt(bounds[1]);
			String[] tab;

			for(int i= 0; i < directionNumber; i++) {

				tab= new String[imagePerDirection];

				for(int j= 0; j < imagePerDirection; j++) {
					tab[j]= reader.readLine().trim();
				}

				map.put(i, tab);
			}

		}catch(Throwable t) {
			throw new RuntimeException("Erreur de Lecture d'image animation : " + url, t);
		}

		return map;
	}

}
