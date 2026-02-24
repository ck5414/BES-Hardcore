package cn.tesseract.asm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public interface Transformer {
    static byte[] readAllBytes(InputStream is) {
        try {
            ByteArrayOutputStream bout = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int read;
            while ((read = is.read(buffer)) != -1) {
                bout.write(buffer, 0, read);
            }
            return bout.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
