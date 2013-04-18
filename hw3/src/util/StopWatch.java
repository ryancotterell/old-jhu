package util;

import java.util.Date;

/**
 * A not very robust stopwatch class to make timing various operations easier. A simple
 * wrapper to some of the Java Date methods. This class is single use only (for each new timing
 * operation create a new StopWatch instance).
 * @author Daniel
 */
public class StopWatch {
	
	/** The start time of the stopwatch*/
	Date start;
	/** The end time of the stopwatch*/
	Date end;
	/** True once the stopwatch has been started and stopped*/
	boolean valid;
	
	public StopWatch()
	{
		start = null;
		end = null;
		valid = false;
	}
	
	/**
	 * Start the stopwatch. This method expects but does not check that start() is called before stop()
	 */
	public void start()
	{
		start = new Date();
	}
	
	/**
	 * Stop the stopwatch
	 * @return the time the stopwatch ran for in milliseconds
	 */
	public long stop()
	{
		end = new Date();
		valid = true;
		return end.getTime() - start.getTime();
	}
	
	/**
	 * Get the time the stopwatch ran for.
	 * @return the time in ms
	 */
	public long getTime()
	{
		if (valid)
			return end.getTime() - start.getTime();
		else
			throw new IllegalStateException("StopWatch has not been stopped yet.");
	}

}
