package testum;

public class ClassCircular {
	public ClassCircular next;
	public String label;

	public ClassCircular(String label) {
		this.label = label;
	}
}
