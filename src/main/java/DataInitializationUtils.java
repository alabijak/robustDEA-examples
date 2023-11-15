public class DataInitializationUtils {
    public static double[][] transposeArray(double[][] array) {
        var result = new double[array[0].length][array.length];
        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                result[j][i] = array[i][j];
            }
        }
        return result;
    }
}
