/**
 * Glazed Lists
 * http://glazedlists.dev.java.net/
 *
 * COPYRIGHT 2003 O'DELL ENGINEERING LTD.
 */
package ca.odell.glazedlists.net;

// NIO is used for BRP
import java.util.*;
import java.nio.*;
import java.io.*;

/**
 * A resource is a dynamic Object that can publish its changes as a series of deltas.
 * It is also possible to construct a resource using a shapshot.
 *
 * @author <a href="mailto:jesse@odel.on.ca">Jesse Wilson</a>
 */
public interface Resource {

    /**
     * Get a binary snapshot of this resource in its current state.
     */
    public List toSnapshot();

    /**
     * Populate this resource with the data from the specified snapshot.
     */
    public void fromSnapshot(List snapshot);
    
    /**
     * Apply the specified delta to the binary image of this resource. After the
     * update has been applied, all {@link ResourceListener}s must be notified.
     */
    public void update(List delta);
    
    /**
     * Register the {@link ResourceListener} to receive notification when this
     * resource is modified.
     */
    public void addResourceListener(ResourceListener listener);
    
    /**
     * Degregister the {@link ResourceListener} from receiving update events.
     */
    public void removeResourceListener(ResourceListener listener);
    
}
