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

package com.teamdiluvian.wakacraft.listener;

import com.teamdiluvian.wakacraft.persistent.SQLWakaDatabase;
import lombok.RequiredArgsConstructor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ClientConnectEvent;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 21/09/2022
 */
@RequiredArgsConstructor
public class WakaHandler implements Listener {

    private final SQLWakaDatabase wakaDatabase;

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerConnect(PostLoginEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();

        if (!proxiedPlayer.hasPermission("wakacraft.use")) {
            return;
        }

        wakaDatabase.loadPlayer(proxiedPlayer.getUniqueId(), proxiedPlayer.getName())
            .whenComplete((player, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            });
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        ProxiedPlayer proxiedPlayer = event.getPlayer();

        if (!proxiedPlayer.hasPermission("wakacraft.use")) {
            return;
        }

        wakaDatabase.savePlayer(proxiedPlayer.getUniqueId(), proxiedPlayer.getName(), System.currentTimeMillis())
            .whenComplete((player, throwable) -> {
                if (throwable != null) {
                    throwable.printStackTrace();
                }
            });
    }

}
