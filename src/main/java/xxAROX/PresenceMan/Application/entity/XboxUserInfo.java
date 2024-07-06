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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;
import net.raphimc.minecraftauth.MinecraftAuth;
import net.raphimc.minecraftauth.step.AbstractStep;
import net.raphimc.minecraftauth.step.bedrock.session.StepFullBedrockSession;
import net.raphimc.minecraftauth.util.MicrosoftConstants;
import xxAROX.PresenceMan.Application.App;
import xxAROX.PresenceMan.Application.AppInfo;
import xxAROX.PresenceMan.Application.Bootstrap;
import xxAROX.PresenceMan.Application.ui.tabs.PartnersTab;
import xxAROX.PresenceMan.Application.utils.Utils;
import xxAROX.WebRequester.WebRequester;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Objects;

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
        var fallback_icon = Objects.requireNonNull(Bootstrap.class.getClassLoader().getResource(AppInfo.icon));
        Image img;
        try {
            img = ImageIO.read(new URL("https://presence-man.com/api/v1/images/heads/" + xuid));
        } catch (Exception e) {
            App.getLogger().error("Error while fetching head: ", e);
            img = new ImageIcon(fallback_icon).getImage();
        }

        return img;
    }
}
