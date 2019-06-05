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
    private boolean isReadyToStep = false;
    private boolean isWhiteTurn = true;

    Table() throws MidiUnavailableException {

        this.launchpad = new Launchpad(this);
        clearDisplay();

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        animateFlush(Color.AMBER);
        clearDisplay();

//        generateStartMap();
        generateTestMap();
        redraw();
    }

    @Override
    public void receive(Pad pad) {
        redraw();
        if (isReadyToStep) {
            isReadyToStep = false;
            for (Vector vector : availableMoves) {

                if (vector != null && lastClickedChecker != null) {

                    if (vector.getX(lastClickedChecker.getX()) == pad.getX() && vector.getY(lastClickedChecker.getY()) == pad.getY()) {

                        if (vector.getLength() > 1) {
                            Vector tmpVector = new Vector(vector.getDirection(), vector.getLength()-1);
                            checkerList.remove(findCheckerByCoordinates(tmpVector.getX(lastClickedChecker.getX()),
                                    tmpVector.getY(lastClickedChecker.getY())));
                        }

                        lastClickedChecker.move(vector);
                        if (isBecomeQueen(lastClickedChecker)) {
                            lastClickedChecker.setQueen();
                        }
                        isWhiteTurn = !isWhiteTurn;
                        redraw();
                        lastClickedChecker = null;
                        break;
                    }
                }
            }
            clearAvailableMoves();
            redraw();
            //обработка хода
        } else {

            isReadyToStep = true;

            for (Checker checker : checkerList) {
                if (checker.getX() == pad.getX() && checker.getY() == pad.getY()) {
                    if (isWhiteTurn) {
                        if (checker.getColor() == Color.GREEN) {
                            redraw();
                            lastClickedChecker = checker;
                            showAvailableMoves(checker);

                        }
                    } else {
                        if (checker.getColor() == Color.RED) {
                            redraw();
                            lastClickedChecker = checker;
                            showAvailableMoves(checker);
                        }
                    }
                }
            }
        }

        if (checkWinner()) {
            closeGame();
        }
    }

    private void closeGame() {
        clearDisplay();
        animateFlush(getWinnerColor());
//        clearCheckers();
//        clearAvailableMoves();
    }

    private boolean isBecomeQueen(Checker checker) {
        if (checker.getColor() == Color.RED && checker.getY() == 0) {
            return true;
        } else if (checker.getColor() == Color.GREEN && checker.getY() == 7) {
            return true;
        } else {
            return false;
        }
    }

    private void animateFlush(Color color) {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 8; j++) {
                try {
                    launchpad.set(Pad.find(i, 7 - j), color);
                    launchpad.set(Pad.find(7 - i, j), color);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Thread.sleep(30);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        try {
            Thread.sleep(250);
        } catch (InterruptedException e) {
            e.printStackTrace();
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
        for (int i = 0; i < availableMoves.size(); i++) {
            Vector vector = availableMoves.get(i);
            availableMoves.remove(vector);
        }
    }

    private boolean checkWinner() {

        int whites = 0;
        int blacks = 0;

        for (Checker checker : checkerList) {
            if (checker.getColor() == Color.RED) {
                blacks++;
            } else {
                whites++;
            }
        }
        System.out.println("G: " + whites + " R: " + blacks);
        if (blacks == 0) {
            System.out.println("Победили зелёные");
            return true;
        } else if (whites == 0) {
            System.out.println("Победили красные");
            return true;
        } else {
            return false;
        }
    }

    private Color getWinnerColor() {

        int whites = 0;
        int blacks = 0;

        for (Checker checker : checkerList) {
            if (checker.getColor() == Color.RED) {
                blacks++;
            } else {
                whites++;
            }
        }

        if (blacks == 0) {
            return Color.GREEN;
        } else if (whites == 0) {
            return Color.RED;
        } else {
            return Color.AMBER;
        }
    }

    private void redraw() {

        clearDisplay();
        System.out.println("Redraw");
        for (Checker checker : checkerList) {
            try {
                if (checker.isQueen) {
                    if (checker.getColor() == Color.RED) {
                        launchpad.set(Pad.find(checker.getX(), checker.getY()), Color.R2G1);
                    } else {
                        launchpad.set(Pad.find(checker.getX(), checker.getY()), Color.R1G2);
                    }
                } else {
                    launchpad.set(Pad.find(checker.getX(), checker.getY()), checker.getColor());
                }
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateTestMap() {

        Checker red_checker = new Checker(1, 1, Color.RED);
        red_checker.isQueen=true;

        Checker green_checker = new Checker(5, 3, Color.GREEN);

        checkerList.add(red_checker);
        checkerList.add(green_checker);
    }

    private void generateStartMap() {
        for (int i = 1; i <= 8; i++) {
            if (i % 2 == 0) {

                checkerList.add(new Checker(i - 1, 1, Color.GREEN));
            } else {
                checkerList.add(new Checker(i - 1, 0, Color.GREEN));
                checkerList.add(new Checker(i - 1, 2, Color.GREEN));
            }
        }

        for (int i = 1; i <= 8; i++) {
            if (i % 2 != 0) {
                checkerList.add(new Checker(i - 1, 6, Color.RED));
            } else {
                checkerList.add(new Checker(i - 1, 5, Color.RED));
                checkerList.add(new Checker(i - 1, 7, Color.RED));
            }
        }
    }

    public enum Direction {
        UP_LEFT,
        UP_RIGHT,
        DOWN_LEFT,
        DOWN_RIGHT
    }

    private boolean isNotLastChecker(int x, int y) {//если это не крайняя клетка поля
        return x != 0 && x != 7 && y != 0 && y != 7;
    }

    private void showAvailableMoves(Checker checker) {

        availableMoves = (ArrayList<Vector>) availableMoves(checker);

        for (Vector availableMove : availableMoves) {

            int x = availableMove.getX(checker.getX());
            int y = availableMove.getY(checker.getY());

            try {
                if (isMapContains(x, y)) {
                    launchpad.set(Pad.find(x, y), Color.AMBER);//выводим возможные варианты хода
                }
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Vector> availableMoves(Checker checker) {

        int x = checker.getX();
        int y = checker.getY();

        List<Vector> list = new ArrayList<>();

        if (x != 0) {
            if (y != 0) {
                if (!checker.isQueen) {
                    if (isBusy(x - 1, y - 1)) {
                        if (isNotLastChecker(x - 1, y - 1)) {
                            if (!isBusy(x - 2, y - 2)) {
                                if (findCheckerByCoordinates(x - 1, y - 1) != null) {
                                    if (Objects.requireNonNull(findCheckerByCoordinates(x - 1, y - 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Direction.DOWN_LEFT, 2));
                                    }
                                }
                            }
                        }
                    } else {

                        if (checker.getColor() == Color.RED || checker.isQueen) {
                            list.add(new Vector(Direction.DOWN_LEFT, 1));
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    while (i >= 0 && j >= 0) {
                        if (!isBusy(i-1, j-1) && isMapContains(i-1, j-1)) {
                            list.add(new Vector(Direction.DOWN_LEFT, iter));
                        } else if (isBusy(i - 1, j - 1) && isMapContains(i - 1, j - 1)) {
                            if (findCheckerByCoordinates(i - 1, j - 1) != null) {
                                if (Objects.requireNonNull(findCheckerByCoordinates(i - 1, j - 1)).getColor() != checker.getColor()) {
                                    list.add(new Vector(Direction.DOWN_LEFT, iter + 1));
                                    break;
                                }
                            }
                        }
                        iter++;
                        i--;
                        j--;

                    }
                }
            }
            if (y != 7) {
                if (!checker.isQueen) {
                    if (isBusy(x - 1, y + 1)) {
                        if (isNotLastChecker(x - 1, y + 1)) {
                            if (!isBusy(x - 2, y + 2)) {
                                if (findCheckerByCoordinates(x - 1, y + 1) != null) {
                                    if (Objects.requireNonNull(findCheckerByCoordinates(x - 1, y + 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Direction.UP_LEFT, 2));
                                    }
                                }

                            }
                        }
                    } else {
                        if (checker.getColor() == Color.GREEN || checker.isQueen) {
                            list.add(new Vector(Direction.UP_LEFT, 1));
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    while (i >= 0 && j < 8) {
                        if (!isBusy(i - 1, j + 1) && isMapContains(i - 1, j + 1)) {
                            list.add(new Vector(Direction.UP_LEFT, iter));
                        } else if (isBusy(i - 1, j + 1) && isMapContains(i - 1, j + 1)) {
                            if (findCheckerByCoordinates(i - 1, j + 1) != null) {
                                if (Objects.requireNonNull(findCheckerByCoordinates(i - 1, j + 1)).getColor() != checker.getColor()) {
                                    list.add(new Vector(Direction.UP_LEFT, iter + 1));
                                    break;
                                }
                            }
                        }
                        iter++;
                        i--;
                        j++;
                    }
                }
            }
        }
        if (x != 7) {
            if (y != 7) {
                if (!checker.isQueen) {
                    if (isBusy(x + 1, y + 1)) {//аналогично на все 4 диагонали
                        if (isNotLastChecker(x + 1, y + 1)) {
                            if (!isBusy(x + 2, y + 2)) {
                                if (findCheckerByCoordinates(x + 1, y + 1) != null) {
                                    if (Objects.requireNonNull(findCheckerByCoordinates(x + 1, y + 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Direction.UP_RIGHT, 2));
                                    }
                                }
                            }
                        }
                    } else {
                        if (checker.getColor() == Color.GREEN || checker.isQueen) {
                            list.add(new Vector(Direction.UP_RIGHT, 1));
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    while (i < 8 && j < 8) {
                        if (!isBusy(i + 1, j + 1) && isMapContains(i + 1, j + 1)) {
                            list.add(new Vector(Direction.UP_RIGHT, iter));
                        } else if (isBusy(i + 1, j + 1) && isMapContains(i + 1, j + 1)) {
                            if (findCheckerByCoordinates(i + 1, j + 1) != null) {
                                if (Objects.requireNonNull(findCheckerByCoordinates(i + 1, j + 1)).getColor() != checker.getColor()) {
                                    list.add(new Vector(Direction.UP_RIGHT, iter + 1));
                                    break;
                                }
                            }
                        }
                        iter++;
                        i++;
                        j++;

                    }
                }
            }
            if (y != 0) {
                if (!checker.isQueen) {
                    if (isBusy(x + 1, y - 1)) {//если соседняя клетка занята, но она не крайняя на поле
                        if (isNotLastChecker(x + 1, y - 1)) {
                            if (!isBusy(x + 2, y - 2)) {//если клетка в которую мы хотим походить свободна
                                if (findCheckerByCoordinates(x + 1, y - 1) != null) {
                                    if (Objects.requireNonNull(findCheckerByCoordinates(x + 1, y - 1)).getColor() != checker.getColor()) {//если мы шагаем не через клетку своего цвета
                                        list.add(new Vector(Direction.DOWN_RIGHT, 2));//то можно перешагнуть
                                    }
                                }
                            }
                        }
                    } else {
                        if (checker.getColor() == Color.RED || checker.isQueen) {
                            list.add(new Vector(Direction.DOWN_RIGHT, 1));//просто ходим на 1 клетку
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    while (i < 8 && j >= 0) {
                        if (!isBusy(i+1, j-1) && isMapContains(i+1, j-1)) {
                            list.add(new Vector(Direction.DOWN_RIGHT, iter));
                        } else if (isBusy(i + 1, j - 1) && isMapContains(i + 1, j - 1)) {
                            if (findCheckerByCoordinates(i + 1, j - 1) != null) {
                                if (Objects.requireNonNull(findCheckerByCoordinates(i + 1, j - 1)).getColor() != checker.getColor()) {
                                    list.add(new Vector(Direction.DOWN_RIGHT, iter+1));
                                    break;
                                }
                            }
                        }
                        iter++;
                        i++;
                        j--;
                    }
                }
            }
        }
        return list;
    }

    private boolean isBusy(int checkerX, int checkerY) {//если клетка по данным координатом занята другой шашкой

        for (Checker checker : checkerList) {
            if (checker.getX() == checkerX && checker.getY() == checkerY) {
                return true;
            }
        }
        return false;
    }

    private boolean isMapContains(int x, int y) {
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }

    private Checker findCheckerByCoordinates(int x, int y) {//находим объект шашки по координатам
        for (Checker checker : checkerList) {
            if (checker.getX() == x && checker.getY() == y) {
                return checker;
            }
        }
        return null;
    }
}

class Vector {
    private Table.Direction direction;
    private int length;

    Vector(Table.Direction direction, int length) {
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

    Table.Direction getDirection() {
        return direction;
    }

    int getLength() {
        return length;
    }
}