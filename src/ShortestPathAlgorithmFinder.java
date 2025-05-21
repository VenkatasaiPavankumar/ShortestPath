import java.io.*;
import java.nio.file.*;
import java.util.*;

public class ShortestPathAlgorithmFinder {

    static class Edge {
        String destination;
        double distance;

        Edge(String destination, double distance) {
            this.destination = destination;
            this.distance = distance;
        }
    }

    static Map<String, List<Edge>> pathGraph = new HashMap<>();
    static List<String> shortestPath = new ArrayList<>();
    static double minimumDistance;

    public static void main(String[] args) throws IOException {
        loadDataFromCSVFile("src/sc_distances.csv");

        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.print("\nEnter start city (City, ST) or 'quit': ");
            String start = scanner.nextLine().trim();
            if (start.equalsIgnoreCase("quit")) break;

            while (!pathGraph.containsKey(start)) {
                System.out.println("City not found. Available cities: " + pathGraph.keySet());
                System.out.print("Enter start city (City, ST): ");
                start = scanner.nextLine().trim();
                if (start.equalsIgnoreCase("quit")) return;
            }

            System.out.print("Enter destination city (City, ST): ");
            String destination = scanner.nextLine().trim();
            while (!pathGraph.containsKey(destination)) {
                System.out.println("City not found. Available cities: " + pathGraph.keySet());
                System.out.print("Enter destination city (City, ST): ");
                destination = scanner.nextLine().trim();
            }

            minimumDistance = Double.MAX_VALUE;
            shortestPath.clear();

            identifyShortestPath(start, destination, 0.0, new ArrayList<>());

            if (!shortestPath.isEmpty()) {
                System.out.println("Shortest Path:");
                System.out.println(String.join(" -> ", shortestPath));
                System.out.printf("Total Distance: %.2f miles\n", minimumDistance);
            } else {
                System.out.println("No path found between the selected cities.");
            }
        }

        scanner.close();
    }

    static void loadDataFromCSVFile(String filename) throws IOException {
        Path filePath = Paths.get(filename).toAbsolutePath().normalize();
        List<String> lines = Files.readAllLines(filePath);
        //Removing the first line headers
        boolean isFirstLine = true;
        for (String line : lines) {
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }

            List<String> parts = new ArrayList<>();
            boolean inQuotes = false;
            StringBuilder current = new StringBuilder();

            for (char c : line.toCharArray()) {
                if (c == '"') {
                    inQuotes = !inQuotes;
                } else if (c == ',' && !inQuotes) {
                    parts.add(current.toString().trim());
                    current.setLength(0);
                } else {
                    current.append(c);
                }
            }
            parts.add(current.toString().trim());

            if (parts.size() == 3) {
                try {
                    String city1 = parts.get(0);
                    String city2 = parts.get(1);
                    double distance = Double.parseDouble(parts.get(2));

                    pathGraph.computeIfAbsent(city1, k -> new ArrayList<>()).add(new Edge(city2, distance));
                    pathGraph.computeIfAbsent(city2, k -> new ArrayList<>()).add(new Edge(city1, distance));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing distance in line: " + line);
                }
            } else {
                System.err.println("Invalid line format: " + line);
            }
        }
    }

    static void identifyShortestPath(String current, String destination, double currentDistance, List<String> path) {
        if (currentDistance >= minimumDistance) return;
        if (path.contains(current)) return;

        path.add(current);

        if (current.equals(destination)) {
            if (currentDistance < minimumDistance) {
                minimumDistance = currentDistance;
                shortestPath = new ArrayList<>(path);
            }
        } else {
            List<Edge> neighbors = new ArrayList<>(pathGraph.getOrDefault(current, Collections.emptyList()));
            neighbors.sort(Comparator.comparingDouble(e -> e.distance));

            for (Edge edge : neighbors) {
                identifyShortestPath(edge.destination, destination, currentDistance + edge.distance, path);
            }
        }

        path.remove(path.size() - 1);
    }
}
