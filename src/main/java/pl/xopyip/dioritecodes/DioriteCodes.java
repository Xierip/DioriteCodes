package pl.xopyip.dioritecodes;

import org.diorite.Diorite;
import org.diorite.chat.ChatColor;
import org.diorite.chat.component.ComponentBuilder;
import org.diorite.command.CommandPriority;
import org.diorite.command.PluginCommandBuilder;
import org.diorite.entity.Player;
import org.diorite.inventory.item.BaseItemStack;
import org.diorite.material.Material;
import org.diorite.plugin.DioritePlugin;
import org.diorite.plugin.Plugin;
import org.diorite.plugin.PluginException;
import org.diorite.scheduler.TaskBuilder;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by xopyip on 29.07.15.
 */
@Plugin(name = "DioriteCodes", version = "1.0", author = "XopyIP", website = "http://xopyip.pl")
public class DioriteCodes extends DioritePlugin {
    private String lastCode = "";
    private long lastCodeGeneration;
    private static final char[] CHARS = {'q', 'w', 'e', 'r', 't', 'y', 'u', 'i', 'o', 'p', 'a', 's', 'd', 'f', 'g', 'h', 'j', 'k', 'l', 'z', 'x', 'c', 'v', 'b', 'n', 'm'};
    private static final Random random = new Random();

    @Override
    public void onEnable() {
        File configFile = new File("plugins/DioriteCodes/items.txt");
        configFile.getParentFile().mkdirs();
        if(!configFile.exists()){
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        List<String> items = null;
        try {
             items = Files.readAllLines(configFile.toPath(), Charset.defaultCharset());
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert items != null;
        if(items.size()<=0){
            System.out.println("Dodaj itemy do pliku /plugins/DioriteCodes/items.txt");
            System.out.println();
            System.out.println("BYE");
            try {
                getPluginLoader().disablePlugin(this);
            } catch (PluginException e) {
                e.printStackTrace();
            }
        }
        lastCodeGeneration = System.currentTimeMillis();
        PluginCommandBuilder code = Diorite.createCommand(this, "code");
        code.priority(CommandPriority.NORMAL);
        final List<String> finalItems = items;
        code.executor((commandSender, command, s, matcher, arguments) -> {
            if (!(commandSender instanceof Player)) {
                commandSender.sendMessage(createComponentBuilder().append(" Komenda tylko dla graczy.").color(ChatColor.RED).create());
                return;
            }
            if (arguments.length() <= 0 || arguments.length() >= 2) {
                commandSender.sendMessage(createComponentBuilder().append(" Poprawne uzycie: /code [kod].").color(ChatColor.RED).create());
                return;
            }

            if (lastCodeGeneration + TimeUnit.MINUTES.toMillis(1) <= System.currentTimeMillis()) {
                commandSender.sendMessage(createComponentBuilder().append(" Minal czas.").color(ChatColor.RED).create());
                return;
            }
            if (arguments.asString(0).equals(lastCode)) {
                commandSender.sendMessage(createComponentBuilder().append(" Brawo!.").color(ChatColor.GREEN).create());
                Diorite.getOnlinePlayers().stream()
                        .filter(player -> !player.getUniqueID().equals(((Player) commandSender).getUniqueID()))
                        .forEach(player ->
                                player.sendMessage(createComponentBuilder().append(" Gracz ").color(ChatColor.BLUE)
                                        .append(commandSender.getName()).color(ChatColor.GOLD).bold(true)
                                        .append(" przepisal kod jako pierwszy.").color(ChatColor.BLUE).create()));
                assert finalItems != null;
                ((Player)commandSender).getInventory().add(new BaseItemStack(Material.matchMaterial(finalItems.get(random.nextInt(finalItems.size())))));
                lastCode = "";
                lastCodeGeneration -= TimeUnit.MINUTES.toMillis(1);

            } else {
                commandSender.sendMessage(createComponentBuilder().append(" Podales zly kod.").color(ChatColor.RED).create());
            }

        });
        Diorite.getCommandMap().registerCommand(code.build());

        TaskBuilder.start(this, () -> {
            StringBuilder stringBuilder = new StringBuilder();
            for (int x = 0; x < 10; x++) {
                char c = CHARS[random.nextInt(CHARS.length - 1)];
                stringBuilder.append(random.nextBoolean() ? String.valueOf(c).toUpperCase() : String.valueOf(c));
            }
            String newCode = stringBuilder.toString();
            lastCode = newCode;
            lastCodeGeneration = System.currentTimeMillis();
            Diorite.broadcastMessage(createComponentBuilder()
                    .append(" Aby otrzymac losowy item wpisz ").color(ChatColor.BLUE)
                    .append("/code " + newCode).color(ChatColor.LIGHT_PURPLE).bold(true)
                    .append(" w ciagu 60 sekund.").color(ChatColor.BLUE).create());
        }).async().delay(10 * 60 * 20).repeated().start(10 * 60 * 20);

    }

    private static ComponentBuilder createComponentBuilder() {
        return new ComponentBuilder("[DioriteCodes]").color(ChatColor.GREEN);
    }
}
