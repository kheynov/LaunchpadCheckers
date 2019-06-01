import org.rjung.util.launchpad.Color;
import org.rjung.util.launchpad.Launchpad;
import org.rjung.util.launchpad.LaunchpadReceiver;
import org.rjung.util.launchpad.Pad;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiUnavailableException;

public class Handler implements LaunchpadReceiver {

    private Table table;
    private Launchpad launchpad;
    private Drawer drawer;

    Handler(Table table, Launchpad lp, Drawer drawer) throws MidiUnavailableException {
        this.table = table;
        this.drawer = drawer;
        this.launchpad = new Launchpad(this);
    }

    @Override
    public void receive(Pad pad) {
        for (int i = 0; i < table.getCheckerList().size(); i++) {
            if (table.getCheckerList().get(i).getX() == pad.getX()
            && table.getCheckerList().get(i).getY() == pad.getY()) {
                drawer.toggleDrawingAvailableMoves(true);
                drawer.setDrawingAvailableMovesChecker(table.getCheckerList().get(i));
            }
        }

        try {
            drawer.render();
        } catch (InvalidMidiDataException e) {
            e.printStackTrace();
        }
    }

}
