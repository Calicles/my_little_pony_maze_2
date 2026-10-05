package com.antoine.services;

import java.io.BufferedReader;
import java.util.HashMap;

/**
 * <b>Classe de service, lit les fichiers décrivant une carte de jeu.</b>
 * <p>La carte est une matrice de numéros de tuiles, le tileSet associe chaque numéro
 * au chemin de son image.</p>
 *
 * @author Antoine
 */
public class Map_reader {

	/**
	 * <p>lit la matrice du fichier .txt</p>
	 * @param fileUrl path du fichier
	 * @return la matrice sous forme d'int.
	 */
	public static int[][] readMap(String fileUrl){
		try(BufferedReader reader= Resources.reader(fileUrl)){

			String[] bounds= reader.readLine().trim().split("\\s+");
			int iMax= Integer.parseInt(bounds[0]);
			int jMax= Integer.parseInt(bounds[1]);
			int[][] map= new int[iMax][jMax];

			for(int i= 0; i < iMax; i++) {
				String[] line= reader.readLine().trim().split("\\s+");
				for(int j= 0; j < jMax; j++) {
					map[i][j]= Integer.parseInt(line[j]);
				}
			}
			return map;

		}catch(Exception e) {
			throw new RuntimeException("Erreur de lecture de la carte : " + fileUrl, e);
		}
	}


	/**
	 * <p>lit l'association numéro/image à partir du fichier .txt</p>
	 * @param fileUrl path du fichier contenant l'association.
	 * @return un tableau associatif numéro de tuile / chemin de l'image.
	 */
	public static HashMap<Integer, String> readTileSet(String fileUrl){
		HashMap<Integer, String> tileSet= new HashMap<>();

		try(BufferedReader reader= Resources.reader(fileUrl)){

			int bounds=  Integer.parseInt(reader.readLine().trim());

			for(int i= 0; i < bounds; i++) {
				String[] line= reader.readLine().trim().split("\\s+");
				tileSet.put(Integer.parseInt(line[0]), line[1]);
			}

		}catch(Exception e) {
			throw new RuntimeException("Erreur de lecture du tileSet : " + fileUrl, e);
		}

		return tileSet;
	}

}
