package ml.mrgonci.latinplay;

import java.io.File;
import java.util.HashMap;
import java.util.UUID;

import java.io.*;
import java.util.concurrent.TimeUnit;

import net.md_5.bungee.api.*;
import net.md_5.bungee.api.chat.*;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;


public class Main extends Plugin implements Listener
{
    private HashMap<UUID, Long> cooldown = new HashMap<UUID, Long>();

    private static Main instance;
    private File file;
    private static Configuration configuration;


    public void onEnable() {

        Main.instance = this;

        this.getLogger().info(ChatColor.GOLD + "");
        this.getLogger().info(ChatColor.GOLD + "[*] LatinStream v2 Cargado [*]");
        this.getLogger().info(ChatColor.GRAY + "Cooldown Arreglado y Soporte de plataformas");
        this.getLogger().info(ChatColor.GOLD + "");

        file = new File(getDataFolder() + "/config.yml");
        try {
            if (!getDataFolder().exists()) {
                getDataFolder().mkdir();
            }
            if (!file.exists()) {
                file.createNewFile();
                configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
                configuration.set("Twitch-Live-Message", "%newline%&f&l&m★-----------------------★&f&l%newline% &a%player% &6&lestá en directo en &5&lTwitch &6&L¡Ve a verlo! %newline% &e&nEnlace: %message%&f%newline%&f&l&m★-----------------------★%newline%");
                configuration.set("Youtube-Live-Message", "%newline%&f&l&m★-----------------------★&f&l%newline% &a%player% &6&lestá en directo en &c&lYou&f&lTube &6&L¡Ve a verlo! %newline% &e&nEnlace: %message%&f%newline%&f&l&m★-----------------------★%newline%");
                configuration.set("Default-Live-Message", "%newline%&f&l&m★-----------------------★&f&l%newline% &a%player% &6&lestá en directo ¡Ve a verlo! %newline% &e&nEnlace: %message%&f%newline%&f&l&m★-----------------------★%newline%");
                configuration.set("Cooldown-Time", 30);
                configuration.set("Correct-Usage", "&cUso correcto: /directo (enlace)");
                configuration.set("Permission-error", "&c¡No tienes permisos para hacer esto!");
                ConfigurationProvider.getProvider(YamlConfiguration.class).save(configuration, file);
            }
            configuration = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[*] An error ocurred loading the files  [*]");
        }


        int time = configuration.getInt("Cooldown-Time");
        String twitchmessage = configuration.getString("Twitch-Live-Message");
        String ytmessage = configuration.getString("Youtube-Live-Message");
        String defaultmsg = configuration.getString("Default-Live-Message");
        String usage = configuration.getString("Correct-Usage");
        String permission = configuration.getString("Permission-error");

        this.getProxy().getPluginManager().registerCommand((Plugin) this, (Command) new Command("directo") {
            public void execute(final CommandSender sender, final String[] args) {
                if (sender instanceof ProxiedPlayer) {
                    final ProxiedPlayer p = (ProxiedPlayer) sender;
                    if (p.hasPermission("latinstream.uso")) {
                        /**
                         * Check if cooldown is 0
                         * */
                        if (cooldown.containsKey(p.getUniqueId())) {
                            long timesleft = ((cooldown.get(p.getUniqueId()) / 1000) + time) - (System.currentTimeMillis() / 1000);

                            if (timesleft > 0) {
                                p.sendMessage((BaseComponent) new TextComponent(ChatColor.RED + "Tienes que esperar " + timesleft + " segundos para ejecutar el comando"));
                            }
                            /*
                          else{
                                p.sendMessage((BaseComponent) new TextComponent(ChatColor.RED + "Tienes que esperar 0 segundos para ejecutar el comando"));
                                cooldown.remove(p.getUniqueId());
                            }*/
                        } else {
                            if (args.length == 1) {
                                String msg = "";
                                for (int i = 0; i < args.length; ++i) {
                                    msg = String.valueOf(msg) + args[i] + " ";
                                }
                                cooldown.put(p.getUniqueId(), System.currentTimeMillis());
                                clearcooldown(p);
                                if (msg.contains("twitch")) {
                                        String sendmsg = twitchmessage.replace("%player%", p.getDisplayName()).replace("%message%", msg).replace("%newline%", "\n");
                                        final BaseComponent[] cp = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', sendmsg));
                                        getProxy().broadcast(cp);
                                }
                                else if (msg.contains("youtube")) {
                                    String sendytmsg = ytmessage.replace("%player%", p.getDisplayName()).replace("%message%", msg).replace("%newline%", "\n");
                                    final BaseComponent[] cp = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', sendytmsg));
                                    getProxy().broadcast(cp);
                                }
                                else{
                                    String defaultmsgs = defaultmsg.replace("%player%", p.getDisplayName()).replace("%message%", msg).replace("%newline%", "\n");
                                    final BaseComponent[] cp = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', defaultmsgs));
                                    getProxy().broadcast(cp);
                                }
                            } else {
                                final BaseComponent[] correctusage = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', usage));
                                p.sendMessage(correctusage);
                            }
                        }
                    } else {
                        final BaseComponent[] permerror = TextComponent.fromLegacyText(ChatColor.translateAlternateColorCodes('&', permission));
                        p.sendMessage(permerror);
                    }
                }
            }
        });
    }
    public void clearcooldown(ProxiedPlayer p) {
        getProxy().getScheduler().schedule(this, new Runnable() {
            @Override
            public void run() {
                cooldown.remove(p.getUniqueId());
            }
        }, 30, 1440, TimeUnit.SECONDS);
    }
    public static Main getInstance() {
        return Main.instance;
    }
}
