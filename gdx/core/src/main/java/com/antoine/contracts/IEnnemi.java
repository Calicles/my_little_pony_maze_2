package com.antoine.contracts;

import com.antoine.geometry.Rectangle;


/**
 * <b>Représente un ennemi</b>
 *
 * @author Antoine
 */
public interface IEnnemi extends IEntity {

    /**
     * <p>Redémarre le processus de reflexion dans le recherche de chemin.</p>
     * Appelé à chaque tour de la boucle de jeu.
     */
    void think();

    /**
     * <p>Translate les coordonnées du personnage par le vecteur de déplacement.</p>
     */
    void memorizeMoves();

    /**
     * <p>Affecte les attributs nécessaires aux déplacement.</p>
     * @param player le joueur dont la position peut être détecté.
     * @param map la carte pour détecter les collisions.
     */
    void setAttributes(Rectangle player, IMap map);

    /**
     * <p>Premier calcul de la direction du déplacement.</p>
     */
    void startThinking();
}
