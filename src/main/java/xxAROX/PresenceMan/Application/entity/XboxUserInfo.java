package xxAROX.PresenceMan.Application.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession;
import net.raphimc.minecraftauth.util.MicrosoftConstants;

@Getter
@AllArgsConstructor
@ToString
public final class XboxUserInfo {
    private String xuid;
    private String gamertag;

    public static final AbstractStep<?, StepFullBedrockSession.FullBedrockSession> DEVICE_CODE_LOGIN = MinecraftAuth.builder()
            .withClientId(MicrosoftConstants.BEDROCK_ANDROID_TITLE_ID).withScope(MicrosoftConstants.SCOPE_TITLE_AUTH)
            .deviceCode()
            .withDeviceToken("Android")
            .sisuTitleAuthentication(MicrosoftConstants.BEDROCK_XSTS_RELYING_PARTY)
            .buildMinecraftBedrockChainStep(true, true);

    public XboxUserInfo(StepFullBedrockSession.FullBedrockSession session) {
        xuid = session.getMcChain().getXuid();
        gamertag = session.getMcChain().getDisplayName();
    }
}
