/*
 * Copyright (c) 2024. By Jan-Michael Sohn also known as @xxAROX.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Application.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import xxAROX.PresenceMan.Application.ui.tabs.FeaturedServersTab;

import java.util.Objects;

@AllArgsConstructor
@Getter @Setter @Accessors(chain = true)
@ToString
public class FeaturedServer {
    private String name;
    private FeaturedServersTab.__Server game;
    private FeaturedServersTab.__Server mode;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FeaturedServer that = (FeaturedServer) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getGame(), that.getGame()) && Objects.equals(getMode(), that.getMode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getGame(), getMode());
    }
}
