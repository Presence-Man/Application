package xxAROX.PresenceMan.Application.entity;

import lombok.*;

import java.util.Objects;

@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public final class Connection {
    private String ip;
    private String network;
    private String server;

    public Connection apply(String ip, String network, String server){
        if (!Objects.equals(ip, this.ip)) return new Connection(ip, network, server);
        else if (!Objects.equals(network, this.network)) return new Connection(ip, network, server);
        else if (!Objects.equals(server, this.server)) {
            this.server = server;
            return this;
        }
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Connection that = (Connection) o;
        return Objects.equals(getIp(), that.getIp()) && Objects.equals(getNetwork(), that.getNetwork()) && Objects.equals(getServer(), that.getServer());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIp(), getNetwork(), getServer());
    }

    public String getSafeName(){
        return server + " on " + network;
    }
}
