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

package com.teamdiluvian.wakacraft;

import com.teamdiluvian.wakacraft.command.WakaCommand;
import com.teamdiluvian.wakacraft.listener.WakaHandler;
import com.teamdiluvian.wakacraft.persistent.SQLWakaDatabase;
import com.teamdiluvian.wakacraft.persistent.connector.HikariWakaConnector;
import me.saiintbrisson.bungee.command.BungeeFrame;
import net.md_5.bungee.api.plugin.Plugin;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import redis.clients.jedis.JedisPool;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.Properties;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 21/09/2022
 */
public class WakaPlugin extends Plugin {

    private SQLWakaDatabase wakaDatabase;
    private JedisPool jedisPool;

    @Override
    public void onLoad() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        File databaseFile = new File(getDataFolder(), "database.properties");

        if(!databaseFile.exists()) {
            try (InputStream inputStream = getResourceAsStream("database.properties")) {
                Files.copy(inputStream, databaseFile.toPath());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        Properties properties = new Properties();
        try {
            properties.load(new FileReader(databaseFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        HikariWakaConnector connector = new HikariWakaConnector(properties);

        connector.connect();

        wakaDatabase = new SQLWakaDatabase(connector);

        jedisPool = new JedisPool(
            new GenericObjectPoolConfig<>(),
            properties.getProperty("redis.url")
        );
    }

    @Override
    public void onEnable() {
        getProxy().getPluginManager().registerListener(this, new WakaHandler(wakaDatabase, jedisPool));

        BungeeFrame bungeeFrame = new BungeeFrame(this);

        bungeeFrame.registerCommands(
            new WakaCommand(wakaDatabase, jedisPool)
        );
    }

    @Override
    public void onDisable() {
        wakaDatabase.getConnector()
            .disconnect();

        jedisPool.close();
    }
}
