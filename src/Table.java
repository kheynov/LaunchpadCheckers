import org.rjung.util.launchpad.Color;
import org.rjung.util.launchpad.Launchpad;
import org.rjung.util.launchpad.LaunchpadReceiver;
import org.rjung.util.launchpad.Pad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Table implements LaunchpadReceiver {

    private Launchpad launchpad;
    private ArrayList<Checker> checkerList = new ArrayList<>();
    private ArrayList<Vector> availableMoves = new ArrayList<>();
    private Checker lastClickedChecker;
    private boolean isCheckerWasClicked = false;

    @Override
    public void receive(Pad pad) {
        redraw();
        if (isCheckerWasClicked) {
            clearAvailableMoves();
            redraw();
            isCheckerWasClicked = false;
            //обработка хода
        } else {
            isCheckerWasClicked = true;
            for (Checker checker : checkerList) {
                if (checker.getX() == pad.getX() && checker.getY() == pad.getY()) {
                    redraw();
                    lastClickedChecker = checker;
                    showAvailableMoves(checker);
                    System.out.println("Redrawing");
                }
            }
        }
    }

    private void clearDisplay() {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                try {
                    launchpad.set(Pad.find(i, j), Color.BLANK);
                } catch (InvalidMidiDataException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void clearAvailableMoves() {
        for (Vector vector : availableMoves) {
            availableMoves.remove(vector);
        }
    }

    private void redraw() {
        clearDisplay();
        for (Checker checker : checkerList) {
            try {
                launchpad.set(Pad.find(checker.getX(), checker.getY()), checker.getColor());
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    Table() throws MidiUnavailableException {
        this.launchpad = new Launchpad(this);
        checkerList.add(new Checker(1, 1, Color.RED));
        checkerList.add(new Checker(2, 2, Color.GREEN));

        checkerList.add(new Checker(5, 4, Color.GREEN));
        /*checkerList.add(new Checker(4, 3, Color.GREEN));
        checkerList.add(new Checker(6, 3, Color.GREEN));
        checkerList.add(new Checker(6, 5, Color.GREEN));
        checkerList.add(new Checker(4, 5, Color.GREEN));*/
        clearDisplay();
        redraw();
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

        Vector(Direction direction, int length) {
            this.direction = direction;
            this.length = length;
        }

        int getX(int checkerX) {
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

        int getY(int checkerY) {
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
        }

        int getLength() {
            return length;
        }
    }


    private boolean isLastChecker(int x, int y) {
        if (x == 0 || x == 7 || y == 0 || y == 7) {
            return true;
        } else {
            return false;
        }
    }

    private void showAvailableMoves(Checker checker) {
        System.out.println(checkerList);
        List<Vector> availableMoves = availableMoves(checker);
        for (int i = 0; i < availableMoves.size(); i++) {
            int x = availableMoves.get(i).getX(checker.getX());
            int y = availableMoves.get(i).getY(checker.getY());
            try {
                launchpad.set(Pad.find(x, y), Color.AMBER);
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    void deleteChecker(Checker checker) {
        checkerList.remove(checker);
    }

    private List<Vector> availableMoves(Checker checker) {
        int x = checker.getX();
        int y = checker.getY();
        List<Vector> list = new ArrayList<>();
        if (isBusy(x + 1, y - 1) && !isLastChecker(x+1, y-1)) {
            if (!isBusy(x + 2, y - 2)) {
                if (Objects.requireNonNull(findChekerByCoordinates(x + 1, y - 1)).getColor() != checker.getColor()){
                    list.add(new Vector(Direction.DOWN_RIGHT, 2));
                }
            }
        } else {
            list.add(new Vector(Direction.DOWN_RIGHT, 1));
        }

        if (isBusy(x + 1, y + 1) && !isLastChecker(x+1, y+1)) {
            if (!isBusy(x + 2, y + 2)) {
                if (Objects.requireNonNull(findChekerByCoordinates(x + 1, y + 1)).getColor() != checker.getColor()){
                    list.add(new Vector(Direction.UP_RIGHT, 2));
                }
            }
        } else {
            list.add(new Vector(Direction.UP_RIGHT, 1));
        }


        if (isBusy(x - 1, y + 1) && !isLastChecker(x-1, y+1)) {
            if (!isBusy(x - 2, y + 2)) {
                if (Objects.requireNonNull(findChekerByCoordinates(x - 1, y + 1)).getColor() != checker.getColor()){
                    list.add(new Vector(Direction.UP_LEFT, 2));
                }
            }
        } else {
            list.add(new Vector(Direction.UP_LEFT, 1));
        }

        if (isBusy(x - 1, y - 1) && !isLastChecker(x - 1, y - 1)) {
            if (!isBusy(x - 2, y - 2)) {
                if (Objects.requireNonNull(findChekerByCoordinates(x - 1, y - 1)).getColor() != checker.getColor()){
                    list.add(new Vector(Direction.DOWN_LEFT, 2));
                }
            }
        } else {
            list.add(new Vector(Direction.DOWN_LEFT, 1));
        }

        return list;
    }

    private boolean isBusy(int checkerX, int checkerY) {
        for (Checker checker : checkerList) {
            if (checker.getX() == checkerX && checker.getY() == checkerY) {
                return true;
            }
        }
        return false;
    }
    private Checker findChekerByCoordinates(int x, int y){
        for(Checker checker : checkerList){
            if (checker.getX() == x && checker.getY() == y){
                return checker;
            }
        }
        return null;
    }


}