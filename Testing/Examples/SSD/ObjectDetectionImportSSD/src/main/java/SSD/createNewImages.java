package SSD;

import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import org.apache.commons.compress.utils.FileNameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class createNewImages {
    public static void main(String[] args) throws IOException {
        String directory = "images/images-to-resize/";
        File[] files = new File(directory).listFiles();
        assert files != null;
        iterateDirectory(files);
    }
    private static void iterateDirectory(File[] files) throws IOException {
        for (File filename : files) {
            if (filename.isDirectory()) {
                iterateDirectory(filename.listFiles());
            } else if(FileNameUtils.getExtension(filename.getName()).equals("png") || FileNameUtils.getExtension(filename.getName()).equals("jpg")){ //make sure file is a jpg image
                SmallMaker.smallMaker(filename.getPath(), 1.1, 30);
            }
        }
    }
}
