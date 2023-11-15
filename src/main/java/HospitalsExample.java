import org.apache.commons.math3.util.Pair;
import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class HospitalsExample {
    private ImpreciseVDEAProblemData data;
    private List<String> alternativeNames;

    public static void main(String[] args) {
        // The example presents the application of DEA robustness methods for problem with imprecise information
        // and value-based efficiency model.
        // The data set represent the exeplary data from in Section 2.2 of the paper
        // "Robustness Analysis for Imprecise Additive Value Efficiency Analysis with an Application to Evaluation of Special Economic Zones in Poland"
        //
        // This data set consists of 12 hospital with performances described with 2 inputs and 2 outputs.
        // Input i2 is ordinal. The performances at all other factors are interval.
        // For all inputs and outputs (except the ordinal i2) there is an admissible marginal value function range defined.
        // the weights for all factors are restricted to be within the range [0.083, 0.250]

        var example = new HospitalsExample();
        example.runExample();

    }

    private void runExample() {
        var printResultUtils = new PrintResultUtils();
        initializeData();
        var extremeDistances = new ImpreciseVDEAExtremeDistances();
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
        alternativeNames = IntStream.range(1, 13)
                .mapToObj(idx -> "H" + idx)
                .toList();
        double[] minI1 = new double[]{24, 17, 23, 45, 15, 60, 35, 31, 28, 47, 50, 35};
        double[] maxI1 = new double[]{24, 19, 25, 51, 17, 65, 42, 31, 30, 50, 53, 38};

        double[] i2 = new double[]{8, 2, 7, 9, 1, 7, 8, 7, 3, 5, 6, 4};

        var minI3 = new double[]{154, 124, 142, 170, 147, 252, 232, 205, 231, 258, 301, 213};
        var maxI3 = new double[]{161, 131, 150, 178, 155, 255, 235, 206, 244, 268, 306, 250};

        var minI4 = new double[]{98, 72, 85, 135, 58, 85, 98, 85, 72, 72, 78, 60};
        var maxI4 = new double[]{100, 76, 90, 148, 62, 95, 100, 85, 76, 75, 80, 65};

        var minO1 = new double[]{90, 170, 172, 120, 96, 218, 190, 130, 195, 240, 280, 250};
        var maxO1 = new double[]{95, 182, 180, 140, 102, 255, 200, 140, 215, 250, 292, 255};

        var minO2 = new double[]{85, 80, 60, 48, 69, 85, 83, 72, 105, 97, 142, 113};
        var maxO2 = new double[]{88, 85, 63, 50, 73, 90, 88, 75, 110, 100, 147, 120};

        var minInputs = DataInitializationUtils.transposeArray(new double[][]{minI1, i2, minI3, minI4});
        var maxInputs = DataInitializationUtils.transposeArray(new double[][]{maxI1, i2, maxI3, maxI4});
        var minOutputs = DataInitializationUtils.transposeArray(new double[][]{minO1, minO2});
        var maxOutputs = DataInitializationUtils.transposeArray(new double[][]{maxO1, maxO2});

        data = new ImpreciseVDEAProblemData(minInputs, minOutputs, maxInputs, maxOutputs,
                List.of("i1", "i2", "i3", "i4"),
                List.of("o1", "o2")
        );
        data.getImpreciseInformation().getOrdinalFactors().add("i2");
        addFunctionShapes();
        addWeightConstraints();
    }

    private void addFunctionShapes() {
        data.setColumnFunctionShapes("i1", List.of(
                        new Pair<>(10.0, 1.0),
                        new Pair<>(25.0, 0.4),
                        new Pair<>(40.0, 0.1),
                        new Pair<>(70.0, 0.0)
                ),
                List.of(
                        new Pair<>(10.0, 1.0),
                        new Pair<>(25.0, 0.5),
                        new Pair<>(40.0, 0.25),
                        new Pair<>(70.0, 0.0)
                ));


        data.setColumnFunctionShapes("i3", List.of(
                new Pair<>(100.0, 1.0),
                new Pair<>(150.0, 0.9),
                new Pair<>(300.0, 0.8),
                new Pair<>(400.0, 0.0)
        ), List.of(
                new Pair<>(100.0, 1.0),
                new Pair<>(150.0, 0.95),
                new Pair<>(300.0, 0.9),
                new Pair<>(400.0, 0.0)
        ));

        data.setColumnFunctionShapes("i4", List.of(
                new Pair<>(50.0, 1.0),
                new Pair<>(100.0, 0.5),
                new Pair<>(125.0, 0.25),
                new Pair<>(150.0, 0.0)
        ), List.of(
                new Pair<>(50.0, 1.0),
                new Pair<>(100.0, 0.6),
                new Pair<>(125.0, 0.4),
                new Pair<>(150.0, 0.0)
        ));

        data.setLowerFunctionShape("o1", List.of(
                new Pair<>(70.0, 0.0),
                new Pair<>(180.0, 0.084615385),
                new Pair<>(200.0, 0.1),
                new Pair<>(320.0, 1.0)
        ));

        data.setUpperFunctionShape("o1", List.of(
                new Pair<>(70.0, 0.0),
                new Pair<>(180.0, 0.1),
                new Pair<>(200.0, 0.228571429),
                new Pair<>(320.0, 1.0)
        ));

        data.setLowerFunctionShape("o2", List.of(
                new Pair<>(40.0, 0.0),
                new Pair<>(60.0, 0.0),
                new Pair<>(100.0, 0.3),
                new Pair<>(180.0, 1.0)
        ));

        data.setUpperFunctionShape("o2", List.of(
                new Pair<>(40.0, 0.0),
                new Pair<>(60.0, 0.1),
                new Pair<>(100.0, 0.3),
                new Pair<>(180.0, 1.0)
        ));
    }

    public void addWeightConstraints() {
        for (int i = 1; i < 5; i++) {
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.GEQ, 0.0833, Map.of("i" + i, 1.0)
            ));
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.LEQ, 0.25, Map.of("i" + i, 1.0)
            ));
        }
        data.getWeightConstraints().add(new Constraint(
                ConstraintOperator.GEQ, 0.0833, Map.of("o1", 1.0)
        ));
        data.getWeightConstraints().add(new Constraint(
                ConstraintOperator.GEQ, 0.0833, Map.of("o2", 1.0)
        ));

        data.getWeightConstraints().add(new Constraint(
                ConstraintOperator.LEQ, 0.25, Map.of("o1", 1.0)
        ));
        data.getWeightConstraints().add(new Constraint(
                ConstraintOperator.LEQ, 0.25, Map.of("o2", 1.0)
        ));
    }
}

