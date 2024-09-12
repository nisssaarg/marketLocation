import java.io.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

class SearchPhotosHandler implements HttpHandler {

    private static final String RECEIVED_NON_GET_REQUEST_TO_PHOTOS_SEARCH = "Received non-GET request to /photos/search";
    private static final String NO_SEARCH_RESULTS_FOR_PARAMETERS = "No search results for parameters: ";
    private static final String NO_SEARCH_RESULTS_FOUND_FOR = "No search results found for: ";
    private static final String SEARCH_PHOTOS_REQUEST_PROCESSED_SUCCESSFULLY_WITH_PARAMETERS = "Search photos request processed successfully with parameters: ";
    private static final String APPLICATION_ZIP = "application/zip";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String RECEIVED_REQUEST_TO_SEARCH_PHOTOS = "Received request to search photos";
    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        if ("GET".equals(exchange.getRequestMethod())) {
            logger.info(RECEIVED_REQUEST_TO_SEARCH_PHOTOS);
            Map<String, String> queryParams = parseQueryParams(exchange.getRequestURI().getQuery());
            Search search = new Search();
            List<String> searchResults = search.searchPhotos(queryParams);

            if (!searchResults.isEmpty()) {
                // Set response headers for a zip file
                exchange.getResponseHeaders().add(CONTENT_TYPE, APPLICATION_ZIP);
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

                logger.info(SEARCH_PHOTOS_REQUEST_PROCESSED_SUCCESSFULLY_WITH_PARAMETERS + queryParams.toString());
            } else {
                String response = NO_SEARCH_RESULTS_FOUND_FOR + queryParams.toString();
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.close();
                logger.info(NO_SEARCH_RESULTS_FOR_PARAMETERS + queryParams.toString());
            }
        } else {
            exchange.sendResponseHeaders(405, -1); // Method Not Allowed
            logger.warning(RECEIVED_NON_GET_REQUEST_TO_PHOTOS_SEARCH);
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
