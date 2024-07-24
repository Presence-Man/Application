/*
 * Copyright (c) 2024-2024. By Jan-Michael Sohn also known as @xxAROX.
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package xxAROX.PresenceMan.Application.entity.infos;

import lombok.Getter;
import lombok.ToString;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.entity.Gateway;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

@ToString
public final class XboxUserInfo {
    @Getter
    private final String xuid;
    @Getter
    private final String gamertag;

    public static final String default_skin_url = "/api/v1/images/skins/{xuid}";
    public static final String default_head_url = "/api/v1/images/heads/{xuid}";

    private String skin_url = default_skin_url;
    private String head_url = default_head_url;

    public Image skin = null; // CACHED
    public Image head = null; // CACHED

    public XboxUserInfo(String xuid, String gamertag) {
        this.xuid = xuid;
        this.gamertag = gamertag;
        skin_url = default_skin_url.replace("{xuid}", xuid);
        head_url = default_head_url.replace("{xuid}", xuid);
    }

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

    public String getSkinURL(){
        return Gateway.getUrl() + skin_url;
    }
    public String getHeadURL(){
        return Gateway.getUrl() + head_url;
    }

    public Image getHeadImage() {
        return getHeadImage(false);
    }
    public Image getHeadImage(boolean regenerate) {
        if (!regenerate && head != null) return head;
        Image img = null;
        try {
            img = ImageIO.read(new URL(getHeadURL()));
        } catch (Exception e) {
            App.getLogger().error("Error while fetching head: ", e);
            try {
                img = ImageIO.read(new URL(head_url = "https://presence-man.com" + default_head_url));
            } catch (IOException ignored) {}
        }
        head = img;
        return img;
    }
    public Image getSkinImage() {
        return getSkinImage(false);
    }
    public Image getSkinImage(boolean regenerate) {
        if (!regenerate && skin != null) return skin;
        Image img = null;
        try {
            img = ImageIO.read(new URL(getSkinURL()));
        } catch (Exception e) {
            App.getLogger().error("Error while fetching head: ", e);
            try {
                img = ImageIO.read(new URL(skin_url = "https://presence-man.com" + Gateway.getUrl() + default_skin_url));
            } catch (IOException ignored) {}
        }
        skin = img;
        return img;
    }
}
