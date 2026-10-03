package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;


public final class SafeHudImage implements GameObj {
    private final Vertex p;
    private final double w, h;
    private final Image img;

    public SafeHudImage(Vertex p, double w, double h, String res) {
        this.p = p;
        this.w = w;
        this.h = h;
        this.img = GameConfig.loadImageOrNull(SafeHudImage.class, res);
    }

    @Override public Vertex pos() { return p; }
    @Override public Vertex velocity() { return new Vertex(0,0); }
    @Override public double width() { return w; }
    @Override public double height() { return h; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (img != null) {
            g2.drawImage(img, (int) p.x, (int) p.y, null);
            return;
        }

        int x = (int) p.x;
        int y = (int) p.y;
        int ww = (int) w;
        int hh = (int) h;

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillRoundRect(x, y, ww, hh, 26, 26);

        g2.setColor(Color.white);
        g2.setFont(new Font("Helvetica", Font.BOLD, 56));
        String t = "WINNER!";
        FontMetrics fm = g2.getFontMetrics();
        g2.drawString(t, x + (ww - fm.stringWidth(t)) / 2, y + 110);

        g2.setFont(new Font("Helvetica", Font.PLAIN, 18));
        String t2 = "Press R to play again";
        FontMetrics fm2 = g2.getFontMetrics();
        g2.drawString(t2, x + (ww - fm2.stringWidth(t2)) / 2, y + 155);
    }
}
