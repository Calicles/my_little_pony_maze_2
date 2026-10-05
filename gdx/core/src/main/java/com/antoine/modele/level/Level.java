package com.antoine.modele.level;

import com.antoine.geometry.Rectangle;

/**
 * <b>Représente un niveau de base : la carte tient entièrement dans l'écran.</b>
 *
 * @author Antoine
 */
public class Level extends AbstractLevel {

	public Level(String name){
		super(name);
	}

	@Override
	public Rectangle getScreen() {
		return new Rectangle(0, getMapWidth(), 0, getMapHeight());
	}
}
