
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.Buffer;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

public class ImageResizer {
    public static void main(String[] args) throws IOException {
        imageResizer("images/airplane1.png");
    }
    private static void imageResizer(String input) throws IOException {
       String sname = input.substring(input.lastIndexOf('/'));
       String name = sname.substring(1);
       String pextend = input.substring(input.lastIndexOf('.'));
       String extend = pextend.substring(1);

       BufferedImage img = ImageIO.read(new File(input));
       File file = new File("resizedimages/"+name);
       ImageIO.write(resizeImage(img,32,32),extend,file);
    }

    private static BufferedImage resizeImage(BufferedImage originalImage, int targetWidth, int targetHeight) throws IOException {
        BufferedImage resizedImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics2D = resizedImage.createGraphics();
        graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null);
        graphics2D.dispose();
        return resizedImage;
    }
}
