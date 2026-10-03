package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** Fadenkreuz an der Torlinie marker zeigt wo man trifft. */
public final class AimMarker implements GameObj {
    private final Vertex pos;
    private final ShotType shotType;

    public AimMarker(double x, double y, ShotType shotType) {
        this.pos = new Vertex(x, y);
        this.shotType = shotType;
    }

    @Override public Vertex pos() { return pos; }
    @Override public Vertex velocity() { return new Vertex(0, 0); }
    @Override public double width() { return 0; }
    @Override public double height() { return 0; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        int x = (int) Math.round(pos.x);
        int y = (int) Math.round(pos.y);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawOval(x - 10, y - 10, 20, 20);
        g2.drawLine(x - 8, y, x + 8, y);
        g2.drawLine(x, y - 8, x, y + 8);

        if (shotType == ShotType.CURVE_LEFT) {
            g2.drawLine(x - 14, y - 4, x - 14, y + 4);
        } else if (shotType == ShotType.CURVE_RIGHT) {
            g2.drawLine(x + 14, y - 4, x + 14, y + 4);
        }
    }
}
