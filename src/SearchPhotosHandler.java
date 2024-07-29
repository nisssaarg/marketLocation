import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class SearchPhotosHandler implements HttpHandler {

    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            logger.info("Received request to search photos");
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            Search search = new Search();
            List<String> searchResults = search.searchPhotos(queryParams);

            if (!searchResults.isEmpty()) {
                // Set response headers for a zip file
                exchange.getResponseHeaders().add("Content-Type", "application/zip");
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                try (ZipOutputStream zos = new ZipOutputStream(byteArrayOutputStream)) {
                    for (String filePath : searchResults) {
                        File file = new File(filePath);
                        if (file.exists()) {
                            try (FileInputStream fis = new FileInputStream(file)) {
                                ZipEntry zipEntry = new ZipEntry(file.getName());
                                zos.putNextEntry(zipEntry);
                                byte[] buffer = new byte[1024];
                                int length;
                                while ((length = fis.read(buffer)) > 0) {
                                    zos.write(buffer, 0, length);
                                }
                                zos.closeEntry();
                            }
                        }
                    }
                }

                // Write the zip content to the response
                byte[] zipBytes = byteArrayOutputStream.toByteArray();
                exchange.sendResponseHeaders(200, zipBytes.length);
                OutputStream os = exchange.getResponseBody();
                os.write(zipBytes);
                os.close();

                logger.info("Search photos request processed successfully with parameters: " + queryParams.toString());
            } else {
                String response = "No search results found for: " + queryParams.toString();
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info("No search results for parameters: " + queryParams.toString());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning("Received non-GET request to /photos/search");
        }
    }

    
    private Map<String, String> parseQueryParams(String query) {
        Map<String, String> queryParams = new HashMap<>();
        if (query != null) {
            for (String param : query.split("&")) {
                String[] pair = param.split("=");
                if (pair.length == 2) {
                    queryParams.put(pair[0], pair[1]);
                }
            }
        }
        return queryParams;
    }
}
