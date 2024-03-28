import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class ToyExample {
    public static void main(String[] args) {
        // The example presents the use of the robustDEA library for a small example presented in the paper
        //Labijak-Kowalska, A., Kadziński, M., robustDEA: a Java library for robust efficiency analysis
        //
        // This data set consists of 5 artificial DMUs with performances described with 2 inputs and 1 output
        //The efficiency is analyzed with the CCR DEA model.

        var example = new ToyExample();

        //initializing input and output performances
        var data = example.initializeData();

        //performing the efficiency analysis
        example.runExample(data);
    }

    private ProblemData initializeData() {
        //initialize the inputs' performances for all DMUs.
        var inputs = new double[][]{
                new double[]{1, 2},
                new double[]{5, 7},
                new double[]{4, 2},
                new double[]{7, 4},
                new double[]{3, 8},
        };

        //initialize the outputs' performances for all DMUs.
        var outputs = new double[][]{
                new double[]{1},
                new double[]{10},
                new double[]{5},
                new double[]{7},
                new double[]{12},
        };

        //create the object representing the data set
        //by passing the inputs and ouputs arrays and the factors' names
        return new ProblemData(inputs, outputs, List.of("in1", "in2"), List.of("out1"));
    }

    private void runExample(ProblemData data) {
        //create some helper variables for printing results to the console
        var printResultUtils = new PrintResultUtils();
        var distributionHeader = new ArrayList<String>();
        distributionHeader.add("[0.00-0.33]");
        for (int i = 1; i < 3; i++) {
            distributionHeader.add(String.format("(%.2f-%.2f]", i / 3.0, (i + 1) / 3.0));
        }
        var alternativeNames = "ABCDE".chars().mapToObj(c -> String.valueOf((char) c)).toList();

        //calculating the extreme efficiency scores for all DMUs
        //using the CCR model
        var extremeEfficiencies = new CCRExtremeEfficiencies();
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);

        //calculating the super-efficiencies for all DMUs
        var superEfficiency = extremeEfficiencies.superEfficiencyForAll(data);

        //calculating the distribution of the efficiency scores (and expected efficiencies)
        //for all DMUs
        //using 100 randomly generated samples and 3 efficiency intervals
        //the random seed is set to 5 to make the results reproducible
        var smaaEfficiency = new CCRSmaaEfficiency(100, 3, new Random(5));
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        //printing the results of the extreme efficiencies and efficiency distribution to the console
        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        //printing the super-efficiency scores to the console
        System.out.println("Super-efficiency:");
        for (int i = 0; i < alternativeNames.size(); i++)
            System.out.printf("%s: %.3f%n", alternativeNames.get(i), superEfficiency.get(i));
        System.out.println();

        //calculating the extreme efficiency ranks for all DMUs
        var extremeRanks = new CCRExtremeRanks();
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

        //calculating the distribution of the efficiency ranks (and expected ranks)
        //for all DMUs using 100 samples (with the random seed set to 5).
        var smaaRanks = new CCRSmaaRanks(100, new Random(5));
        var rankDistribution = smaaRanks.rankDistribution(data);

        printResultUtils.printExtremeValuesAndDistribution(
                minRanks.stream().mapToDouble(x -> (double) ((int) x)).boxed().toList(),
                maxRanks.stream().mapToDouble(x -> (double) ((int) x)).boxed().toList(),
                rankDistribution,
                "Extreme ranks:",
                "Rank distribution:",
                "Expected ranks:",
                alternativeNames,
                IntStream.range(1, alternativeNames.size() + 1).mapToObj(Objects::toString).toList());

        //verification of the presence of necessary and possible efficiency preference relations
        //for all pairs of DMUs
        var preferenceRelations = new CCRPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);

        //printing the preference relations matrix to the console
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");

        //calculating the pairwise efficiency outranking indices for all pairs of DMUs
        //and printing them to the console
        //the reproducibility of the results is maintained by setting the random seed to 5.
        var smaaPreferenceRelations = new CCRSmaaPreferenceRelations(100, new Random(5));
        printResultUtils.printDistribution(smaaPreferenceRelations.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }
}

