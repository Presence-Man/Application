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
import lombok.ToString;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import xxAROX.PresenceMan.Application.App;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

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

    public Image getProfileImage() {
        Image img = null;
        try {
            img = ImageIO.read(new URL("https://presence-man.com/api/v1/images/heads/" + xuid));
        } catch (Exception e) {
            App.getLogger().error("Error while fetching head: ", e);
            try {
                img = ImageIO.read(new URL("https://minecraftfaces.com/wp-content/bigfaces/big-steve-face.png"));
            } catch (IOException ignored) {}
        }

        return img;
    }
}
