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

    public Checker move(Table.Vector dir) {
        Checker check = new Checker(this.x, this.y, this.color);;

        switch (dir.getDirection()) {
            case UP_LEFT:
                check.x -= dir.getLength();
                check.y -= dir.getLength();
                break;
            case UP_RIGHT:
                check.x += dir.getLength();
                check.y -= dir.getLength();
                break;
            case DOWN_LEFT:
                check.x -= dir.getLength();
                check.y += dir.getLength();
                break;
            case DOWN_RIGHT:
                check.x += dir.getLength();
                check.y += dir.getLength();
                break;
        }

        return check;
    }

    int getX() {
        return x;
    }

    int getY() {
        return y;
    }

}
