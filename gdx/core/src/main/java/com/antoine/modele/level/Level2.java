package com.antoine.modele.level;

import com.antoine.geometry.Coordinates;
import com.antoine.geometry.Rectangle;

/**
 * <b>Niveau dont la carte fait deux écrans de haut.</b>
 * <p>Quand le joueur atteint le bord haut ou bas de l'écran, l'affichage passe à l'écran suivant.</p>
 *
 * @author Antoine
 */
public class Level2 extends AbstractLevel {

	/**Hauteur et largeur d'un écran, en tuiles*/
	private static final int SCREEN_TILES= 20;

	/**Partie affichée de la carte, en tuiles*/
	private final Rectangle screen;

	public Level2(String name){
		super(name);
		screen= new Rectangle(0, SCREEN_TILES, SCREEN_TILES, 2 * SCREEN_TILES);
	}

	@Override
	public void playerMovesUp()
	{
		if(!isOnTop(-8) && isOnTopScreen())
			loadMap(-SCREEN_TILES, -player.getHeight());
		super.playerMovesUp();
	}

	@Override
	public void playerMovesDown()
	{
		if(!isOnBottom(4) && isOnBottomScreen())
			loadMap(SCREEN_TILES, player.getHeight());
		super.playerMovesDown();
	}

	private boolean isOnBottomScreen() {
		return (playerScreenPositionY() + player.getHeight()) >
			(SCREEN_TILES * tile_height - 4);
	}

	private boolean isOnTopScreen() {
		return playerScreenPositionY() <= 4;
	}

	/**
	 * <p>Change d'écran et fait passer le joueur de l'autre côté de la limite.</p>
	 * @param yVector le déplacement de l'écran, en tuiles.
	 * @param playerVector le déplacement du joueur, en pixels.
	 */
	private void loadMap(int yVector, int playerVector) {
		screen.translate(0, yVector);
		player.translate(new Coordinates(0, playerVector));
	}

	private int playerScreenPositionY() {
		int coef= player.getY() / (tile_height * SCREEN_TILES);
		return player.getY() - (coef * (tile_height * SCREEN_TILES));
	}

	@Override
	public Rectangle getScreen(){
		return new Rectangle(screen.getBeginX() * tile_width, screen.getEndX() * tile_width,
				screen.getBeginY() * tile_height, screen.getEndY() * tile_height);
	}
}
