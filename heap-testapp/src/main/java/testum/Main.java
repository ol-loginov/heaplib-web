package testum;

import java.lang.management.ManagementFactory;

public class Main {
	public static Store store = new Store();

	public static void main(String[] args) throws InterruptedException {
		System.out.println("PID: " + ManagementFactory.getRuntimeMXBean().getPid());
		Thread.sleep(1000_000);
	}
}
