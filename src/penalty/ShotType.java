package penalty;

/**
 * Verschiedene Schussarten
 * 
 * Jede Art hat andere Physik!
 */
public enum ShotType {
    NORMAL("Normal"),           // Gerader Schuss
    CURVE_LEFT("Drall links"),  // Ball dreht sich nach links
    CURVE_RIGHT("Drall rechts"), // Ball dreht sich nach rechts
    LOB("Lupfer");              // Hoher Bogen uebers Tor (kann perfekt sein!)

    public final String label;  // Text fuers HUD
    ShotType(String label) { this.label = label; }
}
