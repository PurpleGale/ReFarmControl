package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.tracker.CycleStats;
import com.froobworld.farmcontrol.utils.DurationDisplayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class HistoryCommand implements CommandExecutor {
    private final FarmControl farmControl;

    public HistoryCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<CycleStats> cycleHistory = farmControl.getFarmController().getCycleHistoryManager().getCycleHistory();
        if (cycleHistory.isEmpty()) {
            farmControl.getLanguageManager().sendMessage(sender, "command.history.no_history");
            return true;
        }

        farmControl.getLanguageManager().sendMessage(sender, "command.history.header");
        for (CycleStats cycleStats : cycleHistory) {
            sender.sendMessage("");
            long timeSinceCycle = System.currentTimeMillis() - cycleStats.getStartTimeMillis();
            String durationStr = DurationDisplayer.getDurationInMinutesAndSeconds(timeSinceCycle);
            farmControl.getLanguageManager().sendMessage(sender, "command.history.from_time", Placeholder.parsed("time", durationStr));

            Component removedSuffix = farmControl.getLanguageManager().getConditionalSuffix(
                sender,
                "command.history.removed_suffix",
                cycleStats.getEntitiesRemoved(),
                "removed"
            );

            Component line = farmControl.getLanguageManager().getComponent(
                sender,
                "command.history.summary",
                Placeholder.parsed("affected", String.valueOf(cycleStats.getAffectedEntityCount())),
                Placeholder.component("removed_suffix", removedSuffix)
            ).hoverEvent(HoverEvent.showText(cycleStats.getBreakdownComponent()));

            sender.sendMessage(line);
        }

        return true;
    }
}
