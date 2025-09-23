/* ***************************************************************** */
/*                                                                   */
/* IBM Confidential                                                  */
/*                                                                   */
/* OCO Source Materials                                              */
/*                                                                   */
/* Copyright IBM Corp. 2013                                          */
/*                                                                   */
/* The source code for this program is not published or otherwise    */
/* divested of its trade secrets, irrespective of what has been      */
/* deposited with the U.S. Copyright Office.                         */
/*                                                                   */
/* ***************************************************************** */

package com.ibm.lconn.profiles.internal.util;

public class Pair<F, S> {
	private static final int Pair = 0;

	private F first;

	private S second;

	public Pair() {
	}

	public Pair(F first, S second) {
		this.first = first;
		this.second = second;
	}

	public F getFirst() {
		return first;
	}

	public void setFirst(F first) {
		this.first = first;
	}

	public S getSecond() {
		return second;
	}

	public void setSecond(S second) {
		this.second = second;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!this.getClass().equals(o.getClass())) return false;

		final Pair<F, S> pair = (Pair<F, S>) o;
		if ((this.first.equals(pair.getFirst())) && (this.second.equals(pair.getSecond()))) {
			return true;
		}

		return false;
	}

	@Override
	public int hashCode() {
		return this.first.hashCode() + this.second.hashCode();

	}
}
