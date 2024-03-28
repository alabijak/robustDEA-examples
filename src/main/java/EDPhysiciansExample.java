import org.apache.commons.math3.util.Pair;
import put.dea.robustness.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

public class EDPhysiciansExample {
    private VDEAProblemData data;
    private List<String> alternativeNames;

    public static void main(String[] args) {
        // The example presents the application of DEA robustness methods for value-based efficiency model
        // for efficiency analysis of 20 Emergency Department physicians.
        // The data set is described in Section 3 of the paper https://doi.org/10.1111/itor.13099
        //
        // This data set consists of 20 physicians with performances described with 3 inputs and 1 output
        // for all inputs and outputs there is a marginal value function defined,
        // the weights for all factors are restricted to be not greater than 0.5

        var example = new EDPhysiciansExample();
        example.runExample();

    }

    private void runExample() {
        var printResultUtils = new PrintResultUtils();
        //initialize physicians' input and output performances, value function shapes,
        //and custom weight restrictions
        initializeData();

        //calculating the extreme distances to the best DMU for all physicians
        var extremeDistances = new VDEAExtremeDistances();
        var minDistances = extremeDistances.minDistanceForAll(data);
        var maxDistances = extremeDistances.maxDistanceForAll(data);

        //calculating the distribution of the distance to the best physician for all analyzed units
        //using 100 randomly generated samples and 10 distance intervals.
        var smaaDistance = new VDEASmaaDistance(100, 10);
        var distanceDistribution = smaaDistance.distanceDistribution(data);

        //printing results of the extreme, expected distances and distance distribution
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

        //calculating the extreme efficiency scores for all physicians
        var extremeEfficiencies = new VDEAExtremeEfficiencies();
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);

        //calculating the distribution of the efficiency scores (and expected efficiencies)
        //for all physicians
        //using 100 randomly generated samples and 10 efficiency intervals.
        var smaaEfficiency = new VDEASmaaEfficiency(100, 10);
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        //printing the results of the extreme efficiencies and efficiency distribution to the console
        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        //calculating the extreme efficiency ranks for all physicians
        var extremeRanks = new VDEAExtremeRanks();
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

        //calculating the distribution of the efficiency ranks (and expected ranks)
        //for all physicians using 100 randomly generated samples.
        var smaaRanks = new VDEASmaaRanks(100);
        var rankDistribution = smaaRanks.rankDistribution(data);

        //printing the results of the extreme ranks and rank distribution to the console
        printResultUtils.printExtremeValuesAndDistribution(
                minRanks.stream().mapToDouble(x -> x).boxed().toList(),
                maxRanks.stream().mapToDouble(x -> x).boxed().toList(),
                rankDistribution,
                "Extreme ranks:",
                "Rank distribution:",
                "Expected ranks:",
                alternativeNames,
                IntStream.range(1, alternativeNames.size() + 1).mapToObj(Objects::toString).toList());

        //verification of the presence of necessary and possible efficiency preference relations
        //for all pairs of physicians
        var preferenceRelations = new VDEAPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);

        //printing the preference relations matrix to the console
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");

        //calculating the pairwise efficiency outranking indices for all pairs of physicians
        //and printing them to the console
        var smaaPreferences = new VDEASmaaPreferenceRelations(100);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        //initialize physicians' names for printing results
        alternativeNames = IntStream.range(1, 21)
                .mapToObj(Objects::toString)
                .toList();

        //initialize input performances
        var inputs = new double[][]{
                new double[]{2.026, 2.76, 0.92},
                new double[]{1.959, 2.381, 0.774},
                new double[]{2.223, 2.333, 0.643},
                new double[]{1.884, 1.823, 0.661},
                new double[]{1.511, 0.857, 0.487},
                new double[]{1.456, 1.33, 0.648},
                new double[]{1.903, 1.877, 0.596},
                new double[]{1.704, 1.73, 0.678},
                new double[]{1.708, 1.927, 0.657},
                new double[]{1.979, 1.508, 0.82},
                new double[]{1.652, 1.618, 0.592},
                new double[]{2.169, 1.863, 0.608},
                new double[]{1.634, 1.538, 0.786},
                new double[]{1.745, 2.117, 0.738},
                new double[]{1.594, 1.548, 0.602},
                new double[]{2.311, 1.538, 0.462},
                new double[]{1.962, 1.748, 0.557},
                new double[]{1.804, 1.59, 0.723},
                new double[]{1.567, 1.487, 0.601},
                new double[]{1.435, 1.198, 0.568}
        };

        //initialize output performances
        var outputs = new double[][]{
                new double[]{1.0},
                new double[]{0.961},
                new double[]{0.905},
                new double[]{0.952},
                new double[]{0.952},
                new double[]{0.978},
                new double[]{0.956},
                new double[]{0.939},
                new double[]{0.968},
                new double[]{0.922},
                new double[]{0.981},
                new double[]{0.961},
                new double[]{0.979},
                new double[]{0.942},
                new double[]{0.957},
                new double[]{0.974},
                new double[]{0.948},
                new double[]{0.977},
                new double[]{0.937},
                new double[]{0.969},
        };

        data = new VDEAProblemData(inputs, outputs, List.of("i1", "i2", "i3"), List.of("o1"));

        //add custom weight constraints
        addWeightConstraints();

        //add value function shapes
        addFunctionShapes();
    }

    private void addWeightConstraints() {
        for (var factor : List.of("i1", "i2", "i3", "o1")) {
            //for each input and output the weight must be greater than or equal to 0.5
            //adding constraints in form 1*w1 <= 0.5
            data.addWeightConstraint(new Constraint(
                    ConstraintOperator.LEQ, 0.5, Map.of(factor, 1.0)
            ));
        }
    }

    private void addFunctionShapes() {
        //setting the shape of the value function for input i1
        //by defining four characteristic points:
        //[0.6, 1], [0.85, 0.95], [2.0, 0.05], [2.5, 0.0]
        data.setFunctionShape("i1",
                List.of(new Pair<>(0.6, 1.0),
                        new Pair<>(0.85, 0.95),
                        new Pair<>(2.0, 0.05),
                        new Pair<>(2.5, 0.0)));

        //defining the value functions for the remaining inputs and output
        //analogously to the input i1.
        data.setFunctionShape("i2",
                List.of(new Pair<>(0.0, 1.0),
                        new Pair<>(2.0, 0.75),
                        new Pair<>(3.0, 0.0)));

        data.setFunctionShape("i3",
                List.of(new Pair<>(0.0, 1.0),
                        new Pair<>(0.35, 0.25),
                        new Pair<>(0.7, 0.05),
                        new Pair<>(1.0, 0.0)));

        data.setFunctionShape("o1",
                List.of(new Pair<>(0.89, 0.0),
                        new Pair<>(0.95, 0.1),
                        new Pair<>(0.97, 0.2),
                        new Pair<>(1.0, 1.0)));
    }
}

