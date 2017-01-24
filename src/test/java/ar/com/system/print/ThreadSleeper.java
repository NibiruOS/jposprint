package ar.com.system.print;

public class ThreadSleeper implements Sleeper {

	@Override
	public void sleep(int ms) {
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
}
