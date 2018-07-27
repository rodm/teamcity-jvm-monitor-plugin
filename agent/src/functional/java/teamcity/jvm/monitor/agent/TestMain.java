package teamcity.jvm.monitor.agent;

public class TestMain {

    public static void main(String[] args) {
        int[][] data = new int[5][];

        for (int i = 0; i < 5; i++) {
            data[i] = new int[1024 * 1024];
            sleep();
        }
    }

    private static void sleep() {
        try {
            Thread.sleep(1000);
        }
        catch (InterruptedException e) {
            // ignore
        }
    }
}
