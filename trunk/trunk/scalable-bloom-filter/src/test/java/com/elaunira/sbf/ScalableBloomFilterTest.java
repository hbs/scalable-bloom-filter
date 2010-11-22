package com.elaunira.sbf;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests associated to {@link ScalableBloomFilter}.
 * 
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class ScalableBloomFilterTest extends AbstractBloomFilterTest {
	
	private static final Logger logger = 
		LoggerFactory.getLogger(ScalableBloomFilterTest.class);
	
	@Test
	public void testFalsePositiveRate() {
		final int nbElementsToInsert = 1000000;
		
		for (double errorRate : errorRates) {
			ScalableBloomFilter<Integer> bf = 
				new ScalableBloomFilter<Integer>(100, errorRate);
			
			for (int i=0; i<nbElementsToInsert; i++) {
				bf.add(i);
			}
			
			logger.info(bf.toString());
			
			Assert.assertTrue(bf.contains(0));
			Assert.assertTrue(
					getFalsePositiveRate(bf, nbElementsToInsert) 
						<= bf.getFalsePositiveProbability());
		}
	}
	
	@Test
	public void testSizeAfterSerialization() {
		super.testSizeAfterSerialization(
				new ScalableBloomFilter<String>(1000, 0.1));
	}
	
}
