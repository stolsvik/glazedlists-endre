/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package com.odellengineeringltd.glazedlists.util;

// the core Glazed Lists package
import com.odellengineeringltd.glazedlists.*;
// the Glazed Lists' change objects
import com.odellengineeringltd.glazedlists.event.*;

/**
 * A caching list is a mutation list that caches elements in the source list.
 * It should be used when a get() on the source list requires a database lookup
 * or calculation. It is used when there are too many objects to keep in
 * memory simultaneously and object access happens in clusters.
 *
 * <p>This list simply remembers the last <i>n</i> elements which were requested
 * or set. Accesses to such elements does not perform a recursive operation
 * on the source list, instead the element is fetched from the cache.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public class CachingList extends WritableMutationList implements ListChangeListener, EventList {

    /** the cache is implemented using a bounded map with indexes as keys */
    private BoundedMap cache;
    
    /** the count of cache hits and cache misses for testing cache success */
    private int cacheHits;
    private int cacheMisses;

    /**
     * Creates a new Caching list that uses the specified source list and
     * has the specified maximum cache size.
     */
    public CachingList(EventList source, int maxSize) {
        super(source);
        
        cache = new BoundedMap(maxSize);
    }
    
    /**
     * Returns the number of elements in this list. 
     */
    public int size() {
        return source.size();
    }
    
    /**
     * Returns the element at the specified position in this list.
     */
    public Object get(int index) {
        Integer indexObject = new Integer(index);
        Object value = null;
        
        // attempt to get the element from the cache
        value = cache.get(indexObject);
        if(value != null) {
            System.out.print("C");
            cacheHits++;
            return value;
        }

        // get the element from the source list and cache it
        cacheMisses++;
        System.out.print(".");
        value = source.get(index);
        cache.put(indexObject, value);
        return value;
    }
    
    
    /**
     * Gets the index into the source list for the object with the specified
     * index in this list.
     */
    protected int getSourceIndex(int mutationIndex) {
        return mutationIndex;
    }
    
    /**
     * Tests if this mutation shall accept calls to <code>add()</code>,
     * <code>remove()</code>, <code>set()</code> etc.
     */
    protected boolean isWritable() {
        return true;
    }


    /**
     * Gets the total number of times that this list has fetched its result
     * from the cache rather than from the source list.
     */
    public int getCacheHits() {
        return cacheHits;
    }

    /**
     * Gets the total number of times that this list has fetched its result
     * from the source list rather than the cache.
     */
    public int getCacheMisses() {
        return cacheMisses;
    }

    /**
     * Gets the ratio of cache hits to cache misses. This is a number between
     * 0 and 1, where 0 means the cache is unused and 1 means the cache was
     * used exclusively.
     */
    public float getCacheHitRatio() {
        if(cacheHits + cacheMisses == 0) return 0.0F;
        return (float)cacheHits / (float)(cacheHits + cacheMisses);
    }

    /**
     * When the list is changed the cache is cleared. This could be replaced
     * by a more efficient implementation which iterated through the cache
     * and updated elements whose indexes changed.
     */
    public void notifyListChanges(ListChangeEvent listChanges) {
        cache.clear();

        // just pass on the changes
        updates.beginAtomicChange();
        while(listChanges.next()) {
            updates.appendChange(listChanges.getIndex(), listChanges.getType());
        }
        updates.commitAtomicChange();
    }
}
