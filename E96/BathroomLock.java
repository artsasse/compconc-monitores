package E96;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BathroomLock implements Bathroom {
    Lock lock;
    Condition condition;
    int qtdMale, qtdFemale;

    public BathroomLock() {
        lock = new ReentrantLock(true);
        condition = lock.newCondition();
        qtdMale = 0;
        qtdFemale = 0;
    }

    @Override
    public void enterMale() {
        try {
            lock.lock();
            try {
                while (qtdFemale > 0) {
                    condition.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            qtdMale++;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void enterFemale() {
        try {
            lock.lock();
            try {
                while (qtdMale > 0) {
                    condition.await();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void leaveMale() {
        try {
            lock.lock();
            qtdMale--;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void leaveFemale() {
        try {
            lock.lock();
            qtdFemale--;
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }
}
