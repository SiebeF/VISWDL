package org.example;

import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.modality.cv.util.NDImageUtils;
import ai.djl.ndarray.NDArray;
import ai.djl.ndarray.NDManager;
import ai.djl.paddlepaddle.zoo.cv.imageclassification.PpWordRotateTranslator;
import ai.djl.paddlepaddle.zoo.cv.objectdetection.PpWordDetectionTranslator;
import ai.djl.paddlepaddle.zoo.cv.wordrecognition.PpWordRecognitionTranslator;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        //String url = "http://millenia.ph/wp-content/uploads/2012/04/02_dominia.jpg";
        String url = "https://images-global.nhst.tech/image/TXVvcGJoeFY5MC9hTUtYcGdJSnVLc1hkbVQydWJkdmpSSzRPWTBjKzJFST0=/nhst/binary/733a62f9a455ddbad23b8b261b50f21b";
        Image img = ImageFactory.getInstance().fromUrl(url);
        img.getWrappedImage();

        //--Word Detection model--//
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, DetectedObjects.class)
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/det_db.zip")
                .optTranslator(new PpWordDetectionTranslator(new ConcurrentHashMap<String, String>()))
                .build();
        ZooModel<Image, DetectedObjects> detectionModel = criteria.loadModel();
        Predictor<Image, DetectedObjects> detector = detectionModel.newPredictor();

        //draw bounding boxes around object
        DetectedObjects detectedObj = detector.predict(img);
        Image newImage = img.duplicate();
        newImage.drawBoundingBoxes(detectedObj);

        //save image
        Path resultPath = Paths.get("results/boundingBox.png");
        newImage.save(Files.newOutputStream(resultPath), "png");
        newImage.getWrappedImage();

        List<DetectedObjects.DetectedObject> boxes = detectedObj.items();
        Image sample = getSubImage(img, boxes.get(4).getBoundingBox());

        Path resultPath1 = Paths.get("results/wrappedImage.png");
        sample.save(Files.newOutputStream(resultPath1), "png");
        sample.getWrappedImage();

        //--Word Direction model--//
        Criteria<Image, Classifications> criteria2 = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, Classifications.class)
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/cls.zip")
                .optTranslator(new PpWordRotateTranslator())
                .build();
        ZooModel<Image, Classifications> rotateModel = criteria2.loadModel();
        Predictor<Image, Classifications> rotateClassifier = rotateModel.newPredictor();

        //--Word Recognition model--//
        Criteria<Image, String> criteria3 = Criteria.builder()
                .optEngine("PaddlePaddle")
                .setTypes(Image.class, String.class)
                .optModelUrls("https://resources.djl.ai/test-models/paddleOCR/mobile/rec_crnn.zip")
                .optTranslator(new PpWordRecognitionTranslator())
                .build();
        ZooModel<Image, String> recognitionModel = criteria3.loadModel();
        Predictor<Image, String> recognizer = recognitionModel.newPredictor();

        System.out.println(rotateClassifier.predict(sample));
        System.out.println(recognizer.predict(sample));

        //full image
        List<String> names = new ArrayList<>();
        List<Double> prob = new ArrayList<>();
        List<BoundingBox> rect = new ArrayList<>();

        for (DetectedObjects.DetectedObject box : boxes) {
            Image subImg = getSubImage(img, box.getBoundingBox());
            if (subImg.getHeight() * 1.0 / subImg.getWidth() > 1.5) {
                subImg = rotateImg(subImg);
            }
            Classifications.Classification result = rotateClassifier.predict(subImg).best();
            if ("Rotate".equals(result.getClassName()) && result.getProbability() > 0.8) {
                subImg = rotateImg(subImg);
            }
            String name = recognizer.predict(subImg);
            names.add(name);
            prob.add(-1.0);
            rect.add(box.getBoundingBox());
        }
        newImage.drawBoundingBoxes(new DetectedObjects(names, prob, rect));
        Path resultPath2 = Paths.get("results/boundingBox.png");
        newImage.save(Files.newOutputStream(resultPath2), "png");
    }
    //crops the image and extract the word block
    public static Image getSubImage(Image img, BoundingBox box){
        Rectangle rect = box.getBounds();
        double[] extended = extendRect(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight());
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered = {
                (int) (extended[0] * width),
                (int) (extended[1] * height),
                (int) (extended[2] * width),
                (int) (extended[3] * height)
        };
        return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }

    //extends boundingboxes
    public static double[] extendRect(double xmin, double ymin, double width, double height) {
        double centerx = xmin + width / 2;
        double centery = ymin + height / 2;
        if (width > height) {
            width += height * 2.0;
            height *= 3.0;
        } else {
            height += width * 2.0;
            width *= 3.0;
        }
        double newX = centerx - width / 2 < 0 ? 0 : centerx - width / 2;
        double newY = centery - height / 2 < 0 ? 0 : centery - height / 2;
        double newWidth = newX + width > 1 ? 1 - newX : width;
        double newHeight = newY + height > 1 ? 1 - newY : height;
        return new double[] {newX, newY, newWidth, newHeight};
    }

    public static Image rotateImg(Image image){
        try(NDManager manager = NDManager.newBaseManager()){
            NDArray rotated = NDImageUtils.rotate90(image.toNDArray(manager), 1);
            return ImageFactory.getInstance().fromNDArray(rotated);
        }
    }
}