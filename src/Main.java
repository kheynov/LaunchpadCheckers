import org.rjung.util.launchpad.Color;
import org.rjung.util.launchpad.Launchpad;

public class Main implements Runnable {
    boolean isRunning = true;

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {
        Launchpad lp;
        Drawer drawer;
        Handler handler;
        try {
            lp = new Launchpad();
            Table table = new Table(lp);
            drawer = new Drawer(table, lp);
            handler = new Handler(table, lp, drawer);

            drawer.toggleDrawingAvailableMoves(false);

            table.add(new Checker(2, 4, Color.RED));
            table.add(new Checker(3, 3, Color.GREEN));

            drawer.clear();
            drawer.render();


            while (isRunning) {

                Thread.sleep(23);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            isRunning = false;
        }
    }

}
