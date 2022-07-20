package SSD;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.repository.Repository;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ModelZoo;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.loss.Loss;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Paths;

public class TrainingResults {
    public static void main(String[] args) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        Criteria<Image, DetectedObjects> criteria = Criteria.builder()
                .optApplication(Application.CV.OBJECT_DETECTION) //--> ssd
                .setTypes(Image.class, DetectedObjects.class)
                .optFilter("backbone", "resnet50")
                .build();

        //load model
        ZooModel<Image, DetectedObjects> model = ModelZoo.loadModel(criteria);
        getResults(model);
        model.close();
    }
    public static void getResults(Model model) throws TranslateException, IOException {
        int batchSize = 5;
        Repository repo = Repository.newInstance("images", Paths.get("images"));
        ImageFolder dataset = ImageFolder.builder()
                .setRepository(repo)
                .addTransform(new Resize(100,100))
                .addTransform(new ToTensor())
                .setSampling(batchSize, true)
                .build();
        dataset.prepare();

        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                .addEvaluator(new Accuracy());

        Trainer trainer = model.newTrainer(config);
        EasyTrain.evaluateDataset(trainer, dataset);
        System.out.println(trainer.getTrainingResult());
    }
}
