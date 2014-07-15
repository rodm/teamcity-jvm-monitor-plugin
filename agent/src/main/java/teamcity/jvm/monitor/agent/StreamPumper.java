package teamcity.jvm.monitor.agent;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class StreamPumper implements Runnable {

    private InputStream is;

    private OutputStream os;

    StreamPumper(InputStream is, OutputStream os) {
        this.is = is;
        this.os = os;
    }

    @Override
    public void run() {
        final int size = 1024;
        final byte[] buf = new byte[size];

        int length;
        try {
            while ((length = is.read(buf)) > 0) {
                os.write(buf, 0, length);
                os.flush();
            }
        }
        catch (Exception e) {
            // ignore
        }
        finally {
            try {
                os.close();
            } catch (IOException e) {
                // ignore
            }
        }
    }
}
