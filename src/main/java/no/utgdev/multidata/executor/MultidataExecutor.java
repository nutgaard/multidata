package no.utgdev.multidata.executor;


import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

import javax.transaction.Status;
import javax.transaction.TransactionManager;
import java.util.List;
import java.util.concurrent.Callable;

public class MultidataExecutor<T> {
    private final TransactionManager transactionManager;
    private final TransactionManagerStrategy transactionManagerStrategy;

    public MultidataExecutor(TransactionManager transactionManager, TransactionManagerStrategy transactionManagerStrategy) {
        this.transactionManager = transactionManager;
        this.transactionManagerStrategy = transactionManagerStrategy;
    }

    public Object execute(List<Callable> sourceJobs) {
        if (this.transactionManager != null && this.transactionManagerStrategy != null) {
            if (this.transactionManagerStrategy == TransactionManagerStrategy.ALL) {
                return executeAllTransacted(sourceJobs);
            } else {
                return executeSingleTransacted(sourceJobs);
            }
        } else {
            return executeUntransacted(sourceJobs);
        }
    }

    private Object executeUntransacted(List<Callable> sourceJobs) {
        Object result = null;

        for (Callable job : sourceJobs) {
            Try<Object> tried = Try.of((CheckedFunction0<Object>) job::call);
            result = tried.isFailure() ? result : tried.get();
        }

        return result;
    }

    private Object executeSingleTransacted(List<Callable> sourceJobs) {
        Object result = null;

        for (Callable job : sourceJobs) {
            try {
                result = withTx(this.transactionManager, job);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return result;
    }

    private Object executeAllTransacted(List<Callable> sourceJobs) {
        Callable allJobs = () -> {
            Object result = null;

            for (Callable job : sourceJobs) {
                Try<Object> tried = Try.of((CheckedFunction0<Object>) job::call);
                result = tried.isFailure() ? result : tried.get();
            }

            return result;
        };

        try {
            return withTx(this.transactionManager, allJobs);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    public static <T> T withTx(TransactionManager tm, Callable<T> c) throws Exception {
        tm.begin();
        try {
            return c.call();
        } catch (Exception e) {
            tm.setRollbackOnly();
            throw e;
        } finally {
            if (tm.getStatus() == Status.STATUS_ACTIVE) tm.commit();
            else tm.rollback();
        }
    }
}
