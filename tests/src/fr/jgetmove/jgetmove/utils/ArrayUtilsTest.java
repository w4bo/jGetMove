package fr.jgetmove.jgetmove.utils;

import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArrayUtilsTest {
    @Test
    void isIncluded() {
        TreeSet<Integer> containee = new TreeSet<>();
        containee.add(1);
        containee.add(2);

        TreeSet<Integer> container = new TreeSet<>();
        container.add(0);
        container.add(1);
        container.add(3);
        container.add(2);

        assertTrue(ArrayUtils.isIncluded(containee, container));

        HashSet<Integer> containeeDifferentSet = new HashSet<>();
        containeeDifferentSet.add(0);
        containeeDifferentSet.add(3);

        assertTrue(ArrayUtils.isIncluded(containeeDifferentSet, container));

        TreeSet<Integer> falseContainee = new TreeSet<>();
        falseContainee.add(1);
        falseContainee.add(5);

        assertFalse(ArrayUtils.isIncluded(falseContainee, container));
    }

    @Test
    void isSame() {
        HashSet<Integer> sameSet = new HashSet<>();
        sameSet.add(1);
        sameSet.add(5);

        ArrayList<Integer> sameList = new ArrayList<>();
        sameList.add(1);
        sameList.add(5);

        assertTrue(ArrayUtils.isSame(sameSet, sameList));


        Set<Integer> differentSet = new HashSet<Integer>();
        differentSet.add(1);
        differentSet.add(2);

        assertFalse(ArrayUtils.isSame(differentSet, sameList));


        List<Integer> differentList = new LinkedList<>();
        differentList.add(1);
        differentList.add(2);

        assertFalse(ArrayUtils.isSame(sameSet, differentList));

    }

}
