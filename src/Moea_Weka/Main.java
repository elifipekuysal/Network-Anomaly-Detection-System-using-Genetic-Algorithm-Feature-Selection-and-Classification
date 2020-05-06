package Moea_Weka;

import org.moeaframework.Executor;
import org.moeaframework.core.NondominatedPopulation;
import org.moeaframework.core.Solution;
import org.moeaframework.core.variable.EncodingUtils;

import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.bayes.NaiveBayes;
import weka.core.*;
import weka.core.converters.ConverterUtils.DataSource;


import static Moea_Weka.MoeaWeka.algorithm;


public class Main
{
    public static void main(String[] args)
    {
        // train();
        test();
    }

    public static void train() {
        NondominatedPopulation result = new Executor()
                .withAlgorithm(algorithm)  // NSGA-II, PAES, eMOEA
                .withProblemClass(MoeaWeka.class)
                .withMaxEvaluations(10000)
                .withProperty("populationSize", 1000)
                .distributeOnAllCores()
                .run();

        for (Solution solution : result)
        {
            System.out.println("*********");
            boolean[] b  = EncodingUtils.getBinary(solution.getVariable(0));
            for(int i=0;i<MoeaWeka.TotalFeatureNumber;i++)
            {
                System.out.print(b[i] +" ");
            }
            System.out.println();
            System.out.println(solution.getObjective(0));  // 1/rmse
            System.out.println(solution.getObjective(1));  // Removed attribute number

            MoeaWeka.printResultsTrain(solution);
        }
    }

    public static void test() {
        Test test = new Test();
        Boolean[][] chromosomes = test.getChromosomesFromFile();
        Boolean[] chromosome = new Boolean[Test.geneNumber];

        int falseNum = 0;

        int nsgaII = 11;
        int paes = 9;
        int emoea = 13;

        switch (algorithm) {
            case "NSGA-II":

                // NSGA-II -> 0 - 10
                for(int i = 0; i < nsgaII; i++) {
                    for (int j = 0; j < Test.geneNumber; j++) {
                        chromosome[j] = chromosomes[i][j];

                        if(chromosome[j] == false) {
                            falseNum++;
                        }
                    }

                    printChromosome(chromosome);
                    System.out.println("\n" + falseNum);
                    falseNum = 0;

                    MoeaWeka.classificationTest(chromosome);

                    System.out.println("********************************");
                }
                break;

            case "PAES":

                // PAES -> 11 - 19
                for(int i = nsgaII; i < (paes + nsgaII); i++) {
                    for (int j = 0; j < Test.geneNumber; j++) {
                        chromosome[j] = chromosomes[i][j];

                        if(chromosome[j] == false) {
                            falseNum++;
                        }
                    }

                    printChromosome(chromosome);
                    System.out.println("\n" + falseNum);
                    falseNum = 0;

                    MoeaWeka.classificationTest(chromosome);

                    System.out.println("********************************");
                }
                break;

            case "eMOEA":

                // eMOEA -> 20 - 32
                for(int i = (paes + nsgaII); i < (paes + nsgaII + emoea); i++) {
                    for (int j = 0; j < Test.geneNumber; j++) {
                        chromosome[j] = chromosomes[i][j];

                        if(chromosome[j] == false) {
                            falseNum++;
                        }
                    }

                    printChromosome(chromosome);
                    System.out.println("\n" + falseNum);
                    falseNum = 0;

                    MoeaWeka.classificationTest(chromosome);

                    System.out.println("********************************");
                }
                break;

            default:
                break;
        }

        /*  ????????????????????????????????????????????
        for(String[] chromosomeTest : chromosomes) {
            for(int i = 0; i > Test.geneNumber; i++) {
                System.out.println(chromosomeTest[i]);
            }
        }
        ????????????????????????????????????????????  */
    }

    public static void printChromosome (Boolean[] chromosome) {
        for (int i = 0; i < chromosome.length; i++) {
            System.out.print(chromosome[i] + " ");
        }
    }
}