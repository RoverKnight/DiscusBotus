package bot;

public class TestThread extends Thread {

    public TestThread () {
    }

    public void run(){

        try {
            Thread.sleep(60 * 1000);
        } catch (InterruptedException e) {
            return;
        }

    }
}