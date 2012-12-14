package com.thoughtcomplex.starlight.util;

/**
 * Indicates that a class can receive information about changes to special Container classes.
 * @author Isaac Ellingson
 */
public interface BoundDataListener {
	public void boundDataChanged(BoundData b);
}
