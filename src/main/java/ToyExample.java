import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;

public class ToyExample {
    public static void main(String[] args) {
        var example = new ToyExample();
        var data = example.initializeData();
        example.runExample(data);
    }

    private ProblemData initializeData() {
        var inputs = new double[][]{
                new double[]{1, 2},
                new double[]{5, 7},
                new double[]{4, 2},
                new double[]{7, 4},
                new double[]{3, 8},
        };
        var outputs = new double[][]{
                new double[]{1},
                new double[]{10},
                new double[]{5},
                new double[]{7},
                new double[]{12},
        };

        return new VDEAProblemData(inputs, outputs, List.of("in1", "in2"), List.of("out1"));
    }

    private void runExample(ProblemData data) {
        var printResultUtils = new PrintResultUtils();
        var distributionHeader = new ArrayList<String>();
        distributionHeader.add("[0.00-0.33]");
        for (int i = 1; i < 3; i++) {
            distributionHeader.add(String.format("(%.2f-%.2f]", i / 3.0, (i + 1) / 3.0));
        }
        var alternativeNames = "ABCDE".chars().mapToObj(c -> String.valueOf((char) c)).toList();

        var extremeEfficiencies = new CCRExtremeEfficiencies();
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);
        var superEfficiency = extremeEfficiencies.superEfficiencyForAll(data);

        var smaaEfficiency = new CCRSmaaEfficiency(100, 3, new Random(5));
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        System.out.println("Super-efficiency:");
        for (int i = 0; i < alternativeNames.size(); i++)
            System.out.printf("%s: %.3f%n", alternativeNames.get(i), superEfficiency.get(i));
        System.out.println();

        var extremeRanks = new CCRExtremeRanks();
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

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

        var preferenceRelations = new CCRPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");

        var smaaPreferenceRelations = new CCRSmaaPreferenceRelations(100, new Random(5));
        printResultUtils.printDistribution(smaaPreferenceRelations.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }
}

