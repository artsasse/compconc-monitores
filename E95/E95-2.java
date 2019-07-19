import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
   Conta bancária com métodos de saques com ou sem preferências e depósitos.
*/
public class Account2 {
    int balance;
    ReentrantLock lock = new ReentrantLock();
    Condition balanceCondition = lock.newCondition();
    int preferredWithdrawCount = 0;

    /**
      Construtor da conta com saldo inicial sendo passado por parâmetro.
      @param balance - saldo inicial da conta
   */
    Account2(int balance) {
        this.balance = balance;
    }

    /**
      Retorna qual saque deverá ser feito. 
      @return - saque preferencial
   */
    int getPreferredWithdrawCount() {
        return this.preferredWithdrawCount;
    }

    /**
      Deposita dinheiro na conta. 
      @param amount - quantidade a ser depositada
   */
    void deposit(int amount) {
        try {
            lock.lock();
            this.balance += amount;
            balanceCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    /**
      Saque comum de dinheiro na conta. 
      @param amount - quantidade a ser sacada
   */
    void ordinaryWithdraw(int amount) throws InterruptedException {
        try {
            lock.lock();
            while(this.balance < amount || this.preferredWithdrawCount > 0) {
                balanceCondition.await();
            }
            this.balance -= amount;
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    /**
      Saque preferencial de dinheiro na conta. 
      @param k - quantidade a ser sacada
   */
    void preferredWithdraw(int k) {
        try {
            lock.lock();
            synchronized(this) {
                this.preferredWithdrawCount++;
            }
            System.out.println("PreferredWithdrawCount: " + this.preferredWithdrawCount);
            while(this.balance < k) {
                System.out.println("Transação bloqueada");
                balanceCondition.await();
            }
            this.balance -= k;
        } catch(InterruptedException e) {
            e.printStackTrace();
        } finally {
            synchronized(this) {
                this.preferredWithdrawCount--;
            }
            lock.unlock();
        }
    }

    /**
      Retorna o saldo atual da conta.
      @return - saldo atual
   */
    int getBalance() {
        return this.balance;
    }
}