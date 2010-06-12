package javax.util.collections;

import java.util.List;

public class CollectionUtils {

	private CollectionUtils() {
	}

	/**
	 * Compares two lists lexicographically that contain comparable elements.
	 */
	public static <T extends Comparable<? super T>> int compare(List<T> list1, List<T> list2) {
		int size1 = list1.size();
		int size2 = list2.size();
		int minSize = Math.min(size1, size2);
		for (int i = 0; i < minSize; i++) {
			int difference = list1.get(i).compareTo(list2.get(i));
			if (difference != 0) {
				return difference;
			}
		}
		if (size1 == size2) {
			return 0;
		} else {
			return size1 - size2;
		}
	}

}
