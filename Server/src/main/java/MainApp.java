import java.util.ArrayList;
import java.util.List;

public class MainApp {

    public int[] copyArray(int[] array) {
        List<Integer> arr2 = new ArrayList<>();
        for (int i : array) {
            arr2.add(i);
        }
        int lastIndex = arr2.lastIndexOf(4);
        if (lastIndex < 0) {
            throw new RuntimeException();
        }
        int[] result = new int[array.length - lastIndex - 1];
        System.arraycopy(array, lastIndex+1, result, 0, result.length);
        return result;
    }

    public boolean counterOneFour(int[] array) {
        int counterOne = 0;
        int counterFour = 0;
        for (int i : array) {
            if (i != 1 && i != 4) {
                return false;
            }
            if (i == 4) {
                counterFour++;
            }
            if (i == 1) {
                counterOne++;
            }
        }
        return counterOne != 0 && counterFour != 0;
    }
}