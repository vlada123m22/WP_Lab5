# Go2Web

Go2Web is a command-line web search and page-fetching tool that allows users to:
- Fetch and display readable content from a web page.
- Perform a Bing search and display the top 10 results.
- Select a result from the search to fetch its content.
- Utilize a caching mechanism to store fetched pages, reducing unnecessary network requests.
- Handle content negotiation for both HTML and JSON responses.

## Features
- **Fetch Web Pages (-u flag)**: Retrieve readable content from a given URL.
- **Search the Web (-s flag)**: Perform a Bing search and display the top 10 results.
- **Caching Mechanism**: Saves fetched pages to `cache.txt` to reduce repeated network requests.
- **Content Negotiation**: Handles both HTML and JSON responses appropriately.
- **Redirection Handling**: Follows redirects if the requested URL points to a different location.

## Installation
1. Ensure you have Java installed (Java 8 or later).
2. Compile the source code:
   ```sh
   javac Go2Web.java
   ```

## Usage
### Fetch a Web Page
```sh
java Go2Web -u <URL>
```
Example:
```sh
java Go2Web -u https://www.example.com
```

### Perform a Bing Search
```sh
java Go2Web -s "search query"
```
Example:
```sh
java Go2Web -s "Python tutorial"
```
- The program will display the top 10 search results.
- You will be prompted to enter a number to fetch the content of the selected page.

### Help Menu
```sh
java Go2Web -h
```
Displays usage instructions.

## How Caching Works
- Cached results are stored in `cache.txt`.
- If a URL is already cached and is less than **10 minutes old**, it is served from the cache.
- If it is older than **10 minutes**, a new request is made, and the cache is updated.

## Implementation Details
- Uses `HttpURLConnection` to fetch web content.
- Extracts human-readable text from HTML pages.
- Uses regular expressions to extract search results from Bing.
- Handles HTTP redirections and content types (HTML, JSON).


