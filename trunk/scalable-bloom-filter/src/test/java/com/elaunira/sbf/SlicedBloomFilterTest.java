package com.elaunira.sbf;

import junit.framework.Assert;

import org.junit.Test;

/**
 * 
 *
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class SlicedBloomFilterTest extends AbstractBloomFilterTest {

	@Test
	public void testFalsePositiveRate() {
		
		for (double errorRate : errorRates) {
			SlicedBloomFilter<Integer> bf = new SlicedBloomFilter<Integer>(10000, errorRate);
			
			for (int i=0; i<10000; i++) {
				bf.add(i);
			}
			
			System.out.println(bf);
			
			Assert.assertTrue(bf.contains(0));
			Assert.assertFalse(bf.contains(bf.getCapacity()));
			Assert.assertTrue(bf.size() <= bf.getCapacity());
			Assert.assertTrue(getFalsePositiveRate(bf, 10000) < bf.getFalsePositiveProbability());
		}
	}	
	
}
