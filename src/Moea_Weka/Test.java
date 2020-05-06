package Moea_Weka;

import jmetal.encodings.variable.Binary;
import org.moeaframework.core.Solution;
import org.moeaframework.core.Variable;
import org.moeaframework.core.variable.BinaryVariable;
import org.moeaframework.core.variable.EncodingUtils;
import weka.core.Instances;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Scanner;

import static Moea_Weka.MoeaWeka.TotalFeatureNumber;


public class Test {

    private static String fileBest = "C:\\Users\\elifi\\Desktop\\NADSProject\\Results\\bestChromosomes.txt";

    // NSGA-II -> 11
    // PAES -> 9
    // eMOEA -> 13
    protected static int chromosomeNumber = 33;
    protected static int geneNumber = MoeaWeka.TotalFeatureNumber;
    private static Boolean[][] chromosomes = new Boolean[chromosomeNumber][geneNumber];

    // private final static Charset ENCODING = StandardCharsets.UTF_8;


    public Boolean[][] getChromosomesFromFile() {
        Scanner scanner = null;

        try {
            scanner = new Scanner(new File(fileBest));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for (int i = 0; i< chromosomeNumber; i++) {
            Scanner scannerWord = new Scanner(scanner.nextLine());

            for (int j = 0; j < geneNumber; j++) {
                String gene = scannerWord.next();
                chromosomes[i][j] = stringToBoolean(gene);
            }
        }

        // printChromosomes();
        return chromosomes;
    }

    public Boolean stringToBoolean(String gene) {
        if(gene.equals("true")) {
            return true;
        }
        return false;
    }

    public void printChromosomes() {
        for (int i = 0; i< chromosomeNumber; i++) {
            for (int j = 0; j < geneNumber; j++) {
                System.out.print(chromosomes[i][j] + " ");
            }
            System.out.println();
        }
    }

    /*
    public void chromosomesToSolutions(){
        Path path = Paths.get(fileBest);

        try (BufferedReader reader = Files.newBufferedReader(path, ENCODING)){
            String line = null;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void genesToSolutions(){
        Scanner scanner = null;
        try {
            scanner = new Scanner(new File(fileBest));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (scanner.hasNextLine()) {
            int index = 0;
            boolean[] b = null;

            Solution solution = new Solution(1, 2, 1);

            Scanner scannerWord = new Scanner(scanner.nextLine());

            while (scannerWord.hasNext()) {
                String gene = scannerWord.next();
                System.out.println(findBoolean(gene));
            }
        }
    }
    */
}
