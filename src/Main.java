public class Main implements Runnable {

    public static void main(String[] args) {
        new Main().run();
    }

    @Override
    public void run() {
        try {
            new Table();
            while (true) Thread.sleep(23);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
