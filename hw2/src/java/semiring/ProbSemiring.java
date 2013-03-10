package semiring;

public class ProbSemiring implements Semiring {

	@Override
	public double zero() {
		return 0.0;
	}

	@Override
	public double unit() {
		return 1.0;
	}

	@Override
	public double times(double x, double y) {
		return x * y;
	}

	@Override
	public double divide(double x, double y) {
		return x / y;
	}

	@Override
	public double plus(double x, double y) {
		return x + y;
	}

	@Override
	public double convertToR(double x) {
		return x;
	}

	@Override
	public double convertToSemiring(double x) {
		return x;
	}

}