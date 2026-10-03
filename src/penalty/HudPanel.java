package penalty;

import name.panitz.game2d.GameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/** HUD rechts (Info + Steuerung). */
public final class HudPanel implements GameObj {
    private final Vertex p;
    private final double w, h;

    private final Difficulty diff;
    private final ShotType shot;
    private final int streak;
    private final Phase phase;
    private final double charge;
    private final double timeLeft;
    private final ShotResult result;
    private final int resultTicks;
    private final boolean perfectLob;

    public HudPanel(double x, double y, double w, double h,
                    Difficulty diff, ShotType shot, int streak, Phase phase,
                    double charge, double timeLeft,
                    ShotResult result, int resultTicks,
                    boolean perfectLob) {
        this.p = new Vertex(x, y);
        this.w = w;
        this.h = h;
        this.diff = diff;
        this.shot = shot;
        this.streak = streak;
        this.phase = phase;
        this.charge = charge;
        this.timeLeft = timeLeft;
        this.result = result;
        this.resultTicks = resultTicks;
        this.perfectLob = perfectLob;
    }

    @Override public Vertex pos() { return p; }
    @Override public Vertex velocity() { return new Vertex(0, 0); }
    @Override public double width() { return w; }
    @Override public double height() { return h; }
    @Override public void move() {}

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        g2.setColor(new Color(248, 248, 248));
        g2.fillRect((int) p.x, (int) p.y, (int) w, (int) h);
        g2.setColor(new Color(35, 35, 35));
        g2.drawRect((int) p.x, (int) p.y, (int) w - 1, (int) h - 1);

        int x = (int) p.x + 16;
        int y = (int) p.y + 28;

        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("Helvetica", Font.BOLD, 16));
        g2.drawString("The Impossible Penalty", x, y);

        y += 18;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.setColor(new Color(70, 70, 70));
        g2.drawString("Erziele 5 Tore in Folge um zu Gewinnen", x, y);

        y += 16;
        g2.setColor(new Color(220, 220, 220));
        g2.drawLine(x, y, x + (int) w - 32, y);

        y += 22;
        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("Helvetica", Font.BOLD, 13));
        g2.drawString("Status", x, y);

        y += 18;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.drawString("Schwierigkeit:", x, y);
        g2.setFont(new Font("Helvetica", Font.BOLD, 12));
        g2.drawString(diff.label, x + 80, y);

        y += 16;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.drawString("Schuss:", x, y);
        g2.setFont(new Font("Helvetica", Font.BOLD, 12));
        g2.drawString(shot.label, x + 80, y);

        y += 16;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.drawString("Serie:", x, y);
        g2.setFont(new Font("Helvetica", Font.BOLD, 12));
        g2.drawString(streak + " / 5", x + 80, y);

        y += 16;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.drawString("Zeit:", x, y);
        g2.setFont(new Font("Helvetica", Font.BOLD, 12));
        g2.drawString(String.format("%.1fs", Math.max(0.0, timeLeft)), x + 80, y);

        y += 16;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.drawString("Phase:", x, y);
        g2.setFont(new Font("Helvetica", Font.BOLD, 12));
        g2.drawString(phaseLabel(phase), x + 80, y);

        y += 24;
        if (result != ShotResult.NONE && resultTicks > 0) {
            g2.setFont(new Font("Helvetica", Font.BOLD, 14));
            g2.setColor(new Color(10, 10, 10));
            g2.drawString(result.label, x, y);

            if (shot == ShotType.LOB && perfectLob && result == ShotResult.GOAL) {
                y += 16;
                g2.setFont(new Font("Helvetica", Font.BOLD, 12));
                g2.drawString("Perfekter Lupfer!", x, y);
            }
        }

        y += 12;
        g2.setColor(new Color(220, 220, 220));
        g2.drawLine(x, y, x + (int) w - 32, y);

        y += 22;
        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("Helvetica", Font.BOLD, 13));
        g2.drawString("Steuerung", x, y);

        y += 18;
        g2.setFont(new Font("Helvetica", Font.PLAIN, 12));
        g2.setColor(new Color(60, 60, 60));
        g2.drawString("Zielen: Maus / LINKS RECHTS", x, y);
        y += 16;
        g2.drawString("Aufladen/Schuss: LEERTASTE", x, y);
        y += 16;
        g2.drawString("1 Normal   2 Drall L", x, y);
        y += 16;
        g2.drawString("3 Drall R  4 Lupfer", x, y);
        y += 16;
        g2.drawString("N Normal   I Impossible", x, y);
        y += 16;
        g2.drawString("R Reset", x, y);

        y += 22;
        g2.setColor(new Color(20, 20, 20));
        g2.setFont(new Font("Helvetica", Font.BOLD, 13));
        g2.drawString("Power", x, y);

        y += 14;
        int barW = (int) w - 32;
        int barH = 12;
        int bx = (int) p.x + 16;
        int by = y;

        g2.setColor(new Color(220, 220, 220));
        g2.fillRect(bx, by, barW, barH);
        g2.setColor(new Color(40, 40, 40));
        g2.drawRect(bx, by, barW, barH);

        // green window for lob "time finish"
        if (shot == ShotType.LOB) {
            double half = (diff == Difficulty.NORMAL) ? GameConfig.LOB_GREEN_HALF_NORMAL : GameConfig.LOB_GREEN_HALF_IMPOSSIBLE;
            double lo = GameConfig.clamp01(GameConfig.LOB_FINISH_CENTER - half);
            double hi = GameConfig.clamp01(GameConfig.LOB_FINISH_CENTER + half);

            int gx = bx + (int) Math.round(lo * barW);
            int gw = (int) Math.round((hi - lo) * barW);

            g2.setColor(new Color(40, 170, 60, 140));
            g2.fillRect(gx, by, gw, barH);
        }

        int fill = (int) Math.round(GameConfig.clamp01(charge) * barW);
        g2.setColor(new Color(70, 70, 70));
        g2.fillRect(bx, by, fill, barH);
    }

    private static String phaseLabel(Phase phase) {
        return switch (phase) {
            case AIMING -> "Aim";
            case CHARGING -> "Charging";
            case BALL_IN_FLIGHT -> "Shot";
            case AFTER_SHOT -> "Reset";
            case WON -> "Won";
        };
    }
}
