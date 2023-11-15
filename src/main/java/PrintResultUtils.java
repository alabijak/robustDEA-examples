import joinery.DataFrame;
import put.dea.robustness.DistributionResult;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;

public class PrintResultUtils {
    private DecimalFormat decimalFormat;

    public PrintResultUtils() {
        this.decimalFormat = new DecimalFormat("0.000");
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    public void printExtremeValuesAndDistribution(List<Double> minValues,
                                                  List<Double> maxValues,
                                                  DistributionResult distributionResult,
                                                  String extremeValuesTitle,
                                                  String distributionTitle,
                                                  String expectedValuesTitle,
                                                  List<String> alternativeNames,
                                                  List<String> distributionTableHeader) {

        System.out.println(extremeValuesTitle);
        for (int i = 0; i < alternativeNames.size(); i++) {
            System.out.printf("%s: [%s, %s]%n",
                    alternativeNames.get(i),
                    decimalFormat.format(minValues.get(i)),
                    decimalFormat.format(maxValues.get(i)));
        }
        System.out.println();

        printDistribution(distributionResult.distribution(),
                distributionTitle,
                alternativeNames,
                distributionTableHeader);

        System.out.println(expectedValuesTitle);
        for (int i = 0; i < alternativeNames.size(); i++) {
            System.out.printf("%s: %s%n",
                    alternativeNames.get(i),
                    decimalFormat.format(distributionResult.expectedValues().get(i))
            );
        }
        System.out.println();

    }

    public void printDistribution(DataFrame<Double> distribution,
                                  String distributionTitle,
                                  List<String> alternativeNames,
                                  List<String> columnNames) {
        System.out.println(distributionTitle);
        var maxHeaderLength = columnNames.stream().map(String::length).max(Comparator.naturalOrder()).get();
        maxHeaderLength = Math.max(maxHeaderLength, 5);
        var headerFormat = String.format("%%%ds", maxHeaderLength);
        var altNameMaxLen = alternativeNames.stream().map(String::length).max(Comparator.naturalOrder()).get();
        var headerBlankFormat = String.format("%%%ds ", altNameMaxLen);
        System.out.println(String.format(headerBlankFormat, " ") +
                String.join(" ", columnNames.stream()
                        .map(x -> String.format(headerFormat, x))
                        .toList()));
        for (int i = 0; i < alternativeNames.size(); i++) {
            System.out.printf(headerBlankFormat + "%s%n",
                    alternativeNames.get(i),
                    String.join(" ",
                            distribution.row(i).stream()
                                    .map(decimalFormat::format)
                                    .map(x -> String.format(headerFormat, x))
                                    .toList()));
        }
        System.out.println();

    }

    public void printPreferenceRelations(List<List<Boolean>> necessaryPreferences,
                                         List<List<Boolean>> possiblePreferences,
                                         List<String> alternativesNames,
                                         String title) {
        System.out.println(title);
        System.out.println("\t" + String.join("\t", alternativesNames));
        for (int subjectDmu = 0; subjectDmu < alternativesNames.size(); subjectDmu++) {
            System.out.print(alternativesNames.get(subjectDmu) + "\t");
            for (int relativeDmu = 0; relativeDmu < alternativesNames.size(); relativeDmu++) {
                if (necessaryPreferences.get(subjectDmu).get(relativeDmu))
                    System.out.print("N\t");
                else if (possiblePreferences.get(subjectDmu).get(relativeDmu))
                    System.out.print("P\t");
                else
                    System.out.print("\t");
            }
            System.out.println();
        }
        System.out.println();
    }
}
