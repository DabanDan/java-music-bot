package ovh.not.javamusicbot.database;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

class StatementManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(StatementManager.class);
    private final Map<String, String> statements = new HashMap<>();

    StatementManager(URI sqlDirectoryPath) throws IOException {
        List<Path> paths = Files.walk(Paths.get(sqlDirectoryPath))
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());

        for (Path path : paths) {
            String name = formatFileName(path.getFileName().toString());
            LOGGER.debug("Loading sql statement {}", name);
            byte[] bytes = Files.readAllBytes(path);
            String statement = new String(bytes);
            statements.put(name, statement);
        }
    }

    // strips .sql and converts to lower case
    private String formatFileName(String fileName) {
        return fileName.substring(0, fileName.length() - 4).toLowerCase();
    }

    String getStatement(String name) {
        return statements.get(name);
    }
}
