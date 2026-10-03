package penalty;

import name.panitz.game2d.AbstractGameObj;
import name.panitz.game2d.Vertex;

import java.awt.*;

/**
 * Torwart  fuer IMPOSSIBLE):
 * - lernt von letzten Schuessen with neuere Schuesse zaehlen mehr
 * - sagt Kurven voraus direction + power
 * - staerkere Tendenz toward player habits
 * - Taeuscher: geht erst zur Gewohnheit then wechselt zum echten Ziel target
 * - schnellere Reaktion + schnellerer Hechtsprung bei hartem Schuss
 * - folgt dem Spieler bzw. wo er hinzielt
 *
 * Hinweis zur Entstehung:
 * - Die Grundidee ("recency"-Gewichtung + Fake-Step/Mindgame) habe ich mir mit
 *   Hilfe von YouTube-Tutorials als Inspiration geholt.
 * - Die konkrete Umsetzung, Werte (z.B. noise, alpha, fakeProb) und das Debugging
 *   habe ich dann selbst gemacht und im Spiel getestet.
 */
public final class Keeper extends AbstractGameObj {
    private final Image image;

    private double patrolDir = 1.0;

    private boolean diving = false;
    private int reactionDelayTicks = 0;

    // Ziel-Verfolgung (IMPOSSIBLE Modus)
    private double readAimX = GameConfig.FIELD_W / 2.0;
    private double lastAimX = GameConfig.FIELD_W / 2.0;

    // Wohin der Keeper springt
    private double diveTargetX = GameConfig.FIELD_W / 2.0;

    // Mindgame-Variablen (IMPOSSIBLE Modus)
    private double plannedTargetX = GameConfig.FIELD_W / 2.0;
    private boolean willFake = false;
    private int fakeSwitchTick = 0;
    private int diveTicks = 0;

    // Gedaechtnis fuer letzte Schuesse (IMPOSSIBLE)
    private static final int MEM_N = 12;
    private static final int RECENT_K = 4;
    private final int[] mem = new int[MEM_N]; // -1=links, 0=mitte, +1=rechts
    private int memIdx = 0;

    public Keeper(Vertex p, Vertex v, double w, double h, String fileName) {
        super(p, v, w, h);
        this.image = GameConfig.loadImageOrNull(Keeper.class, fileName);
    }

    @Override
    public void paintTo(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (image != null) {
            g2.drawImage(image, (int) pos().x, (int) pos().y, null);
            return;
        }

        // Ersatz-Torwart falls Bild nicht laedt
        int x = (int) pos().x;
        int y = (int) pos().y;
        int ww = (int) width();
        int hh = (int) height();

        g2.setColor(new Color(250, 210, 40));
        g2.fillRoundRect(x, y, ww, hh, 18, 18);
        g2.setColor(new Color(0, 0, 0, 120));
        g2.drawRoundRect(x, y, ww, hh, 18, 18);

        g2.setColor(new Color(0, 0, 0, 160));
        g2.fillOval(x + 28, y + 10, 24, 24);
        g2.fillRoundRect(x + 22, y + 32, 36, 38, 16, 16);
    }

    public void reset() {
        pos().x = GameConfig.FIELD_W / 2.0 - width() / 2.0;
        pos().y = GameConfig.GOAL_LINE_Y + 20;
        velocity().moveTo(new Vertex(0, 0));

        patrolDir = 1.0;
        diving = false;
        reactionDelayTicks = 0;

        readAimX = GameConfig.FIELD_W / 2.0;
        lastAimX = GameConfig.FIELD_W / 2.0;

        diveTargetX = GameConfig.FIELD_W / 2.0;
        plannedTargetX = GameConfig.FIELD_W / 2.0;
        willFake = false;
        fakeSwitchTick = 0;
        diveTicks = 0;
    }

    private int bucket(double x) {
        double c = (GameConfig.POST_LEFT + GameConfig.POST_RIGHT) / 2.0;
        if (x < c - 55) return -1;
        if (x > c + 55) return 1;
        return 0;
    }

    // Gewichtete Tendenz (neuere Schuesse zaehlen mehr)
    private int weightedBucket() {
        double left = 0, center = 0, right = 0;

        for (int v : mem) {
            if (v == -1) left += 1.0;
            else if (v == 1) right += 1.0;
            else center += 1.0;
        }

        for (int i = 0; i < RECENT_K; i++) {
            int idx = (memIdx - 1 - i + MEM_N) % MEM_N;
            int v = mem[idx];
            double w = 2.5 - 0.4 * i; // 2.5, 2.1, 1.7, 1.3
            if (v == -1) left += w;
            else if (v == 1) right += w;
            else center += w;
        }

        if (left >= center && left >= right) return -1;
        if (right >= center && right >= left) return 1;
        return 0;
    }

    private double clampToGoal(double x) {
        return Math.max(GameConfig.POST_LEFT + 10, Math.min(GameConfig.POST_RIGHT - 10, x));
    }

    private double bucketToX(int b) {
        double c = (GameConfig.POST_LEFT + GameConfig.POST_RIGHT) / 2.0;
        if (b == -1) return c - 95;
        if (b == 1) return c + 95;
        return c;
    }

    public void onShotFired(Difficulty diff, double intendedTargetX, ShotType type, double power) {
        diving = true;
        diveTicks = 0;

        if (diff == Difficulty.NORMAL) {
            reactionDelayTicks = 12;
            double noise = 95.0;

            if (type == ShotType.CURVE_LEFT || type == ShotType.CURVE_RIGHT) noise *= 1.15;
            if (type == ShotType.LOB) noise *= 1.25;

            diveTargetX = clampToGoal(intendedTargetX + (Math.random() - 0.5) * noise);
            plannedTargetX = diveTargetX;
            willFake = false;
            fakeSwitchTick = 0;
            return;
        }

        // IMPOSSIBLE
        reactionDelayTicks = (power > 0.75) ? 3 : 5;

        // Predict curve drift
        double curvePredict = 0.0;
        if (type == ShotType.CURVE_LEFT)  curvePredict = -GameConfig.CURVE_PREDICT_MAX * (0.6 + 0.4 * power);
        if (type == ShotType.CURVE_RIGHT) curvePredict =  GameConfig.CURVE_PREDICT_MAX * (0.6 + 0.4 * power);

        double predictedX = clampToGoal(intendedTargetX + curvePredict);

        // remember predicted bucket
        mem[memIdx] = bucket(predictedX);
        memIdx = (memIdx + 1) % MEM_N;

        double noise = 36.0;
        if (type == ShotType.CURVE_LEFT || type == ShotType.CURVE_RIGHT) noise *= 1.15;
        if (type == ShotType.LOB) noise *= 1.05;

        int pref = weightedBucket();
        double mindBias = 0.0;
        if (pref != 0) {
            mindBias = (bucketToX(pref) - predictedX) * 0.45;
        }

        plannedTargetX = clampToGoal(predictedX + mindBias + (Math.random() - 0.5) * noise);

        willFake = false;
        fakeSwitchTick = 0;

        double fakeProb = 0.18;
        if (pref != 0) fakeProb += 0.10;
        if (type == ShotType.CURVE_LEFT || type == ShotType.CURVE_RIGHT) fakeProb -= 0.04;

        if (Math.random() < fakeProb) {
            willFake = true;
            fakeSwitchTick = 10 + (int) (Math.random() * 8);
        }

        if (willFake) {
            if (pref != 0) {
                diveTargetX = clampToGoal(bucketToX(pref));
            } else {
                double c = (GameConfig.POST_LEFT + GameConfig.POST_RIGHT) / 2.0;
                diveTargetX = clampToGoal(c - (plannedTargetX - c));
            }
        } else {
            diveTargetX = plannedTargetX;
        }
    }

    public void tick(Difficulty diff, Phase phase, double aimX, ShotType type, double currentCharge) {
        if (phase == Phase.WON) return;

        // track aim on IMPOSSIBLE during aiming/charging
        if (diff == Difficulty.IMPOSSIBLE && (phase == Phase.AIMING || phase == Phase.CHARGING)) {
            double alpha = 0.16;

            double aimDelta = aimX - lastAimX;
            lastAimX = aimX;
            if (Math.abs(aimDelta) > 12) alpha *= 0.75;

            readAimX = readAimX + (aimX - readAimX) * alpha;
            readAimX = clampToGoal(readAimX);
        }

        // patrol in idle states
        if (phase == Phase.AIMING || phase == Phase.CHARGING || phase == Phase.AFTER_SHOT) {
            diving = false;

            double speed = (diff == Difficulty.IMPOSSIBLE) ? 1.65 : 1.2;

            double bias = 0.0;
            if (diff == Difficulty.IMPOSSIBLE) {
                double centerX = pos().x + width() / 2.0;
                bias = (readAimX - centerX) * 0.06;
            }

            pos().x += speed * patrolDir + bias;

            double min = GameConfig.POST_LEFT + 20;
            double max = GameConfig.POST_RIGHT - width() - 20;
            if (pos().x < min) { pos().x = min; patrolDir = 1.0; }
            if (pos().x > max) { pos().x = max; patrolDir = -1.0; }

            velocity().moveTo(new Vertex(0, 0));
            return;
        }

        // dive during flight
        if (phase == Phase.BALL_IN_FLIGHT && diving) {
            if (reactionDelayTicks > 0) {
                reactionDelayTicks--;
                return;
            }

            diveTicks++;

            double speed = (diff == Difficulty.IMPOSSIBLE) ? 5.3 : 4.1;
            if (diff == Difficulty.IMPOSSIBLE) {
                speed += 0.35 * currentCharge;
            }

            if (diff == Difficulty.IMPOSSIBLE && willFake && diveTicks == fakeSwitchTick) {
                diveTargetX = plannedTargetX;
            }

            double centerX = pos().x + width() / 2.0;
            double dx = diveTargetX - centerX;

            if (Math.abs(dx) < speed) pos().x += dx;
            else pos().x += Math.signum(dx) * speed;

            pos().x = Math.max(GameConfig.POST_LEFT, Math.min(GameConfig.POST_RIGHT - width(), pos().x));
        }
    }
}
