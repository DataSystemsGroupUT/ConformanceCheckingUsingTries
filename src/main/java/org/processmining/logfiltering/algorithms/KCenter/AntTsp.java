package org.processmining.logfiltering.algorithms.KCenter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Random;


public class AntTsp {

    private double c = 1.0;

    private double alpha = 1;

    private double beta = 5;

    private double evaporation = 0.5;

    private double Q = 500;

    private double numAntFactor = 0.8;

    private double pr = 0.01;

    private int maxIterations = 2000;

    public int n = 0; // # towns

    public int m = 0; // # ants

    private double graph[][] = null;

    private double trails[][] = null;

    private Ant ants[] = null;

    private Random rand = new Random();

    private double probs[] = null;

    private int currentIndex = 0;

    public int[] bestTour;

    public double bestTourLength;

    private class Ant {
        public int tour[] = new int[graph.length];

        public boolean visited[] = new boolean[graph.length];

        public void visitTown(int town) {
            tour[currentIndex + 1] = town;
            visited[town] = true;
        }

        public boolean visited(int i) {
            return visited[i];
        }

        public double tourLength() {
            double length = graph[tour[n - 1]][tour[0]];
            for (int i = 0; i < n - 1; i++) {
                length += graph[tour[i]][tour[i + 1]];
            }
            return length;
        }

        public void clear() {
            for (int i = 0; i < n; i++)
                visited[i] = false;
        }
    }


    public void readGraph(String path) throws IOException {
        FileReader fr = new FileReader(path);
        BufferedReader buf = new BufferedReader(fr);
        String line;
        int i = 0;

        while ((line = buf.readLine()) != null) {
            String splitA[] = line.split(" ");
            LinkedList<String> split = new LinkedList<String>();
            for (String s : splitA)
                if (!s.isEmpty())
                    split.add(s);

            if (graph == null)
                graph = new double[split.size()][split.size()];
            int j = 0;

            for (String s : split)
                if (!s.isEmpty())
                    graph[i][j++] = Double.parseDouble(s) + 1;

            i++;
        }

        n = graph.length;
        m = (int) (n * numAntFactor);


        trails = new double[n][n];
        probs = new double[n];
        ants = new Ant[m];
        for (int j = 0; j < m; j++)
            ants[j] = new Ant();
    }


    public static double pow(final double a, final double b) {
        final int x = (int) (Double.doubleToLongBits(a) >> 32);
        final int y = (int) (b * (x - 1072632447) + 1072632447);
        return Double.longBitsToDouble(((long) y) << 32);
    }


    private void probTo(Ant ant) {
        int i = ant.tour[currentIndex];

        double denom = 0.0;
        for (int l = 0; l < n; l++)
            if (!ant.visited(l))
                denom += pow(trails[i][l], alpha)
                        * pow(1.0 / graph[i][l], beta);


        for (int j = 0; j < n; j++) {
            if (ant.visited(j)) {
                probs[j] = 0.0;
            } else {
                double numerator = pow(trails[i][j], alpha)
                        * pow(1.0 / graph[i][j], beta);
                probs[j] = numerator / denom;
            }
        }

    }


    private int selectNextTown(Ant ant) {
              if (rand.nextDouble() < pr) {
            int t = rand.nextInt(n - currentIndex); // random town
            int j = -1;
            for (int i = 0; i < n; i++) {
                if (!ant.visited(i))
                    j++;
                if (j == t)
                    return i;
            }

        }

        probTo(ant);
        double r = rand.nextDouble();
        double tot = 0;
        for (int i = 0; i < n; i++) {
            tot += probs[i];
            if (tot >= r)
                return i;
        }

        throw new RuntimeException("Not supposed to get here.");
    }


    private void updateTrails() {

        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                trails[i][j] *= evaporation;


        for (Ant a : ants) {
            double contribution = Q / a.tourLength();
            for (int i = 0; i < n - 1; i++) {
                trails[a.tour[i]][a.tour[i + 1]] += contribution;
            }
            trails[a.tour[n - 1]][a.tour[0]] += contribution;
        }
    }


    private void moveAnts() {

        while (currentIndex < n - 1) {
            for (Ant a : ants)
                a.visitTown(selectNextTown(a));
            currentIndex++;
        }
    }


    private void setupAnts() {
        currentIndex = -1;
        for (int i = 0; i < m; i++) {
            ants[i].clear(); // faster than fresh allocations.
            ants[i].visitTown(rand.nextInt(n));
        }
        currentIndex++;

    }

    private void updateBest() {
        if (bestTour == null) {
            bestTour = ants[0].tour;
            bestTourLength = ants[0].tourLength();
        }
        for (Ant a : ants) {
            if (a.tourLength() < bestTourLength) {
                bestTourLength = a.tourLength();
                bestTour = a.tour.clone();
            }
        }
    }

    public static String tourToString(int tour[]) {
        String t = new String();
        for (int i : tour)
            t = t + " " + i;
        return t;
    }

    public int[] solve() {
            for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                trails[i][j] = c;

        int iteration = 0;
        while (iteration < maxIterations) {
            setupAnts();
            moveAnts();
            updateTrails();
            updateBest();
            iteration++;
        }
        System.out.println("Best tour length: " + (bestTourLength - n));
        System.out.println("Best tour:" + tourToString(bestTour));
        return bestTour.clone();
    }


    public static void main(String[] args) {
            if (args.length < 1) {
            System.err.println("Please specify a TSP data file.");
            return;
        }
        AntTsp anttsp = new AntTsp();
        try {
            anttsp.readGraph(args[0]);
        } catch (IOException e) {
            System.err.println("Error reading graph.");
            return;
        }

        for (; ; ) {
            anttsp.solve();
        }

    }
} 