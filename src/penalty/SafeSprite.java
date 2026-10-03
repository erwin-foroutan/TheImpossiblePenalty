package penalty;

import name.panitz.game2d.AbstractGameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** Shooter/Ball sprites mit Ersatz-Zeichnung alles müsste eigentlich angezeigt werden. */
public final class SafeSprite extends AbstractGameObj {
    private final Image image;
    private final Kind kind;

    public enum Kind { SHOOTER, BALL }

    private SafeSprite(Vertex p, Vertex v, double w, double h, String res, Kind kind) {
        super(p, v, w, h);
        this.image = GameConfig.loadImageOrNull(SafeSprite.class, res);
        this.kind = kind;
    }

    public static SafeSprite shooter(Vertex p) {
        return new SafeSprite(p, new Vertex(0, 0), 44, 84, GameConfig.RES_SHOOTER, Kind.SHOOTER);
    }

    public static SafeSprite ball(Vertex p) {
        return new SafeSprite(p, new Vertex(0, 0), 24, 24, GameConfig.RES_BALL, Kind.BALL);
    }

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (image != null) {
            g2.drawImage(image, (int) pos().x, (int) pos().y, null);
            return;
        }

        // Fallback drawings
        if (kind == Kind.BALL) {
            int x = (int) pos().x;
            int y = (int) pos().y;
            int s = (int) width();

            g2.setColor(new Color(250, 250, 250));
            g2.fillOval(x, y, s, s);
            g2.setColor(new Color(30, 30, 30, 160));
            g2.drawOval(x, y, s, s);
            g2.drawOval(x + 6, y + 6, s - 12, s - 12);
            return;
        }

        // SHOOTER fallback: simple body + head + legs
        int x = (int) pos().x;
        int y = (int) pos().y;

        g2.setColor(new Color(25, 25, 25, 200));
        g2.fillRoundRect(x + 12, y + 16, 20, 44, 12, 12);
        g2.setColor(new Color(235, 210, 160));
        g2.fillOval(x + 14, y + 4, 16, 16);

        g2.setColor(new Color(20, 120, 210));
        g2.fillRoundRect(x + 10, y + 30, 24, 22, 10, 10);

        g2.setColor(new Color(40, 40, 40));
        g2.fillRoundRect(x + 8, y + 58, 12, 22, 8, 8);
        g2.fillRoundRect(x + 24, y + 58, 12, 22, 8, 8);
    }
}
