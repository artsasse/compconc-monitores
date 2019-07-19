import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class Rooms {
   int numeroDeSalas;
   int populacao;
   ReentrantLock lock;
   Condition[] entradaLiberada;
   Handler[] handlersDeSaida;
   int quartoEmUso;
   int quartosLivres = -1;
   int handlerDeSaidaAtivo = -2;

   public Rooms(int m) {
       numeroDeSalas = m;
       populacao = 0;
       lock = new ReentrantLock(true);
       entradaLiberada = new Condition[m];
       handlersDeSaida = new HandlerDeSaida[m];
       quartoEmUso = quartosLivres;

       for (int i = 0; i < m; i++) {
           entradaLiberada[i] = lock.newCondition();
           setHandlerDeSaida(i, new HandlerDeSaida(i));
       }
   }

   public void enter(int i) {
       lock.lock();
       try {
          //while para proteger de spurious wakeups
           while (quartoEmUso != i && quartoEmUso != quartosLivres) {
               entradaLiberada[i].await();
           }
           quartoEmUso = i;
           populacao++;
       } catch (InterruptedException e) {
           e.printStackTrace();
       } finally {
           lock.unlock();
       }
   }

   public boolean exit() {
       lock.lock();
       try {
           populacao--;
           //se for a ultima thread, ela muda o status do "quartoEmUso" e chama o handler da sala da qual esta saindo
           if (populacao == 0) {
             int x = quartoEmUso;
             quartoEmUso = handlerDeSaidaAtivo;
             handlersDeSaida[x].onEmpty()
             return true;
           }
       }finally {
           lock.unlock();
       }
       return false;
   }

  public void setHandlerDeSaida(int i, Rooms.Handler h) {
       handlersDeSaida[i] = h;
   }

   public interface Handler {
       void onEmpty();
   }

   private class HandlerDeSaida implements Handler {
       int idSala;
       HandlerDeSaida(int idSala) {
           this.idSala = idSala;
       }

       @Override
       public void onEmpty() {
           lock.lock();
           try {
               quartoEmUso = proximaSala(this.idSala);
               if (quartoEmUso >= 0) {
                   entradaLiberada[quartoEmUso].signalAll();
               }
           } finally {
               lock.unlock();
           }
       }

       private int proximaSala(int idSalas) {
           //procuramos sempre a partir da sala seguinte aquela que acabou de ser desocupada
           //Como estamos sempre avançando pelo menos uma sala por vez, podemos
           //afirmar que eventualmente vamos verificar se cada uma das salas tem threads querendo entrar
           //e permitir que uma mesma sala seja escolhida duas vezes seguidas
           for (int i = idSalas; i < numeroDeSalas + idSalas; i++) {
               int salaCandidata = (i + 1) % numeroDeSalas;
               if (lock.hasWaiters(entradaLiberada[salaCandidata])) {
                   return salaCandidata;
               }
           }
           return quartosLivres;
       }
   }
}
