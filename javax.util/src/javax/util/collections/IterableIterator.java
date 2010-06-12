package javax.util.collections;

import java.util.Iterator;

/**
 * Wraps an Iterator into an Iterable so that it can be traversed using a foreach loop.
 */
public class IterableIterator<E> implements Iterable<E> {

	protected Iterator<E> iterator;

	public IterableIterator(Iterator<E> iterator) {
		this.iterator = iterator;
	}

	public Iterator<E> iterator() {
		return iterator;
	}

}