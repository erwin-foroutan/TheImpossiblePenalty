package penalty;

import name.panitz.game2d.*;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import static java.awt.event.KeyEvent.*;

/**
 * The Impossible Penalty - Elfmeterschiessen Spiel
 *
 * Das ist mein Programmierpraktikum-Projekt.
 * Man schiesst Elfmeter gegen einen Torwart und muss 5 Tore hintereinander
 * machen um zu gewinnen.
 *
 * WIE ES FUNKTIONIERT:
 * - Mit Maus oder Pfeiltasten zielen
 * - LEERTASTE zum Laden und Schiessen
 * - Verschiedene Schussarten (Normal, Kurve, Lupfer)
 * - Torwart lernt von deinen Schuessen (vor allem im Unmoeglich-Modus)
 *

 *
 * WO ICH HILFE GEBRAUCHT HABE:
 * - Basis Game-Loop aus den Vorlesungsbeispielen
 * - Torwart-KI: Hab mir auf YouTube Tutorials ueber Game AI angeschaut,
 *   dann selber implementiert und viel getestet bis es gut war
 * - Ball-Kurven-Physik: Durch viel rumprobieren bis es realistisch aussah
 * - Timing-Fenster beim Lupfer: YouTube Tutorial ueber "Game Feel",
 *   dann selber angepasst
 */
public class TheImpossiblePenalty implements Game {

    // === EBENEN ZUM ZEICHNEN ===
    // Das Game2D-Framework braucht Listen von Listen fuer verschiedene Ebenen
    private final List<List<? extends GameObj>> layers = new ArrayList<>();
    private final List<GameObj> background = new ArrayList<>();
    private final List<GameObj> actors = new ArrayList<>();
    private final List<GameObj> hud = new ArrayList<>();

    // === SPIELOBJEKTE ===
    private SafeSprite shooter;    // der Spieler (Schuetze)
    private SafeSprite ball;       // der Fussball
    private Keeper keeper;         // der Torwart
    private StadiumCrowd crowd;    // die Zuschauer

    // Popup-Baelle die nach Toren rumfliegen (nur Deko)
    private final List<PopupBall> popups = new ArrayList<>();

    // === SPIEL-STATUS ===
    private double aimX = GameConfig.FIELD_W / 2.0;  // wo der Spieler hinzielt
    private ShotType shotType = ShotType.NORMAL;     // welche Schussart
    private Difficulty difficulty = Difficulty.NORMAL;  // Schwierigkeitsgrad
    private Phase phase = Phase.AIMING;              // aktuelle Phase

    private double charge = 0.0;        // Wie voll ist die Ladeleiste (0-1)
    private double chargeDir = 1.0;     // Lade-Richtung (hoch/runter)

    private boolean ballActive = false;      // Fliegt der Ball gerade?
    private boolean savedThisShot = false;   // Hat Keeper gehalten?
    private boolean missedThisShot = false;  // Daneben geschossen?
    private int postShotTicks = 0;

    private int streak = 0;  // Tore hintereinander (5 = Sieg!)

    // Resultat-Anzeige (GOAL!, SAVED!, MISS!)
    private ShotResult lastResult = ShotResult.NONE;
    private int resultTicks = 0;

    // Countdown Timer
    private double timeLeftSec = GameConfig.TIME_LIMIT_NORMAL;

    // War der Lupfer perfekt getimed? (dann unaufhaltbar)
    private boolean perfectLob = false;

    // Schusskraft (bleibt waehrend Flug konstant)
    private double currentShotPower = 0.65;

    public TheImpossiblePenalty() {
        shooter = SafeSprite.shooter(new Vertex(GameConfig.FIELD_W / 2.0 - 22, GameConfig.SHOOTER_Y));
        ball = SafeSprite.ball(new Vertex(GameConfig.FIELD_W / 2.0 - 12, GameConfig.BALL_START_Y));
    }

    @Override public int width() { return GameConfig.W; }
    @Override public int height() { return GameConfig.H; }
    @Override public GameObj player() { return shooter; }
    @Override public List<List<? extends GameObj>> goss() { return layers; }

    @Override
    public void init() {
        // Alles zuruecksetzen
        layers.clear();
        background.clear();
        actors.clear();
        hud.clear();
        popups.clear();

        // Ebenen hinzufuegen
        layers.add(background);
        layers.add(actors);
        layers.add(hud);

        // Zuschauer erstellen
        crowd = new StadiumCrowd();
        background.add(crowd);

        // Spielfeld
        background.add(new SafePitch(GameConfig.RES_PITCH));

        // Spieler und Ball erstellen
        shooter = SafeSprite.shooter(new Vertex(GameConfig.FIELD_W / 2.0 - 22, GameConfig.SHOOTER_Y));
        ball = SafeSprite.ball(new Vertex(GameConfig.FIELD_W / 2.0 - 12, GameConfig.BALL_START_Y));

        // Torwart erstellen
        keeper = new Keeper(
                new Vertex(GameConfig.FIELD_W / 2.0 - 40, GameConfig.GOAL_LINE_Y + 20),
                new Vertex(0, 0),
                80, 80,
                GameConfig.RES_KEEPER
        );
        actors.add(keeper);

        // Status zuruecksetzen
        shotType = ShotType.NORMAL;
        difficulty = Difficulty.NORMAL;
        phase = Phase.AIMING;

        charge = 0.0;
        chargeDir = 1.0;

        ballActive = false;
        savedThisShot = false;
        missedThisShot = false;
        postShotTicks = 0;

        streak = 0;
        aimX = GameConfig.FIELD_W / 2.0;

        lastResult = ShotResult.NONE;
        resultTicks = 0;

        perfectLob = false;
        currentShotPower = 0.65;

        resetShotTimer();
        rebuildHud();
    }

    // Maus-Steuerung
    @Override
    public void mouseMovedReaction(MouseEvent e) {
        if (phase == Phase.WON) return;
        if (phase == Phase.BALL_IN_FLIGHT || phase == Phase.AFTER_SHOT) return;

        int mx = e.getX();
        mx = (int) GameConfig.clamp(mx, 0, GameConfig.FIELD_W);

        double minAim = GameConfig.POST_LEFT - 70;
        double maxAim = GameConfig.POST_RIGHT + 70;

        aimX = GameConfig.clamp(mx, minAim, maxAim);
    }

    @Override
    public void doChecks() {
        // Zuschauer updaten
        if (crowd != null) crowd.tick();

        // Resultat-Timer
        if (resultTicks > 0) resultTicks--;
        else lastResult = ShotResult.NONE;

        clampAndApplyAim();

        // Torwart updaten
        keeper.tick(difficulty, phase, aimX, shotType, charge);

        // Countdown (nur beim Zielen/Laden)
        if (phase == Phase.AIMING || phase == Phase.CHARGING) {
            timeLeftSec -= GameConfig.DT;
            if (timeLeftSec <= 0.0) {
                timeLeftSec = 0.0;
                if (phase == Phase.AIMING) {
                    charge = 0.65;  // Default-Ladung
                }
                shoot();
            }
        }

        // Lade-Mechanik (Balken geht hoch und runter)
        if (phase == Phase.CHARGING) {
            charge += 0.015 * chargeDir;
            if (charge >= 1.0) { charge = 1.0; chargeDir = -1.0; }
            if (charge <= 0.0) { charge = 0.0; chargeDir = 1.0; }
        }

        // Ball-Physik
        if (phase == Phase.BALL_IN_FLIGHT && ballActive) {
            applyShotExtras();

            // Ball raus?
            if (ball.pos().x < -100 || ball.pos().x > GameConfig.FIELD_W + 60 || ball.pos().y > GameConfig.H + 100) {
                missedThisShot = true;
                setResult(ShotResult.MISS);
                finishShot();
            }

            // Keeper-Kollision
            // WICHTIG: Perfekter Lupfer kann NICHT gehalten werden!
            if (!savedThisShot && !missedThisShot && !perfectLob) {
                boolean lobProtected = shotType == ShotType.LOB && ball.pos().y > GameConfig.GOAL_LINE_Y + 15;
                if (!lobProtected && keeper.touches(ball)) {
                    savedThisShot = true;
                    setResult(ShotResult.SAVED);
                    finishShot();
                }
            }

            // Tor-Check
            if (!savedThisShot && !missedThisShot && ball.pos().y <= GameConfig.GOAL_LINE_Y) {
                boolean betweenPosts =
                        ball.pos().x + ball.width() / 2.0 >= GameConfig.POST_LEFT &&
                        ball.pos().x + ball.width() / 2.0 <= GameConfig.POST_RIGHT;

                if (betweenPosts) {
                    onGoal();  // TOOOOR!
                } else {
                    missedThisShot = true;
                    setResult(ShotResult.MISS);
                    finishShot();
                }
            }
        }

        // Popup-Baelle
        popups.removeIf(PopupBall::tick);

        // Nach Schuss warten
        if (phase == Phase.AFTER_SHOT) {
            postShotTicks++;
            if (postShotTicks > 60) resetForNextShot();
        }

        // Gewinn-Check
        if (phase != Phase.WON && streak >= 5) {
            phase = Phase.WON;
            showWinnerScreen();
        }

        rebuildHud();
    }

    @Override
    public void keyPressedReaction(KeyEvent e) {
        // R = Reset
        if (e.getKeyCode() == VK_R) {
            init();
            return;
        }
        if (phase == Phase.WON) return;

        switch (e.getKeyCode()) {
            // Pfeiltasten zum Zielen
            case VK_LEFT -> aimX -= 14;
            case VK_RIGHT -> aimX += 14;

            // Schussarten
            case VK_1 -> shotType = ShotType.NORMAL;
            case VK_2 -> shotType = ShotType.CURVE_LEFT;
            case VK_3 -> shotType = ShotType.CURVE_RIGHT;
            case VK_4 -> shotType = ShotType.LOB;

            // Schwierigkeit
            case VK_N -> {
                difficulty = Difficulty.NORMAL;
                if (phase == Phase.AIMING || phase == Phase.CHARGING) resetShotTimer();
            }
            case VK_I -> {
                difficulty = Difficulty.IMPOSSIBLE;
                if (phase == Phase.AIMING || phase == Phase.CHARGING) resetShotTimer();
            }

            // Leertaste
            case VK_SPACE -> onSpace();
        }
    }

    private void onSpace() {
        if (phase == Phase.AIMING) {
            phase = Phase.CHARGING;
            chargeDir = 1.0;
            return;
        }
        if (phase == Phase.CHARGING) shoot();
    }

    private void shoot() {
        // Ball zu Actors hinzufuegen
        if (!actors.contains(ball)) actors.add(ball);

        // Ball-Position setzen
        ball.pos().moveTo(new Vertex(aimX - ball.width() / 2.0, GameConfig.BALL_START_Y));

        // Schusskraft berechnen
        currentShotPower = 0.25 + 0.75 * charge;

        // Perfekter Lupfer Check
        // (Timing-Fenster - aus YouTube Tutorial gelernt, dann angepasst)
        perfectLob = (shotType == ShotType.LOB) && isChargeInGreenWindow(charge, difficulty);

        // Ziel berechnen (mit Kurven-Offset)
        double baseTargetX = aimX;
        if (shotType == ShotType.CURVE_LEFT) baseTargetX -= 50;
        if (shotType == ShotType.CURVE_RIGHT) baseTargetX += 50;

        // Flugzeit berechnen
        double time = (shotType == ShotType.LOB) ? (75 - 25 * currentShotPower) : (55 - 25 * currentShotPower);
        if (time < 18) time = 18;

        double targetY = GameConfig.GOAL_LINE_Y - 8;

        // Geschwindigkeit berechnen
        double vx = (baseTargetX - (ball.pos().x + ball.width() / 2.0)) / time;
        double vy = (targetY - ball.pos().y) / time;

        // Ungenauigkeit (ausser bei perfektem Lupfer)
        if (!perfectLob) {
            double random = (difficulty == Difficulty.NORMAL) ? 0.25 : 0.12;
            vx += (Math.random() - 0.5) * random;
        }

        ball.velocity().moveTo(new Vertex(vx, vy));

        // Torwart informieren
        keeper.onShotFired(difficulty, baseTargetX, shotType, currentShotPower);

        ballActive = true;
        savedThisShot = false;
        missedThisShot = false;
        postShotTicks = 0;

        phase = Phase.BALL_IN_FLIGHT;
    }

    // Kurven und Lupfer-Effekte
    // (Ansatz aus YouTube Tutorial, Werte durch Trial&Error)
    private void applyShotExtras() {
        double curveStrength = GameConfig.curvePerTick(currentShotPower);

        if (shotType == ShotType.CURVE_LEFT) {
            ball.velocity().x += -curveStrength;
        } else if (shotType == ShotType.CURVE_RIGHT) {
            ball.velocity().x += curveStrength;
        }

        if (shotType == ShotType.LOB) {
            if (ball.pos().y > 240) ball.velocity().y *= 0.985;
        }
    }

    private void onGoal() {
        streak += 1;
        setResult(ShotResult.GOAL);

        if (crowd != null) crowd.cheer();

        // Popup-Ball spawnen
        popups.add(new PopupBall(
                SafeSprite.ball(new Vertex(20 + Math.random() * (GameConfig.FIELD_W - 60), 140 + Math.random() * 120)),
                35
        ));

        finishShot();
    }

    private void setResult(ShotResult r) {
        lastResult = r;
        resultTicks = 60;
    }

    private void finishShot() {
        ballActive = false;
        phase = Phase.AFTER_SHOT;

        if (savedThisShot || missedThisShot) streak = 0;

        ball.velocity().moveTo(new Vertex(0, 0));
    }

    private void resetForNextShot() {
        actors.remove(ball);

        ball.pos().moveTo(new Vertex(GameConfig.FIELD_W / 2.0 - 12, GameConfig.BALL_START_Y));
        ball.velocity().moveTo(new Vertex(0, 0));

        keeper.reset();

        charge = 0.0;
        chargeDir = 1.0;

        aimX = GameConfig.FIELD_W / 2.0;

        phase = Phase.AIMING;
        postShotTicks = 0;
        savedThisShot = false;
        missedThisShot = false;

        perfectLob = false;
        currentShotPower = 0.65;

        resetShotTimer();
    }

    private void showWinnerScreen() {
        shooter.velocity().moveTo(new Vertex(0, 0));
        keeper.velocity().moveTo(new Vertex(0, 0));
        if (actors.contains(ball)) ball.velocity().moveTo(new Vertex(0, 0));

        hud.clear();

        hud.add(new SafeHudImage(
                new Vertex(GameConfig.FIELD_W / 2.0 - 300, GameConfig.H / 2.0 - 120),
                600, 240,
                GameConfig.RES_WINNER
        ));

        hud.add(new TextObject(new Vertex(GameConfig.FIELD_W / 2.0 - 260, GameConfig.H / 2.0 + 110),
                "Winner! Press R to play again"));
    }

    private void rebuildHud() {
        if (phase == Phase.WON) return;

        hud.clear();

        for (PopupBall p : popups) hud.add(p.go);

        if (phase == Phase.AIMING || phase == Phase.CHARGING) {
            hud.add(new AimTrajectory());
        }

        hud.add(new AimMarker(effectiveAimX(), GameConfig.GOAL_LINE_Y + 12, shotType));

        hud.add(new CenterBanner(GameConfig.FIELD_W / 2.0, GameConfig.FIELD_H / 2.0 - 40, lastResult, resultTicks, perfectLob, shotType));

        hud.add(new HudPanel(GameConfig.FIELD_W, 0, GameConfig.HUD_W, GameConfig.FIELD_H,
                difficulty, shotType, streak, phase, charge, timeLeftSec, lastResult, resultTicks, perfectLob));
    }

    private void clampAndApplyAim() {
        shooter.velocity().moveTo(new Vertex(0, 0));
        shooter.pos().y = GameConfig.SHOOTER_Y;

        double minAim = GameConfig.POST_LEFT - 70;
        double maxAim = GameConfig.POST_RIGHT + 70;
        aimX = GameConfig.clamp(aimX, minAim, maxAim);

        shooter.pos().x = aimX - shooter.width() / 2.0;
    }

    private double shotTimeLimitSeconds() {
        return (difficulty == Difficulty.IMPOSSIBLE) ? GameConfig.TIME_LIMIT_IMPOSSIBLE : GameConfig.TIME_LIMIT_NORMAL;
    }

    private void resetShotTimer() {
        timeLeftSec = shotTimeLimitSeconds();
    }

    private double effectiveAimX() {
        double x = aimX;
        if (shotType == ShotType.CURVE_LEFT) x -= 50;
        if (shotType == ShotType.CURVE_RIGHT) x += 50;
        return x;
    }

    private static boolean isChargeInGreenWindow(double charge, Difficulty diff) {
        double half = (diff == Difficulty.NORMAL) ? GameConfig.LOB_GREEN_HALF_NORMAL : GameConfig.LOB_GREEN_HALF_IMPOSSIBLE;
        return charge >= (GameConfig.LOB_FINISH_CENTER - half) && charge <= (GameConfig.LOB_FINISH_CENTER + half);
    }

    /**
     * Flugbahn-Vorschau (zeigt wo der Ball hin fliegt)
     * 
     * Idee aus YouTube Tutorial, dann selber angepasst bis es passte
     */
    private final class AimTrajectory implements GameObj {
        @Override public Vertex pos() { return new Vertex(0, 0); }
        @Override public Vertex velocity() { return new Vertex(0, 0); }
        @Override public double width() { return 0; }
        @Override public double height() { return 0; }
        @Override public void move() {}

        @Override
        public void paintTo(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            double previewCharge = (phase == Phase.CHARGING) ? charge : 0.65;
            double power = 0.25 + 0.75 * previewCharge;

            double baseTargetX = aimX;
            if (shotType == ShotType.CURVE_LEFT) baseTargetX -= 50;
            if (shotType == ShotType.CURVE_RIGHT) baseTargetX += 50;

            double time = (shotType == ShotType.LOB) ? (75 - 25 * power) : (55 - 25 * power);
            if (time < 18) time = 18;

            double x = aimX;
            double y = GameConfig.BALL_START_Y;

            double targetY = GameConfig.GOAL_LINE_Y - 8;

            double vx = (baseTargetX - x) / time;
            double vy = (targetY - y) / time;

            double svx = vx;
            double svy = vy;

            double curveStrength = GameConfig.curvePerTick(power);

            g2.setColor(new Color(0, 0, 0, 120));

            int prevX = (int) Math.round(x);
            int prevY = (int) Math.round(y);

            for (int i = 0; i < 28; i++) {
                if (shotType == ShotType.CURVE_LEFT) {
                    svx += -curveStrength * 0.75;
                } else if (shotType == ShotType.CURVE_RIGHT) {
                    svx += curveStrength * 0.75;
                }

                if (shotType == ShotType.LOB) {
                    if (y > 240) svy *= 0.985;
                }

                x += svx * 3.0;
                y += svy * 3.0;

                int cx = (int) Math.round(x);
                int cy = (int) Math.round(y);

                g2.drawLine(prevX, prevY, cx, cy);

                prevX = cx;
                prevY = cy;

                if (y <= GameConfig.GOAL_LINE_Y - 10) break;
            }

            g2.fillOval(prevX - 3, prevY - 3, 6, 6);
        }
    }

    /**
     * Startet das Spiel mit eigenem Icon
     */
    public void play() {
        init();
        
        // Fenster erstellen
        javax.swing.JFrame f = new javax.swing.JFrame("The Impossible Penalty");
        f.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        
        // Dein Logo als Icon setzen
        try {
            java.awt.Image icon = GameConfig.loadImageOrNull(TheImpossiblePenalty.class, GameConfig.RES_ICON);
            if (icon != null) {
                f.setIconImage(icon);
            }
        } catch (Exception e) {
            // Falls Icon nicht lädt, Spiel läuft trotzdem
        }
        
        f.add(new SwingScreen(this));
        f.pack();
        f.setVisible(true);
    }

    public static void main(String[] args) {
        new TheImpossiblePenalty().play();
    }
}
