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

        //initialize the dataset:
        //input and output imprecise performances,
        //value function admissible ranges,
        //custom weight restrictions
        initializeData();

        //calculating the extreme efficiency scores for all robots
        //using the CCR model with imprecise information
        var extremeEfficiencies = new ImpreciseCCRExtremeEfficiency(1.1, 0.01);
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);

        //calculating the distribution of the efficiency scores (and expected efficiencies)
        //for all robots
        //using 100 randomly generated samples and 10 efficiency intervals
        //and CCR model with imprecise data
        var smaaEfficiency = new ImpreciseCCRSmaaEfficiency(1000, 10);
        var efficiencyDistribution = smaaEfficiency.efficiencyDistribution(data);

        //printing the results of the extreme efficiencies and efficiency distribution to the console
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

        //calculating the extreme efficiency ranks for all robots
        //with minimal performance for ordinal factors equal to 0.01
        // and minimal ratio in performance on ordinal factor
        // between two consecutive robots equal to 1.1
        var extremeRanks = new ImpreciseCCRExtremeRanks(1.1, 0.01);
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

        //calculating the distribution of the efficiency ranks (and expected ranks)
        //for all robots using 1000 randomly generated samples.
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

        //verification of the presence of necessary and possible efficiency preference relations
        //for all pairs of robots
        var preferenceRelations = new ImpreciseCCRPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);

        //printing the preference relations matrix to the console
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");

        //calculating the pairwise efficiency outranking indices for all pairs of robots
        //and printing them to the console
        var smaaPreferences = new ImpreciseCCRSmaaPreferenceRelations(1000);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        //defining the robots' names (for printing results)
        alternativeNames = IntStream.range(1, 28)
                .mapToObj(Objects::toString)
                .toList();

        //defining minimal and performances of all robots for input capacity
        var capacityMin = new double[]{50, 60, 40, 1, 45, 1, 4, 10, 9, 5, 25, 10, 8, 20, 40, 75, 10, 9, 10, 1, 25, 0.8, 2, 1, 8, 65, 190};
        var capacityMax = new double[]{65, 70, 50, 3, 55, 2, 5, 20, 12, 8, 35, 15, 12, 35, 55, 85, 18, 15, 13, 4, 30, 1.2, 4, 5, 12, 80, 220};

        //defining the robots' performances for precise factors: cost and velocity
        var cost = new double[]{7.2, 4.8, 5, 7.2, 9.6, 1.07, 1.76, 3.2, 6.72, 2.4, 2.88, 6.9, 3.2, 4, 3.68, 6.88, 8, 6.3, 0.94, 0.16, 2.81, 3.8, 1.25, 1.37, 3.63, 5.3, 4};
        var velocity = new double[]{1.35, 1.1, 1.27, 0.66, 0.05, 0.3, 1, 1, 1.1, 1, 0.9, 0.15, 1.2, 1.2, 1, 1, 2, 1, 0.3, 0.8, 1.7, 1, 0.5, 0.5, 1, 1.25, 0.75};

        //defining the robots' performances for ordinal factor: reputation
        //1- lowest value, 27 - the highest value
        var reputation = new double[]{15, 7, 23, 16, 24, 3, 8, 17, 9, 2, 18, 10, 25, 19, 11, 20, 1, 21, 12, 5, 26, 13, 27, 14, 4, 22, 6};

        //transofrming the arrays to the form of 2D arrays which can be passed to the
        //ProblemData constructor
        var minInputs = DataInitializationUtils.transposeArray(new double[][]{cost, reputation});
        var maxInputs = DataInitializationUtils.transposeArray(new double[][]{cost, reputation});
        var minOutputs = DataInitializationUtils.transposeArray(new double[][]{capacityMin, velocity});
        var maxOutputs = DataInitializationUtils.transposeArray(new double[][]{capacityMax, velocity});

        //create data set object by passing four 2D arrays representing
        //the minimal and maximal performances on inputs and outputs
        //and the factors' names (used in defining the weight constraints and ordinal factors)
        data = new CCRImpreciseProblemData(minInputs, minOutputs, maxInputs, maxOutputs,
                List.of("cost", "reputation"),
                List.of("capacity", "velocity"));

        //setting the input "reputation" as the ordinal one
        data.getImpreciseInformation().getOrdinalFactors().add("reputation");

        //defining the custom weight constraints
        addWeightConstraints();
    }

    protected void addWeightConstraints() {
        //defining the constraint representing the condition:
        //w(velocity) >= 5*w(capacity)
        //in a form 1*w(velocity) -5*w(capacity) >= 0
        data.addWeightConstraint(new Constraint(
                ConstraintOperator.GEQ,
                0,
                Map.of("velocity", 1.0, "capacity", -5.0)));

        //defining the constraint representing the condition:
        //w(velocity) <= 15*w(capacity)
        //in a form 1*w(velocity) - 15*w(capacity) <= 0
        data.addWeightConstraint(new Constraint(
                ConstraintOperator.LEQ,
                0,
                Map.of("velocity", 1.0, "capacity", -15.0)));
    }

}
