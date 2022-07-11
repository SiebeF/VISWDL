/*
 * src: //https://www.youtube.com/watch?v=bz4sBv8LTGM
 * */

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
    //final layers wegnemen resnet 50 --> boat/no boat
    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
//                .optArtifactId("ssd")
                .optFilter("backbone", "resnet50")
                .optProgress(new ProgressBar())
                .build();
//                .optApplication(Application.CV.OBJECT_DETECTION)
//                .setTypes(Image.class, DetectedObjects.class)
//                .optFilter("backbone", "vgg16")
//                .optFilter("dataset", "voc")
//                .optEngine("MXNet")
//                .optProgress(new ProgressBar())
//                .build();

        Path img_path = Paths.get("images/BootMetWindmolens.jpeg");
        Image img = ImageFactory.getInstance().fromFile(img_path);

        //load image
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);

        //predict model
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        DetectedObjects detected_objects = predictor.predict(img);

        model.close();
        System.out.println(detected_objects);

        img.drawBoundingBoxes(detected_objects);
        Path resultPath = Paths.get("results/result.png");
        img.save(Files.newOutputStream(resultPath), "png");
    }
}