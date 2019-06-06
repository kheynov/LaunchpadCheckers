class Vector {//класс Vector необходим для расчётов допустимых ходов и всего что связано с перемещением по полю
    private Direction direction;//Направление вектора
    private int length;//Длина вектора
    public enum Direction {//все направления, в который мы можем походить
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
    }
    Vector(Direction direction, int length) {
        this.direction = direction;
        this.length = length;
    }

    int getX(int checkerX) {//находим конечный "x" вектора относительо данного "x"
        switch (direction) {
            case UP_RIGHT:
                checkerX += length;
                break;
            case DOWN_RIGHT:
                checkerX += length;
                break;
            case DOWN_LEFT:
                checkerX -= length;
                break;
            case UP_LEFT:
                checkerX -= length;
                break;
        }
        return checkerX;
    }

    int getY(int checkerY) {//находим конечный "y" вектора относительо данного "y"
        switch (direction) {
            case UP_LEFT:
                checkerY += length;
                break;
            case UP_RIGHT:
                checkerY += length;
                break;
            case DOWN_LEFT:
                checkerY -= length;
                break;
            case DOWN_RIGHT:
                checkerY -= length;
                break;
        }
        return checkerY;
    }

    Direction getDirection() {
        return direction;
    }//возвращаем направление вектора

    int getLength() {
        return length;
    }//возвращаем длину вектора
}
