/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.swt;

// the core Glazed Lists packages
import ca.odell.glazedlists.*;
// the Glazed Lists util and volatile packages for default comparators
import ca.odell.glazedlists.util.*;
import ca.odell.glazedlists.gui.*;
import ca.odell.glazedlists.impl.sort.*;
// concurrency is similar to java.util.concurrent in J2SE 1.5
import ca.odell.glazedlists.util.concurrent.*;
// for keeping lists of comparators
import java.util.*;
// SWT toolkit stuff for displaying widgets
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.events.*;

/**
 * A TableComparatorChooser is a tool that allows the user to sort a ListTable by clicking
 * on the table's headers. It requires that the ListTable has a SortedList as
 * a source as the sorting on that list is used.
 *
 * <p><strong>Warning:</strong> This class is a a developer preview and subject to
 * many bugs and API changes.
 *
 * @see <a href="http://dev.eclipse.org/viewcvs/index.cgi/org.eclipse.swt.snippets/src/org/eclipse/swt/snippets/Snippet2.java?rev=HEAD">Snippet 2</a>
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public final class TableComparatorChooser extends AbstractTableComparatorChooser {

    /** the table being sorted */
    private Table table;

    /** listeners to sort change events */
    private List sortListeners = new ArrayList();

    /**
     * Creates a new TableComparatorChooser that responds to clicks
     * on the specified table and uses them to sort the specified list.
     *
     * @param eventTableViewer the table viewer for the table to be sorted
     * @param sortedList the sorted list to update.
     * @param multipleColumnSort <code>true</code> to sort by multiple columns
     *      at a time, or <code>false</code> to sort by a single column. Although
     *      sorting by multiple columns is more powerful, the user interface is
     *      not as simple and this strategy should only be used where necessary.
     */
    public TableComparatorChooser(EventTableViewer eventTableViewer, SortedList sortedList, boolean multipleColumnSort) {
        super(sortedList, eventTableViewer.getTableFormat(), multipleColumnSort);
        
        // save the SWT-specific state
        this.table = eventTableViewer.getTable();

        // listen for events on the specified table
        for(int c = 0; c < table.getColumnCount(); c++) {
            table.getColumn(c).addSelectionListener(new ColumnListener(c));
        }
    }

    /**
     * Registers the specified {@link Listener} to receive notification whenever
     * the {@link Table} is sorted by this {@link TableComparatorChooser}.
     */
    public void addSortListener(final Listener sortListener) {
        sortListeners.add(sortListener);
    }
    /**
     * Deregisters the specified {@link Listener} to no longer receive events.
     */
    public void removeSortActionListener(final Listener sortListener) {
        for(Iterator i = sortListeners.iterator(); i.hasNext(); ) {
            if(sortListener == i.next()) {
                i.remove();
                return;
            }
        }
        throw new IllegalArgumentException("Cannot remove nonexistant listener " + sortListener);
    }

    /**
     * Handles column clicks.
     */
    class ColumnListener implements SelectionListener {
        private int column;
        public ColumnListener(int column) {
            this.column = column;
        }
        public void widgetSelected(SelectionEvent e) {
            columnClicked(column, 1);
        }
        public void widgetDefaultSelected(SelectionEvent e) {
            // Do Nothing
        }
    }

    /**
     * Updates the comparator in use and applies it to the table.
     */
    protected final void rebuildComparator() {
        super.rebuildComparator();

        // notify interested listeners that the sorting has changed
        Event sortEvent = new Event();
        sortEvent.widget = table;
        for(Iterator i = sortListeners.iterator(); i.hasNext(); ) {
            Listener listener = (Listener)i.next();
            listener.handleEvent(sortEvent);
        }
    }
}
