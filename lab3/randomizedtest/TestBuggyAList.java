package randomizedtest;

import edu.princeton.cs.algs4.StdRandom;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by hug.
 */
public class TestBuggyAList {
    // YOUR TESTS HERE
    @Test
    public void testThreeAddThreeRemove() {
        AListNoResizing<Integer> listOne = new AListNoResizing<>();
        BuggyAList<Integer> listTwo = new BuggyAList<>();
        listOne.addLast(4);
        listOne.addLast(5);
        listOne.addLast(6);
        listTwo.addLast(4);
        listTwo.addLast(5);
        listTwo.addLast(6);
        assertEquals(listOne.size(), listTwo.size());
        assertEquals(listOne.removeLast(), listTwo.removeLast());
    }

    @Test
    public void randomizedTest() {
        AListNoResizing<Integer> L = new AListNoResizing<>();
        BuggyAList<Integer> Lb = new BuggyAList<>();
        int N = 5000;
        for (int i = 0; i < N; i += 1) {
            int operationNumber = StdRandom.uniform(0, 4);
            if (operationNumber == 0) {
                // addLast
                int randVal = StdRandom.uniform(0, 100);
                L.addLast(randVal);
                Lb.addLast(randVal);
            } else if (operationNumber == 1) {
                // size
                int size = L.size();
                int sizeB = Lb.size();
                assertEquals(size, sizeB);
            } else if (operationNumber == 2) {
                if (L.size() > 0) {
                    assertEquals(L.getLast(), Lb.getLast());
                }
            } else if (operationNumber == 3) {
                if (L.size() > 0) {
                    assertEquals(L.removeLast(), Lb.removeLast());
                }
            }

        }
    }
}
