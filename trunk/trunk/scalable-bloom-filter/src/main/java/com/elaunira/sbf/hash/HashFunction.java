package com.elaunira.sbf.hash;

/**
 * @author Laurent Pellegrino
 *
 * @version $Id$
 */
public interface HashFunction {

	abstract public int hash(String data, int seed);
	
}
