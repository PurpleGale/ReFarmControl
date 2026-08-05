package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FarmControlCommand implements CommandExecutor {
    private final FarmControl farmControl;

    private final ReloadCommand reloadCommand;
    private final StatusCommand statusCommand;
    private final HistoryCommand historyCommand;
    private final NotifyCommand notifyCommand;

    public FarmControlCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
        reloadCommand = new ReloadCommand(farmControl);
        statusCommand = new StatusCommand(farmControl);
        historyCommand = new HistoryCommand(farmControl);
        notifyCommand = new NotifyCommand(farmControl);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (args.length == 0) {
            sendHelp(sender, s);
            return true;
        }
        if (args[0].equalsIgnoreCase("reload") || args[0].equalsIgnoreCase("rl")) {
            if (sender.hasPermission("farmcontrol.command.reload")) {
                return reloadCommand.onCommand(sender, command, s, args);
            } else {
                farmControl.getLanguageManager().sendMessage(sender, "general.no_permission");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("stats")) {
            if (sender.hasPermission("farmcontrol.command.status")) {
                return statusCommand.onCommand(sender, command, s, args);
            } else {
                farmControl.getLanguageManager().sendMessage(sender, "general.no_permission");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("history")) {
            if (sender.hasPermission("farmcontrol.command.history")) {
                return historyCommand.onCommand(sender, command, s, args);
            } else {
                farmControl.getLanguageManager().sendMessage(sender, "general.no_permission");
                return true;
            }
        }
        if (args[0].equalsIgnoreCase("notify")) {
            if (sender.hasPermission("farmcontrol.command.notify")) {
                return notifyCommand.onCommand(sender, command, s, args);
            } else {
                farmControl.getLanguageManager().sendMessage(sender, "general.no_permission");
                return true;
            }
        }
        sendHelp(sender, s);
        return true;
    }

    private void sendHelp(CommandSender sender, String cl) {
        farmControl.getLanguageManager().sendMessage(sender, "command.help.header", Placeholder.parsed("version", farmControl.getDescription().getVersion()));
        farmControl.getLanguageManager().sendMessage(sender, "command.help.github");
        sender.sendMessage("");
        if (sender.hasPermission("farmcontrol.command.reload")) {
            farmControl.getLanguageManager().sendMessage(sender, "command.help.usage_reload", Placeholder.parsed("label", cl));
        }
        if (sender.hasPermission("farmcontrol.command.status")) {
            farmControl.getLanguageManager().sendMessage(sender, "command.help.usage_status", Placeholder.parsed("label", cl));
        }
        if (sender.hasPermission("farmcontrol.command.history")) {
            farmControl.getLanguageManager().sendMessage(sender, "command.help.usage_history", Placeholder.parsed("label", cl));
        }
        if (sender.hasPermission("farmcontrol.command.notify")) {
            farmControl.getLanguageManager().sendMessage(sender, "command.help.usage_notify", Placeholder.parsed("label", cl));
        }
    }

    public TabCompleter getTabCompleter() {
        return new TabCompleter() {
            @Override
            public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
                List<String> completions = new ArrayList<>();
                if (args.length == 1) {
                    if (sender.hasPermission("farmcontrol.command.reload")) {
                        completions.add("reload");
                    }
                    if (sender.hasPermission("farmcontrol.command.status")) {
                        completions.add("status");
                    }
                    if (sender.hasPermission("farmcontrol.command.history")) {
                        completions.add("history");
                    }
                    if (sender.hasPermission("farmcontrol.command.notify")) {
                        completions.add("notify");
                    }
                }
                if (args.length == 2) {
                    if (args[0].equalsIgnoreCase("status") || args[0].equalsIgnoreCase("stats")) {
                        if (sender.hasPermission("farmcontrol.command.status")) {
                            for (World world : Bukkit.getWorlds()) {
                                completions.add(world.getName());
                            }
                        }
                    }
                }
                return StringUtil.copyPartialMatches(args[args.length - 1], completions, new ArrayList<>());
            }
        };
    }

}
