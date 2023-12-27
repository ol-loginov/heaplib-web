package testum;

import java.util.HashMap;

public class ClassA {
	public static final String static_final_String = "ClassA";
	public static int static_Int = 111;
	public static int classAStaticOnly = 500;
	public int classAOnly = 500;
	public final String final_String = "asdasd";

	private Object anObject = new HashMap<>();

	public int[] intArray = new int[]{1, 2, 3, 10};

	public Object[] objectArray = new Object[]{new ClassB()};
}
