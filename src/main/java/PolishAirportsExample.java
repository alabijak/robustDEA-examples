import put.dea.robustness.*;

import java.util.*;
import java.util.stream.IntStream;

public class PolishAirportsExample {
    private ProblemData data;
    private List<String> alternativeNames;

    public static void main(String[] args) {
        // The example presents the application of DEA robustness methods for standard DEA problems
        // with ratio-based efficiency model.
        // The data set represent 11 polish airports and is gathered from the paper
        // https://doi.org/10.1016/j.omega.2016.03.003
        //
        // This data set consists of 11 polish airports with performances described with 4 inputs and 2 outputs.
        //
        // The methods are run with and without the custom weight constraints.

        var example = new PolishAirportsExample();
        System.out.println("Result of the analysis without weight constraints");
        example.runExample(false);
        System.out.println();

        System.out.println("Result of the analysis with custom weight constraints");
        example.runExample(true);

    }

    private void runExample(boolean withWeightConstraints) {
        var printResultUtils = new PrintResultUtils();

        //initialize input and output performances
        initializeData();
        if (withWeightConstraints)
            //defining the custom weight constraints
            addWeightConstraints();

        //calculating the extreme efficiency scores for all airports
        //using the CCR model
        var extremeEfficiencies = new CCRExtremeEfficiencies();
        var minEfficiencies = extremeEfficiencies.minEfficiencyForAll(data);
        var maxEfficiencies = extremeEfficiencies.maxEfficiencyForAll(data);


        //calculating the distribution of the efficiency scores (and expected efficiencies)
        //for all airports
        //using 100 randomly generated samples and 10 efficiency intervals
        //and CCR model
        var smaaEfficiency = new CCRSmaaEfficiency(100, 10);
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


        //calculating the extreme efficiency ranks for all airports
        var extremeRanks = new CCRExtremeRanks();
        var minRanks = extremeRanks.minRankForAll(data);
        var maxRanks = extremeRanks.maxRankForAll(data);

        //calculating the distribution of the efficiency ranks (and expected ranks)
        //for all airports using 100 randomly generated samples.
        var smaaRanks = new CCRSmaaRanks(100);
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
        //for all pairs of airports
        var preferenceRelations = new CCRPreferenceRelations();
        var necessaryRelations = preferenceRelations.checkNecessaryPreferenceForAll(data);
        var possibleRelations = preferenceRelations.checkPossiblePreferenceForAll(data);

        //printing the preference relations matrix to the console
        printResultUtils.printPreferenceRelations(necessaryRelations,
                possibleRelations,
                alternativeNames,
                "Pairwise efficiency preference relations:");


        //calculating the pairwise efficiency outranking indices for all pairs of airports
        //and printing them to the console
        var smaaPreferences = new CCRSmaaPreferenceRelations(100);
        printResultUtils.printDistribution(smaaPreferences.peoi(data),
                "Pairwise efficiency outranking indices:",
                alternativeNames,
                alternativeNames);
    }

    private void initializeData() {
        //defining the airports' names (for printing results)
        alternativeNames = List.of("WAW", "KRK", "KAT", "WRO", "POZ", "LCJ", "GDN",
                "SZZ", "BZG", "RZE", "IEG");

        //defining inputs' performances for all airports
        var inputs = new double[][]{
                new double[]{10.5, 36, 129.4, 7},
                new double[]{3.1, 19, 31.6, 7.9},
                new double[]{3.6, 32, 57.4, 10.5},
                new double[]{1.5, 12, 18, 3},
                new double[]{1.5, 10, 24, 4},
                new double[]{0.6, 12, 24, 3.9},
                new double[]{1.0, 15, 42.9, 2.5},
                new double[]{0.7, 10, 25.7, 1.9},
                new double[]{0.3, 6, 3.4, 1.2},
                new double[]{0.6, 6, 11.3, 2.7},
                new double[]{0.1, 10, 63.4, 3}
        };

        //defining outputs' performances for all airports
        var outputs = new double[][]{
                new double[]{9.5, 129.7},
                new double[]{2.9, 31.3},
                new double[]{2.4, 21.1},
                new double[]{1.5, 18.8},
                new double[]{1.3, 16.2},
                new double[]{0.3, 4.2},
                new double[]{2, 23.6},
                new double[]{0.3, 4.2},
                new double[]{0.3, 6.2},
                new double[]{0.3, 3.5},
                new double[]{0.005, 0.61}
        };

        //create the object representing data set by passing inputs' and outputs' prformances
        //and factors' names
        data = new ProblemData(inputs, outputs, List.of("i1", "i2", "i3", "i4"), List.of("o1", "o2"));
    }

    private void addWeightConstraints() {
        //adding the custom weight constraints
        for (Constraint constraint : Arrays.asList(
                //creating the object representing the constraint:
                //w(i1) - 3w(i3) >= 0
                new Constraint(ConstraintOperator.GEQ, 0,
                        Map.of("i1", 1.0, "i3", -3.0)),
                //creating the object representing the constraint:
                //w(i1) - 5w(i4) <= 0
                new Constraint(ConstraintOperator.LEQ, 0,
                        Map.of("i1", -1.0, "i4", 5.0)),
                new Constraint(ConstraintOperator.LEQ, 0,
                        Map.of("i2", -1.0, "i3", 2.0)),
                new Constraint(ConstraintOperator.LEQ, 0,
                        Map.of("i2", -1.0, "i4", 5.0)),
                new Constraint(ConstraintOperator.GEQ, 0,
                        Map.of("o1", 1.0, "o2", -5.0)))) {
            data.addWeightConstraint(constraint);
        }
    }

}
