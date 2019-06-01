import org.rjung.util.launchpad.Launchpad;

import java.util.ArrayList;
import java.util.List;

public class Table {

    private Launchpad launchpad;
    private ArrayList<Checker> checkerList = new ArrayList<>();
    private State state = State.DRAW_CHECKERS;

    public enum State {
        DRAW_CHECKERS,
        DRAW_AVAILABLE_MOVES
    }

    public State getState() {
        System.out.println("table.state = " + state);
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public enum Direction {
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
    }

    class Vector {
        private Direction direction;
        private int length;

        public Vector(Direction direction, int length) {
            this.direction = direction;
            this.length = length;
        }

        public Direction getDirection() {
            return direction;
        }

        public int getLength() {
            return length;
        }
    }

    Table(Launchpad launchpad) {
        this.launchpad = launchpad;
    }

    boolean isMapContains(int x, int y) {
        return x <= 7 && x >= 0 && y >= 0 && y <= 7;
    }

    void add(Checker checker) { checkerList.add(checker); }

    void delete(Checker checker) { checkerList.remove(checker); }

    List<Vector> availableMoves(Checker checker) {
        int x = checker.getX();
        int y = checker.getY();
        ArrayList<Vector> list = new ArrayList<>();;

        if (x != 7) {
            if (y != 7) {
                list.add(new Vector(Direction.DOWN_RIGHT, 1));
            }
            if (y != 0) {
                list.add(new Vector(Direction.UP_RIGHT, 1));
            }
        }
        if (x != 0) {
            if (y != 0) {
                list.add(new Vector(Direction.UP_LEFT, 1));
            }
            if (y != 7) {
                list.add(new Vector(Direction.DOWN_LEFT, 1));
            }
        }

        for (int i = 0; i < list.size(); i++) {
            for (int j = 0; j < checkerList.size(); j++) {
                if (isMapContains(checker.move(list.get(i)).getX(), checker.move(list.get(i)).getY())) {
                    if (checker.move(list.get(i)).getX() == checkerList.get(j).getX()
                            && checker.move(list.get(i)).getY() == checkerList.get(j).getY()) {
                        list.set(i, new Vector(list.get(i).getDirection(), list.get(i).getLength() + 1));
                    }
                }
            }
        }

        return list;
    }

    public ArrayList<Checker> getCheckerList() {
        return checkerList;
    }

}
