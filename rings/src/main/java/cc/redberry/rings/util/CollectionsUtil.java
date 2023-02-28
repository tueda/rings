package cc.redberry.rings.util;

import com.carrotsearch.hppc.cursors.IntCursor;
import com.carrotsearch.hppc.IntArrayList;
import com.carrotsearch.hppc.IntHashSet;
import com.carrotsearch.hppc.IntObjectHashMap;

public final class CollectionsUtil {
    private CollectionsUtil() {
    }

    public static int sum(IntArrayList list) {
        return ArraysUtil.sum(list.buffer, 0, list.elementsCount);
    }

    public static <E> boolean containsAllKeys(IntObjectHashMap<E> map, Iterable<IntCursor> keys) {
        IntObjectHashMap<E>.KeysContainer container = map.keys();
        for (IntCursor c : keys) {
            int key = c.value;
            if (!container.contains(key)) {
                return false;
            }
        }
        return true;
    }

    public static <E> boolean containsAll(IntHashSet set, int[] elements) {
        for (int e : elements) {
            if (!set.contains(e)) {
                return false;
            }
        }
        return true;
    }
}
