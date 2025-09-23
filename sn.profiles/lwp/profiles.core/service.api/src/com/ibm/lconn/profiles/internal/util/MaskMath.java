/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2015                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

import java.util.BitSet;

public class MaskMath {
	
	/**
	 * Add masks.
	 * if either bit is true, result is true.
	 * if both bits are fals, result is false
	 *      mask       1100
	 *      subtrahend 1010
	 *      result     1110
	 */
	public static final long add(long mask1, long mask2){
		long rtn = mask1 | mask2;
		return rtn;
	}
	
	/**
	 * Subtract subtrahend from mask.
	 * if mask(i) = false bit i is false
	 * if mask(i) = true and subtrahend(i) = true, bit i is false
	 * if mask(i) = true and subtrahend(i) = false, bit i is true
	 *      mask       1100
	 *      subtrahend 1010
	 *      result     0100
	 */
    public final static long subtract( long mask, long subtrahend){
        long rtn = mask & (~subtrahend);
        return rtn;
    }
    
    /**
     * Determine if all the enabled bits in subset are also enabled in set.
     */
    public final static boolean isSubset(long set, long subset){
        // xor is used to determine all the "differences" in the masks.
        // all of the permission bits set has which subset does not
        long xor = set ^ subset;
        // compare these differences the original bitmask. when AND together the result is all the bits
        // that both set and the difference have. if the result does not equal XOR, there is at least one
        // different enabled bit in subset.
        boolean rtn = (set & xor) == xor;
        return rtn;
    }
    
	/**
	 * Add masks.
	 * if either bit is true, result is true.
	 * if both bits are fals, result is false
	 *      mask       1100
	 *      subtrahend 1010
	 *      result     1110
	 */
	public final static BitSet add(BitSet mask1, BitSet mask2){
		assert(mask1.size() == mask2.size());
		BitSet rtn = (BitSet)mask1.clone();
		rtn.or(mask2);
		return rtn;
	}
	
	/**
	 * Subtract subtrahend from mask.
	 * if mask(i) = false bit i is false
	 * if mask(i) = true and subtrahend(i) = true, bit i is false
	 * if mask(i) = true and subtrahend(i) = false, bit i is true
	 *      mask       1100
	 *      subtrahend 1010
	 *      result     0100
	 */
	public static final BitSet subtract(BitSet mask, BitSet subtrahend){
		assert(mask.size() == mask.size());
		// not sure which implementation is faster.
		BitSet rtn = (BitSet)mask.clone();
		BitSet sub = (BitSet)subtrahend.clone();
		sub.flip(0,sub.size());
		rtn.and(sub);
		// which is faster?
		//BitSet rtn = (BitSet)mask.clone();
		//for (int i = 0 ; i < subtrahend.size(); i++){
		//	if (rtn.get(i)){
		//		if (subtrahend.get(i)) rtn.clear(i);
		//	}
		//}
		return rtn;
	}
	
    /**
     * Determine if all the enabled bits in subset are also enabled in set.
     */
    public final static boolean isSubset(BitSet set, BitSet subset){
    	assert(subset.size() == set.size());
    	// not sure what implementation is faster. loop through bits or math operations like above.
    	// the math is more complex with BitSet and not done here.
    	boolean rtn = true;
    	for (int i = 0 ; i < subset.size(); i++){
    		if (subset.get(i)){
    			if (set.get(i) == false){
    				rtn = false;
    				break;
    			}
    		}
    	}
    	return rtn;
    }
}
