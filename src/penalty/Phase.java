package penalty;

/**
 * Die verschiedenen Spiel-Phasen
 * 
 * Damit ich weiss was gerade passiert und entsprechend reagieren kann
 */
public enum Phase {
    AIMING,          // Spieler zielt gerade
    CHARGING,        // Spieler laed den Schuss (SPACE gedrueckt)
    BALL_IN_FLIGHT,  // Ball fliegt zum Tor
    AFTER_SHOT,      // Nach dem Schuss, kurz warten
    WON              // Spieler hat gewonnen (5 Tore!)
}
