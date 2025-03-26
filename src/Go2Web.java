import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class
Go2Web {
    private static final String CACHE_FILE = "cache.txt";
    //private static final long CACHE_EXPIRATION = 10 * 60 * 1000; // Cache expires in 10 minutes
    private static final Map<String, String> cache = new HashMap<>();
    private static final Map<String, Long> cacheTimestamps = new HashMap<>();

    public static void main(String[] args) {
        loadCache();

        if (args.length < 1) {
            printHelp();
            return;
        }

        switch (args[0]) {
            case "-u":
                if (args.length < 2) {
                    System.out.println("Error: URL is required.");
                } else {
                    fetchWebPage(args[1]);
                }
                break;
            case "-s":
                if (args.length < 2) {
                    System.out.println("Error: Search term is required.");
                } else {
                    searchWeb(args[1]);
                }
                break;
            case "-h":
                printHelp();
                break;
            default:
                System.out.println("Invalid option.");
                printHelp();
        }

        saveCache();
    }

    private static void fetchWebPage(String urlString) {
        if (isCached(urlString)) {
            System.out.println("🔄 Serving from cache:");
            System.out.println(cache.get(urlString));
            return;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "text/html,application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            String result = extractTextContent(content.toString());
            cache.put(urlString, result);
            cacheTimestamps.put(urlString, System.currentTimeMillis());

            System.out.println(result);
        } catch (Exception e) {
            //System.out.println("Error fetching the webpage: " + e.getMessage());
            System.out.println(e.getMessage());
        }
    }

    private static void searchWeb(String query) {
        if (isCached(query)) {
            System.out.println("🔄 Serving search results from cache:");
            displayResults(Arrays.asList(cache.get(query).split(";")));
            return;
        }

        try {
            String searchUrl = "https://www.bing.com/search?q=" + URLEncoder.encode(query, "UTF-8");
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            List<String> results = extractSearchResults(content.toString());
            if (results.isEmpty()) {
                System.out.println("No search results found.");
                return;
            }

            // Store results in cache
            cache.put(query, String.join(";", results));
            cacheTimestamps.put(query, System.currentTimeMillis());

            displayResults(results);
        } catch (Exception e) {
            System.out.println("Error performing search: " + e.getMessage());
        }
    }

    private static List<String> extractSearchResults(String html) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("<li class=\\\"b_algo\\\".*?<h2><a href=\\\"(https?://[^\"]+)\\\".*?>(.*?)</a>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        while (matcher.find() && results.size() < 10) {
            String url = matcher.group(1);
            String title = matcher.group(2).replaceAll("<.*?>", "").trim();
            results.add(title + " <" + url + ">");
        }
        return results;
    }

    private static void displayResults(List<String> results) {
        for (int i = 0; i < results.size(); i++) {
            System.out.println((i + 1) + ". " + results.get(i));
        }

        promptUserForSelection(results);
    }

    private static void promptUserForSelection(List<String> results) {
        System.out.println("Enter the number of the website you want to visit:");
        Scanner scanner = new Scanner(System.in);

        while (true) {
            try {
                int choice = Integer.parseInt(scanner.nextLine());
                if (choice >= 1 && choice <= results.size()) {
                    String selectedResult = results.get(choice - 1);
                    String url = selectedResult.substring(selectedResult.indexOf("<") + 1, selectedResult.length() - 1);
                    fetchWebPage(url);
                    break;
                } else {
                    System.out.println("Invalid selection. Please enter a number between 1 and " + results.size());
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a number.");
            }
        }
    }

    private static String extractTextContent(String html) {
        html = html.replaceAll("(?s)<script.*?>.*?</script>", "");
        html = html.replaceAll("(?s)<style.*?>.*?</style>", "");
        html = html.replaceAll("<[^>]+>", " ");
        html = html.replaceAll("&nbsp;", " ");
        html = html.replaceAll("\\s+", " ").trim();
        return html;
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("go2web -u <URL>         # Fetch a webpage and print readable content");
        System.out.println("go2web -s <search-term> # Search using Bing and display top 10 results");
        System.out.println("go2web -h               # Show this help message");
    }

    private static void loadCache() {
        File file = new File(CACHE_FILE);
        if (!file.exists()) return;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|", 3);
                if (parts.length == 3) {
                    String key = parts[0];
                    long timestamp = Long.parseLong(parts[1]);
                    String value = parts[2];
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading cache: " + e.getMessage());
        }
    }

    private static void saveCache() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(CACHE_FILE))) {
            for (String key : cache.keySet()) {
                writer.write(key + "|" + cacheTimestamps.get(key) + "|" + cache.get(key));
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving cache: " + e.getMessage());
        }
    }

    private static boolean isCached(String key) {
        return cache.containsKey(key);
    }
}
