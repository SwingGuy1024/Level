package com.neptunedreams.vulcan.math;

/**
 * Class to take statistics on one dimension of a population. This uses Waldorf's method to determine the
 * standard deviation, which is said to be the best method. I'm not sure why. It looks like it does more work each
 * time it goes through the loop. Maybe it's less likely to overflow, but I'm not really worried about that. Maybe
 * it's more useful if you need the new mean and standard deviation each time through. That makes sense, since
 * the calculations for each are much simpler. This may be the way to go, since I do need the mean and standard
 * deviation each time.
 * <p/>
 * http://stackoverflow.com/questions/895929/how-do-i-determine-the-standard-deviation-stddev-of-a-set-of-values
 */
@SuppressWarnings("unused")
class Population {
	private double mean = 0.0;
	private double sSum = 0.0;
	private int cnt = 0;
	private double meanMin = Double.MAX_VALUE;
	private double meanMax = -Double.MAX_VALUE;

	void addValue(double value) {
		double tmpMean = mean;
		double delta = value - tmpMean;
		mean += delta / ++cnt;
		sSum += delta * (value - mean);
		if (mean > meanMax) { meanMax = mean; }
		if (mean < meanMin) { meanMin = mean; }
	}

	double getMean() { return mean; }

	double getStDevSample() { return Math.sqrt(sSum / (cnt - 1)); }

	double getStDevPopulation() { return Math.sqrt(sSum / (cnt)); }
	
	double getMeanRange() { return meanMax - meanMin; }
}
