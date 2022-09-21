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

package com.teamdiluvian.wakacraft.model;

import lombok.*;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * @author Luiz Otávio de Farias Corrêa
 * @since 21/09/2022
 */
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@Setter
@ToString(of = "uniqueId")
public class WakaPlayer {

    public static WakaPlayer of(@NotNull UUID uniqueId, @NotNull String name, long measureTime, long created) {
        return new WakaPlayer(uniqueId, name, measureTime, created);
    }

    private final UUID uniqueId;

    private String playerName;

    private long measureTime;
    private long created;

    public String getFormatted() {
        long millis = (measureTime - created) / 1000;

        StringBuilder builder = new StringBuilder();

        if (millis >= 86400) {
            builder.append(millis / 86400).append("d ");
            millis %= 86400;
        }

        if (millis >= 3600) {
            builder.append(millis / 3600).append("h ");
            millis %= 3600;
        }

        if (millis >= 60) {
            builder.append(millis / 60).append("m ");
            millis %= 60;
        }

        if (millis > 0) {
            builder.append(millis).append("s");
        }

        return builder.toString();
    }

}
