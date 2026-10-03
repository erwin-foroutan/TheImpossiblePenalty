package penalty;

import javax.swing.ImageIcon;
import java.awt.Image;
import java.net.URL;

/** Gemeinsame Konstanten + helpers. */
public final class GameConfig {
    private GameConfig() {}

    // === SPIELFELD-GROESSEN ===
    public static final int FIELD_W = 800;
    public static final int FIELD_H = 600;
    public static final int HUD_W   = 280;

    public static final int STANDS_H = 120;      // Zuschauer-Bereich oben

    public static final int W = FIELD_W + HUD_W;
    public static final int H = FIELD_H;

    // Tor-Bereich
    public static final int GOAL_LINE_Y = 120;
    public static final int POST_LEFT   = FIELD_W / 2 - 160;
    public static final int POST_RIGHT  = FIELD_W / 2 + 160;

    // Spieler und Ball Positionen
    public static final int SHOOTER_Y    = 500;
    public static final int BALL_START_Y = 470;

    // === ZEIT-LIMITS ===
    public static final double TIME_LIMIT_NORMAL = 30.0;
    public static final double TIME_LIMIT_IMPOSSIBLE = 8.0;
    public static final double DT = 0.013; // SwingScreen timer tick ~13ms

    // === KURVEN-PHYSIK ===
    public static final double CURVE_BASE = 0.025;
    public static final double CURVE_MAX_EXTRA = 0.045;
    public static final double CURVE_PREDICT_MAX = 75.0;

    // === LUPFER TIMING-FENSTER ===
    public static final double LOB_FINISH_CENTER = 0.72;
    public static final double LOB_GREEN_HALF_NORMAL = 0.05;
    public static final double LOB_GREEN_HALF_IMPOSSIBLE = 0.024;

    // === BILD-DATEIEN (case-sensitive!) ===
    public static final String RES_ICON = "penalty/res/icon.png";
    public static final String RES_PITCH   = "penalty/res/pitch.png";
    public static final String RES_SHOOTER = "penalty/res/shooter.png";
    public static final String RES_BALL    = "penalty/res/ball.png";
    public static final String RES_KEEPER  = "penalty/res/keeper.png";
    public static final String RES_WINNER  = "penalty/res/winner.png";

    public static double clamp(double v, double lo, double hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static double clamp01(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }

    public static double curvePerTick(double power) {
        double p = clamp01(power);
        return CURVE_BASE + CURVE_MAX_EXTRA * p;
    }

    public static Image loadImageOrNull(Class<?> cls, String resourcePath) {
        try {
            URL url = cls.getClassLoader().getResource(resourcePath);
            if (url == null) return null;
            return new ImageIcon(url).getImage();
        } catch (Exception ex) {
            return null;
        }
    }
}
