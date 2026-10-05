package com.antoine.modele.level;

import com.antoine.contracts.IEntity;
import com.antoine.contracts.IMap;
import com.antoine.geometry.Rectangle;
import com.antoine.geometry.Tile;

/**
 * <b>Classe abstraite qui représente le cadre d'un niveau de jeu.</b>
 * <p>Un niveau contient une carte, un joueur et une sortie ; il est terminé quand le joueur
 * est entièrement entré dans la sortie.</p>
 *
 * @author Antoine
 */
public abstract class AbstractLevel {

	/**Nom du niveau (apple, rarity...), sert aussi d'identifiant pour la musique*/
	private final String name;

	/**La carte de jeu*/
	protected IMap map;

	/**Le joueur*/
	protected IEntity player;

	/**Les dimensions de la map sous forme de rectangle*/
	protected Rectangle mapSize;

	/**Les dimensions et coordonnées du rectange qui simule la sortie d'un labyrinthe*/
	private Rectangle exit;

	/**Path de l'image de fin, affichée une fois le joueur sorti*/
	protected String endImageUrl;

	/**Etat, niveau terminé ou non*/
	protected boolean running;

	/**Dimensions des tuiles*/
	protected int tile_width, tile_height;

	protected AbstractLevel(String name){
		this.name= name;
		running= true;
	}

	public String getName() {return name;}

	public void setMap(IMap map){
		this.map= map;
		tile_width= map.getTile_width();
		tile_height= map.getTile_height();
		exit= tileToRectangle(map.findExit());
		int[] tab= map.getDimension();
		mapSize= new Rectangle(tab[0], tab[1]);
	}

	public void setPlayer(IEntity player){
		this.player= player;
	}

	public void setEndImageUrl(String endImageUrl){
		this.endImageUrl= endImageUrl;
	}

	public IEntity getPlayer(){return player;}

	public String getEndImageUrl(){return endImageUrl;}

	public IMap getMap(){return map;}

	public int getMapWidth(){return mapSize.getWidth();}

	public int getMapHeight(){return mapSize.getHeight();}

	/**
	 * @return le boss du niveau, null s'il n'y en a pas.
	 */
	public IEntity getBoss(){return null;}

	/**
	 * @return la partie de la carte visible à l'écran, en pixels.
	 */
	public abstract Rectangle getScreen();

	/**
	 *
	 * @return true si niveau n'est pas terminé, false sinon.
	 */
	public boolean isRunning() {return running;}

	/**
	 * @return true si le joueur a atteint la sortie.
	 */
	public boolean isFinished() {return !running;}

	/**
	 * <p>Démarre le niveau (utile aux niveaux qui ont leur propre déroulement).</p>
	 */
	public void start() {}

	/**
	 * <p>Fait avancer le déroulement propre au niveau (animations, ennemis).</p>
	 * @param delta le temps écoulé depuis le dernier appel, en secondes.
	 */
	public void update(float delta) {}

	public void playerMovesReleased(){
		player.movesReleased();
	}

	public void playerMovesLeft() {
		checkRunning();
		player.movesLeft();
		player.memorizeMoves(map);
	}

	public void playerMovesRight() {
		checkRunning();
		player.movesRight();
		player.memorizeMoves(map);
	}

	public void playerMovesUp() {
		checkRunning();
		player.movesUp();
		player.memorizeMoves(map);
	}

	public void playerMovesDown() {
		checkRunning();
		player.movesDown();
		player.memorizeMoves(map);
	}

	/**
	 * <p>Teste si le joueur atteint la bordure haute de la carte, par projection (ajout du vecteur)</p>
	 * @param toTest le vecteur.
	 * @return true si le joueur atteint la bordure, false sinon.
	 */
	protected boolean isOnTop(int toTest) {
		return mapSize.isOnTop(player.getY() + toTest);
	}

	/**
	 * @see #isOnTop(int)
	 * @param toTest le vecteur
	 * @return true si le joueur atteint la bordure bas de la carte.
	 */
	protected boolean isOnBottom(int toTest) {
		int y= player.getY() + player.getHeight();
		return mapSize.isOnBottom(y + toTest);
	}

	/**
	 * <p>Transforme la tuile qui symbolise la sortie en rectangle pouvant contenir le joueur.</p>
	 * @param tile la tuile marquée comme sortie.
	 * @return un nouveau rectangle de dimensions de deux tuiles.
	 */
	private Rectangle tileToRectangle(Tile tile) {
		int endX= tile.getX() + tile_width * 2;
		int endY= tile.getY() + tile_height * 2;
		return new Rectangle(tile.getX(), endX, tile.getY(), endY);
	}

	/**
	 * <p>Vérifie si le joueur est totalement dans l'espace de sortie.</p>
	 * Place le flag running à false si le joueur est sorti.
	 */
	protected void checkRunning(){
		if(Rectangle.isInBox(exit, player.getPosition())){
			running= false;
		}
	}
}
