import org.apache.commons.math3.util.Pair;
import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class PolishSEZExample {
    private ImpreciseVDEAProblemData data;
    private List<String> alternativeNames;

    public static void main(String[] args) {
        // The example presents the application of DEA robustness methods for problem with imprecise information
        // and value-based efficiency model.
        // The data set represent the data about Polish Special Economic Zones from in Section 5 of the paper
        // "Robustness Analysis for Imprecise Additive Value Efficiency Analysis with an Application to Evaluation of Special Economic Zones in Poland"
        //
        // This data set consists of 14 Polish Special Economic Zones with performances described with 2 inputs and 2 outputs.
        // The performances of input "area" and output "jobs" are interval as they changed in time.
        // The other input and output are precisely defined.
        // For all inputs and outputs (except the ordinal i2) there is an admissible marginal value function range defined.
        // The weights for all factors are restricted to be within the range [1/6, 1/3]

        var example = new PolishSEZExample();
        example.runExample();

    }

    private void runExample() {
        var printResultUtils = new PrintResultUtils();
        initializeData();
        var extremeDistances = new ImpreciseVDEAExtremeDistances(1.0, 0.0, 1.0);
        var minDistances = extremeDistances.minDistanceForAll(data);
        var maxDistances = extremeDistances.maxDistanceForAll(data);
        var smaaDistance = new ImpreciseVDEASmaaDistance(100, 10);
        var distanceDistribution = smaaDistance.distanceDistribution(data);
        var distributionHeader = new ArrayList<String>();
        distributionHeader.add("[0.0-0.1]");
        for (int i = 1; i < 10; i++) {
            distributionHeader.add(String.format("(%.1f-%.1f]", 0.1 * i, 0.1 * (i + 1)));
        }
        printResultUtils.printExtremeValuesAndDistribution(minDistances,
                maxDistances, distanceDistribution,
                "Extreme distances to the best unit:",
                "Distance to the best unit distribution:",
                "Expected values of distance to the best unit:",
                alternativeNames,
                distributionHeader);

        var extremeEfficiencies = new ImpreciseVDEAExtremeEfficiencies();
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);
        var smaaEfficiency = new ImpreciseVDEASmaaEfficiency(100, 10);
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        var extremeRanks = new ImpreciseVDEAExtremeRanks();
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);
        var smaaRanks = new ImpreciseVDEASmaaRanks(100);
        var rankDistribution = smaaRanks.rankDistribution(data);

        printResultUtils.printExtremeValuesAndDistribution(
                minRanks.stream().mapToDouble(x -> x).boxed().toList(),
                maxRanks.stream().mapToDouble(x -> x).boxed().toList(),
                rankDistribution,
                "Extreme ranks:",
                "Rank distribution:",
                "Expected ranks:",
                alternativeNames,
                IntStream.range(1, alternativeNames.size() + 1).mapToObj(Objects::toString).toList());

        var preferenceRelations = new ImpreciseVDEAPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");
        var smaaPreferences = new ImpreciseVDEASmaaPreferenceRelations(100);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        alternativeNames = List.of("KAM", "KAT", "KOS", "KRA", "LEG", "LOD", "MIE", "POM", "SLU",
                "STA", "SUW", "TAR", "WAL", "WAR");
        double[] minArea = new double[]{373.8344, 2614.3965, 1936.9046, 866.7958, 1341.1473, 1416.8445,
                1643.1187, 2246.2929, 910.1585, 664.1551, 635.0653, 1868.2066, 3554.956, 1364.677};
        double[] maxArea = new double[]{540.8285, 2614.3965, 2201.2549, 949.6604, 1341.1473, 1754.6376,
                1723.9743, 2246.2929, 910.1585, 707.9814, 662.9506, 1868.2066, 3774.5461, 1390.7303};

        double[] expenditures = new double[]{2557.3, 16605.1, 7133.4, 4240.4, 5131.8, 13318.7, 7838.1,
                10481.6, 1592.3, 1790.9, 2500.1, 7470.7, 22789.5, 3124.6};

        var minJobs = new double[]{7347, 59964, 31927, 25862, 14367, 33401, 24815, 22921, 3478, 6829,
                7258, 20740, 48954, 17643};
        var maxJobs = new double[]{7530, 64481, 32400, 29580, 15294, 36122, 34992, 24893, 3941, 7260,
                8336, 23734, 50268, 20778};

        var financialResult = new double[]{555.1, 17663.5, 22984.9, 1373, 7614.5, 7402.8, 4956, 1479.1,
                761.5, 701, 2734.4, 18220.4, 11862.8, 1647.4};

        var minInputs = DataInitializationUtils.transposeArray(new double[][]{minArea, expenditures});
        var maxInputs = DataInitializationUtils.transposeArray(new double[][]{maxArea, expenditures});
        var minOutputs = DataInitializationUtils.transposeArray(new double[][]{minJobs, financialResult});
        var maxOutputs = DataInitializationUtils.transposeArray(new double[][]{maxJobs, financialResult});

        data = new ImpreciseVDEAProblemData(minInputs, minOutputs, maxInputs, maxOutputs,
                List.of("area", "expenditures"),
                List.of("jobs", "financial_result")
        );
        addFunctionShapes();
        addWeightConstraints();
    }

    private void addFunctionShapes() {

        data.setColumnFunctionShapes("area", List.of(
                        new Pair<>(373.8344, 1.0),
                        new Pair<>(2074.19025, 0.5),
                        new Pair<>(3774.5461, 0.0)
                ),
                List.of(
                        new Pair<>(373.8344, 1.0),
                        new Pair<>(2074.19025, 0.7),
                        new Pair<>(3774.5461, 0.0)
                ));


        data.setColumnFunctionShapes("expenditures", List.of(
                new Pair<>(1592.3, 1.0),
                new Pair<>(6891.6, 0.7),
                new Pair<>(17490.2, 0.1),
                new Pair<>(22789.5, 0.0)
        ), List.of(
                new Pair<>(1592.3, 1.0),
                new Pair<>(6891.6, 0.85),
                new Pair<>(17490.2, 0.4),
                new Pair<>(22789.5, 0.0)
        ));

        data.setColumnFunctionShapes("jobs", List.of(
                new Pair<>(3478.0, 0.0),
                new Pair<>(20000.0, 0.6),
                new Pair<>(33979.5, 0.8),
                new Pair<>(64481.0, 1.0)
        ), List.of(
                new Pair<>(3478.0, 0.0),
                new Pair<>(20000.0, 0.7),
                new Pair<>(33979.5, 1.0),
                new Pair<>(64481.0, 1.0)
        ));

        data.setColumnFunctionShapes("financial_result", List.of(
                new Pair<>(555.1, 0.0),
                new Pair<>(11770.0, 0.0),
                new Pair<>(17377.45, 0.2),
                new Pair<>(22984.9, 1.0)
        ), List.of(
                new Pair<>(555.1, 0.0),
                new Pair<>(11770.0, 0.1),
                new Pair<>(17377.45, 0.4),
                new Pair<>(22984.9, 1.0)
        ));
    }

    public void addWeightConstraints() {
        var names = List.of("area", "expenditures", "jobs", "financial_result");
        for (var factor : names) {
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.GEQ, 1.0 / 6.0, Map.of(factor, 1.0)
            ));
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.LEQ, 1.0 / 3.0, Map.of(factor, 1.0)
            ));
        }
    }
}

