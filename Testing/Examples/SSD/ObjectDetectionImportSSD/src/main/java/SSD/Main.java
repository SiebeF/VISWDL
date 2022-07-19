package SSD;
/*
 * src: //https://www.youtube.com/watch?v=bz4sBv8LTGM
 * */

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.*;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.TranslateException;
import au.com.bytecode.opencsv.CSVWriter;
import org.apache.commons.compress.utils.FileNameUtils;
import org.apache.commons.io.FilenameUtils;
import org.tensorflow.op.linalg.Det;

import javax.swing.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    //final layers wegnemen resnet 50 --> boat/no boat
    public static String modelName;
    public static String dataset;
    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Model (resnet50 - vgg16 - mobilenet1.0): ");
        modelName = sc.nextLine();
        System.out.print("Dataset: ");
        dataset = sc.nextLine();
        System.out.print("Image Folder: ");
        String folderName = sc.nextLine();

        //clear file
        File file = new File("results/"+modelName+"_"+dataset+".csv");
        PrintWriter writer = new PrintWriter(file);
        writer.print("");
        writer.close();

        run(modelName, dataset, folderName);
    }

    //put important data in csv file
    private static void toCSV(String filename, double mAP, int imageWidth, int imageHeight, String className, String filepath) throws IOException {
        File file = new File("results/"+modelName+"_"+dataset+".csv");
        try{
            FileWriter outputfile = new FileWriter(file, true); //true: file niet overwriten, toevoegen
            CSVWriter writer = new CSVWriter(outputfile);
            List<String[]> data = new ArrayList<String[]>();
            data.add(new String[] {filename, String.valueOf(mAP), String.valueOf(imageWidth), String.valueOf(imageHeight), className, filepath});
            writer.writeAll(data);

            writer.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
    private static Criteria<Image, DetectedObjects> getCriteria(String model, String dataset) throws Exception {
        if (model.equals("resnet50") || model.equals("mobilenet1.0")){
            return Criteria.builder()
                    .optApplication(Application.CV.OBJECT_DETECTION) //--> ssd
                    .setTypes(Image.class, DetectedObjects.class)
                    .optFilter("backbone", model)
                    .build();
        }
        else if (model.equals("vgg16")){
            return Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION)
                    .setTypes(Image.class, DetectedObjects.class)
                    .optFilter("backbone", "vgg16")
                    .optFilter("dataset", dataset)
                    .optEngine("MXNet")
                    .optProgress(new ProgressBar())
                    .build();
        }
        else{
            throw new Exception("Invalid model");
        }
    }
    //use the loaded model on the image
    private static void makePrediction(ZooModel<Image, DetectedObjects> model,Predictor<Image, DetectedObjects> predictor, Image img, String imageName, String filepath) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        DetectedObjects detected_objects = predictor.predict(img);
//        model.close();

        System.out.println(detected_objects);

        img.drawBoundingBoxes(detected_objects);

        String nameWithoutExtension = FilenameUtils.removeExtension(imageName);
        Path resultPath = Paths.get("resultImages/result_"+ nameWithoutExtension+".png");
        img.save(Files.newOutputStream(resultPath), "png");
//        Object image_obj = img.getWrappedImage();
//        System.out.println(image_obj);
        //save data to csv
        if(detected_objects.getNumberOfObjects() == 0){
            toCSV(nameWithoutExtension,0,img.getWidth(),img.getHeight(),null, filepath);
        }
        else{
            int objectAmount = detected_objects.getNumberOfObjects();
            int bestIndex = 0;
            double probability = 0;
            String className = "";
            boolean boatDetected = false;
            Image croppedImg = null;
            List<DetectedObjects.DetectedObject> boxes = detected_objects.items();

            for(int i = 0; i < objectAmount; i++){
                //try to take the best probability of a detected boat
                if(detected_objects.item(i).getClassName().equals("boat")){
                    probability = detected_objects.item(i).getProbability();
                    className = detected_objects.item(i).getClassName();
                    croppedImg = getSubImage(img, boxes.get(i).getBoundingBox());
                    boatDetected = true;
                } else if (!boatDetected && i == (objectAmount-1)) { // if there was no boat detected return the best result
                    probability = detected_objects.best().getProbability();
                    className = detected_objects.best().getClassName();
                    croppedImg = getSubImage(img, boxes.get(i).getBoundingBox());
                }
            }

//            Path resultPath1 = Paths.get("resultImages/cropped_Img.png");
//            croppedImg.save(Files.newOutputStream(resultPath1), "png");

            toCSV(nameWithoutExtension,probability, img.getWidth(), img.getHeight(), className, filepath);
        }
    }

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

    //create a list of all images
    private static List<String> createPathList(List<String> paths, File[] files) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        //src: https://www.geeksforgeeks.org/java-program-to-traverse-in-a-directory/
        for (File filename : files) {
            if (filename.isDirectory()) {
                createPathList(paths, filename.listFiles());
            } else if(FileNameUtils.getExtension(filename.getName()).equals("png") || FileNameUtils.getExtension(filename.getName()).equals("jpg")|| FileNameUtils.getExtension(filename.getName()).equals("jpeg")){ //make sure file is a jpg image
                paths.add(filename.getPath());
            }
        }
        return paths;
    }

    private static void run(String modelName, String dataset, String folderName) throws Exception {
        Criteria<Image, DetectedObjects> criteria = getCriteria(modelName, dataset);
        //load model
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);

        //predict model
        Predictor<Image, DetectedObjects> predictor = model.newPredictor();
        File[] files = new File(folderName).listFiles();
        List<String> pathList = new ArrayList<>();
        pathList = createPathList(pathList, files);
        for (String path : pathList){
            Path imgPath = Paths.get(path);
            File file = new File(path);
            Image img = ImageFactory.getInstance().fromFile(imgPath);
            //String type = path.substring(path.indexOf("/")+1, path.lastIndexOf("/") );
            String type = "histogram eq.";
            makePrediction(model, predictor, img, file.getName(), type);
        }
        model.close();
    }
}