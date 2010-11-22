package com.elaunira.sbf;

import junit.framework.Assert;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Tests associated to the {@link SlicedBloomFilter}.
 *
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class SlicedBloomFilterTest extends AbstractBloomFilterTest {

	private static final Logger logger = 
		LoggerFactory.getLogger(SlicedBloomFilterTest.class);
	
	@Test
	public void testFalsePositiveRate() {
		final int nbElementsToInset = 1000000;
		
		for (double errorRate : errorRates) {
			SlicedBloomFilter<Integer> bf = new SlicedBloomFilter<Integer>(nbElementsToInset, errorRate);
			
			for (int i=0; i<nbElementsToInset; i++) {
				bf.add(i);
			}
			
			logger.info(bf.toString());
			
			Assert.assertTrue(bf.contains(0));
			Assert.assertTrue(bf.size() <= bf.getCapacity());
			Assert.assertTrue(
					getFalsePositiveRate(bf, nbElementsToInset) 
						<= bf.getFalsePositiveProbability());
		}
	}	
	
	@Test
	public void testSizeAfterSerialization() {
		super.testSizeAfterSerialization(
				new SlicedBloomFilter<String>(1000313, 0.1));
	}
	
}
