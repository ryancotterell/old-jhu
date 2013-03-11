package semiring;

/**
* Semiring interface
* @author ryan
*
*/
public interface Semiring {
	public double zero();
	public double unit();
	public double times(double x,double y);
	public double divide(double x, double y);
	public double plus(double x,double y);
	public double convertToR(double x);
	public double convertToSemiring(double x);
	
}