package com.elaunira.sbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

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
		try {
			File wordsFile = null;
			try {
				wordsFile = new File(SlicedBloomFilter.class.getResource("/words").toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			
			BufferedReader br = new BufferedReader(new FileReader(wordsFile));
			
			logger.info("Initial words file size is " + wordsFile.length() / 1024 + " Ko.");
			
			String line;
			int count = 0;
			
			SlicedBloomFilter<String> bf = new SlicedBloomFilter<String>(479829, 0.1);
			
			while ((line = br.readLine()) != null) {
				bf.add(line);
				count++;
			}
			
			logger.info(count + " words inserted into the bloom filter.");
			
			File slicedBloomFilterFile = new File("src/test/resources/sliced-bloom-filter");
			
			FileOutputStream fos = null;
			ObjectOutputStream out = null;
			try {
				fos = new FileOutputStream(slicedBloomFilterFile);
				out = new ObjectOutputStream(fos);
				out.writeObject(bf);
				out.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
			
			logger.info("The size of the bloom filter when serialized is " + slicedBloomFilterFile.length() / 1024 + " Ko.");
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
