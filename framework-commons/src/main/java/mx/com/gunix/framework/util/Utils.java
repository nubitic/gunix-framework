package mx.com.gunix.framework.util;

import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public abstract class Utils {
	/**
	 * <a href="http://stackoverflow.com/questions/12026885/common-util-to-break-a-list-into-batch#answer-30072617">http://stackoverflow.com/questions/12026885/common-util-to-break-a-list-into-batch#answer-30072617</a>
	 * */
	public static <T> Stream<List<T>> partition(List<T> source, int length) {
		if (length <= 0)
			throw new IllegalArgumentException("length = " + length);
		int size = source.size();
		if (size <= 0)
			return Stream.empty();
		int fullChunks = (size - 1) / length;
		return IntStream.range(0, fullChunks + 1).mapToObj(n -> source.subList(n * length, n == fullChunks ? size : (n + 1) * length));
	}
}
