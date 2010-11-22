package com.elaunira.sbf;

import java.io.UnsupportedEncodingException;

import com.elaunira.sbf.hash.Murmur2;

/**
 * Some utility methods for Bloom filters.
 *
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public class BloomFilterUtil {

	/**
	 * Returns {@code hashCount} hashes for the specified {@code key} by
	 * using only one hash function. Indeed, we can derive as many hash values
	 * as you need as a linear combination of two without compromising the
	 * performance of a Bloom filter. This is explained in the paper entitled <a
	 * href="http://www.eecs.harvard.edu/~kirsch/pubs/bbbf/esa06.pdf">Less
	 * Hashing, Same Performance: Building a Better Bloom Filter</a> by <em>Adam
	 * Kirsch</em> and <em>Michael Mitzenmacher</em>.
	 * <p>
	 * The hash function used for it is the Murmur 2 hash function which is
	 * reputed for being really fast.
	 * 
	 * @param key
	 *            the value to hash.
	 * 
	 * @param hashCount
	 *            the number of hashes wished.
	 * 
	 * @param max
	 *            value used to restrict the hash values obtained in the [0;
	 *            max[ range.
	 * 
	 * @return {@code hashCount} hashes for the specified {@code key} by
	 *         using only one hash function.
	 */
	public static int[] getHashBuckets(String key, int hashCount, int max) {
		byte[] b;
		try {
			b = key.getBytes("UTF-16");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		int[] result = new int[hashCount];
		int hash1 = Murmur2.hash32(b, 0);
		int hash2 = Murmur2.hash32(b, hash1);
		for (int i = 0; i < hashCount; i++) {
			result[i] = Math.abs((hash1 + i * hash2) % max);
		}
		return result;
	}
	
	public static int computeSlicesCount(int capacity, double falsePositiveProbability) {
		return (int) (Math.ceil(
				Math.log(1 / falsePositiveProbability) / Math.log(2)));
	}
	
	public static int computeBitsPerSlice(int capacity, double falsePositiveProbability, int slicesCount) {
		return 
			(int) Math.ceil(
				(2 * capacity * Math.abs(Math.log(falsePositiveProbability))) 
					/ (slicesCount * Math.pow(Math.log(2), 2)));
	}
	
}
