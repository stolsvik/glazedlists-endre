/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists;

import ca.odell.glazedlists.impl.testing.GlazedListsTests;
import ca.odell.glazedlists.impl.testing.ListConsistencyListener;
import ca.odell.glazedlists.matchers.*;
import junit.framework.TestCase;

import java.util.List;

/**
 * Tests the generic FilterList class.
 */
public class FilterListTest extends TestCase {

    /**
     * This test ensures FilterList's generic arguments are correct.
     * Specifically, a FilterList<E> should be able to accept any Matcher<? super E>
     * and MatcherEditor<? super E>. In other words:
     *
     * <p>FilterList<Number> can accept a Matcher<Number> or Matcher<Object>, but not
     * a Matcher<Integer>.
     *
     * <p>FilterList<Number> can accept a MatcherEditor<Number> or MatcherEditor<Object>, but not
     * a MatcherEditor<Integer>.
     */
    public void testGenericsCompile() {
        final Matcher<Number> numberMatcher = new MinimumNumberMatcher(0);
        final MatcherEditor<Number> numberMatcherEditor = new MinimumNumberMatcherEditor();

        // constructor should accept Matcher<Object>
        new FilterList<Number>(new BasicEventList<Number>(), Matchers.falseMatcher());
        // constructor should accept Matcher<Number>
        new FilterList<Number>(new BasicEventList<Number>(), numberMatcher);
        // constructor should accept MatcherEditor<Object>
        new FilterList<Number>(new BasicEventList<Number>(), new AllOrNothingMatcherEditor());
        // constructor should accept MatcherEditor<Number>
        new FilterList<Number>(new BasicEventList<Number>(), numberMatcherEditor);

        final FilterList<Number> filtered = new FilterList<Number>(new BasicEventList<Number>());
        // setMatcher should accept Matcher<Object>
        filtered.setMatcher(Matchers.falseMatcher());
        // setMatcher should accept Matcher<Number>
        filtered.setMatcher(numberMatcher);
        // setMatcher should accept MatcherEditor<Object>
        filtered.setMatcherEditor(new AllOrNothingMatcherEditor());
        // setMatcher should accept MatcherEditor<Number>
        filtered.setMatcherEditor(numberMatcherEditor);
    }

    public void testRemovedValueInListEvent() {
        // construct a (contrived) list of initial values
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("LIMPBIZKIT"));

        CollectionMatcherEditor<String> collectionMatcherEditor = new CollectionMatcherEditor<String>();
        FilterList<String> filtered = new FilterList<String>(original, collectionMatcherEditor);

        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(filtered);
        listConsistencyListener.setPreviousElementTracked(true);

        original.removeAll(GlazedListsTests.stringToList("MI"));
        assertEquals(GlazedListsTests.stringToList("LPBZKT"), filtered);

        // constrained
        collectionMatcherEditor.setCollection(GlazedListsTests.stringToList("LPBZ"));
        assertEquals(GlazedListsTests.stringToList("LPBZ"), filtered);

        // changed
        collectionMatcherEditor.setCollection(GlazedListsTests.stringToList("PBZT"));
        assertEquals(GlazedListsTests.stringToList("PBZT"), filtered);

        // relaxed
        collectionMatcherEditor.setCollection(GlazedListsTests.stringToList("LPBZT"));
        assertEquals(GlazedListsTests.stringToList("LPBZT"), filtered);

        // match none
        collectionMatcherEditor.matchNone();
        assertEquals(GlazedListsTests.stringToList(""), filtered);

        // match all
        collectionMatcherEditor.matchAll();
        assertEquals(GlazedListsTests.stringToList("LPBZKT"), filtered);
    }


    public void testReplacedValueInListEvent() {
        // construct a (contrived) list of initial values
        EventList<String> original = new BasicEventList<String>();
        original.addAll(GlazedListsTests.stringToList("LIMPBIZKIT"));

        CollectionMatcherEditor<String> collectionMatcherEditor = new CollectionMatcherEditor<String>();
        collectionMatcherEditor.setCollection(GlazedListsTests.stringToList("LIPBZ"));
        FilterList<String> filtered = new FilterList<String>(original, collectionMatcherEditor);
        assertEquals(GlazedListsTests.stringToList("LIPBIZI"), filtered);

        ListConsistencyListener<String> listConsistencyListener = ListConsistencyListener.install(filtered);
        listConsistencyListener.setPreviousElementTracked(true);

        // updated with no change
        original.set(0, "Z");
        assertEquals(GlazedListsTests.stringToList("ZIPBIZI"), filtered);

        // updated out
        original.set(4, "E");
        assertEquals(GlazedListsTests.stringToList("ZIPIZI"), filtered);

        // updated in
        original.set(2, "Z");
        assertEquals(GlazedListsTests.stringToList("ZIZPIZI"), filtered);
    }

    /**
     * This test demonstrates Issue 213.
     */
    public void testRelax() {
        // construct a (contrived) list of initial values
        EventList<Integer> original = new BasicEventList<Integer>();
        List<Integer> values = GlazedListsTests.intArrayToIntegerCollection(new int [] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 10, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 0, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 1 });
        original.addAll(values);
        
        // prepare a filter to filter our list
        MinimumValueMatcherEditor editor = new MinimumValueMatcherEditor();
        FilterList<Integer> myFilterList = new FilterList<Integer>(original, editor);
        ListConsistencyListener<Integer> listConsistencyListener = ListConsistencyListener.install(myFilterList);
        listConsistencyListener.setPreviousElementTracked(true);


        // relax the list
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        
        // now try constrain
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 10, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11, 11 });
        original.clear();
        original.addAll(values);
        
        // constrain the list
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(11);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(12);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

        // now try more changes
        values = GlazedListsTests.intArrayToIntegerCollection(new int[] { 8, 6, 7, 5, 3, 0, 9 });
        original.clear();
        original.addAll(values);

        // constrain the list
        editor.setMinimum(5);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(10);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(0);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));
        editor.setMinimum(1);
        assertEquals(myFilterList, GlazedListsTests.filter(original, editor.getMatcher()));

    }

	/**
	 * Test Matchers that fire matchAll() and matchNone() events.
	 */
	public void testMatchAllOrNothing() {
		EventList<Integer> baseList = new BasicEventList<Integer>();
		baseList.add(new Integer(1));
		baseList.add(new Integer(2));
		baseList.add(new Integer(3));
		baseList.add(new Integer(4));
		baseList.add(new Integer(5));

		AllOrNothingMatcherEditor matcher = new AllOrNothingMatcherEditor();
		FilterList<Integer> filterList = new FilterList<Integer>(baseList,matcher);
        ListConsistencyListener<Integer> listConsistencyListener = ListConsistencyListener.install(filterList);
        listConsistencyListener.setPreviousElementTracked(true);

		// Test initial size
		assertEquals(5, filterList.size());

		// Clear it
		matcher.showAll(false);
		assertEquals(0, filterList.size());

		// Clear it again
		matcher.showAll(false);
		assertEquals(0, filterList.size());

		// Put it back
		matcher.showAll(true);
		assertEquals(5, filterList.size());

		// Put it back again
		matcher.showAll(true);
		assertEquals(5, filterList.size());
	}

    public void testDispose() {
        EventList<String> baseList = GlazedLists.eventList(GlazedListsTests.stringToList("ABCCBA"));
        FilterList<String> filterList = new FilterList<String>(baseList);

        GlazedListsTests.ListEventCounter<String> counter = new GlazedListsTests.ListEventCounter<String>();
        filterList.addListEventListener(counter);

        TextMatcherEditor<String> editor = new TextMatcherEditor<String>(GlazedLists.toStringTextFilterator());
        filterList.setMatcherEditor(editor);

        assertEquals(0, counter.getCountAndReset());

        editor.setFilterText(new String[] {"B"});
        assertEquals(1, counter.getCountAndReset());

        // dispose() should not produce any ListEvents, and the MatcherEditor should be nulled out
        filterList.dispose();
        assertEquals(0, counter.getCountAndReset());

        // the editor should have been disconnected during dispose(), so this should produce no ListEvent
        editor.setFilterText(new String[] {"C"});
        assertEquals(0, counter.getCountAndReset());
    }
}


/**
 * A Matcher for minimum Number values.
 */
class MinimumNumberMatcher implements Matcher<Number> {
    private final int number;
    public MinimumNumberMatcher(int number) {
        this.number = number;
    }
    public boolean matches(Number item) {
        return item != null && item.intValue() >= number;
    }
}

/**
 * A MatcherEditor for minimum Number values.
 */
class MinimumNumberMatcherEditor extends AbstractMatcherEditor<Number> {
    private int minimum = 0;
    public MinimumNumberMatcherEditor() {
        currentMatcher = new MinimumNumberMatcher(0);
    }
    public void setMinimum(int value) {
        if(value < minimum) {
            this.minimum = value;
            fireRelaxed(new MinimumNumberMatcher(minimum));
        } else if(value == minimum) {
            // do nothing
        } else {
            this.minimum = value;
            fireConstrained(new MinimumNumberMatcher(minimum));
        }
    }
}

/**
 * A MatcherEditor for minimum values.
 */
class MinimumValueMatcherEditor extends AbstractMatcherEditor<Integer> {
    private int minimum = 0;
    public MinimumValueMatcherEditor() {
        currentMatcher = GlazedListsTests.matchAtLeast(0);
    }
    public void setMinimum(int value) {
        if(value < minimum) {
            this.minimum = value;
            fireRelaxed(GlazedListsTests.matchAtLeast(minimum));
        } else if(value == minimum) {
            // do nothing
        } else {
            this.minimum = value;
            fireConstrained(GlazedListsTests.matchAtLeast(minimum));
        }
    }
}

/**
 * Matcher that allows testing matchAll() and matchNone().
 */
class AllOrNothingMatcherEditor extends AbstractMatcherEditor {
    /**
     * @param state True show everything, otherwise show nothing
     */
    public void showAll(boolean state) {
        if (state) fireMatchAll();
        else fireMatchNone();
    }
}
