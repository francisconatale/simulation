/**
 * M/M/C/K Queue Model
 *
 * Multiple servers (c), FINITE capacity K (max customers in system),
 * Poisson arrivals, exponential service.
 *
 * Parameters:
 *   lambda (λ) - arrival rate (customers per unit time)
 *   mu     (μ) - service rate per server
 *   c          - number of parallel servers
 *   K          - maximum system capacity (K >= c)
 *
 * Note: No stability condition required (finite K always stable).
 * The effective arrival rate λ_eff = λ·(1 - P_K).
 */
public class MMCKQueue {

    private final double lambda;  // nominal arrival rate
    private final double mu;      // service rate per server
    private final int    c;       // number of servers
    private final int    K;       // system capacity (max customers total)
    private final double a;       // offered load = λ/μ

    // Pre-computed probability array p[n] for n = 0..K
    private final double[] p;

    public MMCKQueue(double lambda, double mu, int c, int K) {
        if (lambda <= 0) throw new IllegalArgumentException("lambda must be > 0");
        if (mu <= 0)     throw new IllegalArgumentException("mu must be > 0");
        if (c < 1)       throw new IllegalArgumentException("c must be >= 1");
        if (K < c)       throw new IllegalArgumentException("K must be >= c");

        this.lambda = lambda;
        this.mu     = mu;
        this.c      = c;
        this.K      = K;
        this.a      = lambda / mu;

        this.p = computeProbabilities();
    }

    // -------------------------------------------------------------------------
    // Core probabilities
    // -------------------------------------------------------------------------

    /**
     * Probability of exactly n customers in system (0 <= n <= K).
     *   Pn = P0 * a^n / n!                   for n <= c
     *   Pn = P0 * a^n / (c! * c^(n-c))       for c < n <= K
     */
    public double getPn(int n) {
        if (n < 0 || n > K) throw new IllegalArgumentException("n must be in [0, K]");
        return p[n];
    }

    /** P0 = probability system is empty */
    public double getP0() {
        return p[0];
    }

    /** PK = probability system is FULL (arriving customers are rejected) */
    public double getPK() {
        return p[K];
    }

    // -------------------------------------------------------------------------
    // Performance metrics
    // -------------------------------------------------------------------------

    /**
     * Effective arrival rate (λ_eff = λ * (1 - P_K)).
     * Customers are rejected when system is full.
     */
    public double getEffectiveLambda() {
        return lambda * (1.0 - getPK());
    }

    /**
     * Server utilization ρ = λ_eff / (c * μ).
     */
    public double getUtilization() {
        return getEffectiveLambda() / (c * mu);
    }

    /**
     * Probability of customer rejection (blocking probability) = P_K.
     */
    public double getBlockingProbability() {
        return getPK();
    }

    /**
     * Average number of customers IN THE SYSTEM:
     * L = Σ(n=0..K) n * Pn
     */
    public double getL() {
        double l = 0.0;
        for (int n = 0; n <= K; n++) {
            l += n * p[n];
        }
        return l;
    }

    /**
     * Average number of customers IN THE QUEUE (waiting, not in service):
     * Lq = Σ(n=c+1..K) (n-c) * Pn
     */
    public double getLq() {
        double lq = 0.0;
        for (int n = c + 1; n <= K; n++) {
            lq += (n - c) * p[n];
        }
        return lq;
    }

    /**
     * Average number of customers IN SERVICE:
     * Ls = L - Lq
     */
    public double getLs() {
        return getL() - getLq();
    }

    /**
     * Average sojourn time IN SYSTEM (Little's Law applied to admitted customers):
     * W = L / λ_eff
     */
    public double getW() {
        double leff = getEffectiveLambda();
        if (leff == 0) return Double.POSITIVE_INFINITY;
        return getL() / leff;
    }

    /**
     * Average waiting time IN QUEUE:
     * Wq = Lq / λ_eff
     */
    public double getWq() {
        double leff = getEffectiveLambda();
        if (leff == 0) return Double.POSITIVE_INFINITY;
        return getLq() / leff;
    }

    /**
     * Average service time = 1/μ
     */
    public double getWs() {
        return 1.0 / mu;
    }

    /**
     * Probability that an arriving (non-rejected) customer has to wait:
     * P(wait) = Σ(n=c..K-1) Pn  / (1 - PK)
     */
    public double getProbabilityWait() {
        double pkNotFull = 1.0 - getPK();
        if (pkNotFull == 0) return 1.0;
        double sumWait = 0.0;
        for (int n = c; n < K; n++) {
            sumWait += p[n];
        }
        return sumWait / pkNotFull;
    }

    // -------------------------------------------------------------------------
    // Report
    // -------------------------------------------------------------------------

    public void printReport() {
        System.out.println("========================================");
        System.out.println("         M/M/C/K Queue Report           ");
        System.out.println("========================================");
        System.out.printf("  Arrival rate       λ    = %.4f%n", lambda);
        System.out.printf("  Service rate/srv   μ    = %.4f%n", mu);
        System.out.printf("  Servers            c    = %d%n", c);
        System.out.printf("  System capacity    K    = %d%n", K);
        System.out.printf("  Offered load       a    = %.4f (Erlangs)%n", a);
        System.out.printf("  Effective λ        λeff = %.6f%n", getEffectiveLambda());
        System.out.printf("  Utilization        ρ    = %.6f%n", getUtilization());
        System.out.println("----------------------------------------");
        System.out.println("  Probabilities:");
        System.out.printf("    P0 (empty)             = %.6f%n", getP0());
        System.out.printf("    PK (full / blocked)    = %.6f%n", getPK());
        System.out.printf("    P(customer waits)      = %.6f%n", getProbabilityWait());
        int maxPrint = Math.min(K, 8);
        for (int n = 0; n <= maxPrint; n++) {
            System.out.printf("    P(N=%2d)               = %.6f%n", n, getPn(n));
        }
        if (K > maxPrint) {
            System.out.printf("    ... (up to P(%d) available via getPn(n))%n", K);
        }
        System.out.println("----------------------------------------");
        System.out.println("  Averages (customers):");
        System.out.printf("    L  (in system)         = %.6f%n", getL());
        System.out.printf("    Lq (in queue)          = %.6f%n", getLq());
        System.out.printf("    Ls (in service)        = %.6f%n", getLs());
        System.out.println("  Averages (time):");
        System.out.printf("    W  (in system)         = %.6f%n", getW());
        System.out.printf("    Wq (in queue)          = %.6f%n", getWq());
        System.out.printf("    Ws (service time)      = %.6f%n", getWs());
        System.out.println("========================================");
    }

    // -------------------------------------------------------------------------
    // Internal computation
    // -------------------------------------------------------------------------

    /**
     * Computes Pn for n = 0..K via normalization.
     * Uses unnormalized weights w[n] then divides by Σw.
     */
    private double[] computeProbabilities() {
        double[] w = new double[K + 1];

        // Unnormalized weights
        // w[0] = 1  (base)
        // w[n] = a^n / n!            for n <= c
        // w[n] = a^n / (c! * c^(n-c)) for c < n <= K
        w[0] = 1.0;
        for (int n = 1; n <= K; n++) {
            if (n <= c) {
                w[n] = Math.pow(a, n) / MMCQueue.factorial(n);
            } else {
                w[n] = Math.pow(a, n) / (MMCQueue.factorial(c) * Math.pow(c, n - c));
            }
        }

        // Normalize
        double total = 0.0;
        for (double wn : w) total += wn;

        double[] prob = new double[K + 1];
        for (int n = 0; n <= K; n++) {
            prob[n] = w[n] / total;
        }
        return prob;
    }
}
