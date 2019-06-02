import org.rjung.util.launchpad.Color;

public class Main implements Runnable {

    boolean isRunning = true;

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {

        try {
            Table table = new Table();

            while (isRunning) {
                Thread.sleep(23);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            isRunning = false;
        }
    }

}
