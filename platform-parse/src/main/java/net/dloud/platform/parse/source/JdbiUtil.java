package net.dloud.platform.parse.source;

import org.jdbi.v3.core.Handle;
import org.jdbi.v3.core.Jdbi;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashSet;
import java.util.Set;

/**
 * Utility for working with Jdbi and Spring transaction bound resources
 */
public class JdbiUtil {
    private static final Set<Handle> TRANSACTIONAL_HANDLES = new HashSet<>();

    private JdbiUtil() {
        throw new UnsupportedOperationException("utility class");
    }

    /**
     * Obtain a Handle instance, either the transactionally bound one if we are in a transaction,
     * or a new one otherwise.
     *
     * @param jdbi the Jdbi instance from which to obtain the handle
     * @return the Handle instance
     */
    public static Handle getHandle(Jdbi jdbi) {
        Handle bound = (Handle) TransactionSynchronizationManager.getResource(jdbi);
        if (bound == null) {
            bound = jdbi.open();
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.bindResource(jdbi, bound);
                TransactionSynchronizationManager.registerSynchronization(new Adapter(jdbi, bound));
                TRANSACTIONAL_HANDLES.add(bound);
            }
        }
        return bound;
    }

    /**
     * Close a handle if it is not transactionally bound, otherwise no-op
     *
     * @param handle the handle to consider closing
     */
    public static void closeIfNeeded(Handle handle) {
        if (!TRANSACTIONAL_HANDLES.contains(handle)) {
            handle.close();
        }
    }

    private static class Adapter extends TransactionSynchronizationAdapter {
        private final Jdbi db;
        private final Handle handle;

        Adapter(Jdbi db, Handle handle) {
            this.db = db;
            this.handle = handle;
        }

        @Override
        public void resume() {
            TransactionSynchronizationManager.bindResource(db, handle);
        }

        @Override
        public void suspend() {
            TransactionSynchronizationManager.unbindResource(db);
        }

        @Override
        public void beforeCompletion() {
            TRANSACTIONAL_HANDLES.remove(handle);
            TransactionSynchronizationManager.unbindResource(db);
        }
    }
}
