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

        //initialize the data set: inputs' and outputs' performance ranges,
        //admissible value function shapes
        //and custom weight restrictions
        initializeData();

        //calculating the extreme distance to the best SEZ for all units
        //with minimal performance for ordinal factors equal to 0.0,
        // minimal ratio in performance on ordinal factors between two consecutive SEZs equal to 1.0001,
        // and the minimal ratio in value functions between two consecutive SEZs equal to 1.0.
        var extremeDistances = new ImpreciseVDEAExtremeDistances(1.0001, 0.0, 1.0);
        var minDistances = extremeDistances.minDistanceForAll(data);
        var maxDistances = extremeDistances.maxDistanceForAll(data);

        //calculating the distribution of the distance to the best SEZ for all analyzed units
        //using 100 randomly generated samples and 10 distance intervals.
        var smaaDistance = new ImpreciseVDEASmaaDistance(100, 10);
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

        //calculating the extreme efficiency scores for all SEZs
        var extremeEfficiencies = new ImpreciseVDEAExtremeEfficiencies(1.0001, 0.0, 1.0);
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);

        //calculating the distribution of the efficiency scores (and expected efficiencies)
        //for all SEZs
        //using 1000 randomly generated samples and 10 efficiency intervals.
        var smaaEfficiency = new ImpreciseVDEASmaaEfficiency(1000, 10);
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        //printing the results of the extreme efficiencies and efficiency distribution to the console
        printResultUtils.printExtremeValuesAndDistribution(minEfficiencies,
                maxEfficiencies, efficiencyDistribution,
                "Extreme efficiencies:",
                "Efficiency distribution:",
                "Expected efficiency scores:",
                alternativeNames,
                distributionHeader);

        //calculating the extreme efficiency ranks for all SEZs
        var extremeRanks = new ImpreciseVDEAExtremeRanks(1.0001, 0.0, 1.0);
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

        //calculating the distribution of the efficiency ranks (and expected ranks)
        //for all SEZs using 100 randomly generated samples.
        var smaaRanks = new ImpreciseVDEASmaaRanks(100);
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
        //for all pairs of SEZs
        var preferenceRelations = new ImpreciseVDEAPreferenceRelations(1.0001, 0.0, 1.0);
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);

        //printing the preference relations matrix to the console
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");

        //calculating the pairwise efficiency outranking indices for all pairs of SEZs
        //and printing them to the console
        var smaaPreferences = new ImpreciseVDEASmaaPreferenceRelations(100);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        //initialize the names of SEZs (for printing results)
        alternativeNames = List.of("KAM", "KAT", "KOS", "KRA", "LEG", "LOD", "MIE", "POM", "SLU",
                "STA", "SUW", "TAR", "WAL", "WAR");

        //initialize the minimal and maximal possible performance values for "area" input.
        double[] minArea = new double[]{373.8344, 2614.3965, 1936.9046, 866.7958, 1341.1473, 1416.8445,
                1643.1187, 2246.2929, 910.1585, 664.1551, 635.0653, 1868.2066, 3554.956, 1364.677};
        double[] maxArea = new double[]{540.8285, 2614.3965, 2201.2549, 949.6604, 1341.1473, 1754.6376,
                1723.9743, 2246.2929, 910.1585, 707.9814, 662.9506, 1868.2066, 3774.5461, 1390.7303};

        //initialize the (precise) performance values for "expenditures" input.
        double[] expenditures = new double[]{2557.3, 16605.1, 7133.4, 4240.4, 5131.8, 13318.7, 7838.1,
                10481.6, 1592.3, 1790.9, 2500.1, 7470.7, 22789.5, 3124.6};

        //initialize the minimal and maximal possible performance values for "jobs" output.
        var minJobs = new double[]{7347, 59964, 31927, 25862, 14367, 33401, 24815, 22921, 3478, 6829,
                7258, 20740, 48954, 17643};
        var maxJobs = new double[]{7530, 64481, 32400, 29580, 15294, 36122, 34992, 24893, 3941, 7260,
                8336, 23734, 50268, 20778};

        //initialize the (precise) performance values for "financial_result" output.
        var financialResult = new double[]{555.1, 17663.5, 22984.9, 1373, 7614.5, 7402.8, 4956, 1479.1,
                761.5, 701, 2734.4, 18220.4, 11862.8, 1647.4};

        var minInputs = DataInitializationUtils.transposeArray(new double[][]{minArea, expenditures});
        var maxInputs = DataInitializationUtils.transposeArray(new double[][]{maxArea, expenditures});
        var minOutputs = DataInitializationUtils.transposeArray(new double[][]{minJobs, financialResult});
        var maxOutputs = DataInitializationUtils.transposeArray(new double[][]{maxJobs, financialResult});

        //initialize the data set object with four arrays representing the minimal and maximal possible
        //performances for inputs and outputs,
        //and the factors' names.
        data = new ImpreciseVDEAProblemData(minInputs, minOutputs, maxInputs, maxOutputs,
                List.of("area", "expenditures"),
                List.of("jobs", "financial_result")
        );

        //defining the admissble ranges of the marginal value functions
        addFunctionShapes();

        //adding the custom weight constraints
        addWeightConstraints();
    }

    private void addFunctionShapes() {
        //defining the admissible range for marginal value function for "area" input
        //with a form of two functions with 3 characteristic points
        //for each (lower and upper) function
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

        //define the value functions for all other factors in a similar way as for "area".
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
        //defining the restrictions for weights of all inputs and outputs
        //the weights must be between 0.16667 and 0.33333 for all factors,
        //i.e. the constraints take form:
        //w >= 0.16667 and
        //w <= 0.33333
        var names = List.of("area", "expenditures", "jobs", "financial_result");
        for (var factor : names) {
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.GEQ, 0.16667, Map.of(factor, 1.0)
            ));
            data.getWeightConstraints().add(new Constraint(
                    ConstraintOperator.LEQ, 0.33333, Map.of(factor, 1.0)
            ));
        }
    }
}

