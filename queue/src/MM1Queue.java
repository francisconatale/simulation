/**
 * M/M/1 Queue Model
 *
 * Single server, infinite capacity, Poisson arrivals, exponential service.
 *
 * Parameters:
 *   lambda (λ) - arrival rate (customers per unit time)
 *   mu     (μ) - service rate (customers per unit time)
 *
 * Stability condition: ρ = λ/μ < 1
 */
public class MM1Queue {

    private final double lambda; // arrival rate
    private final double mu;     // service rate
    private final double rho;    // traffic intensity = λ/μ

    public MM1Queue(double lambda, double mu) {
        if (lambda <= 0) throw new IllegalArgumentException("lambda must be > 0");
        if (mu <= 0)     throw new IllegalArgumentException("mu must be > 0");
        this.lambda = lambda;
        this.mu     = mu;
        this.rho    = lambda / mu;
    }

    /** Checks stability: ρ < 1 */
    public boolean isStable() {
        return rho < 1.0;
    }

    /** Traffic intensity ρ = λ/μ */
    public double getRho() {
        return rho;
    }

    /** Server utilization (same as ρ for M/M/1) */
    public double getUtilization() {
        return rho;
    }

    /**
     * Probability that the system is empty: P0 = 1 - ρ
     */
    public double getP0() {
        checkStability();
        return 1.0 - rho;
    }

    /**
     * Probability of exactly n customers in system: Pn = (1-ρ) * ρ^n
     */
    public double getPn(int n) {
        if (n < 0) throw new IllegalArgumentException("n must be >= 0");
        checkStability();
        return (1.0 - rho) * Math.pow(rho, n);
    }

    /**
     * Probability of more than n customers: P(N > n) = ρ^(n+1)
     */
    public double getPMoreThan(int n) {
        checkStability();
        return Math.pow(rho, n + 1);
    }

    /**
     * Average number of customers IN THE SYSTEM (queue + service):
     * L = ρ / (1 - ρ) = λ / (μ - λ)
     */
    public double getL() {
        checkStability();
        return rho / (1.0 - rho);
    }

    /**
     * Average number of customers IN THE QUEUE (waiting):
     * Lq = ρ² / (1 - ρ) = λ² / (μ(μ - λ))
     */
    public double getLq() {
        checkStability();
        return (rho * rho) / (1.0 - rho);
    }

    /**
     * Average number of customers IN SERVICE:
     * Ls = ρ
     */
    public double getLs() {
        checkStability();
        return rho;
    }

    /**
     * Average time a customer spends IN THE SYSTEM (waiting + service):
     * W = 1 / (μ - λ)     [Little's Law: W = L/λ]
     */
    public double getW() {
        checkStability();
        return 1.0 / (mu - lambda);
    }

    /**
     * Average time a customer spends WAITING IN QUEUE:
     * Wq = λ / (μ(μ - λ))  [Little's Law: Wq = Lq/λ]
     */
    public double getWq() {
        checkStability();
        return lambda / (mu * (mu - lambda));
    }

    /**
     * Average service time: Ws = 1/μ
     */
    public double getWs() {
        return 1.0 / mu;
    }

    /** Variance of number in system: σ²(N) = ρ / (1-ρ)² */
    public double getVarianceN() {
        checkStability();
        return rho / Math.pow(1.0 - rho, 2);
    }

    /** Variance of sojourn time: σ²(W) = 1 / (μ-λ)² * (2 - 1/μ*(μ-λ)) -- simplified */
    public double getVarianceW() {
        checkStability();
        // For M/M/1: Var(W) = 1 / (μ-λ)²   (since service is exponential)
        return 1.0 / Math.pow(mu - lambda, 2);
    }

    /** Prints a full report of all metrics */
    public void printReport() {
        System.out.println("========================================");
        System.out.println("           M/M/1 Queue Report           ");
        System.out.println("========================================");
        System.out.printf("  Arrival rate      λ  = %.4f%n", lambda);
        System.out.printf("  Service rate      μ  = %.4f%n", mu);
        System.out.printf("  Traffic intensity ρ  = %.4f%n", rho);
        System.out.printf("  Stable?              %s%n", isStable() ? "YES" : "NO (ρ >= 1)");

        if (!isStable()) {
            System.out.println("  [System is unstable — queue grows unbounded]");
            System.out.println("========================================");
            return;
        }

        System.out.println("----------------------------------------");
        System.out.println("  Probabilities:");
        System.out.printf("    P(system empty) P0 = %.6f%n", getP0());
        System.out.printf("    P(N=1)          P1 = %.6f%n", getPn(1));
        System.out.printf("    P(N=2)          P2 = %.6f%n", getPn(2));
        System.out.printf("    P(N=5)          P5 = %.6f%n", getPn(5));
        System.out.printf("    P(N>5)             = %.6f%n", getPMoreThan(5));
        System.out.println("----------------------------------------");
        System.out.println("  Averages (customers):");
        System.out.printf("    L  (in system)  = %.6f%n", getL());
        System.out.printf("    Lq (in queue)   = %.6f%n", getLq());
        System.out.printf("    Ls (in service) = %.6f%n", getLs());
        System.out.println("  Averages (time):");
        System.out.printf("    W  (in system)  = %.6f%n", getW());
        System.out.printf("    Wq (in queue)   = %.6f%n", getWq());
        System.out.printf("    Ws (in service) = %.6f%n", getWs());
        System.out.println("  Variances:");
        System.out.printf("    Var(N)          = %.6f%n", getVarianceN());
        System.out.printf("    Var(W)          = %.6f%n", getVarianceW());
        System.out.println("========================================");
    }

    private void checkStability() {
        if (!isStable()) {
            throw new IllegalStateException(
                String.format("System is unstable: ρ = %.4f >= 1. Metrics are not defined.", rho)
            );
        }
    }
}
