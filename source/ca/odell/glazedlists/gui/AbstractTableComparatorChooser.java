/* Glazed Lists                                                 (c) 2003-2005 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.gui;

// Primary GlazedList package
import ca.odell.glazedlists.*;
// To track clicks
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
// For Comparators
import ca.odell.glazedlists.impl.sort.*;
import ca.odell.glazedlists.impl.gui.SortingState;
import ca.odell.glazedlists.impl.gui.ClickDoubleClickSortingStrategy;
import ca.odell.glazedlists.impl.gui.SortingStrategy;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a table
 * widget by clicking on the table's headers. It requires that the table has a
 * SortedList as a source as the sorting on that list is used.
 *
 * @author <a href="mailto:kevin@swank.ca">Kevin Maltby</a>
 */
public abstract class AbstractTableComparatorChooser<E> {

    /** the sorted list to choose the comparators for */
    protected SortedList<E> sortedList;

    /** the columns to sort over */
    private TableFormat<E> tableFormat;

    /** the potentially foreign comparator associated with the sorted list */
    protected Comparator<E> sortedListComparator = null;

    /** manage which columns are sorted and in which order */
    private final SortingState sortingState = new SortingState(this);

    /** the sorting strategy handles clicks and knows what to do with them. */
    private SortingStrategy sortingStrategy = null;

    /**
     * Create a {@link AbstractTableComparatorChooser} that sorts the specified
     * {@link SortedList} over the specified columns.
     */
    protected AbstractTableComparatorChooser(SortedList<E> sortedList, TableFormat<E> tableFormat) {
        this.sortedList = sortedList;
        this.setTableFormat(tableFormat);

        this.sortingState.addPropertyChangeListener(new SortingStateListener());
    }

    /**
     * Set the sorting strategy that this class uses to handle mouse clicks.
     */
    protected void setStrategy(SortingStrategy sortingStrategy) {
        // clean up the old sorting strategy
        if(this.sortingStrategy != null) {
            this.sortingStrategy.dispose();
        }

        // set up the new sorting strategy
        this.sortingStrategy = sortingStrategy;
        if(this.sortingStrategy != null) {
            this.sortingStrategy.setSortingState(sortingState);
        }
    }

    /**
     * Handle changes to the sorting state by applying the new comparator
     * to the {@link SortedList}.
     */
    private class SortingStateListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent propertyChangeEvent) {
            rebuildComparator();
        }
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    protected void rebuildComparator() {
        final Comparator<E> rebuiltComparator = sortingState.buildComparator();

        // select the new comparator
        sortedList.getReadWriteLock().writeLock().lock();
        try {
            sortedListComparator = rebuiltComparator;
            sortedList.setComparator(rebuiltComparator);
        } finally {
            sortedList.getReadWriteLock().writeLock().unlock();
        }
    }

    /**
     * Adjusts the TableFormat this comparator chooser uses when selecting
     * comparators. Calling this method will clear any active sorting.
     */
    protected void setTableFormat(TableFormat<E> tableFormat) {
        this.tableFormat = tableFormat;

        // handle a change in the layout of our columns
        sortingState.rebuildColumns(tableFormat);
    }


    /**
     * Gets the list of comparators for the specified column. The user is
     * free to add comparators to this list or clear the list if the specified
     * column cannot be sorted.
     */
    public List getComparatorsForColumn(int column) {
        return sortingState.getColumns().get(column).getComparators();
    }

    /**
     * Get the columns that the TableComparatorChooser is sorting by.
     *
     * @return a List of Integers. The first Integer is the primary sorting column,
     *      the second is the secondary, etc. This list may be empty but never null.
     */
    public List<Integer> getSortingColumns() {
        return sortingState.getSortingColumns();
    }

    /**
     * Gets the index comparator in use for the specified column. This comparator
     * may be retrieved using {@link #getComparatorsForColumn(int)}.
     *
     * @return the comparator index for the specified column, or -1 if that column
     *      is not being used to sort.
     */
    public int getColumnComparatorIndex(int column) {
        return sortingState.getColumns().get(column).getComparatorIndex();
    }

    /**
     * Gets whether the comparator in use for the specified column is reverse.
     */
    public boolean isColumnReverse(int column) {
        return sortingState.getColumns().get(column).isReverse();
    }

    /**
     * Append the comparator specified by the column, comparator index and reverse
     * parameters to the end of the sequence of comparators this
     * {@link AbstractTableComparatorChooser} is sorting the {@link SortedList}
     * by.
     *
     * <p><i>Append</i> implies that if this {@link AbstractTableComparatorChooser}
     * is already sorting that list by another column, this comparator will only
     * be used to break ties from that {@link Comparator}. If the table is already
     * sorting by the specified column, it will be silently discarded.
     *
     * <p>Suppose we're currently not sorting the table, this method will cause
     * the table to be sorted by the column specified. If we are sorting the table
     * by some column c, this will sort by that column first and the column
     * specified here second.
     *
     * <p>If this {@link AbstractTableComparatorChooser} doesn't support multiple
     * column sort, this will replace the current {@link Comparator} rather than
     * appending to it.
     *
     * @param column the column to sort by
     * @param comparatorIndex the comparator to use, specify <code>0</code> for the
     *      default comparator.
     * @param reverse whether to reverse the specified comparator.
     */
    public void appendComparator(int column, int comparatorIndex, boolean reverse) {
        sortingState.appendComparator(column, comparatorIndex, reverse);
        sortingState.fireSortingChanged();
    }

    /**
     * Clear all sorting state and set the {@link SortedList} to use its
     * natural order.
     */
    public void clearComparator() {
        sortingState.clearComparators();
        sortingState.fireSortingChanged();
    }

    /**
     * Handle a column being clicked by sorting that column.
     */
    protected void columnClicked(int column, int clicks) {
        ((ClickDoubleClickSortingStrategy)sortingStrategy).columnClicked(column, clicks);
        sortingState.fireSortingChanged();
    }

    /**
     * Examines the current {@link Comparator} of the SortedList and
     * adds icons to the table header renderers in response.
     *
     * <p>To do this, clicks are injected into each of the
     * corresponding <code>ColumnClickTracker</code>s.
     */
    protected void redetectComparator(Comparator<E> currentComparator) {
        sortedListComparator = currentComparator;
        sortingState.detectStateFromComparator(currentComparator);
    }

    /**
     * Gets the sorting style currently applied to the specified column.
     */
    protected int getSortingStyle(int column) {
        return sortingState.getColumns().get(column).getSortingStyle();
    }

    /**
     * Creates a {@link Comparator} that can compare list elements
     * given a {@link Comparator} that can compare column values for the specified
     * column. This returns a {@link Comparator} that extracts the table values for
     * the specified column and then delegates the actual comparison to the specified
     * comparator.
     */
    public Comparator createComparatorForElement(Comparator<E> comparatorForColumn, int column) {
        return new TableColumnComparator<E>(tableFormat, column, comparatorForColumn);
    }

    /**
     * Encode the current sorting state as a {@link String}. This specially formatted
     * {@link String} is ideal for persistence using any preferences API. The
     * state of this {@link AbstractTableComparatorChooser} can be restored
     * by passing the return value of this method to {@link #fromString(String)}.
     */
    public String toString() {
        return sortingState.toString();
    }

    /**
     * <p>This class is capable of representing its own state with a String, to
     * persist sorting state externally. The format uses zero or more column specifications,
     * separated by commas. Here are some valid examples:
     *
     * <table border><tr><th>String Representation</th><th>Description</th></tr>
     * <tr><td><code>"column 3"</code></td><td>Sort using the column at index 3, using that column's first comparator, in forward order</td></tr>
     * <tr><td><code>"column 3 reversed"</code></td><td>Sort using the column at index 3, using that column's first comparator, in reverse order</td></tr>
     * <tr><td><code>"column 3, column 1"</code></td><td>Sort using the column at index 3, using that column's first comparator, in forward order<br>
     *                                     <i>then by</i><br> the column at index 1, using that column's first comparator, in forward order.</td></tr>
     * <tr><td><code>"column 3 comparator 2"</code></td><td>Sort using the column at index 3, using that column's comparator at index 2, in forward order</td></tr>
     * <tr><td><code>"column 3 comparator 2 reversed"</code></td><td>Sort using the column at index 3, using that column's comparator at index 2, in reverse order</td></tr>
     * <tr><td><code>"column 3 reversed, column 1 comparator 2, column 5 comparator 1 reversed, column 0"</code></td><td>Sort using the column at index 3, using that column's first comparator, in reverse order<br>
     *                                     <i>then by</i><br> the column at index 1, using that column's comparator at index 2, in forward order<br>
     *                                     <i>then by</i><br> the column at index 5, using that column's comparator at index 1, in reverse order<br>
     *                                     <i>then by</i><br> the column at index 0, using that column's first comparator, in forward order.</td></tr>
     * </table>
     *
     * <p>More formally, the grammar for this String representation is as follows:
     * <br><code>&lt;COLUMN&gt; = column &lt;COLUMN INDEX&gt; (comparator &lt;COMPARATOR INDEX&gt;)? (reversed)?</code>
     * <br><code>&lt;COMPARATOR SPEC&gt; = ( &lt;COLUMN&gt; (, &lt;COLUMN&gt;)* )?</code>
     */
    public void fromString(String stringEncoded) {
        sortingState.fromString(stringEncoded);
        sortingState.fireSortingChanged();
    }

    public void dispose() {
        // null out references to potentially long lived objects
        this.sortedList = null;
        this.tableFormat = null;
        this.sortedListComparator = null;
        this.sortingStrategy.dispose();
    }
}