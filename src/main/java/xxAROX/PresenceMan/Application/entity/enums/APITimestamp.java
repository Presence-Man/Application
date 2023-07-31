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
