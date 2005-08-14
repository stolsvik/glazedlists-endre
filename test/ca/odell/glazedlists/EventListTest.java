/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

// for being a JUnit test case
import junit.framework.*;
// standard collections
import java.util.*;

import ca.odell.glazedlists.event.ListEventListener;
import ca.odell.glazedlists.event.ListEvent;

/**
 * Verifies that EventList matches the List API.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class EventListTest extends TestCase {

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
     * Validates that removeAll() works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=169">Bug 169</a>
     */
    public void testRemoveAll() {
        List jesse = new ArrayList(); jesse.addAll(Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') }));
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });

        // create the reference list
        List jesseArrayList = new ArrayList();
        jesseArrayList.addAll(jesse);
        jesseArrayList.removeAll(wilson);

        // test the BasicEventList list
        List jesseBasicEventList = new BasicEventList();
        jesseBasicEventList.addAll(jesse);
        jesseBasicEventList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseBasicEventList);

        // test the SortedList list
        List jesseSortedList = new SortedList(new BasicEventList(), null);
        jesseSortedList.addAll(jesse);
        jesseSortedList.removeAll(wilson);
        assertEquals(jesseArrayList, jesseSortedList);
    }

    /**
     * Validates that retainAll() works.
     */
    public void testRetainAll() {
        List jesse = new ArrayList(); jesse.addAll(Arrays.asList(new Character[] { new Character('J'), new Character('E'), new Character('S'), new Character('S'), new Character('E') }));
        List wilson = Arrays.asList(new Character[] { new Character('W'), new Character('I'), new Character('L'), new Character('S'), new Character('O'), new Character('N') });

        // create the reference list
        List jesseArrayList = new ArrayList();
        jesseArrayList.addAll(jesse);
        jesseArrayList.retainAll(wilson);

        // test the BasicEventList list
        List jesseBasicEventList = new BasicEventList();
        jesseBasicEventList.addAll(jesse);
        jesseBasicEventList.retainAll(wilson);
        assertEquals(jesseArrayList, jesseBasicEventList);

        // test the SortedList list
        List jesseSortedList = new SortedList(new BasicEventList(), null);
        jesseSortedList.addAll(jesse);
        jesseSortedList.retainAll(wilson);
        assertEquals(jesseArrayList, jesseSortedList);
    }

    /**
     * Validates that contains() works with null.
     */
    public void testContainsNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(false, list.contains(null));
            assertEquals(true,  list.contains("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.contains(null));
            assertEquals(true, list.contains("Western"));
            assertEquals(false, list.contains("Molson"));
        }
    }

    /**
     * Validates that containsAll() works with null.
     */
    public void testContainsAllNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Molson" })));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Molson", null })));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Molson", "Busch" })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(false, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Molson" })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Sleeman", "Western" })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.containsAll(Arrays.asList(new String[] { null, null })));
        }
    }

    /**
     * Validates that indexOf() works with null.
     */
    public void testIndexOfNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertTrue(-1 == list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertTrue(-1 != list.indexOf(null));
            assertTrue(-1 != list.indexOf("Western"));
            assertTrue(-1 == list.indexOf("Molson"));
        }
    }



    /**
     * Validates that lastIndexOf() works with null.
     */
    public void testLastIndexOfNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertTrue(-1 == list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertTrue(-1 != list.lastIndexOf(null));
            assertTrue(-1 != list.lastIndexOf("Western"));
            assertTrue(-1 == list.lastIndexOf("Molson"));
        }
    }

    /**
     * Validates that remove() works with null.
     */
    public void testRemoveNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(false, list.remove(null));
            assertEquals(true,  list.remove("Sleeman"));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.remove(null));
            assertEquals(true, list.remove("Western"));
            assertEquals(false, list.remove("Molson"));
        }
    }

    /**
     * Validates that removeAll() works with null.
     */
    public void testRemoveAllNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(false,  list.removeAll(Arrays.asList(new String[] { null, "Busch" })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Western", "Busch" })));
            assertEquals(true, list.removeAll(Arrays.asList(new String[] { "Sleeman", null })));
            assertEquals(false, list.removeAll(Arrays.asList(new String[] { "Western", null })));
        }
    }

    /**
     * Validates that retainAll() works with null.
     */
    public void testRetainAllNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();

            // test a list that doesn't contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            assertEquals(true,  list.retainAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.retainAll(Arrays.asList(new String[] { "Moslon", null })));

            // test a list that does contain nulls
            list.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            assertEquals(true,  list.retainAll(Arrays.asList(new String[] { "Western", null })));
            assertEquals(true, list.retainAll(Arrays.asList(new String[] { "Moslon", null })));
        }
    }


    /**
     * Validates that hashCode() works with null.
     */
    public void testHashCodeNull() {
        // get all different list types
        List listTypes = new ArrayList();
        listTypes.add(new ArrayList());
        listTypes.add(new BasicEventList());
        listTypes.add(new SortedList(new BasicEventList()));

        // test all different list types
        for(Iterator i = listTypes.iterator(); i.hasNext(); ) {
            List list = (List)i.next();
            List copy = new ArrayList();

            // test a list that doesn't contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList(new String[] { "Molson", "Sleeman", "Labatts", "Western" }));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));

            // test a list that does contain nulls
            list.clear();
            copy.clear();
            list.addAll(Arrays.asList(new String[] { null, "Sleeman", null, "Western" }));
            copy.addAll(list);
            assertEquals(copy.hashCode(), list.hashCode());
            assertTrue(list.equals(copy));
            copy.set(0, "Busch");
            assertFalse(list.equals(copy));
        }
    }

    /**
     * Test that the {@link GlazedLists#eventList(java.util.Collection)} factory
     * method works.
     *
     * @see <a href="https://glazedlists.dev.java.net/issues/show_bug.cgi?id=234">Bug 234</a>
     */
    public void testGlazedListsEventList() {
        // make sure they have different backing stores
        List list = new ArrayList();
        EventList eventList = GlazedLists.eventList(list);
        assertEquals(list, eventList);

        list.add("A");
        assertTrue(!list.equals(eventList));

        eventList.add("B");
        assertTrue(!list.equals(eventList));

        // make sure null is supported
        EventList empty = GlazedLists.eventList(null);
        assertEquals(Collections.EMPTY_LIST, empty);
    }


    /**
     * Tests taht the {@link GlazedLists#syncEventListToList(EventList, java.util.List)}
     * factory method.
     */
    public void testGlazedListsSync() {
        EventList source = new BasicEventList();
        source.add("McCallum");
        source.add("Keith");
        List target = new ArrayList();
        target.add("Greene");

        ListEventListener listener = GlazedLists.syncEventListToList(source, target);
        assertEquals(source, target);

        source.add("Szakra");
        assertEquals(source, target);

        source.addAll(Arrays.asList(new String[] { "Moore", "Holmes" }));
        assertEquals(source, target);

        source.add(1, "Burris");
        assertEquals(source, target);

        source.set(1, "Crandell");
        assertEquals(source, target);

        Collections.sort(source);
        assertEquals(source, target);

        source.clear();
        assertEquals(source, target);

        source.removeListEventListener(listener);
        source.add("Davis");
        assertFalse(source.equals(target));
    }

    public void testEventListLock() {
        final EventList source = new BasicEventList();

        // asymmetric unlocking of the readlock should fail-fast
        try {
            source.getReadWriteLock().readLock().unlock();
            fail("failed to receive an IllegalStateException when unlocking and unlocked readlock");
        } catch (IllegalStateException iae) {}

        // asymmetric unlocking of the writelock should fail-fast
        try {
            source.getReadWriteLock().writeLock().unlock();
            fail("failed to receive an IllegalStateException when unlocking and unlocked writelock");
        } catch (IllegalStateException iae) {}

        // symmetric locking/unlocking of the readlock should succeed
        source.getReadWriteLock().readLock().lock();
        source.getReadWriteLock().readLock().unlock();

        // symmetric locking/unlocking of the writelock should succeed
        source.getReadWriteLock().writeLock().lock();
        source.getReadWriteLock().writeLock().unlock();
    }

    public void testCombineEvents() {
        TransactionalEventList list = new TransactionalEventList(new BasicEventList());
        for (int i = 0; i < 16; i++)
             list.add(new Integer(0));

        list.addListEventListener(new ConsistencyTestList(list, "transactional", true));

        list.beginEvent();

        for(int i = 0; i < 4; i++) list.add(8, new Object());
        for(int j = 7; j >= 0; j--) {
            for(int i = 0; i < 10; i++) list.add(j, new Object());
        }
        list.remove(55);
        list.remove(95);
        list.remove(14);
        list.remove(22);
        list.remove(27);
        list.remove(78);
        list.remove(1);
        list.remove(85);
        list.remove(52);
        list.remove(14);
        list.remove(39);
        list.remove(38);
        list.remove(61);
        list.remove(69);
        list.remove(8);
        list.remove(57);
        list.remove(10);
        list.remove(5);
        list.remove(71);
        list.remove(60);
        list.remove(42);
        list.remove(21);
        list.remove(15);
        list.remove(59);
        list.remove(15);
        list.remove(14);
        list.remove(24);
        list.remove(43);
        list.remove(35);
        list.remove(12);
        list.remove(11);
        list.remove(34);
        list.remove(42);
        list.remove(32);
        list.remove(19);
        list.add(32, new Integer(92));
        list.remove(44);
        list.remove(19);
        list.remove(45);
        list.remove(55);
        list.remove(23);
        list.remove(11);
        list.remove(8);
        list.remove(50);
        list.remove(29);
        list.remove(31);
        list.remove(33);
        list.remove(45);
        list.remove(15);
        list.remove(25);
        list.remove(8);
        list.add(40, new Integer(95));
        list.remove(32);
        list.remove(3);
        list.remove(26);
        list.remove(14);
        list.remove(36);
        list.add(39, new Integer(96));
        list.remove(34);
        list.remove(21);
        list.remove(13);
        list.remove(32);
        list.remove(30);
        list.add(36, new Integer(97));
        list.remove(43);
        list.remove(2);
        list.remove(34);
        list.remove(35);
        list.remove(17);
        list.add(39, new Integer(98));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.add(29, new Integer(99));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.add(22, new Integer(100));
        for(int i = 0; i < 5; i++) {
            list.remove(list.size() - 1);
        }
        list.set(25, new Integer(101)); // critical
        for(int j = 0; j < 4; j++) {
            for(int i = 0; i < 5; i++) list.remove(0);
            list.add(0, new Integer(102));
        }
        for(int i = 0; i < 10; i++) list.remove(0);
        list.add(0, new Integer(107));
        for(int i = 0; i < 2; i++) list.remove(0);

        list.commitEvent();
    }

    private static class TransactionalEventList extends TransformedList {
        public TransactionalEventList(EventList source) {
            super(source);
            source.addListEventListener(this);
        }

        public void listChanged(ListEvent listChanges) {
            updates.forwardEvent(listChanges);
        }

        public void beginEvent() {
            updates.beginEvent(true);
        }

        public void commitEvent() {
            updates.commitEvent();
        }

        protected boolean isWritable() {
            return true;
        }
    }
}