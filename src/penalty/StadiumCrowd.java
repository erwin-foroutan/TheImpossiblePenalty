package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** Gezeichnete Zuschauer hinter dem Tor (ohne Bilder). */
public final class StadiumCrowd implements GameObj {
    private final Vertex p = new Vertex(0, 0);
    private int tick = 0;
    private int cheerTicks = 0;

    private final int x0 = 0;
    private final int y0 = 0;
    private final int w = GameConfig.FIELD_W;
    private final int h =  GameConfig.STANDS_H;;

    private final int stepX = 6;
    private final int stepY = 6;
    private final int seed = 12345;

    public void tick() {
        tick++;
        if (cheerTicks > 0) cheerTicks--;
    }

    public void cheer() {
        cheerTicks = 40;
    }

    @Override public Vertex pos() { return p; }
    @Override public Vertex velocity() { return new Vertex(0,0); }
    @Override public double width() { return 0; }
    @Override public double height() { return 0; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        GradientPaint gp = new GradientPaint(
                0, y0, new Color(10, 10, 14, 230),
                0, y0 + h, new Color(30, 30, 40, 180)
        );
        g2.setPaint(gp);
        g2.fillRect(x0, y0, w, h);

        g2.setColor(new Color(255, 255, 255, 40));
        g2.fillRect(0, y0 + h - 26, w, 6);

        int local = seed;
        boolean cheerFlash = cheerTicks > 0 && ((tick / 3) % 2 == 0);

        for (int y = y0 + 12; y < y0 + h - 30; y += stepY) {
            int row = (y - y0) / stepY;

            int size = 1 + (row / 8);
            if (size > 3) size = 3;

            int xs = 6;
            if (row > 10) xs = 5;
            if (row > 18) xs = 4;

            for (int x = 8; x < w - 8; x += xs) {
                local = local * 1103515245 + 12345;
                int r = (local >>> 16) & 255;

                int br = 40 + (r % 160);
                int alpha = 70 + (r % 120);

                boolean phone = ((r % 25) == 0) && ((tick / 6) % 2 == 0);
                if (cheerFlash && (r % 6 == 0)) phone = true;

                if (phone) {
                    g2.setColor(new Color(255, 255, 255, 230));
                } else {
                    int tint = (r % 3);
                    if (tint == 0) g2.setColor(new Color(br, br, br, alpha));
                    else if (tint == 1) g2.setColor(new Color(br, Math.max(0, br - 10), Math.min(255, br + 20), alpha));
                    else g2.setColor(new Color(Math.min(255, br + 20), br, Math.max(0, br - 10), alpha));
                }

                g2.fillRect(x, y, size, size);
            }
        }

        g2.setColor(new Color(0, 0, 0, 110));
        g2.fillRect(0, y0, w, 12);
        g2.fillRect(0, y0 + h - 30, w, 18);
    }
}
