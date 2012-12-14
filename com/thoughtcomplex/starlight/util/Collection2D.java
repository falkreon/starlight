package com.thoughtcomplex.starlight.util;

import java.util.Collection;

public interface Collection2D<E> {
	public boolean put(E e, int x, int y);
	public void clear();
	public boolean contains(Object o);
	public boolean containsAll(Collection<?> c);
	public boolean containsAll(Collection2D<?> c);
	public boolean isEmpty();
	
}
