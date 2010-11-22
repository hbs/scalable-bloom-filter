package com.elaunira.sbf;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Scalable Bloom Filter is an implementation of a SBF as described in the
 * original paper entitled <a
 * href="http://asc.di.fct.unl.pt/~nmp/pubs/ref--04.pdf">Scalable Bloom
 * Filters</a>.
 * <p>
 * This implementation is greatly inspired from the Python version available at
 * {@link https://github.com/jaybaird/python-bloomfilter}.
 * 
 * @author Laurent Pellegrino
 * 
 * @version $Id$
 */
public class ScalableBloomFilter<E> extends BloomFilter<E> {

	private static final long serialVersionUID = 1L;

	// tightening ratio of error probability
	private final double ratio;

	// growth ratio when full
	private final Mode scale;
	
	private final LinkedList<SlicedBloomFilter<E>> filters;
	
	public enum Mode {
		// slower, but takes up less memory
		SMALL_SET_GROWTH(2),
		// faster, but takes up more memory faster
		LARGE_SET_GROWTH(4);
		
		final int value;
		
		Mode(int v) {
			this.value = v;
		}
	}
	
	public ScalableBloomFilter() {
		this(Mode.SMALL_SET_GROWTH, 0.9, 100, 0.001);
	}
	
	public ScalableBloomFilter(int initialCapacity, double falsePositiveProbability) {
		this(Mode.SMALL_SET_GROWTH, 0.9, initialCapacity, falsePositiveProbability);
	}
	
	public ScalableBloomFilter(Mode mode, double ratio, int capacity, double falsePositiveProbability) {
		super(capacity, falsePositiveProbability);
		
		this.ratio = ratio;
		this.scale = mode;
		
		this.filters = new LinkedList<SlicedBloomFilter<E>>();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean add(E elt) {
		if (this.contains(elt)) {
			return true;
		}
		
		if (this.filters.isEmpty() 
				|| this.filters.getLast().isFull()) {
			this.filters.add(
					new SlicedBloomFilter<E>(
							(int) (super.capacity * Math.pow(this.scale.value, this.filters.size())),
									falsePositiveProbability * Math.pow(this.ratio, this.filters.size())));
		}
		
		this.filters.getLast().addWithoutCheck(elt);
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean contains(E elt) {
		Iterator<SlicedBloomFilter<E>> iterator = this.filters.descendingIterator();
		
		while (iterator.hasNext()) {
			if (iterator.next().contains(elt)) {
				return true;
			}
		}
		
		return false;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int size() {
		int sum = 0;
		for (SlicedBloomFilter<E> bf : this.filters) {
			sum += bf.size();
		}
		return sum;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public int getCapacity() {
		return -1;
	}

	/**
	 * Returns the initial capacity of the filter when it has been created.
	 * 
	 * @return the initial capacity of the filter when it has been created.
	 */
	public int getInitialCapacity() {
		return super.capacity;
	}
	
	/**
	 * Returns the tightening ratio of error probability.
	 * 
	 * @return the tightening ratio of error probability.
	 */
	public double getRatio() {
		return this.ratio;
	}
	
	/**
	 * Returns the growth ratio value used when a new filter has to be appended.
	 * 
	 * @return the growth ratio value used when a new filter has to be appended.
	 */
	public Mode getScale() {
		return this.scale;
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return 
			super.toString() 
				+ "[ratio=" + this.ratio + ", scale=" + this.scale.value + "]";
	}
	
}
