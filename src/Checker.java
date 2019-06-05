import org.rjung.util.launchpad.Color;

class Checker {

    private Color color;
    private int x, y;
    boolean isQueen = false;

    Checker(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    Color getColor() {
        return color;
    }

    void setQueen(){
        this.isQueen=true;
    }

    void move(Vector dir) {
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
