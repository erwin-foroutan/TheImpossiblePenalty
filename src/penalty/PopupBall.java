package penalty;


public final class PopupBall {
    public final SafeSprite go;
    private int ttl;

    public PopupBall(SafeSprite go, int ttl) {
        this.go = go;
        this.ttl = ttl;
    }


    public boolean tick() {
        ttl--;
        go.pos().y -= 0.6;
        return ttl <= 0;
    }
}
