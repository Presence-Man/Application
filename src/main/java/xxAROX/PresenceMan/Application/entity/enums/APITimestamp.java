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

package xxAROX.PresenceMan.Application.entity.enums;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString
public enum APITimestamp {
    CUSTOM(),
    APP_START(-1L),
    NETWORK_SESSION_CREATE(-2L),
    SERVER_SESSION_CREATE(-3L),
    ;
    private long value;
    APITimestamp(long timestamp){
        value = timestamp;
    }
    APITimestamp(){
        this(0L);
    }

    public void setValue(long value) {
        this.value = value;
    }
}
