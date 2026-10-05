package com.antoine.modele.level;

import com.antoine.contracts.IMap;
import com.antoine.geometry.Coordinates;
import com.antoine.geometry.DoubleBoxes;
import com.antoine.geometry.Rectangle;

/**
 * <b>Niveau à défilement : l'écran suit le joueur sur une carte plus grande que lui.</b>
 * <p>L'écran défile quand le joueur sort d'une zone centrale (la scrollBox).</p>
 *
 * @author Antoine
 */
public class Level3 extends AbstractLevel {

	/**L'écran et sa zone de défilement*/
	protected DoubleBoxes boxes;

	public Level3(String name){
		super(name);
	}

	@Override
	public void setMap(IMap map){
		super.setMap(map);
		initBoxes();
	}

	/**
	 * <p>Place l'écran et la zone de défilement au départ du niveau.</p>
	 */
	protected void initBoxes() {
		Rectangle screen= new Rectangle(0, 20*tile_width, 20*tile_height,
				40* tile_height);
		Rectangle scrollBox= new Rectangle(5*tile_width, 15*tile_width,
				25*tile_height, 35*tile_height);
		this.boxes= new DoubleBoxes(screen, scrollBox);
	}

	@Override
	public void playerMovesUp() {
		checkRunning();
		player.movesUp();
		scrollUp(player.memorizeMoves(map));
	}

	@Override
	public void playerMovesDown() {
		checkRunning();
		player.movesDown();
		scrollDown(player.memorizeMoves(map));
	}

	@Override
	public void playerMovesLeft() {
		checkRunning();
		player.movesLeft();
		scrollLeft(player.memorizeMoves(map));
	}

	@Override
	public void playerMovesRight() {
		checkRunning();
		player.movesRight();
		scrollRight(player.memorizeMoves(map));
	}

	protected void scrollUp(Coordinates vector){
		if(!screenOnTop() &&
				boxes.isPlayerOnTopScroll(player.getY()+ vector.getY()))
			boxes.scroll(0, vector.getY() );
	}

	protected void scrollDown(Coordinates vector){
		if(!screenOnBottom() &&
				boxes.isPlayerOnBottomScroll(player.getY()+
						player.getHeight() + vector.getY()))
			boxes.scroll(0, vector.getY());
	}

	protected void scrollLeft(Coordinates vector){
		if(!screenOnLeft() &&
				boxes.isPlayerOnLeftScroll(player.getX() + vector.getX()))
			boxes.scroll(vector.getX(), 0);
	}

	protected void scrollRight(Coordinates vector){
		if(!screenOnRight() &&
				boxes.isPlayerOnRightScroll(player.getX() + player.getWidth() + vector.getX()))
			boxes.scroll(vector.getX(), 0);
	}

	private boolean screenOnRight() {
		return boxes.getScreenEndX() >= mapSize.getEndX();
	}

	private boolean screenOnLeft() {
		return boxes.getScreenBeginX() <= 0;
	}

	private boolean screenOnBottom() {
		return boxes.getScreenEndY() >= mapSize.getEndY();
	}

	private boolean screenOnTop() {
		return boxes.getScreenBeginY() <= 0;
	}

	@Override
	public Rectangle getScreen() {
		return boxes.getScreen();
	}
}
