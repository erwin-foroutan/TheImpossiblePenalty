package penalty;

/**
 * Die verschiedenen Schwierigkeitsgrade
 * 
 * NORMAL: Torwart ist langsamer und ungenauer
 * IMPOSSIBLE: Torwart lernt von deinen Schuessen und ist viel schneller!
 */
public enum Difficulty {
    NORMAL("Normal"),
    IMPOSSIBLE("Unmoeglich");

    public final String label;  // Text der im HUD angezeigt wird
    Difficulty(String label) { this.label = label; }
}
