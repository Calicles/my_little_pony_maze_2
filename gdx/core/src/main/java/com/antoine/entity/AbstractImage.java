package com.antoine.entity;

import com.antoine.services.Resources;

/**
 * Classe gérant une image 2D et ses dimensions.
 * <p>L'image est représentée par son chemin : c'est l'affichage qui la charge.</p>
 * @author antoine
 */
public abstract class AbstractImage {

	/**
	 * Le chemin de l'image pour affichage
	 */
	protected String image;

	protected AbstractImage(){}

	public void setImage(String imageUrl) {
		image= imageUrl;
	}

	public int getWidth() {return Resources.imageSize(image)[0];}
	public int getHeight() {return Resources.imageSize(image)[1];}
	public String getImage() {return image;}

}
