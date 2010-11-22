package com.elaunira.sbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Some useful values and methods to test the Bloom filters implementations.
 *
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class AbstractBloomFilterTest {
	
	private static final Logger logger = 
		LoggerFactory.getLogger(AbstractBloomFilterTest.class);
	
	protected static final double[] errorRates = {
		0.9, 0.8, 0.7, 0.6, 0.5, 
		0.4, 0.3, 0.2, 0.1, 0.01, 
		0.001, 0.0001, 0.00001
	};
	
	public static double getFalsePositiveRate(BloomFilter<?> bf, int nbElementsAdded) {
		return Math.abs(((double) bf.size() / nbElementsAdded) - 1);
	}
	
	public void testSizeAfterSerialization(BloomFilter<String> bf) {
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
