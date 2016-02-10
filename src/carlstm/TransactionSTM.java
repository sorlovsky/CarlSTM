package carlstm;

import carlstm.CarlSTM;
import carlstm.NoActiveTransactionException;
import carlstm.Transaction;
import carlstm.TransactionAbortedException;
import carlstm.TxObject;

/**
 * Created by simonorlovsky on 2/8/16.
 */
public class TransactionSTM {

    private static TxObject<Integer> x = new TxObject<Integer>(0);

    /**
     * A transaction that repeatedly increments the integer value stored in a
     * TxObject.
     */
    static class MyTransaction implements Transaction<Integer> {
        /*
         * (non-Javadoc)
         *
         * @see carlstm.Transaction#run()
         */
        private TxInfo info;
        @Override
        public Integer run() throws NoActiveTransactionException,
                TransactionAbortedException {
            try {
                info = new TxInfo();
                // This print may happen more than once if the transaction aborts
                // and restarts.

                // This loop repeatedly reads and writes a TxObject. The read and
                // write operations should all behave as if the entire transaction
                // happened exactly once, and as if there were no
                // intervening reads or writes from other threads.

                for (int i = 0; i < 5; i++) {
                    Integer val = x.read();
                    x.write(val + 1, info);
                    System.out.println(Thread.currentThread().getName()
                            + " wrote x = " + (val + 1));
                    Thread.yield();
                }
                return x.read();
            }
        }
    }

    /**
     * A Java Thread that executes a transaction and prints its result.
     */
    static class MyThread extends Thread {

        public TxInfo info = new TxInfo();
        /*
         * (non-Javadoc)
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            int result = CarlSTM.execute(new MyTransaction());

            // Should print 5 or 10, depending on which thread went first.
            System.out
                    .println(Thread.currentThread().getName() + ": " + result);
            Thread.currentThread().info;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        // Create two threads
        Thread thread1 = new MyThread();
        Thread thread2 = new MyThread();

        // Start the threads (executes MyThread.run)
        thread1.start();
        thread2.start();

        // Wait for the threads to finish.
        thread1.join();
        thread2.join();
    }
}
