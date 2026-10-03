package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** Pitch background  mit Ersatz-Zeichnung (Linux safe hoffentlich. */
public final class SafePitch implements GameObj {
    private final Vertex p = new Vertex(0, 0);
    private final Image img;

    public SafePitch(String res) {
        this.img = GameConfig.loadImageOrNull(SafePitch.class, res);
    }

    @Override public Vertex pos() { return p; }
    @Override public Vertex velocity() { return new Vertex(0, 0); }
    @Override public double width() { return GameConfig.FIELD_W; }
    @Override public double height() { return GameConfig.FIELD_H; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        final int standsH = GameConfig.STANDS_H;

        // Save clip once
        Shape oldClip = g2.getClip();

        // Draw pitch only below stands, so crowd stays visible
        g2.setClip(0, standsH, GameConfig.FIELD_W, GameConfig.FIELD_H - standsH);

        if (img != null) {
            g2.drawImage(img, 0, 0, null);
        } else {
            paintFallbackPitch(g2, standsH);
        }

        // Restore clip
        g2.setClip(oldClip);

        // Separator line between crowd and pitch
        g2.setColor(new Color(255, 255, 255, 60));
        g2.drawLine(0, standsH, GameConfig.FIELD_W, standsH);
    }

    private static void paintFallbackPitch(Graphics2D g2, int standsH) {
        // Grass base
        g2.setColor(new Color(36, 145, 70));
        g2.fillRect(0, standsH, GameConfig.FIELD_W, GameConfig.FIELD_H - standsH);

        // Stripes
        g2.setColor(new Color(30, 130, 63));
        for (int x = 0; x < GameConfig.FIELD_W; x += 80) {
            g2.fillRect(x, standsH, 40, GameConfig.FIELD_H - standsH);
        }

        // Goal + net (simple)
        int goalY = GameConfig.GOAL_LINE_Y;
        int left = GameConfig.POST_LEFT;
        int right = GameConfig.POST_RIGHT;

        // If goal line is inside crowd zone (possible), clamp it down to be visible in pitch area
        if (goalY < standsH + 10) goalY = standsH + 10;

        // Frame
        g2.setColor(new Color(245, 245, 245));
        g2.setStroke(new BasicStroke(4f));
        g2.drawRect(left, goalY, right - left, 120);

        // Net grid
        g2.setStroke(new BasicStroke(1f));
        g2.setColor(new Color(255, 255, 255, 170));
        for (int x = left + 12; x < right; x += 18) {
            g2.drawLine(x, goalY + 2, x, goalY + 118);
        }
        for (int y = goalY + 12; y < goalY + 120; y += 18) {
            g2.drawLine(left + 2, y, right - 2, y);
        }

        // Reset stroke
        g2.setStroke(new BasicStroke(1f));
    }
}
