import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
   Conta bancária com métodos de saque e depósito.
*/
public class Account
{
   private double balance;
   private Lock balanceChangeLock;
   private Condition sufficientFundsCondition;

   /**
      Construtor da conta com saldo inicial igual a zero.
   */
   public Account()
   {
      balance = 0;
      balanceChangeLock = new ReentrantLock();
      sufficientFundsCondition = balanceChangeLock.newCondition();
   }

   /**
      Deposita dinheiro na conta. 
      @param amount - quantidade a ser depositada
   */
   public void deposit(double amount)
   {
      balanceChangeLock.lock();
      try
      {
         System.out.print("Depositando " + amount);
         double newBalance = balance + amount;
         System.out.println(", novo saldo igual a " + newBalance);
         balance = newBalance;
         sufficientFundsCondition.signalAll();
      }
      finally
      {
         balanceChangeLock.unlock();
      }
   }
   
   /**
      Saca dinheiro da conta.
      @param amount - quantidade a ser sacada
   */
   public void withdraw(double amount)
         throws InterruptedException
   {
      balanceChangeLock.lock();
      try
      {
         while (balance < amount)
            sufficientFundsCondition.await();
         System.out.print("Sacando " + amount);
         double newBalance = balance - amount;
         System.out.println(", novo saldo igual a " + newBalance);
         balance = newBalance;
      }
      finally
      {
         balanceChangeLock.unlock();
      }
   }
   
   /**
      Retorna o saldo atual da conta.
      @return - saldo atual
   */
   public double getBalance()
   {
      return balance;
   }
}