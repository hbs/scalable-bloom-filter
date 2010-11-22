package com.elaunira.sbf;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import com.elaunira.sbf.hash.Murmur2;

/**
 * This bloom filter is a variant of a classical bloom filter as explained in
 * the <a href=
 * "http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.153.6902&rep=rep1&type=pdf"
 * >Approximate caches for packet classification</a>. It consists of
 * partitioning the {@code M} bits among the {@code k} hash functions, thus
 * creating {@code k} slices of {@code m = M / k} bits.
 * <p>
 * Using slices result in a more robust filter, with no element specially
 * sensitive to false positives.
 * <p>
 * This class is <strong>not thread-safe</strong>. Moreover, when an element is
 * added into the Bloom filter, it is based on the uniqueness of this object
 * which is defined by the {@link #hashCode()} method. Therefore it is really
 * important to provide a correct {@link #hashCode()} method for elements which
 * have to be passed to the {@link #add} method.
 * 
 * @author Laurent Pellegrino
 * 
 * @version $Id$
 */
public class KeyValueSlicedBloomFilter<K, V> extends KeyValueBloomFilter<K, V> {

	private static final long serialVersionUID = 1L;
	
	// the number of slices to use (equals to the number 
	// of hash function to use)
	private int slicesCount;
	
	// the number of bits per slice
	private int bitsPerSlice;
	
	// the set containing the values for each slice
	private LinkedList<V>[] filter;

	// the number of elements added in the Bloom filter
	private int count;

	/**
	 * This BloomFilter must be able to store at least {@code capacity} elements
	 * while maintaining no more than {@code falsePositiveProbability} chance of
	 * false positives.
	 * 
	 * @param capacity
	 *            the maximum number of elements the Bloom filter can contain
	 *            without to transcend the {@code falsePositiveProbability}.
	 * 
	 * @param falsePositiveProbability
	 *            the maximum false positives rate allowed by this filter.
	 */
	@SuppressWarnings("unchecked")
	public KeyValueSlicedBloomFilter(int capacity, double falsePositiveProbability) {
		super(capacity, falsePositiveProbability);
		
		this.slicesCount = 
			this.computeSlicesCount(capacity, falsePositiveProbability);
		
		this.bitsPerSlice = 
			this.computeBitsPerSlice(capacity, falsePositiveProbability);

		this.filter = new LinkedList[this.slicesCount * this.bitsPerSlice];
	}
	
	private int computeSlicesCount(int capacity, double falsePositiveProbability) {
		return (int) (Math.ceil(
				Math.log(1 / falsePositiveProbability) / Math.log(2)));
	}
	
	private int computeBitsPerSlice(int capacity, double falsePositiveProbability) {
		return 
			(int) Math.ceil(
				(2 * capacity * Math.abs(Math.log(falsePositiveProbability))) 
					/ (this.slicesCount * Math.pow(Math.log(2), 2)));
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean add(K key, V value) {
		if (this.lazyContains(key)) {
			return true;
		}
		
		this.addWithoutCheck(key, value);
		
		return false;
	}

	/**
	 * Adds the specified element without verifying that the element is
	 * contained by the Bloom filter. The size of the Bloom filter is
	 * incremented by one even if the element is already contained by the
	 * filter. Therefore, this method should only be used if you know what you
	 * do.
	 * 
	 * @param elt
	 *            the element to add to the Bloom filter.
	 */
	public void addWithoutCheck(K key, V value) {
		if (this.isFull()) {
			throw new IllegalStateException("bloom filter is at capacity");
		}
		
		int[] hashes = 
			getHashBuckets(
					Integer.toString(key.hashCode()), 
					this.slicesCount, this.bitsPerSlice);
		
		int offset = 0;
		for (int k : hashes) {
			if (this.filter[offset + k] == null) {
				this.filter[offset + k] = new LinkedList<V>();
			}
			this.filter[offset + k].add(value);
			offset += this.bitsPerSlice;
		}
		
		this.count++;
	}

	/**
	 * {@inheritDoc}
	 */
	public Iterator<V> contains(K key) {
		int[] hashes = 
			getHashBuckets(
					Integer.toString(key.hashCode()),
					this.slicesCount, this.bitsPerSlice);
		
		List<LinkedList<V>> interestingSets = new ArrayList<LinkedList<V>>(hashes.length);
		
		int offset = 0;
		for (int k : hashes) {
			if (this.filter[offset + k] == null) {
				return null;
			}
			interestingSets.add(this.filter[offset + k]);
			offset += this.bitsPerSlice;
		}
		
		LinkedList<V> result = interestingSets.get(0);
		for (int i=1; i<interestingSets.size(); i++) {
			result.retainAll(interestingSets.get(i));
		}
		
		return result.iterator();
	}
	
	private boolean lazyContains(K key) {
		int[] hashes = 
			getHashBuckets(
					Integer.toString(key.hashCode()),
					this.slicesCount, this.bitsPerSlice);
		
		int offset = 0;
		for (int k : hashes) {
			if (this.filter[offset + k] == null) {
				return false;
			}
		}
		
		return true;
	}	
	
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

	/**
	 * Returns a boolean indicating if the Bloom filter has reached its maximal
	 * capacity.
	 * 
	 * @return {@code true} whether the Bloom filter has reached its maximal
	 *         capacity, {@code false} otherwise.
	 */
	public boolean isFull() {
		return this.count > this.capacity;
	}
	
	/**
	 * Returns the number of elements added in this Bloom filter.
	 * 
	 * @return the number of elements added in this Bloom filter.
	 */
	public int size() {
		return this.count;
	}
	
	/**
	 * Returns the number of bits per slice.
	 * 
	 * @return the number of bits per slice.
	 */
	public int getBitsPerSlice() {
		return bitsPerSlice;
	}
	
	/**
	 * Returns the number of slices associated to this filter.
	 * 
	 * @return the number of slices associated to this filter.
	 */
	public int getSlicesCount() {
		return slicesCount;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return super.toString() + 
			"[slicesCount=" + this.slicesCount + ", bitsPerSlice=" + this.bitsPerSlice + "]";
	}

	private void writeObject(ObjectOutputStream out) throws IOException {
		GZIPOutputStream zos = new GZIPOutputStream(out);
		ObjectOutputStream oos = new ObjectOutputStream(zos);
		
		oos.writeInt(super.capacity);
		oos.writeDouble(super.falsePositiveProbability);

		int index = 0;
		for (LinkedList<V> values : this.filter) {
			if (values != null) {
				for (V value : values) {
					oos.writeInt(index);
					oos.writeObject(value);
				}
			}
			index++;
		}
		
		zos.finish();
	}

	@SuppressWarnings("unchecked")
	private void readObject(ObjectInputStream in) throws IOException,
			ClassNotFoundException {
		GZIPInputStream zis = new GZIPInputStream(in);
		ObjectInputStream ois = new ObjectInputStream(zis);
		
		super.capacity = ois.readInt();
		super.falsePositiveProbability = ois.readDouble();
		
		this.slicesCount = 
			this.computeSlicesCount(
					super.capacity, super.falsePositiveProbability);
		this.bitsPerSlice = 
			this.computeBitsPerSlice(
					super.capacity, super.falsePositiveProbability);
		this.filter = new LinkedList[this.slicesCount * this.bitsPerSlice];
		
		boolean eof = false;
		int index = 0;
		
		while (!eof) {
			try {
				index = ois.readInt();

				if (this.filter[index] == null) {
					this.filter[index] = new LinkedList<V>();
				}

				this.filter[index].add((V) ois.readObject());
			} catch (EOFException e) {
				eof = true;
			}
		}
	}

}
