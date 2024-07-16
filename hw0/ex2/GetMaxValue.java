package ex2;

public class GetMaxValue {
    public static int max(int[] m) {
        int max = -1;
        int i = 0;
        while (i < m.length) {
            if (m[i] > max) {
                max = m[i];
            }
            i++;
        }
        return max;
    }
    public static void main(String[] args) {
        int[] numbers = new int[]{9, 2, 15, 2, 22, 10, 6};
        System.out.println(max(numbers));
    }
}
