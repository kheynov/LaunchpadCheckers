public class Main implements Runnable {

    static boolean isRunning = true;

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {

        try {
            new Table();
            while (isRunning) {
                Thread.sleep(23);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            isRunning = false;
        }
    }

}
