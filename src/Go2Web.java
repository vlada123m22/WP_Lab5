import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class Go2Web {
    private static final Map<String, String> cache = new HashMap<>();

    public static void main(String[] args) {
        if (args.length == 0) {
            printHelp();
            return;
        }

        if (args[0].equals("-h")) {
            printHelp();
            return;
        }

        try {
            if (args[0].equals("-u")) {
                String url = args[1];
                String response = makeHttpRequest(url);
                System.out.println(response);
            } else if (args[0].equals("-s")) {
                String searchTerm = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
                String searchResults = searchUsingEngine(searchTerm);
                System.out.println(searchResults);
            } else {
                printHelp();
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    private static void printHelp() {
        System.out.println("Usage:");
        System.out.println("go2web -u <URL>         # Make an HTTP request to the specified URL and print the response");
        System.out.println("go2web -s <search-term> # Search the term using your favorite search engine and print top 10 results");
        System.out.println("go2web -h               # Show this help");
    }

    private static String makeHttpRequest(String urlString) throws IOException {
        if (cache.containsKey(urlString)) {
            return cache.get(urlString);  // Return cached response if available
        }

        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Handle HTTP redirect (3xx)
        int responseCode = connection.getResponseCode();
        if (responseCode >= 300 && responseCode < 400) {
            String redirectUrl = connection.getHeaderField("Location");
            return makeHttpRequest(redirectUrl);  // Follow the redirect
        }

        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();

        String responseStr = response.toString();
        cache.put(urlString, responseStr);  // Cache the response
        return responseStr;
    }

    private static String searchUsingEngine(String searchTerm) throws IOException {
        // Simple query to Google (or any other search engine URL)
        String queryUrl = "https://www.google.com/search?q=" + URLEncoder.encode(searchTerm, "UTF-8");
        String response = makeHttpRequest(queryUrl);

        // Simple extraction of search results (just a naive approach, improving the extraction is needed)
        List<String> results = extractSearchResults(response);
        StringBuilder resultStr = new StringBuilder("Top 10 Results:\n");

        for (int i = 0; i < Math.min(10, results.size()); i++) {
            resultStr.append((i + 1) + ". " + results.get(i) + "\n");
        }

        return resultStr.toString();
    }

    private static List<String> extractSearchResults(String html) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("<h3.*?><a href=\"(.*?)\">(.*?)</a></h3>");
        Matcher matcher = pattern.matcher(html);

        while (matcher.find()) {
            String link = matcher.group(1);
            String title = matcher.group(2);
            results.add(title + " (" + link + ")");
        }
        return results;
    }
}
