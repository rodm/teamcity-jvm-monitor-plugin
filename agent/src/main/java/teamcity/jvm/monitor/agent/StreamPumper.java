/*
 * Copyright 2018 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
