package github;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

public class FileManager {

    public static void download(String url, String name) throws IOException {
        URL _url = new URL(url);

        BufferedInputStream inputStream = new BufferedInputStream(_url.openStream());
        FileOutputStream outputStream = new FileOutputStream(name);

        int count;
        byte[] buffer = new byte[1024];
        while((count = inputStream.read(buffer, 0, 1024)) != -1)
            outputStream.write(buffer, 0, count);

        outputStream.close();
        inputStream.close();
    }

    public static void delete(String name) {
        File file = new File(name);
        if(file.exists())
            file.delete();
    }

}
