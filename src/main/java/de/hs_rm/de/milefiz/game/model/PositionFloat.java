package de.hs_rm.de.milefiz.game.model;

public class PositionFloat {
    private float x;
    private float y;

    public PositionFloat() {
    }

    public PositionFloat(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    @Override
    public String toString() {
        return "PositionFloat [x=" + x + ", y=" + y + "]";
    }

}
