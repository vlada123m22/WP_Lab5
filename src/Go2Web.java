import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Go2Web {
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
            System.out.println(result);
        } catch (Exception e) {
            System.out.println("Error fetching the webpage: " + e.getMessage());
        }
    }

    private static void searchWeb(String query) {
        try {
            String searchUrl = "https://www.bing.com/search?q=" + query.replace(" ", "+");
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

            extractSearchResults(content.toString());
        } catch (Exception e) {
            System.out.println("Error performing search: " + e.getMessage());
        }
    }

    private static void extractSearchResults(String html) {
        Pattern pattern = Pattern.compile("<a href=\\\"(https?://[^\\\"]+)\\\".*?>(.*?)</a>");
        Matcher matcher = pattern.matcher(html);
        int count = 0;
        while (matcher.find() && count < 10) {
            System.out.println((count + 1) + ". " + matcher.group(2) + " - " + matcher.group(1));
            count++;
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
        System.out.println("Usage:");
        System.out.println("go2web -u <URL>         # Fetch a webpage and print the readable content");
        System.out.println("go2web -s <search-term> # Search a term using Bing and display top 10 results");
        System.out.println("go2web -h               # Show this help message");
    }
}