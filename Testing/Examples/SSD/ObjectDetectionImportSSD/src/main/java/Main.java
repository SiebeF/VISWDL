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
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

public class Main {
    //final layers wegnemen resnet 50 --> boat/no boat
    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
        File[] files = new File("images").listFiles();
        getDirectory(files);
    }

    private static Criteria<Image, DetectedObjects> getCriteria(){
        return (Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION) //--> ssd
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "resnet50")
                .optProgress(new ProgressBar())
                .build());
//                .optApplication(Application.CV.OBJECT_DETECTION)
//                .setTypes(Image.class, DetectedObjects.class)
//                .optFilter("backbone", "vgg16")
//                .optFilter("dataset", "voc")
//                .optEngine("MXNet")
//                .optProgress(new ProgressBar())
//                .build();
    }

    //put important data in csv file
    private static void toCSV(double mAP, int imageWidth, int imageHeight, String className) throws IOException {
        File file = new File("results/results.csv");

        try{
            FileWriter outputfile = new FileWriter(file, true); //true: file niet overwriten, toevoegen
            CSVWriter writer = new CSVWriter(outputfile);
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {String.valueOf(mAP), String.valueOf(imageWidth), String.valueOf(imageWidth), className});
            writer.writeAll(data);

            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //use the loaded model on the image
    private static void makePrediction(Image img, String imageName) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        //load model
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(getCriteria());

        //predict model
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        DetectedObjects detected_objects = predictor.predict(img);
        model.close();

        System.out.println(detected_objects);

        img.drawBoundingBoxes(detected_objects);
        String nameWithoutExtension = FilenameUtils.removeExtension(imageName);
        Path resultPath = Paths.get("results/result_"+ nameWithoutExtension+".png");
        img.save(Files.newOutputStream(resultPath), "png");

        //save data to csv
        try {
            double mAP = detected_objects.best().getProbability();
            String className = detected_objects.best().getClassName();
            int imageSize = img.getHeight() * img.getWidth();
            toCSV(mAP, img.getWidth(), img.getHeight(), className);
        }
        catch(final Exception e){
            e.printStackTrace();
            toCSV(0,0,0,null);
        }
    }

    //create a list of all images
    private static void getDirectory(File[] files) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        //src: https://www.geeksforgeeks.org/java-program-to-traverse-in-a-directory/

        for (File filename : files) {
            if (filename.isDirectory()) {
                getDirectory(Objects.requireNonNull(filename.listFiles()));
            } else if(FileNameUtils.getExtension(filename.getName()).equals("jpg")){ //make sure file is a jpg image
                Path img_path = Paths.get(filename.getPath());
                Image img = ImageFactory.getInstance().fromFile(img_path);
                System.out.println(filename.getName());
                makePrediction(img, filename.getName());
            }
        }
    }
}