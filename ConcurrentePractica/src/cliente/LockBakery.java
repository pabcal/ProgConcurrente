package cliente;

public class LockBakery {
    private final int N;
    private final boolean[] choosing;
    private final int[] number;

    public LockBakery(int n) {
        this.N = n;
        this.choosing = new boolean[N];
        this.number = new int[N];
    }

    public void takeLock(int id) {
        choosing[id] = true;
        number[id] = 1 + max();
        choosing[id] = false;

        for (int j = 0; j < N; j++) {
            if (j == id) continue;

            while (choosing[j]) {
                Thread.yield();
            }

            while (number[j] != 0 && lexCompare(number[j], j, number[id], id)) {
                Thread.yield();
            }
        }
    }

    public void releaseLock(int id) {
        number[id] = 0;
    }

    private int max() {
        int max = 0;
        for (int value : number) {
            if (value > max) max = value;
        }
        return max;
    }

    private boolean lexCompare(int a1, int b1, int a2, int b2) {
        return (a1 < a2) || (a1 == a2 && b1 < b2);
    }
}
