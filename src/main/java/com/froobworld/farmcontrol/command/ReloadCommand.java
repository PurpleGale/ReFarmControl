package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ReloadCommand implements CommandExecutor {
    private final FarmControl farmControl;

    public ReloadCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        try {
            farmControl.reload();
            farmControl.getLanguageManager().sendMessage(sender, "command.reload.success");
        } catch (Exception e) {
            farmControl.getLanguageManager().sendMessage(sender, "command.reload.error");
            e.printStackTrace();
        }
        return true;
    }
}
