/*
 * Copyright 2019 Rod MacKenzie.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package teamcity.jvm.monitor.server;

public class JvmLogName {

    private final String fileName;
    private final String displayName;

    public JvmLogName(String file) {
        this.fileName = file;
        this.displayName = createDisplayName(file);
    }

    @SuppressWarnings("unused")
    public String getFileName() { return fileName; }

    @SuppressWarnings("unused")
    public String getDisplayName() { return displayName; }

    private String createDisplayName(String file) {
        String[] parts = removeExtension(file).split("-");
        if (parts.length > 1) {
            return parts[1] + " (pid: " + parts[0] + ")";
        } else {
            return "<unknown> (pid: " + parts[0] + ")";
        }
    }

    private String removeExtension(String file) {
        return file.substring(0, file.length() - ".txt".length());
    }
}
