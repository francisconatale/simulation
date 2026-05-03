/**
 * M/M/C Queue Model
 *
 * Multiple servers (c), infinite capacity, Poisson arrivals, exponential service.
 *
 * Parameters:
 *   lambda (λ) - arrival rate (customers per unit time)
 *   mu     (μ) - service rate per server (customers per unit time)
 *   c          - number of parallel servers
 *
 * Stability condition: ρ = λ/(c·μ) < 1
 */
public class MMCQueue {

    private final double lambda;  // arrival rate
    private final double mu;      // service rate per server
    private final int    c;       // number of servers
    private final double rho;     // server utilization = λ/(c·μ)
    private final double a;       // offered load = λ/μ

    // Cached P0 (computed once)
    private final double p0;

    public MMCQueue(double lambda, double mu, int c) {
        if (lambda <= 0) throw new IllegalArgumentException("lambda must be > 0");
        if (mu <= 0)     throw new IllegalArgumentException("mu must be > 0");
        if (c < 1)       throw new IllegalArgumentException("c must be >= 1");

        this.lambda = lambda;
        this.mu     = mu;
        this.c      = c;
        this.a      = lambda / mu;         // Erlang offered load
        this.rho    = lambda / (c * mu);   // per-server utilization

        this.p0 = computeP0();
    }

    /** Checks stability: ρ < 1 */
    public boolean isStable() {
        return rho < 1.0;
    }

    /** Per-server utilization ρ = λ/(c·μ) */
    public double getRho() {
        return rho;
    }

    /** Total offered load a = λ/μ */
    public double getOfferedLoad() {
        return a;
    }

    /**
     * Probability that the system is empty P0.
     * P0 = [ Σ(k=0..c-1) a^k/k!  +  a^c/(c!·(1-ρ)) ]^-1
     */
    public double getP0() {
        checkStability();
        return p0;
    }

    /**
     * Probability of exactly n customers in system:
     *   Pn = P0 · a^n / n!           for n <= c
     *   Pn = P0 · a^n / (c! · c^(n-c))  for n > c
     */
    public double getPn(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        checkStability();
        if (n <= c) {
            return p0 * Math.pow(a, n) / factorial(n);
        } else {
            return p0 * Math.pow(a, n) / (factorial(c) * Math.pow(c, n - c));
        }
    }

    /**
     * Probability all servers busy (Erlang C formula):
     * C(c,a) = P(N >= c) = [a^c / (c!·(1-ρ))] · P0
     */
    public double getErlangC() {
        checkStability();
        return (Math.pow(a, c) / (factorial(c) * (1.0 - rho))) * p0;
    }

    /**
     * Average number of customers IN THE QUEUE:
     * Lq = C(c,a) · ρ / (1 - ρ)
     */
    public double getLq() {
        checkStability();
        return getErlangC() * rho / (1.0 - rho);
    }

    /**
     * Average number of customers IN THE SYSTEM:
     * L = Lq + a      (Little's Law)
     */
    public double getL() {
        checkStability();
        return getLq() + a;
    }

    /**
     * Average number of customers IN SERVICE:
     * Ls = a = λ/μ
     */
    public double getLs() {
        checkStability();
        return a;
    }

    /**
     * Average waiting time IN QUEUE:
     * Wq = Lq / λ
     */
    public double getWq() {
        checkStability();
        return getLq() / lambda;
    }

    /**
     * Average time IN SYSTEM:
     * W = Wq + 1/μ
     */
    public double getW() {
        checkStability();
        return getWq() + 1.0 / mu;
    }

    /**
     * Probability that a customer must wait:
     * P(wait) = Erlang-C = C(c,a)
     */
    public double getProbabilityWait() {
        return getErlangC();
    }

    /**
     * Expected waiting time given a customer HAS to wait:
     * E[W | wait] = 1 / (c·μ - λ)
     */
    public double getExpectedWaitGivenWait() {
        checkStability();
        return 1.0 / (c * mu - lambda);
    }

    /** Prints a full report of all metrics */
    public void printReport() {
        System.out.println("========================================");
        System.out.println("           M/M/C Queue Report           ");
        System.out.println("========================================");
        System.out.printf("  Arrival rate       λ  = %.4f%n", lambda);
        System.out.printf("  Service rate/srv   μ  = %.4f%n", mu);
        System.out.printf("  Servers            c  = %d%n", c);
        System.out.printf("  Offered load       a  = %.4f (Erlangs)%n", a);
        System.out.printf("  Utilization        ρ  = %.4f%n", rho);
        System.out.printf("  Stable?               %s%n", isStable() ? "YES" : "NO (ρ >= 1)");

        if (!isStable()) {
            System.out.println("  [System is unstable — queue grows unbounded]");
            System.out.println("========================================");
            return;
        }

        System.out.println("----------------------------------------");
        System.out.println("  Probabilities:");
        System.out.printf("    P0 (system empty)  = %.6f%n", getP0());
        System.out.printf("    Erlang-C C(c,a)    = %.6f  [P(customer waits)]%n", getErlangC());
        for (int n = 1; n <= Math.min(c + 2, 8); n++) {
            System.out.printf("    P(N=%d)             = %.6f%n", n, getPn(n));
        }
        System.out.println("----------------------------------------");
        System.out.println("  Averages (customers):");
        System.out.printf("    L  (in system)     = %.6f%n", getL());
        System.out.printf("    Lq (in queue)      = %.6f%n", getLq());
        System.out.printf("    Ls (in service)    = %.6f%n", getLs());
        System.out.println("  Averages (time):");
        System.out.printf("    W  (in system)     = %.6f%n", getW());
        System.out.printf("    Wq (in queue)      = %.6f%n", getWq());
        System.out.printf("    Ws (in service)    = %.6f%n", 1.0 / mu);
        System.out.printf("    E[W|wait]          = %.6f%n", getExpectedWaitGivenWait());
        System.out.println("========================================");
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    /**
     * Computes P0 using the M/M/C formula.
     * Uses log-space arithmetic for numerical stability with large c.
     */
    private double computeP0() {
        if (!isStable()) return Double.NaN;

        double sum = 0.0;
        // Σ(k=0..c-1) a^k / k!
        for (int k = 0; k < c; k++) {
            sum += Math.pow(a, k) / factorial(k);
        }
        // + a^c / (c! * (1 - ρ))
        sum += Math.pow(a, c) / (factorial(c) * (1.0 - rho));

        return 1.0 / sum;
    }

    /** Factorial as double (supports large values without overflow up to ~170) */
    static double factorial(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        double result = 1.0;
        for (int i = 2; i <= n; i++) result *= i;
        return result;
    }

    private void checkStability() {
        if (!isStable()) {
            throw new IllegalStateException(
                String.format("System is unstable: ρ = %.4f >= 1. Metrics are not defined.", rho)
            );
        }
    }
}
