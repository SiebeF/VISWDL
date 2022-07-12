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
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import au.com.bytecode.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

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

<<<<<<< HEAD
        Path img_path = Paths.get("images/737207_2.jpg");
=======
        Path img_path = Paths.get("images/BootMetWindmolens.jpeg");
>>>>>>> d525a7f3261b120d6a4c6e7a1401902d9f07ca10
        Image img = ImageFactory.getInstance().fromFile(img_path);

        //load model
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);

        //predict model
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        DetectedObjects detected_objects = predictor.predict(img);

        model.close();
        System.out.println(detected_objects);

        img.drawBoundingBoxes(detected_objects);
        Path resultPath = Paths.get("results/result.png");
        img.save(Files.newOutputStream(resultPath), "png");

        //save data to csv
        double mAP = detected_objects.best().getProbability();
        int imageSize = img.getHeight()*img.getWidth();
        toCSV(mAP, imageSize);
    }

    private static void toCSV(double mAP, int imageSize) throws IOException {
        File file = new File("results/results.csv");

        try{
            FileWriter outputfile = new FileWriter(file, true); //true: file niet overwriten, toevoegen
            CSVWriter writer = new CSVWriter(outputfile);
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {String.valueOf(mAP), String.valueOf(imageSize)});
            writer.writeAll(data);

            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}