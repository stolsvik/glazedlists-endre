/* Glazed Lists                                                 (c) 2003-2008 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/

package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.event.ListEvent;
import ca.odell.glazedlists.gui.TableFormat;

import javax.swing.event.TableModelEvent;

/**
 * An extension of the {@link EventTableModel} for better integration with
 * JXTable.
 * <p>
 * In particular, this table model implements a different strategy to tranform
 * {@link ListEvent}s to {@link TableModelEvent}s. Whereas EventTableModel
 * converts each ListEvent block to a TableModelEvent, EventJXTableModel tries
 * to create only one TableModelEvent for a ListEvent, that does not represent a
 * reorder. If the ListEvent contains multiple blocks, a special
 * <em>data changed</em> TableModelEvent will be fired, indicating that all row
 * data has changed. Note, that such a <em>data changed</em> TableModelEvent can
 * lead to a loss of the table selection.
 * </p>
 *
 * @deprecated Use {@link DefaultEventTableModel} with a
 *             {@link GlazedListsSwing#manyToOneEventAdapterFactory()
 *             ManyToOneTableModelEventAdapter} instead. This class will be
 *             removed in the GL 2.0 release. The wrapping of the source list
 *             with an EDT safe list has been determined to be undesirable (it
 *             is better for the user to provide their own EDT safe list).
 *
 * @see GlazedListsSwing#eventTableModelWithThreadProxyList(EventList,
 *      TableFormat, ca.odell.glazedlists.swing.TableModelEventAdapter.Factory)
 * @see GlazedListsSwing#manyToOneEventAdapterFactory()
 * @author Holger Brands
 */
@Deprecated
public class EventJXTableModel<E> extends EventTableModel<E> {

    /**
     * {@inheritDoc}
     */
    public EventJXTableModel(EventList<E> source, String[] propertyNames, String[] columnLabels,
            boolean[] writable) {
        super(source, propertyNames, columnLabels, writable);
        setEventAdapter(GlazedListsSwing.<E>manyToOneEventAdapterFactory().create(this));
    }

    /**
     * {@inheritDoc}
     */
    public EventJXTableModel(EventList<E> source, TableFormat<? super E> tableFormat) {
        super(source, tableFormat);
        setEventAdapter(GlazedListsSwing.<E>manyToOneEventAdapterFactory().create(this));
    }
}
