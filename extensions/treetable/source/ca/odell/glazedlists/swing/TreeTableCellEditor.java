package ca.odell.glazedlists.swing;

import ca.odell.glazedlists.TreeList;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.EventObject;

/**
 * This editor removes some of the burden of producing an appropriate looking
 * component for the hierarchy column of a tree table. Specifically, it takes
 * care of adding components to a panel that render a node location within the
 * tree, but leaves the rendering of the <strong>data</strong> of the node to
 * a delegate {@link TableCellEditor} that is supplied in the constructor.
 *
 * <p>For example, in the following tree representation, the spacing and +/-
 * icons would be added to each tree node by this editor, while the data text
 * would be added by the component returned from the delegate
 * {@link TableCellEditor}.
 *
 * <pre>
 * - Cars
 *   + BMW
 *   - Ford
 *       Taurus
 *       Focus
 *   - Lexus
 *       ES 300
 *       LS 600h L
 * </pre>
 *
 * @author James Lemieux
 */
public class TreeTableCellEditor extends AbstractCellEditor implements TableCellEditor {

    /** The panel capable of laying out a spacer component, expander button, and data Component to produce the entire tree node display. */
    private final TreeTableCellPanel component = new TreeTableCellPanel();

    /** The data structure that answers questions about the tree node. */
    private TreeList treeList;

    /** The user-supplied editor that produces the look of the tree node's data. */
    private TableCellEditor delegate;

    /** <tt>true</tt> indicates the expander button should be visible even if the parent has no children. */
    private boolean showExpanderForEmptyParent;

    /** Respond to editing changes in the delegate TableCellEditor. */
    private final CellEditorListener delegateListener = new DelegateTableCellEditorListener();

    /**
     * Decorate the component returned from the <code>delegate</code> with
     * extra components that display the tree nodes location within the tree.
     * If <code>delegate</code> is <tt>null</tt> then a
     * {@link DefaultCellEditor} using a {@link JTextField} will be used as
     * the delegate.
     *
     * @param delegate the editor that produces the data for the tree node
     * @param treeList the data structure that answers questions about the tree
     *      node and the tree that contains it
     */
    public TreeTableCellEditor(TableCellEditor delegate, TreeList treeList) {
        this.delegate = delegate == null ? createDelegateEditor() : delegate;
        this.delegate.addCellEditorListener(delegateListener);
        this.treeList = treeList;
    }

    /**
     * Build the delegate TableCellEditor that handles editing the data of
     * each tree node.
     */
    protected TableCellEditor createDelegateEditor() {
        final JTextField textField = new JTextField();
        textField.setBorder(BorderFactory.createEmptyBorder(0, 1, 0, 0));
        return new DefaultCellEditor(textField);
    }

    /**
     * Return a decorated form of the component returned by the data
     * {@link TableCellEditor}.
     */
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        final Component c = delegate.getTableCellEditorComponent(table, value, isSelected, row, column);

        final int depth;
        final boolean isExpanded;
        final boolean hasChildren;
        final boolean allowsChildren;

        treeList.getReadWriteLock().readLock().lock();
        try {
            // read information about the tree node from the TreeList
            depth = treeList.depth(row);
            isExpanded = treeList.isExpanded(row);
            hasChildren = treeList.hasChildren(row);
            allowsChildren = treeList.getAllowsChildren(row);
        } finally {
            treeList.getReadWriteLock().readLock().unlock();
        }

        // ask our special component to configure itself for this tree node
        component.configure(depth, hasChildren, showExpanderForEmptyParent, allowsChildren, isExpanded, c, false);
        return component;
    }

   /**
    * This method checks if the <code>event</code> is a <code>MouseEvent</code>
    * and determines if it occurred overtop of the component created by the
    * delegate TableCellEditor. If so, it translates the coordinates of the
    * <code>event</code> so they are relative to that component and then asks
    * the delegate TableCellEditor if it believes supports cell editing.
    *
    * Effectively, this implies that clicks overtop of other areas of the
    * editor component are ignored. In truth, they are handled elsewhere (a
    * MouseListener on the JTable installed within TreeTableSupport).
    *
    * @param event the event attempting to begin a cell edit
    * @return true if cell is ready for editing, false otherwise
    */
    public boolean isCellEditable(EventObject event) {
        if (event instanceof MouseEvent) {
            final MouseEvent me = (MouseEvent) event;

            // we're going to check if the single click was overtop of the
            // node component and give a translated version of the
            // MouseEvent to the delegate TableCellEditor if it was

            final TreeTableCellPanel renderedPanel = TreeTableUtilities.prepareRenderer(me);

            // if the click occurred over the node component
            if (renderedPanel != null && renderedPanel.isPointOverNodeComponent(me.getPoint())) {
                // shift the click over by the expand icon and space
                final Rectangle delegateRendererBounds = renderedPanel.getNodeComponent().getBounds();
                final MouseEvent translatedMouseEvent = new MouseEvent(me.getComponent(), me.getID(), me.getWhen(), me.getModifiers(), me.getX() - delegateRendererBounds.x, me.getY() - delegateRendererBounds.y , me.getClickCount(), me.isPopupTrigger(), me.getButton());

                // allow the actual editor to decide
                return delegate.isCellEditable(translatedMouseEvent);
            }

            return false;
        }

        // otherwise, we're here because of an unrecognized EventObject
        return super.isCellEditable(event);
    }

    /** @inheritDoc */
    public boolean shouldSelectCell(EventObject anEvent) {
        return delegate.shouldSelectCell(anEvent);
    }

    /** @inheritDoc */
    public Object getCellEditorValue() {
        return delegate.getCellEditorValue();
    }

    /**
     * If <code>b</code> is <tt>true</tt> then the expand/collapse button must
     * be displayed for nodes which allow children but do not currently have
     * children. This implies that empty tree nodes with the
     * <strong>potential</strong> for children may be displayed differently
     * than pure leaf nodes which are guaranteed to never have children.
     */
    void setShowExpanderForEmptyParent(boolean b) {
        showExpanderForEmptyParent = b;
    }

    /**
     * Use the given <code>delegate</code> to edit the data associated with
     * each tree node.
     */
    void setDelegate(TableCellEditor delegate) {
        this.delegate = delegate;
    }

    /**
     * Returns the delegate TableCellEditor being decorated.
     */
    public TableCellEditor getDelegate() {
        return delegate;
    }

    /**
     * Cleanup the data within this editor as it is no longer being used and
     * must be prepared for garbage collection.
     */
    public void dispose() {
        delegate.removeCellEditorListener(delegateListener);

        delegate = null;
        treeList = null;
    }

    /**
     * When the delegate TableCellEditor changes its editing state, we follow suit.
     */
    private class DelegateTableCellEditorListener implements CellEditorListener {
        public void editingCanceled(ChangeEvent e) {
            cancelCellEditing();
        }
        public void editingStopped(ChangeEvent e) {
            stopCellEditing();
        }
    }
}