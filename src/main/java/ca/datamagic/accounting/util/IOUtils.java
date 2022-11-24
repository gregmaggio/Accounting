/**
 * 
 */
package ca.datamagic.accounting.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Greg
 *
 */
public class IOUtils {
	private static Logger logger = LogManager.getLogger(IOUtils.class);
	
	public static String readEntireStream(InputStream inputStream) throws IOException {
        StringBuffer textBuffer = new StringBuffer();
        byte[] buffer = new byte[1024];
        int bytesRead = 0;
        while ((bytesRead = inputStream.read(buffer, 0, buffer.length)) != -1) {
            textBuffer.append(new String(buffer, 0, bytesRead));
        }
        return textBuffer.toString();
    }

	public static void writeToStream(OutputStream outputStream, String content) throws IOException {
		byte[] bytes = content.getBytes();
		outputStream.write(bytes);
		outputStream.flush();
	}
	
    public static void closeQuietly(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Throwable t) {
            logger.warn("Throwable", t);
        }
    }

    public static void closeQuietly(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (Throwable t) {
        	logger.warn("Throwable", t);
        }
    }
}
