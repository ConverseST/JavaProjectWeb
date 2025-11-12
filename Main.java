import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final int DEFAULT_PORT = 8080;
    private static final Path WEB_ROOT = Paths.get("web").toAbsolutePath().normalize();

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {
                System.out.println("[WARN] Invalid port supplied. Falling back to " + DEFAULT_PORT);
            }
        }

        if (!Files.exists(WEB_ROOT) || !Files.isDirectory(WEB_ROOT)) {
            System.err.println("[ERROR] Unable to start preview: missing web assets at " + WEB_ROOT);
            System.exit(1);
        }

        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticFileHandler());
        server.setExecutor(null);
        server.start();

        String stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        System.out.printf("[%s] Preview ready at http://localhost:%d%n", stamp, port);
        System.out.println("Press Ctrl+C to stop the server.");
    }

    private static class StaticFileHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String requestPath = sanitizePath(exchange.getRequestURI().getPath());

            Path filePath = WEB_ROOT.resolve(requestPath).normalize();
            if (!filePath.startsWith(WEB_ROOT) || !Files.exists(filePath) || Files.isDirectory(filePath)) {
                filePath = WEB_ROOT.resolve("index.html");
            }

            byte[] response = Files.readAllBytes(filePath);
            Headers headers = exchange.getResponseHeaders();
            headers.add("Content-Type", detectContentType(filePath));
            headers.add("Cache-Control", "no-cache");

            exchange.sendResponseHeaders(200, response.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(response);
            }
        }

        private String sanitizePath(String rawPath) {
            if (rawPath == null || rawPath.isBlank() || "/".equals(rawPath)) {
                return "index.html";
            }
            String sanitized = rawPath.startsWith("/") ? rawPath.substring(1) : rawPath;
            if (sanitized.isBlank()) {
                sanitized = "index.html";
            }
            return sanitized;
        }

        private String detectContentType(Path file) {
            String probe = null;
            try {
                probe = Files.probeContentType(file);
            } catch (IOException ignored) {
            }
            if (probe != null) {
                return probe;
            }
            String fallback = URLConnection.guessContentTypeFromName(file.toString());
            return fallback != null ? fallback : "application/octet-stream";
        }
    }
}

