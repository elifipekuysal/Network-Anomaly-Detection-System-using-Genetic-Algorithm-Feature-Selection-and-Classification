package Moea_Weka;

import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;
import org.moeaframework.problem.AbstractProblem;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.SMO;
import weka.classifiers.functions.SimpleLogistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Bagging;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Remove;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;


public class MoeaWeka extends AbstractProblem
{
    // NSGA-II, PAES, eMOEA
    public static String algorithm = "NSGA-II";

    // J48, NaiveBayes, SMO, SimpleLogistic, RandomForest, AdaBoostM1, Bagging
    public static String classifier = "J48";

    public static String datasetTrain ="KDDCup99Train.arff";  // 41 + 1
    public static String datasetTest ="KDDCup99Test-labeled.arff";  // 41 + 1
    public static int TotalFeatureNumber = 41;

    private static int removedAttributeNumber;

    public MoeaWeka()
    {
        super(1, 2, 1);
    }

    @Override
    public void evaluate(Solution solution)
    {
        double penalty = 1e1;

        int numFeatures = countFeatures(solution);

        double rmse = classification(solution);


        // Meta Classifier
        // Remove rmFilter =  featureSelection(solution);
        // double rmse = classificationMeta(solution, rmFilter);

        solution.setObjective(0, 1.0/rmse);  // RMSE = Root Mean Squared Error
        solution.setObjective(1, TotalFeatureNumber-numFeatures);

        solution.setConstraint(0, (numFeatures>=5.0) ? 0.0 : penalty);
    }

    @Override
    public Solution newSolution()
    {
        Solution solution = new Solution(1, 2, 1);
//		 for(int i=0;i<FeatureNumber;i++)
//		 {
//			 solution.setVariable(i, EncodingUtils.newReal(1.0, 1.0));
//		 }

        solution.setVariable(0, EncodingUtils.newBinary(TotalFeatureNumber));

        return solution;
    }

    int countFeatures(Solution solution)
    {
        boolean[] b  = EncodingUtils.getBinary(solution.getVariable(0));
        int count = 0;

        for(int i=0;i<MoeaWeka.TotalFeatureNumber;i++)
        {
            if(b[i]==true)
                count++;
        }

        return count;
    }

    public double classification(Solution solution)
    {
        Instances data = null;
        //double accuracy = 0.0;
        double rmse = 0.0;

        try
        {
            DataSource source = new DataSource(datasetTrain);
            //DataSource source = new DataSource(datasetTest);

            data = source.getDataSet();


            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // remove attributes
            deleteAttributes(solution, data);

            //Implement classifiers
            Classifier cls = selectClassifier(classifier);

            long startTime = System.nanoTime();

            cls.buildClassifier(data);

            long stopTime = System.nanoTime();

            long delayNS = stopTime - startTime;

            Evaluation eval;

            eval = new Evaluation(data);
            eval.evaluateModel(cls, data);
            rmse = eval.rootMeanSquaredError();

            //System.out.println(eval.toSummaryString("\nResults\n======\n", true));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return rmse;
    }

    public static void deleteAttributes(Solution solution, Instances data) {
        int removedAttr = 0;

        // remove attributes
        boolean[] b  = EncodingUtils.getBinary(solution.getVariable(0));

        for(int i=0; i < TotalFeatureNumber; i++)
        {
            System.out.print(b[i] + " ");
            if(b[i]==false)
            {
                // data.deleteAttributeAt(i - removedAttr);
                removedAttr++;
            }
        }
        System.out.println("\n" + removedAttr);
    }

    public static void printResultsTrain(Solution solution)
    {
        Instances data = null;
        double rmse = 0.0;

        try
        {
            DataSource source = new DataSource(datasetTrain);
            data = source.getDataSet();

            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            // remove attributes
            deleteAttributes(solution, data);

            System.out.println("Number of attributes: " + data.numAttributes());

            //Implement classifiers
            Classifier cls = selectClassifier(classifier);

            long startTime = System.nanoTime();

            cls.buildClassifier(data);

            long stopTime = System.nanoTime();

            long delayNS = stopTime - startTime;

            Evaluation eval;
            eval = new Evaluation(data);
            eval.evaluateModel(cls, data);

            System.out.println(eval.toSummaryString("\nResults\n======\n", true));
            writeFileTrain(solution, eval, data, delayNS);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void writeFileTrain(Solution solution, Evaluation eval, Instances data, long delayNS) {
        BufferedWriter outputResult = null;
        BufferedWriter outputBest = null;

        try {
            File fileResult = new File("C:\\Users\\elifi\\Desktop\\NADSProject\\Results\\trainResults.csv");
            File fileBest = new File("C:\\Users\\elifi\\Desktop\\NADSProject\\Results\\bestChromosomes.txt");

            outputResult = new BufferedWriter(new FileWriter(fileResult, true));
            outputBest = new BufferedWriter(new FileWriter(fileBest, true));

            boolean[] b  = EncodingUtils.getBinary(solution.getVariable(0));
            for(int i=0 ; i<TotalFeatureNumber ; i++)
            {
                outputBest.write(b[i] +" ");
            }
            outputBest.newLine();

            //"algorithm","classifier","num_of_attr","removed_attr_num","correctly_classified","accuracy","1/rmse","delay"
            outputResult.write(algorithm + ",");
            outputResult.write(classifier + ",");
            outputResult.write(data.numAttributes() + ",");
            outputResult.write((int) solution.getObjective(1) + ",");
            outputResult.write(eval.correct() + ",");
            outputResult.write((eval.correct() * 100) / eval.numInstances() + ",");
            outputResult.write(solution.getObjective(0) + ",");
            outputResult.write("" + TimeUnit.MILLISECONDS.convert(delayNS, TimeUnit.NANOSECONDS));
            outputResult.newLine();

        } catch (IOException e) {

            e.printStackTrace();
        }
        finally
        {
            try {
                outputResult.close();
                outputBest.close();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    // **************************************************
    // TEST FUNCTIONS

    public static void classificationTest(Boolean[] chromosome)
    {
        Instances trainData = null;
        Instances testData = null;
        double rmse = 0.0;

        try
        {
            DataSource sourceTrain = new DataSource(datasetTrain);
            trainData = sourceTrain.getDataSet();

            if (trainData.classIndex() == -1)
                trainData.setClassIndex(trainData.numAttributes() - 1);

            DataSource sourceTest = new DataSource(datasetTest);
            testData = sourceTest.getDataSet();

            if (testData.classIndex() == -1)
                testData.setClassIndex(testData.numAttributes() - 1);

            // remove attributes
            deleteAttributesFromChromosome(chromosome, trainData, testData);

            System.out.println("Number of attributes: " + trainData.numAttributes());

            //Implement classifiers
            Classifier cls = selectClassifier(classifier);

            long startTime = System.nanoTime();

            cls.buildClassifier(trainData);

            long stopTime = System.nanoTime();

            long delayNS = stopTime - startTime;


            Evaluation eval = new Evaluation(trainData);
            eval.evaluateModel(cls, testData);

            rmse = eval.rootMeanSquaredError();

            System.out.println(eval.toSummaryString("\nResults\n======\n", true));

            writeFileTest(chromosome, eval, trainData, rmse, delayNS);
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static void deleteAttributesFromChromosome(Boolean[] chromosome, Instances dataTrain, Instances dataTest) {
        int removedAttr = 0;

        for(int i=0; i < chromosome.length; i++)
        {
            // System.out.print(chromosome[i] + " ");
            if(chromosome[i] == false)
            {
                dataTrain.deleteAttributeAt(i - removedAttr);
                dataTest.deleteAttributeAt(i - removedAttr);
                removedAttr++;
            }
        }
        // System.out.println("\n" + removedAttr);

        removedAttributeNumber = removedAttr;
    }

    public static void writeFileTest(Boolean[] chromosome, Evaluation eval, Instances data, double rmse, long delayNS) {
        BufferedWriter outputTest = null;

        try {
            File fileResult = new File("C:\\Users\\elifi\\Desktop\\NADSProject\\Results\\testResults.csv");
            outputTest = new BufferedWriter(new FileWriter(fileResult, true));

            //"algorithm","classifier","num_of_attr","removed_attr_num","correctly_classified","accuracy","1/rmse","delay"
            outputTest.write(algorithm + ",");
            outputTest.write(classifier + ",");
            outputTest.write(data.numAttributes() + ",");
            outputTest.write(removedAttributeNumber + ",");
            outputTest.write((int) eval.correct() + ",");
            outputTest.write((eval.correct() * 100) / eval.numInstances() + ",");
            outputTest.write((1.0 / rmse) + ",");
            outputTest.write("" + TimeUnit.MILLISECONDS.convert(delayNS, TimeUnit.NANOSECONDS));
            outputTest.newLine();

        } catch (IOException e) {

            e.printStackTrace();
        }
        finally
        {
            try {
                outputTest.close();

            } catch (IOException e) {

                e.printStackTrace();
            }
        }
    }

    public static Classifier selectClassifier(String classifier) {
        Classifier cls;

        // J48, NaiveBayes, SMO, SimpleLogistic, RandomForest, AdaBoostM1, Bagging
        switch(classifier) {
            case "J48":
                cls = new J48();
                break;
            case "NaiveBayes":
                cls = new NaiveBayes();
                break;
            case "SMO":
                cls = new SMO();
                break;
            case "SimpleLogistic":
                cls = new SimpleLogistic();
                break;
            case "RandomForest":
                cls = new RandomForest();
                break;
            case "AdaBoostM1":
                cls = new AdaBoostM1();
                break;
            case "Bagging":
                cls = new Bagging();
                break;
            default:
                cls = null;
        }

        return cls;
    }
}


/*
    public double classificationMeta(Solution solution, Remove rmFilter)
    {
        Instances data = null;
        double rmse = 0.0;

        try
        {
            DataSource source = new DataSource(datasetTrain);
            //DataSource source = new DataSource(datasetTest);

            data = source.getDataSet();


            if (data.classIndex() == -1)
                data.setClassIndex(data.numAttributes() - 1);

            //System.out.println(data.numAttributes());

            *//*for(int i=1; i <= data.numAttributes()-1; i++) {
                System.out.println(data.attribute(i));
            }*//*

            // remove attributes
            deleteAttributes(solution, data);

            //System.out.println("*************************************");

           *//* for(int i=1; i <= data.numAttributes()-1; i++) {
                System.out.println(data.attribute(i));
            }*//*

            //Implement classifiers
            Classifier cls = new J48();
            // Classifier cls = new NaiveBayes();
            // Classifier cls = new MultilayerPerceptron();
            // Classifier cls = new RandomForest();
            // Classifier cls = new AdaBoostM1();
            // Classifier cls = new Bagging();

            cls.buildClassifier(data);

            //!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
            //Setting the attribute size of the train and test datasets
            data.setClassIndex(countFeatures(solution));
            // dataTest.setClassIndex(countFeatures(solution));

            //Create FilteredClassifier for filtered classification
            FilteredClassifier fc = new FilteredClassifier();

            //Set rmFilter's properties to the FilteredClassifier object
            fc.setFilter(rmFilter);

            //Set classifiers to the FilteredClassifier object to do classification with selected attributes
            fc.setClassifier(cls);

            //Do training with train instance by using the rmFilter for filtering the features which has "0" value
            fc.buildClassifier(data);

            *//*float wrongPred = 0;

            //test.numInstances()
            for (int i = 0; i < TotalFeatureNumber; i++) {
                double pred = fc.classifyInstance(data.instance(i));     //test.instance(i)
                String labelAct = data.instance(i).toString(data.classIndex());
                //String labelPred = test.classAttribute().value((int) pred);     //Prediction for test dataset's instances
                String labelPred = data.classAttribute().value((int) pred);

                //System.out.print("Actual: " + labelAct);
                //System.out.print("  ->->->  ");
                //System.out.println("Predicted: " + labelPred);

                if (!(labelAct.equals(labelPred))) {
                    wrongPred++;
                }
            }

            //Accuracy rate
            accuracy= ((TotalFeatureNumber - wrongPred) * 100) / TotalFeatureNumber;
            //Print accuracy rate
            //System.out.println("Accuracy rate: %" + accuracy);*//*


            Evaluation eval;

            eval = new Evaluation(data);
            eval.evaluateModel(cls, data);
            rmse = eval.rootMeanSquaredError();
            //System.out.println(eval.toSummaryString("\nResults\n======\n", true));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //return accuracy;
        return rmse;
    }

    public static Remove featureSelection(Solution solution) {
        //Filter for removing specified attribute
        Remove rmFilter = new Remove();

        //Set counter for holding the removed attribute number
        int aRemoved = 0;

        // remove attributes
        boolean[] b  = EncodingUtils.getBinary(solution.getVariable(0));

        int tempFeatureNumber = MoeaWeka.TotalFeatureNumber;

        //If the value is 0 in the specified index then remove this attribute from the dataset
        // it's important to iterate from last to first, because when we remove
        // an instance, the rest shifts by one position.
        for (int i = tempFeatureNumber - 1; 4 <= i; i--) {  //i is assigned with the value sampleSize-3 to protect the label attribute from removal
            if (b[i]==false) {
                //System.out.println((i+1) + ". attribute is removed.");
                //Remove the attribute at count index
                rmFilter.setAttributeIndices(""+(i+1)+""); //Remove object is starting from the 1st index
                tempFeatureNumber--;
            }
        }

        return rmFilter;
    }
    */