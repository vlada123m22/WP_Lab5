import java.io.*;
import java.net.*;
import java.util.*;
import java.util.regex.*;

public class Go2Web {
    // Cache to store previously fetched web pages
    private static final Map<String, String> cache = new HashMap<>();

    public static void main(String[] args) {
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
    }

    private static void fetchWebPage(String urlString) {
        // Check if the page is cached
        if (cache.containsKey(urlString)) {
            System.out.println("Serving from cache:");
            System.out.println(cache.get(urlString));
            return;
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept", "text/html,application/json");

            // Handle HTTP redirects
            int status = conn.getResponseCode();
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM) {
                String newUrl = conn.getHeaderField("Location");
                url = new URL(newUrl);
                conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");
                conn.setRequestProperty("Accept", "text/html,application/json");
            }

            // Read response content
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();

            // Handle content negotiation
            String responseType = conn.getContentType();
            String result = responseType.contains("application/json") ? content.toString() : extractTextContent(content.toString());

            // Cache the response
            cache.put(urlString, result);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("Error fetching the webpage: " + e.getMessage());
        }
    }

    private static void searchWeb(String query) {
        try {
            String searchUrl = "https://www.bing.com/search?q=" + URLEncoder.encode(query, "UTF-8");
            URL url = new URL(searchUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");

            // Read search results page
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder content = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
            reader.close();
            //System.out.println(content.toString());
            // Extract search results
            List<String> results = extractSearchResults(content.toString());
            if (results.isEmpty()) {
                System.out.println("No search results found.");
                return;
            }

            // Display search results
            for (int i = 0; i < results.size(); i++) {
                System.out.println((i + 1) + ". " + results.get(i));
            }

            // Prompt user for selection
            promptUserForSelection(results);

        } catch (Exception e) {
            System.out.println("Error performing search: " + e.getMessage());
        }
    }

    private static List<String> extractSearchResults(String html) {
        List<String> results = new ArrayList<>();
        Pattern pattern = Pattern.compile("<li class=\\\"b_algo\\\".*?<h2><a href=\\\"(https?://[^\"]+)\\\".*?>(.*?)</a>", Pattern.DOTALL);
        Matcher matcher = pattern.matcher(html);

        int count = 0;
        while (matcher.find() && count < 10) {
            String url = matcher.group(1);
            String title = matcher.group(2).replaceAll("<.*?>", "").trim(); // Remove HTML tags from title
            results.add(title + " <" + url + ">");
            count++;
        }

        return results;
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
        // Remove unwanted content like scripts and styles
        html = html.replaceAll("(?s)<script.*?>.*?</script>", "");
        html = html.replaceAll("(?s)<style.*?>.*?</style>", "");
        html = html.replaceAll("<[^>]+>", " "); // Remove HTML tags
        html = html.replaceAll("&nbsp;", " "); // Replace non-breaking spaces
        html = html.replaceAll("\\s+", " ").trim(); // Normalize spaces
        return html;
    }

    private static void printHelp() {
        // Display available command options
        System.out.println("Usage:");
        System.out.println("go2web -u <URL>         # Fetch a webpage and print the readable content");
        System.out.println("go2web -s <search-term> # Search a term using Bing and display top 10 results");
        System.out.println("go2web -h               # Show this help message");
    }
}
