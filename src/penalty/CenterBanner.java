package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** Grosser Text in der Mitte  GOAL / MISS / SAVED + PERFECT LOB. */
public final class CenterBanner implements GameObj {
    private final Vertex p;
    private final ShotResult result;
    private final int ticksLeft;
    private final boolean perfectLob;
    private final ShotType shotType;

    public CenterBanner(double centerX, double centerY, ShotResult result, int ticksLeft, boolean perfectLob, ShotType shotType) {
        this.p = new Vertex(centerX, centerY);
        this.result = result;
        this.ticksLeft = ticksLeft;
        this.perfectLob = perfectLob;
        this.shotType = shotType;
    }

    @Override public Vertex pos() { return p; }
    @Override public Vertex velocity() { return new Vertex(0,0); }
    @Override public double width() { return 0; }
    @Override public double height() { return 0; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        if (result == ShotResult.NONE || ticksLeft <= 0) return;

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        float a = 1.0f;
        if (ticksLeft < 15) a = ticksLeft / 15.0f;

        String text = result.label;
        String sub = "";
        if (shotType == ShotType.LOB && perfectLob && result == ShotResult.GOAL) {
            sub = "PERFECT LOB!";
        }

        int boxW = 360;
        int boxH = sub.isEmpty() ? 90 : 118;

        int x = (int) Math.round(p.x - boxW / 2.0);
        int y = (int) Math.round(p.y - boxH / 2.0);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.55f * a));
        g2.setColor(Color.black);
        g2.fillRoundRect(x, y, boxW, boxH, 26, 26);

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f * a));

        g2.setFont(new Font("Helvetica", Font.BOLD, 52));
        FontMetrics fm = g2.getFontMetrics();
        int tx = (int) Math.round(p.x - fm.stringWidth(text) / 2.0);
        int ty = (int) Math.round(p.y + 18);

        g2.setColor(new Color(0, 0, 0, (int) (220 * a)));
        g2.drawString(text, tx + 2, ty + 2);

        g2.setColor(new Color(255, 255, 255, (int) (255 * a)));
        g2.drawString(text, tx, ty);

        if (!sub.isEmpty()) {
            g2.setFont(new Font("Helvetica", Font.BOLD, 20));
            FontMetrics fm2 = g2.getFontMetrics();
            int sx = (int) Math.round(p.x - fm2.stringWidth(sub) / 2.0);
            int sy = (int) Math.round(p.y + 48);

            g2.setColor(new Color(0, 0, 0, (int) (200 * a)));
            g2.drawString(sub, sx + 1, sy + 1);

            g2.setColor(new Color(80, 220, 110, (int) (255 * a)));
            g2.drawString(sub, sx, sy);
        }

        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }
}
