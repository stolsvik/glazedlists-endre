/* Glazed Lists                                                 (c) 2003-2006 */
/* http://publicobject.com/glazedlists/                      publicobject.com,*/
/*                                                     O'Dell Engineering Ltd.*/
package ca.odell.glazedlists.util.concurrent;

/**
 * This factory provides an implementation of {@link Lock} that is optimized
 * for the current Java Virtual Machine.
 *
 * @author <a "mailto:rob@starlight-systems.com">Rob Eden</a>
 * @author James Lemieux
 */
public interface LockFactory {

    /** The Lock factory for this JVM. */
    public static final LockFactory DEFAULT = new DelegateLockFactory();

    /**
     * Create a {@link ReadWriteLock}.
     */
    public ReadWriteLock createReadWriteLock();

    /**
     * Create a {@link Lock}.
     */
    public Lock createLock();
}

/**
 * An implementation of {@link LockFactory} that detects and delegates to
 * a JVM specific LockFactory implementation optimized for the current JVM.
 */
class DelegateLockFactory implements LockFactory {

    /** The true JVM-specific LockFactory to which we delegate. */
    private LockFactory delegate;

    DelegateLockFactory() {
        try {
            // if the J2SE 5.0 ReadWriteLock class can be loaded, we're running on a JDK 1.5 VM
            Class.forName("java.util.concurrent.locks.ReadWriteLock");

            // and if we can load our J2SE 5.0 LockFactory implementation
            // (i.e. it's not a Glazed Lists 1.4 implementation running on a JDK 1.5 VM)
            // then use the J2SE 5.0 LockFactory implementation
            delegate = (LockFactory) Class.forName("ca.odell.glazedlists.impl.java15.J2SE50LockFactory").newInstance();

        } catch (Throwable t) {
            // otherwise fall back to a J2SE 1.4 LockFactory
            delegate = new J2SE14LockFactory();
        }
    }

    @Override
    public ReadWriteLock createReadWriteLock() {
        return delegate.createReadWriteLock();
    }

    @Override
    public Lock createLock() {
        return delegate.createLock();
    }
}