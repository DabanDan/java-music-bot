package ovh.not.javamusicbot.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Database {
    private static final Logger LOGGER = LoggerFactory.getLogger(Database.class);

    private final ExecutorService executor = Executors.newCachedThreadPool(r -> new Thread(r, "patron-database"));
    private final StatementManager statementManager;
    private final HikariDataSource hikari;

    public Database(String sqlDirectoryPath, HikariConfig config) throws IOException {
        statementManager = new StatementManager(new File(sqlDirectoryPath).toURI());
        hikari = new HikariDataSource(config);
    }

    public CompletableFuture<Optional<GuildSettings>> settingsSelectGuildSettings(long guildId) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            String statement = statementManager.getStatement("select_guild_settings");

            try (Connection connection = hikari.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setLong(1, guildId);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    return Optional.empty();
                }

                GuildSettings guildSettings = new GuildSettings(resultSet);
                return Optional.of(guildSettings);
            } catch (SQLException e) {
                e.printStackTrace();
                return Optional.empty();
            }
        }, executor);
    }

    public CompletableFuture<Boolean> settingsUpdateGuildSettings(long guildId, String prefix) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            String statement = statementManager.getStatement("update_guild_settings");

            try (Connection connection = hikari.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setString(1, prefix);
                preparedStatement.setLong(2, guildId);

                int updated = preparedStatement.executeUpdate();
                LOGGER.debug("settingsUpdatedGuildSettings updated {} rows", updated);

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    public CompletableFuture<Boolean> settingsUpsertGuildSettings(long guildId, String prefix) throws SQLException {
        return CompletableFuture.supplyAsync(() -> {
            String statement = statementManager.getStatement("upsert_guild_prefix");

            try (Connection connection = hikari.getConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(statement);
                preparedStatement.setLong(1, guildId);
                preparedStatement.setString(2, prefix);
                preparedStatement.setString(3, prefix);

                boolean executed = preparedStatement.execute();
                LOGGER.debug("settingsUpsertGuildSettings executed {}", executed);

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }, executor);
    }

    public class GuildSettings {
        private final long id;
        private final long guildId;
        private final String prefix;

        private GuildSettings(ResultSet results) throws SQLException {
            id = results.getLong("id");
            guildId = results.getLong("guild_id");
            prefix = results.getString("prefix");
        }

        public long getId() {
            return id;
        }

        public long getGuildId() {
            return guildId;
        }

        public String getPrefix() {
            return prefix;
        }
    }
}
