package testum;

import java.lang.ref.WeakReference;

public class Store {
	public volatile ClassA A = new ClassA();
	public volatile ClassB B = new ClassB();
	public volatile ClassA_Derived A_Derived = new ClassA_Derived();
	private ClassB[] array_B = new ClassB[0];
	public volatile WeakReference<ClassA_Derived> A_Derived_Weak = new WeakReference<>(new ClassA_Derived());
	public MultiArray multiArray = new MultiArray();

	public ClassCircular circularA = new ClassCircular("aa");
	public ClassCircular circularB = new ClassCircular("bbbbbbbbb");
	public ClassCircular circularC = new ClassCircular("ccccccccccccccccccccccccccc");

	public Store() {
		circularA.next = circularB;
		circularB.next = circularC;
		circularC.next = circularA;
	}
}
