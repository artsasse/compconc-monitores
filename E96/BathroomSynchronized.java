package E96;

public class BathroomSynchronized implements Bathroom {
    final Object lock;
    volatile int qtdMale, qtdFemale;
    volatile boolean occupied;

    public BathroomSynchronized() {
        lock = new Object();
        qtdMale = 0;
        qtdFemale = 0;
        occupied = false;
    }

    @Override
    public void enterMale() {
        synchronized (lock) {
            try {
                while (qtdFemale > 0 && occupied) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                qtdMale++;
                occupied = true;
            }
        }
    }

    @Override
    public void enterFemale() {
        synchronized (lock) {
            try {
                while (qtdMale > 0 && occupied) {
                    lock.wait();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                qtdFemale++;
                occupied = true;
            }
        }
    }

    @Override
    public void leaveMale() {
        synchronized (lock) {
            qtdMale--;
            occupied = false;
            lock.notifyAll();
        }
    }

    @Override
    public void leaveFemale() {
        synchronized (lock) {
            qtdFemale--;
            occupied = false;
            lock.notifyAll();
        }
    }
}
