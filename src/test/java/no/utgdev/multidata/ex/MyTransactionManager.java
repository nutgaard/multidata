package no.utgdev.multidata.ex;

import javax.transaction.*;

public class MyTransactionManager implements javax.transaction.TransactionManager {
    private int status = -1;

    private static void log(String method) {
        System.out.println("[MyTransactionManager::" + method +"]");
    }

    @Override
    public void begin() throws NotSupportedException, SystemException {
        log("begin");
        status = Status.STATUS_ACTIVE;
    }

    @Override
    public void commit() throws RollbackException, HeuristicMixedException, HeuristicRollbackException, SecurityException, IllegalStateException, SystemException {
        log("commit");
        status = 0;
    }

    @Override
    public int getStatus() throws SystemException {
        return status;
    }

    @Override
    public Transaction getTransaction() throws SystemException {
        return null;
    }

    @Override
    public void resume(Transaction transaction) throws InvalidTransactionException, IllegalStateException, SystemException {

    }

    @Override
    public void rollback() throws IllegalStateException, SecurityException, SystemException {
        log("rollback");
        status = Status.STATUS_ROLLING_BACK;
    }

    @Override
    public void setRollbackOnly() throws IllegalStateException, SystemException {
        log("setRollbackOnly");
        status = Status.STATUS_MARKED_ROLLBACK;
    }

    @Override
    public void setTransactionTimeout(int i) throws SystemException {

    }

    @Override
    public Transaction suspend() throws SystemException {
        return null;
    }
}
