package deque;

import org.junit.Test;

import java.util.Comparator;

import static org.junit.Assert.*;

public class MaxArrayDequeTest {

    private static class StringComparator implements Comparator<String> {
        @Override
        public int compare(String s1, String s2) {
            return s1.compareTo(s2);
        }
    }
    public static Comparator<String> getNameComparator() {
        return new StringComparator();
    }

    @Test
    public void maxStringTest() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(getNameComparator());
        mad.addLast("daaa");
        mad.addLast("a");
        mad.addLast("c");
        mad.addLast("b");
        assertEquals(mad.max(), "daaa");
    }

    @Test
    public void externalComparatorTest() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(null);
        mad.addLast("daaa");
        mad.addLast("a");
        mad.addLast("c");
        mad.addLast("b");
        assertEquals(mad.max(getNameComparator()), "daaa");
    }

    @Test
    public void nullStringTest() {
        MaxArrayDeque<String> mad = new MaxArrayDeque<>(getNameComparator());
        assertNull(mad.max());
    }



}
