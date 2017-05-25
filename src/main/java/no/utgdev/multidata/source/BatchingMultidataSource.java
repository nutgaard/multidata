package no.utgdev.multidata.source;

import no.utgdev.multidata.Utils;

import javax.transaction.TransactionManager;
import java.util.List;
import java.util.concurrent.Callable;

import static no.utgdev.multidata.executor.MultidataExecutor.withTx;

public class BatchingMultidataSource<T> implements MultidataSource<T> {
    private final MultidataSource<T> source;
    private final int batchsize;
    private final TransactionManager transactionManager;

    public BatchingMultidataSource(MultidataSource<T> source, int batchsize, TransactionManager transactionManager) {
        this.source = source;
        this.batchsize = batchsize;
        this.transactionManager = transactionManager;
    }

    @Override
    public T save(T data) {
        return this.source.save(data);
    }

    @Override
    public List<T> save(List<T> data) {
        if (this.transactionManager != null) {
            return saveTransacted(data);
        }
        return Utils.batchExec(this.batchsize, this.source::save, data);
    }

    private List<T> saveTransacted(List<T> data) {
        return Utils.batchExec(this.batchsize, objects -> {
            Callable<List<T>> doit = () -> this.source.save(objects);
            try {
                return withTx(this.transactionManager, doit);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }, data);
    }
}
