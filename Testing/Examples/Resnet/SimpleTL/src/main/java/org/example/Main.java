package org.example;

import ai.djl.MalformedModelException;
import ai.djl.Model;
import ai.djl.basicdataset.cv.classification.AbstractImageFolder;
import ai.djl.basicdataset.cv.classification.Cifar10;
import ai.djl.basicdataset.cv.classification.ImageFolder;
import ai.djl.engine.Engine;
import ai.djl.modality.Classifications;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.transform.Normalize;
import ai.djl.modality.cv.transform.Resize;
import ai.djl.modality.cv.transform.ToTensor;
import ai.djl.ndarray.types.Shape;
import ai.djl.nn.Blocks;
import ai.djl.nn.SequentialBlock;
import ai.djl.nn.SymbolBlock;
import ai.djl.nn.core.Linear;
import ai.djl.repository.Repository;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.training.DefaultTrainingConfig;
import ai.djl.training.EasyTrain;
import ai.djl.training.Trainer;
import ai.djl.training.dataset.Batch;
import ai.djl.training.dataset.Dataset;
import ai.djl.training.evaluator.Accuracy;
import ai.djl.training.listener.TrainingListener;
import ai.djl.training.loss.Loss;
import ai.djl.training.util.ProgressBar;
import ai.djl.translate.Pipeline;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {
    public static void main(String[] args) throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        runApplication();
    }

    public static void runApplication() throws ModelNotFoundException, MalformedModelException, IOException, TranslateException {
        Criteria<Image, Classifications> criteria = Criteria.builder()
                .setTypes(Image.class, Classifications.class)
                .optProgress(new ProgressBar())
                .optArtifactId("resnet")
                .optFilter("layers", "50")
                .optFilter("flavor", "v1").build();
        Model model = criteria.loadModel();
        changeBlock(model, 1);

        int batchSize = 32;
        int limit = Integer.MAX_VALUE; // change this to a small value for a dry run
        // int limit = 160; // limit 160 records in the dataset for a dry run
        Pipeline pipeline = new Pipeline(
                new ToTensor(),
                new Normalize(new float[] {0.4914f, 0.4822f, 0.4465f}, new float[] {0.2023f, 0.1994f, 0.2010f}));
        Repository repository = Repository.newInstance("images", Paths.get("images"));
        ImageFolder trainDataset = createDataset(repository, batchSize);

        DefaultTrainingConfig config = new DefaultTrainingConfig(Loss.softmaxCrossEntropyLoss())
                //softmaxCrossEntropyLoss is a standard loss for classification problems
                .addEvaluator(new Accuracy()) // Use accuracy so we humans can understand how accurate the model is
                .optDevices(Engine.getInstance().getDevices(1)) // Limit your GPU, using more GPU actually will slow down coverging
                .addTrainingListeners(TrainingListener.Defaults.logging());

        // Now that we have our training configuration, we should create a new trainer for our model
        Trainer trainer = model.newTrainer(config);
        Train(trainer, trainDataset, model);
    }

    private static void changeBlock(Model model, int setOutput){
        SequentialBlock newBlock = new SequentialBlock();
        SymbolBlock block = (SymbolBlock) model.getBlock();
        block.removeLastBlock();
        newBlock.add(block);
        newBlock.add(Blocks.batchFlattenBlock());
        newBlock.add(Linear.builder().setUnits(setOutput).build());
        model.setBlock(newBlock);
    }
    private static ImageFolder createDataset(Repository repository, int batchSize) throws TranslateException, IOException {
        ImageFolder dataset = ImageFolder.builder()
                        .setRepository(repository)
                        .addTransform(new Resize(100,100))
                        .addTransform(new ToTensor())
                        .setSampling(batchSize, true)
                        .build();
        dataset.prepare();
        System.out.println(dataset.getSynset());
        return dataset;
    }
    private static void Train(Trainer trainer, ImageFolder trainDataset, Model model) throws TranslateException, IOException {
        int epoch = 10;
        Shape inputShape = new Shape(1, 3, 32, 32);
        trainer.initialize(inputShape);
        for (int i = 0; i < epoch; ++i) {
            int index = 0;
            for (Batch batch : trainer.iterateDataset(trainDataset)) {
                EasyTrain.trainBatch(trainer, batch);
                trainer.step();
                batch.close();
            }

            // reset training and validation evaluators at end of epoch
            trainer.notifyListeners(listener -> listener.onEpoch(trainer));
        }
        Path modelDir = Paths.get("build/resnet");
        Files.createDirectories(modelDir);

        model.setProperty("Epoch", String.valueOf(epoch));
        model.save(modelDir, "resnet");
    }
}