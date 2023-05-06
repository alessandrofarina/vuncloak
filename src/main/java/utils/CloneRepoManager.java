package utils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;

public class CloneRepoManager {

    public static File getRoot(String root) {
        File file = new File(root);
        if(!file.exists())
            file.mkdir();
        return file;
    }

    public static Collection<File> findPOMs(String root) {
        return _findPOMs(root);
    }

    private static Collection<File> _findPOMs(String path) {
        ArrayList<File> poms = new ArrayList<>();

        File root = new File(path);
        if(root.exists()) {
            File[] files = root.listFiles();

            if(files != null) {
                for(File file: files) {
                    if(file.isDirectory())
                        poms.addAll(_findPOMs(file.getPath()));
                    else if(file.getName().equals("pom.xml"))
                        poms.add(file);
                }
            }
        }

        return poms;
    }

}
