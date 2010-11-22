package com.elaunira.sbf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;

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
public class KeyValueSlicedBloomFilterTest extends AbstractBloomFilterTest {

	private static final Logger logger = 
		LoggerFactory.getLogger(KeyValueSlicedBloomFilterTest.class);
	
	@Test
	@SuppressWarnings("unchecked")
	public void testSizeAfterSerialization() {
		File wordsFile = null;
		try {
			wordsFile = new File(SlicedBloomFilter.class.getResource("/words")
					.toURI());
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}

		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(wordsFile));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		logger.info("Initial words file size is "
						+ wordsFile.length() / 1024 + " Ko.");

		String line;
		int count = 0;

		KeyValueSlicedBloomFilter<String, Integer> bf = 
			new KeyValueSlicedBloomFilter<String, Integer>(
					1000313, 0.1);

		try {
			while ((line = br.readLine()) != null) {
				bf.add(line, count);
				count++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		logger.info(count + " words inserted into the bloom filter.");
		logger.info(bf.toString());

		File slicedBloomFilterFile = new File(
				"src/test/resources/kv-sliced-bloom-filter");

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
		logger.info("The size of the bloom filter when serialized is "
				+ slicedBloomFilterFile.length() / 1024 + " Ko.");
		
		logger.info("Try to restore the bloom filter from file.");
		
		FileInputStream fis = null;
		ObjectInputStream ois = null;
		bf = null;
		try {
			fis = new FileInputStream(slicedBloomFilterFile);
			ois = new ObjectInputStream(fis);
			bf = (KeyValueSlicedBloomFilter<String, Integer>) ois.readObject();
			ois.close();
		} catch (IOException ex) {
			ex.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		logger.info(bf.toString());
	}
	
}
