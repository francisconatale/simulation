/**
 * Open Jackson Queuing Network
 *
 * Models a network of M/M/c nodes with Poisson external arrivals and
 * probabilistic routing between nodes (Jackson's theorem).
 *
 * Jackson's Theorem: Under the conditions below, each node i in steady state
 * behaves as an independent M/M/c_i queue with arrival rate Λ_i.
 *
 * Conditions:
 *   1. External arrivals at node i are Poisson with rate λ_i (can be 0).
 *   2. Service at node i is exponential with rate μ_i (per server).
 *   3. After service at node i, a customer goes to node j with probability p_ij,
 *      or leaves the network with probability 1 - Σ_j p_ij.
 *
 * Traffic equations (flow balance):
 *   Λ_i = λ_i + Σ_j Λ_j * p_ji     for each node i
 *
 * This is a linear system:  (I - P^T) · Λ = λ
 * Solved with Gaussian elimination.
 *
 * Usage:
 *   QueueNode[] nodes = { new QueueNode("A", 5.0, 1, 3.0),
 *                         new QueueNode("B", 4.0, 2, 1.0) };
 *   // Routing matrix: p[i][j] = probability from node i to node j
 *   double[][] routing = { {0.0, 0.5},
 *                          {0.3, 0.0} };
 *   QueueNetwork net = new QueueNetwork(nodes, routing);
 *   net.solve();
 *   net.printReport();
 */
public class QueueNetwork {

    private final QueueNode[]  nodes;    // network nodes
    private final double[][]   routing;  // routing matrix p[i][j]
    private final int          N;        // number of nodes
    private boolean            solved = false;

    /**
     * @param nodes   Array of QueueNode (each with its external arrival rate)
     * @param routing Routing probability matrix (N x N).
     *                routing[i][j] = probability that a customer finishing
     *                service at node i goes next to node j.
     *                Row sums must be <= 1 (remainder exits the network).
     */
    public QueueNetwork(QueueNode[] nodes, double[][] routing) {
        if (nodes == null || nodes.length == 0)
            throw new IllegalArgumentException("nodes array must not be empty");
        this.N = nodes.length;

        if (routing.length != N)
            throw new IllegalArgumentException("routing matrix rows must equal number of nodes");
        for (int i = 0; i < N; i++) {
            if (routing[i].length != N)
                throw new IllegalArgumentException("routing matrix must be N x N");
            double rowSum = 0;
            for (double v : routing[i]) {
                if (v < 0) throw new IllegalArgumentException("routing probabilities must be >= 0");
                rowSum += v;
            }
            if (rowSum > 1.0 + 1e-9)
                throw new IllegalArgumentException(
                    "Row " + i + " of routing matrix sums to " + rowSum + " > 1");
        }

        this.nodes   = nodes;
        this.routing = routing;
    }

    // -------------------------------------------------------------------------
    // Solve
    // -------------------------------------------------------------------------

    /**
     * Solves the traffic equations (I - P^T)·Λ = λ using Gaussian elimination,
     * then assigns the total arrival rate Λ_i to each node so it can compute
     * its own M/M/c metrics.
     */
    public void solve() {
        double[] lambda = new double[N];
        for (int i = 0; i < N; i++) {
            lambda[i] = nodes[i].getExternalRate();
        }

        // Build matrix A = (I - P^T)
        // Equation: A · Λ = λ
        // A[i][j] = δ(i,j) - p[j][i]    (column j of P becomes row j of P^T)
        double[][] A = new double[N][N];
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                A[i][j] = (i == j ? 1.0 : 0.0) - routing[j][i];
            }
        }

        double[] Lambda = gaussianElimination(A, lambda.clone());

        for (int i = 0; i < N; i++) {
            if (Lambda[i] < 0) {
                System.err.printf("Warning: Λ[%d] = %.6f < 0 — check routing matrix.%n", i, Lambda[i]);
                Lambda[i] = 0;
            }
            nodes[i].setTotalArrivalRate(Lambda[i]);
        }

        solved = true;
    }

    // -------------------------------------------------------------------------
    // Network-level metrics (require solve() first)
    // -------------------------------------------------------------------------

    /** Total external arrival rate to the whole network */
    public double getTotalExternalArrivalRate() {
        double total = 0;
        for (QueueNode n : nodes) total += n.getExternalRate();
        return total;
    }

    /**
     * Average number of customers in the ENTIRE NETWORK (sum of L_i):
     * L_net = Σ L_i
     */
    public double getNetworkL() {
        requireSolved();
        double l = 0;
        for (QueueNode n : nodes) l += n.getL();
        return l;
    }

    /**
     * Average number of customers WAITING in the entire network:
     * Lq_net = Σ Lq_i
     */
    public double getNetworkLq() {
        requireSolved();
        double lq = 0;
        for (QueueNode n : nodes) lq += n.getLq();
        return lq;
    }

    /**
     * Average end-to-end sojourn time for a customer traversing the network.
     * Uses Little's Law: W_net = L_net / λ_total_effective
     *
     * Note: This is the expected total time assuming one visit per node
     * on average — for networks with multiple visits, use getVisitRatios().
     */
    public double getNetworkW() {
        requireSolved();
        double lambdaExt = getTotalExternalArrivalRate();
        if (lambdaExt == 0) return Double.POSITIVE_INFINITY;
        return getNetworkL() / lambdaExt;
    }

    /**
     * Average end-to-end waiting time in queues across the network:
     * Wq_net = Lq_net / λ_total_external
     */
    public double getNetworkWq() {
        requireSolved();
        double lambdaExt = getTotalExternalArrivalRate();
        if (lambdaExt == 0) return Double.POSITIVE_INFINITY;
        return getNetworkLq() / lambdaExt;
    }

    /**
     * Visit ratio for node i relative to node ref.
     * V_i = Λ_i / Λ_ref
     */
    public double getVisitRatio(int nodeIndex, int refNodeIndex) {
        requireSolved();
        double ref = nodes[refNodeIndex].getTotalArrivalRate();
        if (ref == 0) return 0;
        return nodes[nodeIndex].getTotalArrivalRate() / ref;
    }

    /** Returns true if ALL nodes are stable (ρ_i < 1) */
    public boolean isStable() {
        if (!solved) return false;
        for (QueueNode n : nodes) if (!n.isStable()) return false;
        return true;
    }

    // -------------------------------------------------------------------------
    // Report
    // -------------------------------------------------------------------------

    public void printReport() {
        requireSolved();

        System.out.println("========================================");
        System.out.println("      Open Jackson Network Report       ");
        System.out.println("========================================");
        System.out.printf("  Nodes in network   : %d%n", N);
        System.out.printf("  Total external λ   : %.4f%n", getTotalExternalArrivalRate());
        System.out.printf("  Network stable?    : %s%n", isStable() ? "YES" : "NO");
        System.out.println();

        System.out.println("  Routing matrix (p[i][j]):");
        System.out.print("         ");
        for (QueueNode n : nodes) System.out.printf("  %-8s", n.getName());
        System.out.println();
        for (int i = 0; i < N; i++) {
            System.out.printf("  %-8s", nodes[i].getName());
            for (int j = 0; j < N; j++) {
                System.out.printf("  %-8.4f", routing[i][j]);
            }
            double exit = 1.0;
            for (double v : routing[i]) exit -= v;
            System.out.printf("  [exit=%.4f]%n", Math.max(0, exit));
        }

        System.out.println();
        System.out.println("  Per-Node Analysis:");
        System.out.println("  " + "-".repeat(72));
        for (int i = 0; i < N; i++) {
            QueueNode n = nodes[i];
            System.out.printf("  Node %-10s | c=%d | μ=%.4f | λext=%.4f | Λ=%.4f%n",
                n.getName(), n.getServers(), n.getMu(),
                n.getExternalRate(), n.getTotalArrivalRate());
            if (!n.isStable()) {
                System.out.printf("               | *** UNSTABLE (ρ=%.4f >= 1) ***%n",
                    n.getUtilization());
            } else {
                System.out.printf("               | ρ=%-7.4f | L=%-8.5f | Lq=%-8.5f%n",
                    n.getUtilization(), n.getL(), n.getLq());
                System.out.printf("               | W=%-7.5f | Wq=%-7.5f | ErlangC=%.5f%n",
                    n.getW(), n.getWq(), n.getErlangC());
            }
        }
        System.out.println("  " + "-".repeat(72));
        System.out.println();
        System.out.println("  Network Totals:");
        System.out.printf("    L_net  (avg customers in network) = %.6f%n", getNetworkL());
        System.out.printf("    Lq_net (avg waiting in network)   = %.6f%n", getNetworkLq());
        System.out.printf("    W_net  (avg sojourn time)         = %.6f%n", getNetworkW());
        System.out.printf("    Wq_net (avg wait time)            = %.6f%n", getNetworkWq());
        System.out.println("========================================");
    }

    // -------------------------------------------------------------------------
    // Gaussian elimination (partial pivoting)
    // -------------------------------------------------------------------------

    /**
     * Solves A·x = b using Gaussian elimination with partial pivoting.
     * Returns x (modifies b in place).
     */
    private static double[] gaussianElimination(double[][] A, double[] b) {
        int n = b.length;
        // Augmented matrix [A | b]
        double[][] M = new double[n][n + 1];
        for (int i = 0; i < n; i++) {
            System.arraycopy(A[i], 0, M[i], 0, n);
            M[i][n] = b[i];
        }

        for (int col = 0; col < n; col++) {
            // Find pivot
            int pivot = col;
            for (int row = col + 1; row < n; row++) {
                if (Math.abs(M[row][col]) > Math.abs(M[pivot][col])) pivot = row;
            }
            // Swap rows
            double[] tmp = M[col]; M[col] = M[pivot]; M[pivot] = tmp;

            if (Math.abs(M[col][col]) < 1e-12)
                throw new ArithmeticException("Singular system in traffic equations — check routing matrix");

            // Eliminate below
            for (int row = col + 1; row < n; row++) {
                double factor = M[row][col] / M[col][col];
                for (int k = col; k <= n; k++) {
                    M[row][k] -= factor * M[col][k];
                }
            }
        }

        // Back substitution
        double[] x = new double[n];
        for (int i = n - 1; i >= 0; i--) {
            x[i] = M[i][n];
            for (int j = i + 1; j < n; j++) {
                x[i] -= M[i][j] * x[j];
            }
            x[i] /= M[i][i];
        }
        return x;
    }

    private void requireSolved() {
        if (!solved) throw new IllegalStateException("Call solve() before accessing metrics.");
    }
}
