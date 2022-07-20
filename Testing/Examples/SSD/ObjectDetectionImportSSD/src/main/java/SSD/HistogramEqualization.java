package SSD;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import org.apache.commons.compress.utils.FileNameUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

//https://www.codeproject.com/Tips/1172662/Histogram-Equalisation-in-Java
public class HistogramEqualization {
    public static void main(String[] args) throws IOException, TranslateException {
        File[] files = new File("testImage/image").listFiles();
        runApplication(files);
    }
    static void histogramequalization(String path) throws IOException {
        //naam scheiden
        String sname = path.substring(path.lastIndexOf('/'));
        String name = sname.substring(1);
        String pextend = path.substring(path.lastIndexOf('.'));
        String extend = pextend.substring(1);

        BufferedImage img0 = ImageIO.read(new File(path));
        BufferedImage img1 = getGrayscaleImage(img0);
        BufferedImage img2 = equalize(img1);
        File f2 = new File("histogramequalizationimages/"+name);
        ImageIO.write(img2,extend,f2);
    }

    static BufferedImage getGrayscaleImage(BufferedImage src) {
        BufferedImage gImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = src.getRaster();
        WritableRaster gr = gImg.getRaster();
        for (int i = 0; i < wr.getWidth(); i++) {
            for (int j = 0; j < wr.getHeight(); j++) {
                gr.setSample(i, j, 0, wr.getSample(i, j, 0));
            }
        }
        gImg.setData(gr);
        return gImg;
    }
    static BufferedImage equalize(BufferedImage src){
        BufferedImage nImg = new BufferedImage(src.getWidth(), src.getHeight(),
                BufferedImage.TYPE_BYTE_GRAY);
        WritableRaster wr = src.getRaster();
        WritableRaster er = nImg.getRaster();
        int totpix= wr.getWidth()*wr.getHeight();
        int[] histogram = new int[256];

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                histogram[wr.getSample(x, y, 0)]++;
            }
        }

        int[] chistogram = new int[256];
        chistogram[0] = histogram[0];
        for(int i=1;i<256;i++){
            chistogram[i] = chistogram[i-1] + histogram[i];
        }

        float[] arr = new float[256];
        for(int i=0;i<256;i++){
            arr[i] =  (float)((chistogram[i]*255.0)/(float)totpix);
        }

        for (int x = 0; x < wr.getWidth(); x++) {
            for (int y = 0; y < wr.getHeight(); y++) {
                int nVal = (int) arr[wr.getSample(x, y, 0)];
                er.setSample(x, y, 0, nVal);
            }
        }
        nImg.setData(er);
        return nImg;
    }
    private static void runApplication(File[] files) throws IOException, TranslateException {
        //src: https://www.geeksforgeeks.org/java-program-to-traverse-in-a-directory/

        for (File filename : files) {
            if (filename.isDirectory()) {
                runApplication(filename.listFiles());
            } else{//else if(FileNameUtils.getExtension(filename.getName()).equals("jpg")){ //make sure file is a jpg image
                histogramequalization(filename.getPath());
            }
        }
    }
}