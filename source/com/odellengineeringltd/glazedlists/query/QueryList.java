/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.query;

// the core Glazed Lists packages
import com.odellengineeringltd.glazedlists.*;
import com.odellengineeringltd.glazedlists.event.*;
// Java collections are used for underlying data storage
import java.util.*;
// For calling methods on the event dispacher thread
import javax.swing.SwingUtilities;
// for iterating over a mutation list the lazy way
import com.odellengineeringltd.glazedlists.util.EventListIterator;


/**
 * A QueryList is a list who controls its contents by a query. The QueryList
 * may re-run the query on a specified interval to ensure that the list is always
 * up-to-date. When a user edits an object, the user should notify the lists with
 * the updated object so that the list can add, update (repaint) or remove the
 * object if necessary.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class QueryList extends AbstractList implements EventList {

    /** the arraylist that holds this querylist's data */
    private ArrayList values = new ArrayList();

    /** the treeset mirrors the values, but it is sorted */
    private SortedSet before = new TreeSet();
    
    /** the query containing this list's elements */
    protected Query query = null;

    /** the change event and notification system */
    protected ListChangeSequence updates = new ListChangeSequence();

    /**
     * Creates a new QueryList. This list cannot be manipulated directly. The only
     * way to change the values is to change the results from the query, either by
     * changing the query or by changing its results.
     */
    public QueryList() {
    }
    
    /**
     * Whenever a user or external force updates an object that may be in this list
     * the list should be notified.
     *
     * <li>If the updated object is already in the list and it still belongs in the list
     * after the update, it will be updated.
     * <li>If the updated object is already in the list and it no longer belongs in the
     * list after the update, it will be removed.
     * <li>If the updated object is not alreayd in the list and it belongs in the list,
     * it will be added.
     */
    public synchronized void notifyObjectUpdated(Comparable updated) {
        boolean queryMatches = false;
        if(query != null) queryMatches = query.matchesObject(updated);
        int listIndex = values.indexOf(updated);
        updates.beginAtomicChange();
        // when it matches the query and its not in the list, add it!
        if(queryMatches && listIndex == -1) {
            // find the best place to insert this item into the list
            int insertLocation = 0;
            for(; insertLocation < values.size(); insertLocation++) {
                Comparable listElement = (Comparable)values.get(insertLocation);
                if(updated.compareTo(listElement) > 0) continue;
                else break;
            }
            updates.appendChange(insertLocation, ListChangeBlock.INSERT);
            before.add(updated);
            values.add(insertLocation, updated);
        // when it matches and it is in the list, update it!
        } else if(queryMatches && listIndex != -1) {
            updates.appendChange(listIndex, ListChangeBlock.UPDATE);
            Object old = values.get(listIndex);
            values.set(listIndex, updated);
            // update the before copy by removing and re-adding it
            before.remove(old);
            before.add(updated);
        // when it doesn't match and it is in the list, remove it!
        } else if(!queryMatches && listIndex != -1) {
            updates.appendChange(listIndex, ListChangeBlock.DELETE);
            values.remove(listIndex);
            before.remove(updated);
        }
        updates.commitAtomicChange();
    }
    
    /**
     * Whenever the query runs, it should call this method. This will notify all the
     * appropriate listeners about objects being updated, inserted and removed.
     *
     * @return the number of changes (inserts, deletes) as a consequence of this update.
     */
    protected void setQueryResults(SortedSet after) {
        // skip these results if the set is null
        if(after == null) return;
        // keep a running list of what's been added and what's been deleted
        updates.beginAtomicChange();
        // use a temporary values list
        ArrayList updatedValues = new ArrayList();
        // iterate through the all values simultaneously, looking for differences
        Iterator beforeIterator = before.iterator();
        Iterator afterIterator = after.iterator();
        // set up the current objects to examine
        Comparable currentBefore = null;
        Comparable currentAfter = null;
        if(beforeIterator.hasNext()) currentBefore = (Comparable)beforeIterator.next();
        // for all elements in the update set
        while(afterIterator.hasNext()) {
            // get the current pivot - the element to manage on this iteration
            currentAfter = (Comparable)afterIterator.next();
            // when the before list holds items smaller than the after list item,
            // the before list items are out-of-date and must be deleted 
            while(currentBefore != null && currentAfter.compareTo(currentBefore) > 0) {
                updates.appendChange(updatedValues.size(), ListChangeBlock.DELETE);
                if(beforeIterator.hasNext()) {
                    currentBefore = (Comparable)beforeIterator.next();
                } else { 
                    currentBefore = null;
                }
            }
            // when the before list holds an item identical to the after list item,
            // the item has not changed
            if(currentBefore != null && currentAfter.compareTo(currentBefore) == 0) {
                updatedValues.add(currentAfter);
                if(beforeIterator.hasNext()) {
                    currentBefore = (Comparable)beforeIterator.next();
                } else { 
                    currentBefore = null;
                }
            // when the before list holds no more items or an item that is larger than
            // the current after list item, insert the after list item
            } else {
                updates.appendChange(updatedValues.size(), ListChangeBlock.INSERT);
                updatedValues.add(currentAfter);
            }
        }
        // when the before list holds items larger than the largest after list item,
        // the before list items are out-of-date and must be deleted 
        while(currentBefore != null) {
            updates.appendChange(updatedValues.size(), ListChangeBlock.DELETE);
            if(beforeIterator.hasNext()) {
                currentBefore = (Comparable)beforeIterator.next();
            } else {
                currentBefore = null;
            }
        }
        // now that the change has occurred, store the updates
        synchronized(this) {
            values = updatedValues;
            before = after;
            // fire all the changes to change listeners
            updates.commitAtomicChange();
        }
    }
    
    /**
     * Gets the specified element from the list.
     */
    public synchronized Object get(int index) {
        return values.get(index);
    }

    /**
     * Gets the total number of elements in the list.
     */
    public synchronized int size() {
        return values.size();
    }
    
    /**
     * Gets the current query from the query list.
     */
    public Query getQuery() {
        return query;
    }

    /**
     * Registers the specified listener to receive notification of changes
     * to this list.
     */
    public final void addListChangeListener(ListChangeListener listChangeListener) {
        updates.addListChangeListener(listChangeListener);
    }

    /**
     * Removes the specified listener from receiving change updates for this list.
     */
    public void removeListChangeListener(ListChangeListener listChangeListener) {
        updates.removeListChangeListener(listChangeListener);
    }

    /**
     * For implementing the EventList interface. As a QueryList is a root
     * list, this always returns <code>this</code>.
     */
    public EventList getRootList() {
        return this;
    }

    /**
     * Returns an iterator over the elements in this list in proper sequence.
     */
    public Iterator iterator() {
        return new EventListIterator(this);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence).
     */
    public ListIterator listIterator() {
        return new EventListIterator(this);
    }

    /**
     * Returns a list iterator of the elements in this list (in proper
     * sequence), starting at the specified position in this list.
     */
    public ListIterator listIterator(int index) {
        return new EventListIterator(this, index);
    }

    /**
     * Gets this list in String form for debug or display.
     */
    public String toString() {
        synchronized(getRootList()) {
            StringBuffer result = new StringBuffer();
            result.append("[");
            for(int i = 0; i < size(); i++) {
                if(i != 0) result.append(", ");
                result.append(get(i));
            }
            result.append("]");
            return result.toString();
        }
    }
}
