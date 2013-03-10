package semiring;

public class LogSemiring implements Semiring {

	@Override
	public double zero() {
		return Double.NEGATIVE_INFINITY;
	}

	@Override
	public double unit() {
		return 0.0;
	}

	@Override
	public double times(double x, double y) {
		if (x == Double.NEGATIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
			return Double.NEGATIVE_INFINITY;
		}
		return x + y;
	}

	@Override
	public double divide(double x, double y) {
		if (x == Double.NEGATIVE_INFINITY || y == Double.NEGATIVE_INFINITY) {
			return Double.NEGATIVE_INFINITY;
		}
		return x - y;
	}

	@Override
	public double plus(double x, double y) {
				
		if (y <= x) {
			if (y == Double.NEGATIVE_INFINITY) {
				return x;
			}
			return x + Math.log1p(Math.exp(y - x));
		} else {
			if (x == Double.NEGATIVE_INFINITY) {
				return y;
			}
			return y + Math.log1p(Math.exp(x - y));
		}
		
	}

	@Override
	public double convertToR(double x) {
		return Math.exp(x);
	}

	@Override
	public double convertToSemiring(double x) {
		return Math.log(x);
	}

}