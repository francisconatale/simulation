/**
 * Main.java — Demonstration & entry point for all queue models.
 *
 * Run:
 *   javac src/*.java -d out
 *   java -cp out Main
 *
 * Each section shows one queue type with example parameters.
 * Replace the values to compute your own scenarios.
 */
public class Main {

    public static void main(String[] args) {
        demoMM1();
        demoMMC();
        demoMMCK();
        demoQueueNetwork();
        demoSensitivityMM1();
    }

    // =========================================================================
    // M/M/1 Demo
    // =========================================================================
    static void demoMM1() {
        System.out.println();
        System.out.println("####  M/M/1  ####");

        double lambda = 4.0; // customers/hour
        double mu     = 6.0; // customers/hour

        MM1Queue q = new MM1Queue(lambda, mu);
        q.printReport();

        // Access individual metrics programmatically:
        System.out.println("Direct metric access:");
        System.out.printf("  Wq = %.4f time units%n", q.getWq());
        System.out.printf("  P(N > 3) = %.4f%n", q.getPMoreThan(3));
    }

    // =========================================================================
    // M/M/C Demo
    // =========================================================================
    static void demoMMC() {
        System.out.println();
        System.out.println("####  M/M/C  ####");

        double lambda = 10.0; // arrivals/hour
        double mu     = 4.0;  // service/hour per server
        int    c      = 3;    // servers

        MMCQueue q = new MMCQueue(lambda, mu, c);
        q.printReport();
    }

    // =========================================================================
    // M/M/C/K Demo
    // =========================================================================
    static void demoMMCK() {
        System.out.println();
        System.out.println("####  M/M/C/K  ####");

        double lambda = 8.0;  // arrivals/hour
        double mu     = 3.0;  // service/hour per server
        int    c      = 2;    // servers
        int    K      = 5;    // max customers in system (includes those in service)

        MMCKQueue q = new MMCKQueue(lambda, mu, c, K);
        q.printReport();

        // Compare M/M/2/5 vs M/M/2/10 (effect of capacity on blocking):
        System.out.println();
        System.out.println("  Capacity sensitivity (λ=8, μ=3, c=2):");
        System.out.printf("  %-5s | %-10s | %-10s | %-10s%n", "K", "PK (block)", "Lq", "Wq");
        System.out.printf("  %-5s-+-%-10s-+-%-10s-+-%-10s%n", "-----", "----------", "----------", "----------");
        for (int cap : new int[]{3, 5, 8, 12, 20, 50}) {
            MMCKQueue qi = new MMCKQueue(8.0, 3.0, 2, cap);
            System.out.printf("  %-5d | %-10.5f | %-10.5f | %-10.5f%n",
                cap, qi.getBlockingProbability(), qi.getLq(), qi.getWq());
        }
    }

    // =========================================================================
    // Open Jackson Queue Network Demo
    // =========================================================================
    static void demoQueueNetwork() {
        System.out.println();
        System.out.println("####  OPEN JACKSON NETWORK  ####");

        /*
         * Example: 3-node network (A → B → C, with feedback loops)
         *
         *   External arrivals: A gets λ=3, B gets λ=1, C gets 0
         *
         *   Routing after service:
         *     A → B: 60%,  A exits: 40%
         *     B → A: 10%,  B → C: 50%,  B exits: 40%
         *     C → B: 20%,  C exits: 80%
         *
         *   Service rates: μA=6, μB=5, μC=4
         *   Servers:       cA=1,  cB=2,  cC=1
         */
        QueueNode nodeA = new QueueNode("A", 6.0, 1, 3.0);
        QueueNode nodeB = new QueueNode("B", 5.0, 2, 1.0);
        QueueNode nodeC = new QueueNode("C", 4.0, 1, 0.0);

        double[][] routing = {
            // A→A   A→B   A→C
            { 0.00, 0.60, 0.00 },
            // B→A   B→B   B→C
            { 0.10, 0.00, 0.50 },
            // C→A   C→B   C→C
            { 0.00, 0.20, 0.00 }
        };

        QueueNetwork net = new QueueNetwork(
            new QueueNode[]{nodeA, nodeB, nodeC},
            routing
        );
        net.solve();
        net.printReport();
    }

    // =========================================================================
    // Sensitivity analysis: how W changes with ρ for M/M/1
    // =========================================================================
    static void demoSensitivityMM1() {
        System.out.println();
        System.out.println("####  M/M/1 Sensitivity: ρ vs W, Lq  ####");
        System.out.println("  (μ = 10 fixed, varying λ)");
        System.out.printf("  %-6s | %-6s | %-10s | %-10s | %-10s%n", "λ", "ρ", "L", "Lq", "Wq");
        System.out.printf("  %-6s-+-%-6s-+-%-10s-+-%-10s-+-%-10s%n",
            "------", "------", "----------", "----------", "----------");

        double mu = 10.0;
        for (double lam : new double[]{1, 2, 4, 6, 7, 8, 9, 9.5, 9.9}) {
            MM1Queue q = new MM1Queue(lam, mu);
            System.out.printf("  %-6.2f | %-6.4f | %-10.5f | %-10.5f | %-10.5f%n",
                lam, q.getRho(), q.getL(), q.getLq(), q.getWq());
        }
        System.out.println();
    }
}
