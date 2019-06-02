public class Drawer {
/*
    private Table table;
    private Launchpad launchpad;
    private Checker isDrawingAvailableMovesChecker;

    Drawer(Table table, Launchpad launchpad) {
        this.table = table;
        this.launchpad = launchpad;
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
        if (table.getState() == Table.State.DRAW_AVAILABLE_MOVES){
            renderAvailableMoves(table.availableMoves(isDrawingAvailableMovesChecker));
            table.setState(Table.State.DRAW_CHECKERS);
        }
    }

    public void clear() throws InvalidMidiDataException {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                launchpad.set(Pad.find(i, j), Color.BLANK);
            }
        }
    }*/

}
