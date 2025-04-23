package cliente;

public class TicketLimiter {
    private final int K;
    private int number = 1;
    private int next   = 1;

    public TicketLimiter(int K) {
        this.K = K;
    }

    /**
     * Cada hilo toma un ticket único (incremento atomico).
     */
    public synchronized int takeTicket() {
        return number++;
    }

    /**
     * Espera hasta que su ticket esté dentro de la ventana [next, next+K).
     */
    public void enter(int myTicket) {
        synchronized (this) {
            while (myTicket < next || myTicket >= next + K) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    /**
     * Cuando sale, avanza next y despierta a los hilos que pudieran entrar.
     */
    public synchronized void leave() {
        next++;
        notifyAll();
    }
}
