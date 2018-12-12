package net.dloud.platform.common.extend;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * @author QuDasheng
 * @create 2016-09-22 11:03
 */
public class StreamUtil {
    private static final int BUFFER_SIZE = 4096;
    private static final byte[] EMPTY_CONTENT = new byte[0];

    public static String copyToString(InputStream in) throws IOException {
        return copyToString(in, Charset.defaultCharset());
    }

    public static String copyToString(InputStream in, Charset charset) throws IOException {
        StringBuilder out = new StringBuilder();
        InputStreamReader reader = new InputStreamReader(in, charset);
        char[] buffer = new char[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = reader.read(buffer)) != -1) {
            out.append(buffer, 0, bytesRead);
        }
        return out.toString();
    }
}
