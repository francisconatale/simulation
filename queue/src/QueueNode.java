/**
 * Represents a single node in an Open Jackson Queuing Network.
 *
 * Each node is an M/M/c queue analyzed independently after solving
 * the traffic equations (flow balance equations).
 *
 * Fields:
 *   name         - node identifier (label)
 *   mu           - service rate per server at this node
 *   servers      - number of servers at this node (c)
 *   externalRate - external Poisson arrival rate (λ_i from outside)
 *
 * After network analysis is run, the field totalArrivalRate (Λ_i)
 * is set and the node can report its own M/M/c metrics.
 */
public class QueueNode {

    private final String name;
    private final double mu;
    private final int    servers;
    private final double externalRate; // λ_i (external arrivals to this node)

    // Set by QueueNetwork after solving traffic equations
    private double totalArrivalRate = 0.0; // Λ_i (external + internal)

    // Cached M/M/c result for this node (set after network solve)
    private MMCQueue mmcQueue = null;

    public QueueNode(String name, double mu, int servers, double externalRate) {
        if (mu <= 0)         throw new IllegalArgumentException("mu must be > 0");
        if (servers < 1)     throw new IllegalArgumentException("servers must be >= 1");
        if (externalRate < 0) throw new IllegalArgumentException("externalRate must be >= 0");
        this.name         = name;
        this.mu           = mu;
        this.servers      = servers;
        this.externalRate = externalRate;
    }

    /** Convenience constructor for single-server node */
    public QueueNode(String name, double mu, double externalRate) {
        this(name, mu, 1, externalRate);
    }

    // -------------------------------------------------------------------------
    // Getters
    // -------------------------------------------------------------------------

    public String getName()         { return name; }
    public double getMu()           { return mu; }
    public int    getServers()      { return servers; }
    public double getExternalRate() { return externalRate; }
    public double getTotalArrivalRate() { return totalArrivalRate; }

    // -------------------------------------------------------------------------
    // Called by QueueNetwork
    // -------------------------------------------------------------------------

    void setTotalArrivalRate(double rate) {
        this.totalArrivalRate = rate;
        this.mmcQueue = new MMCQueue(rate, mu, servers);
    }

    /** Whether this node is stable (ρ < 1) */
    public boolean isStable() {
        return mmcQueue != null && mmcQueue.isStable();
    }

    /** Per-server utilization ρ = Λ_i / (c_i * μ_i) */
    public double getUtilization() {
        requireSolved();
        return totalArrivalRate / (servers * mu);
    }

    /** Erlang-C: probability a customer waits */
    public double getErlangC() {
        requireSolved();
        return mmcQueue.getErlangC();
    }

    /** Average customers in system L */
    public double getL()  { requireSolved(); return mmcQueue.getL(); }

    /** Average customers in queue Lq */
    public double getLq() { requireSolved(); return mmcQueue.getLq(); }

    /** Average customers in service Ls */
    public double getLs() { requireSolved(); return mmcQueue.getLs(); }

    /** Average sojourn time W */
    public double getW()  { requireSolved(); return mmcQueue.getW(); }

    /** Average waiting time in queue Wq */
    public double getWq() { requireSolved(); return mmcQueue.getWq(); }

    /** Average service time 1/μ */
    public double getWs() { return 1.0 / mu; }

    // -------------------------------------------------------------------------
    // Internal
    // -------------------------------------------------------------------------

    private void requireSolved() {
        if (mmcQueue == null) {
            throw new IllegalStateException(
                "Node '" + name + "' has not been solved yet. Call QueueNetwork.solve() first."
            );
        }
    }

    @Override
    public String toString() {
        return "QueueNode{name='" + name + "', mu=" + mu + ", servers=" + servers
             + ", externalRate=" + externalRate + "}";
    }
}
