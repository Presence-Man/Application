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
