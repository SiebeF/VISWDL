package SSD;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class SmallMaker {
    public static void main(String[] args) throws IOException {
        smallMaker("images/images-to-resize/a-port-bow-view-of-the-russian-navys-pacific-fleet-mayak-class-supply-ship-5cf39f-1024.jpg",1.1,20);
    }
    public static void smallMaker(String input, double divisor, int iterations) throws IOException {
        //naam scheiden
        String sname = input.substring(input.lastIndexOf('/'));
        String name = sname.substring(1);
        String pextend = input.substring(input.lastIndexOf('.'));
        String extend = pextend.substring(1);
        String namewoext = name.substring(0,name.lastIndexOf('.'));
        //map maken
        double divisor0=divisor;
        String filepath = "/home/localadmin/Desktop/VISWDL/VISWDL/Testing/Examples/SSD/ObjectDetectionImportSSD/resizedimages/";
        File f = new File(filepath+ namewoext + "_div="+ divisor+"_it=" + iterations);
        if (f.mkdir() == true) {
            System.out.println("Directory has been created successfully");
        }
        else {
            System.out.println("Directory cannot be created");
        }
        BufferedImage img0 = ImageIO.read(new File(input));
        int width0 = img0.getWidth();
        double dnwidth0 = width0;
        int inwidth0 = (int) dnwidth0;
        int height0 = img0.getHeight();
        double dnheight0 = height0;
        int inheight0 = (int) dnheight0;
        File file0 = new File(filepath+ namewoext  + "_div="+ divisor+"_it=" + iterations+"/"+0+"_"+(inwidth0*inheight0)+pextend);
        ImageIO.write(img0,extend,file0);
        int height = img0.getHeight();
        int width = img0.getWidth();

        //fotos verwerken
        for(int i = 0; i<iterations;i++) {
            //BufferedImage img = ImageIO.read(new File(input))
            double dnwidth = width/divisor;
//            int inwidth = (int) dnwidth;
            width = (int) dnwidth;
            double dnheight = height/divisor;
//            int inheight = (int) dnheight;
            height = (int) dnheight;
            File file = new File(filepath+ namewoext  + "_div="+ divisor0+"_it=" + iterations+"/"+(i+1)+"_"+(width*height)+pextend);
            //input = "/home/localadmin/Desktop/VISWDL/Testing/Examples/TestImages/ImageSmallMaker/resizedimages/"+ namewoext + "_div="+ divisor+"_it=" + iterations+"/"+(i+1)+"_"+(inwidth*inheight)+pextend;
            ImageIO.write(resizeImage(img0,width,height),extend,file);
        }
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, originalImage.getType());
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}