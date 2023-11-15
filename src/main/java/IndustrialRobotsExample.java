import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class IndustrialRobotsExample {
    private CCRImpreciseProblemData data;
    private List<String> alternativeNames;

    public static void main(String[] args) {
        // The example presents the application of DEA robustness methods for problem with imprecise information
        // and ratio-based efficiency model.
        // The data set represent 27 industrial robots and is gathered from the paper
        // https://doi.org/10.1007/s12351-023-00755-z
        //
        // This data set consists of 27 industrial robots with performances described with 2 inputs and 2 outputs.
        // Input reputation is ordinal. The performances of output capacity are interval.
        // there ale also some custom weight constraint defined.

        var example = new IndustrialRobotsExample();
        example.runExample();

    }

    private void runExample() {
        var printResultUtils = new PrintResultUtils();
        initializeData();
        var extremeEfficiencies = new ImpreciseCCRExtremeEfficiency(1.1, 0.01);
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);
        var smaaEfficiency = new ImpreciseCCRSmaaEfficiency(1000, 10);
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);
        var distributionHeader = new ArrayList<String>();
        distributionHeader.add("[0.0-0.1]");
        for (int i = 1; i < 10; i++) {
            distributionHeader.add(String.format("(%.1f-%.1f]", 0.1 * i, 0.1 * (i + 1)));
        }
        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        var extremeRanks = new ImpreciseCCRExtremeRanks(1.1, 0.01);
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);
        var smaaRanks = new ImpreciseCCRSmaaRanks(1000);
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

        var preferenceRelations = new ImpreciseCCRPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");
        var smaaPreferences = new ImpreciseCCRSmaaPreferenceRelations(1000);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        alternativeNames = IntStream.range(1, 28)
                .mapToObj(Objects::toString)
                .toList();
        var cost = new double[]{7.2, 4.8, 5, 7.2, 9.6, 1.07, 1.76, 3.2, 6.72, 2.4, 2.88, 6.9, 3.2, 4, 3.68, 6.88, 8, 6.3, 0.94, 0.16, 2.81, 3.8, 1.25, 1.37, 3.63, 5.3, 4};
        var reputation = new double[]{15, 7, 23, 16, 24, 3, 8, 17, 9, 2, 18, 10, 25, 19, 11, 20, 1, 21, 12, 5, 26, 13, 27, 14, 4, 22, 6};
        var capacityMin = new double[]{50, 60, 40, 1, 45, 1, 4, 10, 9, 5, 25, 10, 8, 20, 40, 75, 10, 9, 10, 1, 25, 0.8, 2, 1, 8, 65, 190};
        var capacityMax = new double[]{65, 70, 50, 3, 55, 2, 5, 20, 12, 8, 35, 15, 12, 35, 55, 85, 18, 15, 13, 4, 30, 1.2, 4, 5, 12, 80, 220};
        var velocity = new double[]{1.35, 1.1, 1.27, 0.66, 0.05, 0.3, 1, 1, 1.1, 1, 0.9, 0.15, 1.2, 1.2, 1, 1, 2, 1, 0.3, 0.8, 1.7, 1, 0.5, 0.5, 1, 1.25, 0.75};

        var minInputs = DataInitializationUtils.transposeArray(new double[][]{cost, reputation});
        var maxInputs = DataInitializationUtils.transposeArray(new double[][]{cost, reputation});
        var minOutputs = DataInitializationUtils.transposeArray(new double[][]{capacityMin, velocity});
        var maxOutputs = DataInitializationUtils.transposeArray(new double[][]{capacityMax, velocity});

        data = new CCRImpreciseProblemData(minInputs, minOutputs, maxInputs, maxOutputs,
                List.of("cost", "reputation"),
                List.of("capacity", "velocity"));
        data.getImpreciseInformation().getOrdinalFactors().add("reputation");
        addWeightConstraints();
    }

    protected void addWeightConstraints() {
        data.addWeightConstraint(new Constraint(
                ConstraintOperator.GEQ,
                0,
                Map.of("velocity", 1.0, "capacity", -5.0)));

        data.addWeightConstraint(new Constraint(
                ConstraintOperator.LEQ,
                0,
                Map.of("velocity", 1.0, "capacity", -15.0)));
    }

}
