package testum;

public class MultiArray {
	public final int[] arrayOfInt1 = new int[10];
	public final int[][] arrayOfInt2 = new int[5][5];
	public final int[][][] arrayOfInt3 = new int[3][3][3];
	public final int[][] arrayOfInt4 = new int[2][];
	public final Integer[] arrayOfInteger1 = new Integer[]{1, 2};
	public final Object[] arrayOfObject1 = new Object[3];

	public MultiArray() {
		arrayOfInt4[0] = new int[]{100, 101, 102, 103};

		arrayOfObject1[0] = new int[]{200, 201};
		arrayOfObject1[2] = new String[]{"string1", "string2"};
	}
}
