package penalty;

/**
 * Was ist mit dem Schuss passiert?
 * 
 * Wird in der Mitte vom Bildschirm angezeigt
 */
public enum ShotResult {
    NONE(""),           // Noch kein Ergebnis
    GOAL("GOAL!"),      // TOOOR!
    SAVED("SAVED!"),    // Keeper hat gehalten
    MISS("MISS!");      // Daneben oder an den Pfosten

    public final String label;  // Der Text der angezeigt wird
    ShotResult(String label) { this.label = label; }
}
