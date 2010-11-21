package com.elaunira.sbf;


/**
 * Test the bloom filters implementation.
 *
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class AbstractBloomFilterTest {
	
	protected static final double[] errorRates = {
		0.9, 0.8, 0.7, 0.6, 0.5, 
		0.4, 0.3, 0.2, 0.1, 0.01, 
		0.001, 0.0001, 0.00001
	};
	
	public static double getFalsePositiveRate(BloomFilter<?> bf, int nbElementsAdded) {
		return Math.abs(((double) bf.size() / nbElementsAdded) - 1);
	}
	
}
