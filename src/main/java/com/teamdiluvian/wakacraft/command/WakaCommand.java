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

package com.teamdiluvian.wakacraft.command;

import com.teamdiluvian.wakacraft.model.WakaPlayer;
import com.teamdiluvian.wakacraft.persistent.SQLWakaDatabase;
import lombok.RequiredArgsConstructor;
import me.saiintbrisson.minecraft.command.annotation.Command;
import me.saiintbrisson.minecraft.command.annotation.Optional;
import me.saiintbrisson.minecraft.command.command.Context;
import me.saiintbrisson.minecraft.command.target.CommandTarget;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Arrays;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 21/09/2022
 */
@RequiredArgsConstructor
public class WakaCommand {

    private final SQLWakaDatabase wakaDatabase;

    @Command(
        name = "wakacraft",
        target = CommandTarget.PLAYER
    )
    public void handleHelpCommand(Context<ProxiedPlayer> context) {
        ProxiedPlayer proxiedPlayer = context.getSender();

        String[] messages = {
            " ",
            " &eWakacraft Help",
            "  &e/wakacraft get [player] &7- Get the waka time of a player.",
            "  &e/wakacraft reset [player] &7- Reset the waka time of a player.",
            "  &e/wakacraft set [player] [time] &7- Set the waka time of a player.",
            " "
        };

        BaseComponent[] components = TextComponent.fromLegacyText(
            colorize(String.join("\n", messages))
        );

        proxiedPlayer.sendMessage(components);
    }

    @Command(
        name = "wakacraft.get"
    )
    public void handleGetCommand(Context<ProxiedPlayer> context, @Optional String name) {
        ProxiedPlayer proxiedPlayer = context.getSender();

        wakaDatabase.loadPlayer(
            name == null ? proxiedPlayer.getUniqueId() : null,
            name == null ? proxiedPlayer.getName() : name
        ).whenComplete((wakaPlayer, throwable) -> {
            if (throwable != null) {
                proxiedPlayer.sendMessage(
                    TextComponent.fromLegacyText(
                        colorize("&cAn error occurred while trying to get the waka time of the player.")
                    )
                );
                return;
            }

            if (wakaPlayer == null) {
                proxiedPlayer.sendMessage(
                    TextComponent.fromLegacyText(
                        colorize("&cThe player &e" + name + " &cwas not found.")
                    )
                );
                return;
            }

            proxiedPlayer.sendMessage(
                TextComponent.fromLegacyText(
                    colorize("&e" + wakaPlayer.getFormatted() + " &eof waka time.")
                )
            );
        });
    }

    @Command(
        name = "wakacraft.reset"
    )
    public void handleResetCommand(Context<ProxiedPlayer> context, @Optional String name) {
        ProxiedPlayer proxiedPlayer = context.getSender();

        wakaDatabase.resetPlayer(
            name == null ? proxiedPlayer.getUniqueId() : null,
            name == null ? proxiedPlayer.getName() : name
        ).thenAccept((unused) -> {
            proxiedPlayer.sendMessage(
                TextComponent.fromLegacyText(
                    colorize("&aThe waka time of the player &e" + name + " &awas reset.")
                )
            );
        });
    }

    @Command(
        name = "wakacraft.set"
    )
    public void handleSetCommand(Context<ProxiedPlayer> context, String name, long time) {
        ProxiedPlayer proxiedPlayer = context.getSender();

        wakaDatabase.savePlayer(
            null,
            name,
            time
        ).thenAccept(unused -> {
            proxiedPlayer.sendMessage(
                TextComponent.fromLegacyText(
                    colorize("&aThe waka time of the player &e" + name + " &awas set to &etime&a.")
                )
            );
        });
    }

    private String colorize(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
