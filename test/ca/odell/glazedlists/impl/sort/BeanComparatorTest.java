/**
 * Glazed Lists
 * http;//glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.impl.sort;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;
// test objects
import java.awt.Color;
import ca.odell.glazedlists.GlazedLists;

/**
 * This test verifies that the BeanComparator works as expected.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class BeanComparatorTest extends TestCase {

    /**
     * Tests that comparison by property works.
     */
    public void testCompare() {
        Comparator comparator = GlazedLists.beanPropertyComparator(Position.class, "position");

        assertTrue(comparator.compare(new Position(4), new Position(1)) > 0);
        assertTrue(comparator.compare(new Position(1), new Position(4)) < 0);
        assertTrue(comparator.compare(new Position(3), new Position(3)) == 0);
    }
    
    /**
     * Tests that comparison by property works.
     */
    public void testSort() {
        // prepare the sample list
        List unsorted = new ArrayList();
        unsorted.add(new Position(4));
        unsorted.add(new Position(1));
        unsorted.add(new Position(3));

        List sorted1 = new ArrayList();
        sorted1.addAll(unsorted);
        Collections.sort(sorted1);
        
        List sorted2 = new ArrayList();
        sorted2.addAll(unsorted);
        Collections.sort(sorted2, GlazedLists.beanPropertyComparator(Position.class, "position"));

        assertEquals(sorted1, sorted2);
    }


    /**
     * Simple class that sorts in the same order as its position value.
     */
    public static class Position implements Comparable {
        int position;
    
        public Position(int position) {
            super();
            this.position = position;
        }
    
        public int getPosition() {
            return position;
        }
    
        public void setPosition(int order) {
            this.position = order;
        }
    
        /**
         * @see java.lang.Comparable#compareTo(java.lang.Object)
         */
        public int compareTo(Object o) {
            return position - ((Position)o).position;
        }
        
        public String toString() {
            return "P:" + position;
        }
    }
}

