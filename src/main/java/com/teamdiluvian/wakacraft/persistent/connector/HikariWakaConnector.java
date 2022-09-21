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

package com.teamdiluvian.wakacraft.persistent.connector;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.util.Properties;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 03/09/2022
 */
@RequiredArgsConstructor
public class HikariWakaConnector  {

  private final Properties properties;

  private HikariDataSource dataSource;

  public Properties getProperties() {
    return properties;
  }

  public void connect() {
    if (dataSource != null) {
      throw new UnsupportedOperationException("Already connected");
    }

    dataSource = new HikariDataSource(
      new HikariConfig(properties)
    );
  }

  public void disconnect() {
    if (dataSource == null) {
      throw new UnsupportedOperationException("Already disconnected");
    }

    dataSource.close();
  }

  public boolean isConnected() {
    return dataSource != null && dataSource.isRunning();
  }

  public Connection createConnection() throws Exception {
    if (!isConnected()) {
      throw new UnsupportedOperationException("Not connected");
    }

    return dataSource.getConnection();
  }
}
