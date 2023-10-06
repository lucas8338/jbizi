package jbizi;

import java.io.*;

/**
 * class to copy files from class path.
 * */
public class CopyFromClassPath {
    String path;
    String destination;

    /**
     * @param path:
     *            the path the file in the class path.
     * @param destination:
     *                   a directory where to put the file.
     * */
    public CopyFromClassPath(String path, String destination){
        this.path = path;
        this.destination = destination;
    }

    public void copy() throws IOException {
        File pathFile = new File(this.path); // just to get the file name.
        File destFile = new File(new File(this.destination).getAbsolutePath()  + "/" + pathFile.getName());

        if (!destFile.exists()) {
            InputStream input = ClassLoader.getSystemResourceAsStream(this.path);
            try (FileOutputStream testngDest = new FileOutputStream(destFile)) {
                testngDest.write(input.readAllBytes());
            }
        }
    }
}
