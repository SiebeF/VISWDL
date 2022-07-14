package SSD;
/*
 * src: //https://www.youtube.com/watch?v=bz4sBv8LTGM
 * */

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Main {
    //final layers wegnemen resnet 50 --> boat/no boat
    public static void main(String[] args) throws IOException, ModelNotFoundException, MalformedModelException, TranslateException {
                Criteria<Image, DetectedObjects> criteria = Criteria.builder()
//                .optApplication(Application.CV.OBJECT_DETECTION) //--> ssd
//                .setTypes(Image.class, DetectedObjects.class)
//                .optFilter("backbone", "mobilenet1.0")
//                .build();

                .optApplication(Application.CV.OBJECT_DETECTION)
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "vgg16")
                .optFilter("dataset", "voc")
                .optEngine("MXNet")
                .optProgress(new ProgressBar())
                .build();
        //load model
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);

        //predict model
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        File[] files = new File("resizedimages").listFiles();
        runApplication(model, predictor ,files);
        model.close();
    }

    //put important data in csv file
    private static void toCSV(String filename, double mAP, int imageWidth, int imageHeight, String className) throws IOException {
        File file = new File("results/resizedImages.csv");

        try{
            FileWriter outputfile = new FileWriter(file, true); //true: file niet overwriten, toevoegen
            CSVWriter writer = new CSVWriter(outputfile);
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {filename, String.valueOf(mAP), String.valueOf(imageWidth), String.valueOf(imageHeight), className});
            writer.writeAll(data);

            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }

    //use the loaded model on the image
    private static void makePrediction(ZooModel<Image, DetectedObjects> model,Predictor<Image, DetectedObjects> predictor, Image img, String imageName) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        DetectedObjects detected_objects = predictor.predict(img);
//        model.close();

        System.out.println(detected_objects);

        img.drawBoundingBoxes(detected_objects);

        String nameWithoutExtension = FilenameUtils.removeExtension(imageName);
//        Path resultPath = Paths.get("results/result_"+ nameWithoutExtension+".png");
//        img.save(Files.newOutputStream(resultPath), "png");
//        Object image_obj = img.getWrappedImage();
//        System.out.println(image_obj);
        //save data to csv
        try {
            double probability = detected_objects.best().getProbability();
            String className = detected_objects.best().getClassName();
            int imageSize = img.getHeight() * img.getWidth();
            toCSV(nameWithoutExtension,probability, img.getWidth(), img.getHeight(), className);
        }
        catch(final Exception e){
            e.printStackTrace();
            toCSV(nameWithoutExtension,0,img.getWidth(),img.getHeight(),null);
        }
    }

    //create a list of all images
    private static void runApplication(ZooModel<Image, DetectedObjects> model,Predictor<Image, DetectedObjects> predictor, File[] files) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        //src: https://www.geeksforgeeks.org/java-program-to-traverse-in-a-directory/

        for (File filename : files) {
            if (filename.isDirectory()) {
                runApplication(model, predictor ,filename.listFiles());
            } else if(FileNameUtils.getExtension(filename.getName()).equals("png") || FileNameUtils.getExtension(filename.getName()).equals("jpg")){ //make sure file is a jpg image
                Path img_path = Paths.get(filename.getPath());
                Image img = ImageFactory.getInstance().fromFile(img_path);
                System.out.println(filename.getName());
                makePrediction(model, predictor, img,  filename.getName());
            }
        }
    }
}