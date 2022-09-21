/*
 * MIT License
 *
 * Copyright (c) 2022 Luiz Otávio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.teamdiluvian.wakacraft.persistent;

import com.teamdiluvian.wakacraft.model.WakaPlayer;
import com.teamdiluvian.wakacraft.persistent.connector.HikariWakaConnector;
import com.teamdiluvian.wakacraft.util.TextReader;
import me.saiintbrisson.minecraft.command.annotation.Command;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 03/09/2022
 */
public class SQLWakaDatabase {

    private final HikariWakaConnector connector;

    private final ExecutorService executorService;
    private final TextReader textReader;

    public SQLWakaDatabase(@NotNull HikariWakaConnector connector) {
        this.connector = connector;

        String properties = connector.getProperties()
            .getProperty("maximumPoolSize");

        if (properties == null) {
            properties = "2";
        }

        executorService = Executors.newFixedThreadPool(
            Integer.parseInt(properties),
            runnable -> {
                Thread thread = new Thread(runnable);

                thread.setName("Wakacraft-Thread");

                return thread;
            }
        );

        textReader = new TextReader();
        try {
            textReader.loadFromResources("sql/");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (Connection connection = connector.createConnection()) {
            Statement statement = connection.createStatement();

            statement.execute(
                textReader.getSql("create_wakacraft_table")
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public @NotNull HikariWakaConnector getConnector() {
        return connector;
    }

    public @NotNull CompletableFuture<WakaPlayer> loadPlayer(@Nullable UUID unique, @Nullable String name) {
        if (isDisconnected()) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("Connection is not available"));
        }

        String query = unique != null ? textReader.getSql("retrieve_wakacraft_by_id") :
            textReader.getSql("retrieve_wakacraft_by_name");

        return CompletableFuture.supplyAsync(() -> {
            UUID uuid = unique != null ? unique : UUID.nameUUIDFromBytes(
                String.format("OfflinePlayer:%s", name).getBytes()
            );

            try (Connection connection = connector.createConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);

                preparedStatement.setString(1, unique != null ? unique.toString() : name);

                ResultSet resultSet = preparedStatement.executeQuery();
                if (!resultSet.next()) {
                    return createPlayer(uuid, name, System.currentTimeMillis()).join();
                }

                return WakaPlayer.of(
                    unique != null ? unique : uuid, name,
                    resultSet.getLong("measure_time"),
                    resultSet.getLong("created_at")
                );
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    public @NotNull CompletableFuture<Void> savePlayer(@Nullable UUID uniqueId, @Nullable String name, long measureTime) {
        if (isDisconnected()) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("Connection is not available"));
        }

        String query = uniqueId != null ? textReader.getSql("update_wakacraft_data_by_id") :
            textReader.getSql("update_wakacraft_data_by_name");

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = connector.createConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);

                preparedStatement.setLong(1, measureTime);

                preparedStatement.setString(
                    2,
                    uniqueId != null ? uniqueId.toString() : name
                );

                preparedStatement.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    public @NotNull CompletableFuture<Void> resetPlayer(@Nullable UUID uniqueId, @Nullable String name) {
        if (isDisconnected()) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("Connection is not available"));
        }

        String query = uniqueId != null ? textReader.getSql("reset_wakacraft_data_by_id") :
            textReader.getSql("reset_wakacraft_data_by_name");

        return CompletableFuture.runAsync(() -> {
            try (Connection connection = connector.createConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);

                preparedStatement.setLong(1, System.currentTimeMillis());

                preparedStatement.setString(
                    2,
                    uniqueId != null ? uniqueId.toString() : name
                );

                preparedStatement.executeUpdate();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    public @NotNull CompletableFuture<WakaPlayer> createPlayer(@NotNull UUID uniqueId, @Nullable String name, long measureTime) {
        if (isDisconnected()) {
            return CompletableFuture.failedFuture(new UnsupportedOperationException("Connection is not available"));
        }

        String query = textReader.getSql("create_wakacraft_data");
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = connector.createConnection()) {
                PreparedStatement preparedStatement = connection.prepareStatement(query);

                preparedStatement.setString(1, uniqueId.toString());

                preparedStatement.setString(2, name);
                preparedStatement.setLong(3, measureTime);

                preparedStatement.executeUpdate();

                return WakaPlayer.of(uniqueId, name, measureTime, System.currentTimeMillis());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }, executorService);
    }

    public @NotNull ExecutorService getThreadFactory() {
        return executorService;
    }

    private boolean isDisconnected() {
        return !connector.isConnected();
    }
}
