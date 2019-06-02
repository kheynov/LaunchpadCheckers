import org.rjung.util.launchpad.Color;

class Checker {

    private Color color;
    private int x, y;

    Checker(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public Color getColor() {
        return color;
    }

    public void move(Table.Vector dir) {
        switch (dir.getDirection()) {
            case UP_LEFT:
                this.x -= dir.getLength();
                this.y += dir.getLength();
                break;
            case UP_RIGHT:
                this.x += dir.getLength();
                this.y += dir.getLength();
                break;
            case DOWN_LEFT:
                this.x -= dir.getLength();
                this.y -= dir.getLength();
                break;
            case DOWN_RIGHT:
                this.x += dir.getLength();
                this.y -= dir.getLength();
                break;
        }
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

}
