import org.rjung.util.launchpad.Color;
import org.rjung.util.launchpad.Launchpad;
import org.rjung.util.launchpad.Pad;

import javax.sound.midi.InvalidMidiDataException;
import java.util.ArrayList;
import java.util.List;

public class Drawer {

    private Table table;
    private Launchpad launchpad;
    private boolean isDrawingAvailableMoves = false;
    private Checker isDrawingAvailableMovesChecker;

    Drawer(Table table, Launchpad launchpad) {
        this.table = table;
        this.launchpad = launchpad;
    }

    public void toggleDrawingAvailableMoves(boolean state) {
        this.isDrawingAvailableMoves = state;
        System.out.println(state);
    }

    public void setDrawingAvailableMovesChecker(Checker check) {
        this.isDrawingAvailableMovesChecker = check;
    }

    private void renderAvailableMoves(List<Table.Vector> availableMoves) throws InvalidMidiDataException {
        for (Table.Vector availableMove : availableMoves) {
            launchpad.set(Pad.find(isDrawingAvailableMovesChecker.move(availableMove).getX(),
                    isDrawingAvailableMovesChecker.move(availableMove).getY()),
                    Color.AMBER);
        }
    }

    private void renderCheckers(List<Checker> field) throws InvalidMidiDataException {
        for (Checker checker : field) {
            launchpad.set(Pad.find(checker.getX(), checker.getY()), checker.getColor());
        }
    }

    public void render() throws InvalidMidiDataException {
        clear();
        List<Checker> field = table.getCheckerList();
        renderCheckers(field);
        if (isDrawingAvailableMoves){
            renderAvailableMoves(table.availableMoves(isDrawingAvailableMovesChecker));
            toggleDrawingAvailableMoves(false);
        }


    }

    public void clear() throws InvalidMidiDataException {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                launchpad.set(Pad.find(i, j), Color.BLANK);
            }
        }
    }

}