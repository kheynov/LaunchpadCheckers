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

    private Launchpad launchpad;//объект для связи с MIDI устройством
    private ArrayList<Checker> checkerList = new ArrayList<>();//лист со всеми шашками на поле
    private ArrayList<Vector> availableMoves = new ArrayList<>();//список доступных ходов для конкретной шашки
    private Checker lastClickedChecker;//последняя нажатая шашка
    private boolean isReadyToStep = false;//отрисовали доступные ходы, можем ходить
    private boolean isWhiteTurn = true;//очередь ходить для белых шашек

    Table() throws MidiUnavailableException {

        this.launchpad = new Launchpad(this);//объявляем Novation Launchpad
        clearDisplay();//очищаем экран

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        animateFlush(Color.AMBER);//рисуем анимацию желтым цветом
        clearDisplay();//снова очищаем дисплей
        generateStartMap();//создаем начальное поле с шашками
        redraw();//перерисовываем
    }

    @Override
    public void receive(Pad pad) {
        if (isReadyToStep) {//если ходим
            isReadyToStep = false;//убираем флажок с переменной, значит мы походили или отменили ход, значит потом надо будет снова отрисовать ходы
            for (Vector vector : availableMoves) {//для каждого доступного хода
                if (vector != null && lastClickedChecker != null) {//если ход и шашка не null
                    if (vector.getX(lastClickedChecker.getX()) == pad.getX() && vector.getY(lastClickedChecker.getY()) == pad.getY()) {//если мы нажали именно на шашку
                        if (vector.getLength() > 1) {//если мы рубим(длина хода > 1)
                            Vector tmpVector = new Vector(vector.getDirection(), vector.getLength() - 1);//создаем временный(template) Vector на котором находится шашка которую мы хотим срубить
                            checkerList.remove(findCheckerByCoordinates(tmpVector.getX(lastClickedChecker.getX()),//удаляем шашку противника, мы же её срубили
                                    tmpVector.getY(lastClickedChecker.getY())));

                            lastClickedChecker.move(vector);//передвигаем шашку
                            if (!isExtraMoveAvailable(availableMoves(lastClickedChecker), lastClickedChecker)) {//если мы можем дальше срубить
                                showAvailableMoves(lastClickedChecker);//предлагаем еще ходить
                                isWhiteTurn = !isWhiteTurn;
                            }
                        } else {
                            lastClickedChecker.move(vector);//просто ходим на одну клетку
                            isWhiteTurn = !isWhiteTurn;//очередь соперника
                        }

                        if (isBecomeQueen(lastClickedChecker)) {//если шашка стала дамкой
                            lastClickedChecker.setQueen();
                        }

                        redraw();//перерисовываем все поле
                        lastClickedChecker = null;//стираем информацию о последней нажатой шашке
                        break;
                    }
                }
            }
            clearAvailableMoves();//очищаем доступные ходы
            redraw();
            //обработка хода
        } else {
            isReadyToStep = true;//следующим действием будем ходить
            for (Checker checker : checkerList) {//для всех шашек на поле
                if (checker.getX() == pad.getX() && checker.getY() == pad.getY()) {//если мы нажали на шашку
                    if (isWhiteTurn) {//если очередь ходить для белых
                        if (checker.getColor() == Color.GREEN) {//и если мы нажали на белую шашку
                            redraw();//перерисовываем
                            lastClickedChecker = checker;//обозначаем как используемую в данный момент
                            showAvailableMoves(checker);//показываем доступные ходы
                        }
                    } else {
                        if (checker.getColor() == Color.RED) {//аналогично для другого цвета
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
        }//если шашки одного цвета, значит игра окончена

    }

    private void closeGame() {//игра окончена
        clearDisplay();//очищаем дисплей
        animateFlush(getWinnerColor());//делаем анимацию цветом победителя
//        Main.isRunning = false;//выключаем программу
    }

    private boolean isBecomeQueen(Checker checker) {//Проверяем шашку, стала ли она дамкой
        if (checker.getColor() == Color.RED && checker.getY() == 0) {//крайние значения для соответствующих цветов
            return true;
        } else return checker.getColor() == Color.GREEN && checker.getY() == 7;
    }

    private void animateFlush(Color color) {//анимация, просто два цикла, которые по очереди зажигают светодиоды змейкой с двух сторон
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

    private void clearDisplay() {//очищаем дисплей, просто присваиваем каждому светодиоду 0

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

    private boolean isExtraMoveAvailable(List<Vector> availableMoves, Checker checker) {//если мы еще можем походить
        if (!checker.isQueen) {//если шашка не дамка(дамки и так слишком дизбалансные)
            for (Vector move : availableMoves) {//если мы можем походить в любую из сторон больше чем на 1 клетку, т.е. срубить, то мы можем ходить еще
                if (move.getLength() >= 2) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    private void clearAvailableMoves() {//просто очищаем массив с доступными ходами, чтобы мы могли отрисовать их для другой шашки
        for (int i = 0; i < availableMoves.size(); i++) {
            Vector vector = availableMoves.get(i);
            availableMoves.remove(vector);
        }
    }

    private boolean checkWinner() {//считаем сколько шашек каждого цвета осталось на поле, и проверяем, не выиграла ли какая-либо сторона
        int whites = 0;
        int blacks = 0;
        for (Checker checker : checkerList) {
            if (checker.getColor() == Color.RED || checker.getColor() == Color.R2G1) {
                blacks++;
            } else {
                whites++;
            }
        }

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

    private Color getWinnerColor() {//смотрим какого цвета победитель
        int whites = 0;
        int blacks = 0;

        for (Checker checker : checkerList) {
            if (checker.getColor() == Color.RED || checker.getColor() == Color.R2G1) {
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

    private void redraw() {//перерисовываем карту

        clearDisplay();//очищаем дисплей
        for (Checker checker : checkerList) {//для каждой шашки
            try {
                if (checker.isQueen) {//если дамка
                    if (checker.getColor() == Color.RED) {//если она красная по своей сути
                        launchpad.set(Pad.find(checker.getX(), checker.getY()), Color.R2G1);//рисуем ее цвета дамки
                    } else {
                        launchpad.set(Pad.find(checker.getX(), checker.getY()), Color.R1G2);//рисуем её цвета зелёной дамки
                    }
                } else {
                    launchpad.set(Pad.find(checker.getX(), checker.getY()), checker.getColor());//если шашка не дамка, рисуем как есть
                }
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private void generateStartMap() {//создание стартового поля, просто 2 цикла по определению четной и нечётной клетки поля
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

    private boolean isNotLastChecker(int x, int y) {//если это не крайняя клетка поля
        return x != 0 && x != 7 && y != 0 && y != 7;
    }

    private void showAvailableMoves(Checker checker) {//рисуем доступные ходы

        availableMoves = (ArrayList<Vector>) availableMoves(checker);//генерируем доступные ходы в методе availableMoves(Checker checker)

        for (Vector availableMove : availableMoves) {//для каждого хода

            int x = availableMove.getX(checker.getX());//находим конечную точку относительно координат шашки, на которую нажали
            int y = availableMove.getY(checker.getY());

            try {
                if (isMapContains(x, y)) {//если эта координата не за пределами поля
                    launchpad.set(Pad.find(x, y), Color.AMBER);//выводим возможные варианты хода
                }
            } catch (InvalidMidiDataException e) {
                e.printStackTrace();
            }
        }
    }

    private List<Vector> availableMoves(Checker checker) {

        int x = checker.getX();//координаты шашки для которой мы ищем доступные ходы
        int y = checker.getY();

        List<Vector> list = new ArrayList<>();

        if (x != 0) {//если мы не слева
            if (y != 0) {//если мы не в левом нижнем углу (мы не можем ходить вправо вниз и вправо вверх(см прошлое условие))

                if (!checker.isQueen) {//если шашка не дамка

                    if (isBusy(x - 1, y - 1)) {//если клетка в которую мы хотим походить занята

                        if (isNotLastChecker(x - 1, y - 1)) {//и если это не последняя клетка поля(ведь тогда ее нельзя срубить)

                            if (!isBusy(x - 2, y - 2)) {//если клетка за той, которую мы хотим срубить свободна

                                if (findCheckerByCoordinates(x - 1, y - 1) != null) {//удостоверяемся, что клетка которую мы рубим не null

                                    if (Objects.requireNonNull(findCheckerByCoordinates(x - 1, y - 1)).getColor() != checker.getColor()) {//если это фишка соперника(другого цвета)
                                        list.add(new Vector(Vector.Direction.DOWN_LEFT, 2));//то мы её рубим
                                    }
                                }
                            }
                        }
                    } else {//если клетка в которую мы ходим свободна, то мы просто ходим туда
                        if (checker.getColor() == Color.RED) {//если шашка красного цвета (ведь шашки назад ходить не могут, только рубить)
                            list.add(new Vector(Vector.Direction.DOWN_LEFT, 1));
                        }
                    }
                } else {// если шашка все-таки дамка
                    int i = checker.getX(), j = checker.getY();//координаты шашек
                    int iter = 1;//счетчик уже сделанных ходов
                    int blockCounter = 0;//счетчик заблокированных ходов
                    while (i >= 0 && j >= 0) {//пока мы не уперлись в стену
                        if (!isBusy(i - 1, j - 1) && isMapContains(i - 1, j - 1)) {//если соседняя клетка не занята и она в пределах карты
                            list.add(new Vector(Vector.Direction.DOWN_LEFT, iter));//ходим туда
                        } else if (isBusy(i - 1, j - 1) && isMapContains(i - 1, j - 1)) {//если клетка все таки занята

                            if (findCheckerByCoordinates(i - 1, j - 1) != null) {//Если в этой клетке находится шашка

                                if (Objects.requireNonNull(findCheckerByCoordinates(i - 1, j - 1)).getColor() != checker.getColor() && !isBusy(i - 2, j - 2)) {//если соседняя шашка другого цвета и следующая за ней свободна
                                    list.add(new Vector(Vector.Direction.DOWN_LEFT, iter + 1));//ходим в следующую за ней(рубим)
                                    break;//и выходим из цикла, дальше мы идти не будем
                                } else {
                                    blockCounter++;//если в той клетке было занятно, увеличиваем счетчик заблокированных ходов
                                }

                            }
                        } else {
                            blockCounter++;//увеличиваем счетчик заблокированных ходов
                        }
                        if (blockCounter >= 1) {//если нам на пути встретилось подряд уже два препятствия, значит туда нам уже не попасть
                            break;//выходим из цикла
                        }
                        iter++;//увеличиваем/уменьшаем счетчики направлений и т.п.
                        i--;
                        j--;

                    }
                }
            }
            if (y != 7) {//если мы не в левом верхнем углу
                if (!checker.isQueen) {//здесь все аналогично остальным направлениям, изменяется только направление из 4-х

                    if (isBusy(x - 1, y + 1)) {

                        if (isNotLastChecker(x - 1, y + 1)) {

                            if (!isBusy(x - 2, y + 2)) {

                                if (findCheckerByCoordinates(x - 1, y + 1) != null) {

                                    if (Objects.requireNonNull(findCheckerByCoordinates(x - 1, y + 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Vector.Direction.UP_LEFT, 2));
                                    }
                                }
                            }
                        }
                    } else {
                        if (checker.getColor() == Color.GREEN) {
                            list.add(new Vector(Vector.Direction.UP_LEFT, 1));
                        }
                    }

                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    int blockCounter = 0;
                    while (i >= 0 && j < 8) {
                        if (!isBusy(i - 1, j + 1) && isMapContains(i - 1, j + 1)) {
                            list.add(new Vector(Vector.Direction.UP_LEFT, iter));
                        } else if (isBusy(i - 1, j + 1) && isMapContains(i - 1, j + 1)) {
                            if (findCheckerByCoordinates(i - 1, j + 1) != null) {
                                if (Objects.requireNonNull(findCheckerByCoordinates(i - 1, j + 1)).getColor() != checker.getColor() && !isBusy(i - 2, j + 2)) {
                                    list.add(new Vector(Vector.Direction.UP_LEFT, iter + 1));
                                    break;
                                } else {
                                    blockCounter++;
                                }
                            }
                        } else {
                            blockCounter++;
                        }
                        if (blockCounter >= 1) {
                            break;
                        }
                        iter++;
                        i--;
                        j++;
                    }
                }
            }
        }
        if (x != 7) {//если мы не справа
            if (y != 7) {//если мы не справа сверху
                if (!checker.isQueen) {

                    if (isBusy(x + 1, y + 1)) {//аналогично на все 4 диагонали

                        if (isNotLastChecker(x + 1, y + 1)) {

                            if (!isBusy(x + 2, y + 2)) {

                                if (findCheckerByCoordinates(x + 1, y + 1) != null) {

                                    if (Objects.requireNonNull(findCheckerByCoordinates(x + 1, y + 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Vector.Direction.UP_RIGHT, 2));
                                    }
                                }
                            }
                        }
                    } else {
                        if (checker.getColor() == Color.GREEN) {
                            list.add(new Vector(Vector.Direction.UP_RIGHT, 1));
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    int blockCounter = 0;
                    while (i < 8 && j < 8) {
                        if (!isBusy(i + 1, j + 1) && isMapContains(i + 1, j + 1)) {
                            list.add(new Vector(Vector.Direction.UP_RIGHT, iter));
                        } else if (isBusy(i + 1, j + 1) && isMapContains(i + 1, j + 1)) {

                            if (findCheckerByCoordinates(i + 1, j + 1) != null) {

                                if (Objects.requireNonNull(findCheckerByCoordinates(i + 1, j + 1)).getColor() != checker.getColor() && !isBusy(i + 2, j + 2)) {
                                    list.add(new Vector(Vector.Direction.UP_RIGHT, iter + 1));
                                    break;
                                } else {
                                    blockCounter++;
                                }
                            }
                        } else {
                            blockCounter++;
                        }
                        if (blockCounter >= 1) {
                            break;
                        }
                        iter++;
                        i++;
                        j++;
                    }
                }
            }
            if (y != 0) {//если мы не в правом нижнем углу
                if (!checker.isQueen) {

                    if (isBusy(x + 1, y - 1)) {

                        if (isNotLastChecker(x + 1, y - 1)) {

                            if (!isBusy(x + 2, y - 2)) {

                                if (findCheckerByCoordinates(x + 1, y - 1) != null) {

                                    if (Objects.requireNonNull(findCheckerByCoordinates(x + 1, y - 1)).getColor() != checker.getColor()) {
                                        list.add(new Vector(Vector.Direction.DOWN_RIGHT, 2));
                                    }
                                }
                            }
                        }
                    } else {
                        if (checker.getColor() == Color.RED) {
                            list.add(new Vector(Vector.Direction.DOWN_RIGHT, 1));
                        }
                    }
                } else {
                    int i = checker.getX(), j = checker.getY();
                    int iter = 1;
                    int blockCounter = 0;
                    while (i < 8 && j >= 0) {
                        if (!isBusy(i + 1, j - 1) && isMapContains(i + 1, j - 1)) {
                            list.add(new Vector(Vector.Direction.DOWN_RIGHT, iter));
                        } else if (isBusy(i + 1, j - 1) && isMapContains(i + 1, j - 1)) {

                            if (findCheckerByCoordinates(i + 1, j - 1) != null) {

                                if (Objects.requireNonNull(findCheckerByCoordinates(i + 1, j - 1)).getColor() != checker.getColor() && !isBusy(i + 2, j - 2)) {
                                    list.add(new Vector(Vector.Direction.DOWN_RIGHT, iter + 1));
                                    break;
                                } else {
                                    blockCounter++;
                                }
                            }
                        } else {
                            blockCounter++;
                        }
                        if (blockCounter >= 1) {
                            break;
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
        for (Checker checker : checkerList) {//проходим по всем шашкам и сверяем с данными координатами
            if (checker.getX() == checkerX && checker.getY() == checkerY) {
                return true;
            }
        }
        return false;
    }

    private boolean isMapContains(int x, int y) {//проверка на принадлежность шашки игровому полю
        return x >= 0 && x <= 7 && y >= 0 && y <= 7;
    }

    private Checker findCheckerByCoordinates(int x, int y) {//находим объект шашки по координатам
        for (Checker checker : checkerList) {//проходим по всем шашкам и ищем совпадение
            if (checker.getX() == x && checker.getY() == y) {
                return checker;//если нашли, возвращаем объект найденной шашки
            }
        }
        return null;
    }
}
