package xxAROX.PresenceMan.Application.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.raphimc.mcauth.step.bedrock.StepMCChain;

@Getter
@AllArgsConstructor
@ToString
public final class XboxUserInfo {
    private String xuid;
    private String gamertag;

    public XboxUserInfo(StepMCChain.MCChain fromInput) {
        xuid = fromInput.xuid();
        gamertag = fromInput.displayName();
    }
}
