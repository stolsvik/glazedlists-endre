/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.test;

// for being a JUnit test case
import junit.framework.*;
// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.util.*;
// standard collections
import java.util.*;

/**
 * This test attempts to cause atomic change events that have change blocks
 * with indexes in random order.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class IndexOrderTest extends TestCase {

    /** for randomly choosing list indicies */
    private Random random = new Random(3);
    
    /**
     * Prepare for the test.
     */
    public void setUp() {
    }

    /**
     * Clean up after the test.
     */
    public void tearDown() {
    }

    /**
     * Test to verify that the lists work with change indicies out of order.
     *
     * <p>This creates a long chain of lists designed to cause events where the indicies
     * are out of order. The resultant list is a list of integer arrays of size two.
     * That list has been filtered to not contain any elements where the first index is
     * greater than 50. It has been sorted in increasing order.
     */
    public void testIndexOutOfOrder() {
        EventList unsorted = new BasicEventList();
        SortedList sortedOnce = new SortedList(unsorted, new IntegerArrayComparator(0));
        SortedList sortedTwice = new SortedList(sortedOnce, new IntegerArrayComparator(1));
        AbstractFilterList filteredOnce = new IntegerArrayFilterList(sortedTwice, 0, 50);
        
        ArrayList controlList = new ArrayList();
        
        // add a block of new elements one hundred times
        for(int a = 0; a < 15; a++) {

            // create a block of ten elements
            List currentChange = new ArrayList();
            for(int b = 0; b < controlList.size() || b < 10; b++) {
                currentChange.add(new int[] { random.nextInt(100), random.nextInt(100) });
            }
            
            // add that block
            unsorted.addAll(currentChange);
            
            // manually create a replica
            controlList.addAll(currentChange);
            Collections.sort(controlList, sortedTwice.getComparator());
            for(Iterator i = controlList.iterator(); i.hasNext(); ) {
                if(filteredOnce.filterMatches(i.next())) continue;
                i.remove();
            }
            
            // print the two lists
            /*System.out.println("MODIFICATION #: " + a);
            for(int i = 0; i < controlList.size(); i++) {
                int[] filter = (int[])filteredOnce.get(i);
                int[] control = (int[])controlList.get(i);
                System.out.println(filter[0] + " " + control[0] + "   " + filter[1] + " " + control[1]);
            }*/
            
            // verify the replica matches
            assertEquals(controlList, filteredOnce);
        }
    }
    
    /**
     * A special comparator that compares two integer arrays by the element
     * at a specified index.
     *
     * If the elements are identical it compares by the opposite index.
     * If those elements are idential it compares by System.identityHashCode.
     */
    class IntegerArrayComparator implements Comparator {
        private int index;
        public IntegerArrayComparator(int index) {
            this.index = index;
        }
        public int compare(Object alpha, Object beta) {
            int[] alphaArray = (int[])alpha;
            int[] betaArray = (int[])beta;
            int compared = alphaArray[index] - betaArray[index];
            if(compared != 0) return compared;
            compared = alphaArray[1 - index] - betaArray[1 - index];
            if(compared != 0) return compared;
            return System.identityHashCode(alpha) - System.identityHashCode(beta);
        }
    }

    /**
     * A special filter list that filters out integer arrays that don't have
     * an element lower than a specified thresshold.
     */
    class IntegerArrayFilterList extends AbstractFilterList {
        private int index;
        private int threshhold;
        public IntegerArrayFilterList(EventList source, int index, int threshhold) {
            super(source);
            this.index = index;
            this.threshhold = threshhold;
        }
        public boolean filterMatches(Object element) {
            int[] array = (int[])element;
            if(array[index] <= threshhold) return true;
            return false;
        }
    }
}
