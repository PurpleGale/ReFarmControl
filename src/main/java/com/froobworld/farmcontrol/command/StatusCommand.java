package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.controller.FarmController;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import com.froobworld.farmcontrol.data.FcData;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

public class StatusCommand implements CommandExecutor {
    private final FarmControl farmControl;

    public StatusCommand(FarmControl farmControl) {
        this.farmControl = farmControl;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String cl, @NotNull String[] args) {
        World world;
        if (args.length == 1) {
            if (sender instanceof Player) {
                world = ((Player) sender).getWorld();
            } else {
                farmControl.getLanguageManager().sendMessage(sender, "general.must_specify_world");
                return false;
            }
        } else {
            world = Bukkit.getWorld(args[1]);
        }
        if (world == null) {
            String worldName = args.length > 1 ? args[1] : "unknown";
            farmControl.getLanguageManager().sendMessage(sender, "general.unknown_world", Placeholder.parsed("world_name", worldName));
            return true;
        }
        AtomicInteger entityCount = new AtomicInteger(0);
        AtomicInteger affectedCount = new AtomicInteger(0);
        Map<String, AtomicInteger> actionCount = new HashMap<>();

        final CompletableFuture<Void> completableFuture = new CompletableFuture<>();
        farmControl.getHookManager().getEntityGetterHook().getSnapshotEntities(world, FarmController.ENTITY_CLASSES).thenAccept(entities -> {
            entityCount.set(entities.size());
            for (SnapshotEntity entity : entities) {
                FcData fcData = entity.getFcData();
                if (fcData != null && !fcData.getActions().isEmpty()) {
                    affectedCount.incrementAndGet();
                    for (String action : fcData.getActions()) {
                        actionCount.computeIfAbsent(action, a -> new AtomicInteger(0)).incrementAndGet();
                    }
                }
            }
            completableFuture.complete(null);
        });
        completableFuture.thenRunAsync(() -> {
            farmControl.getLanguageManager().sendMessage(sender, "command.status.header", Placeholder.parsed("world", world.getName()));
            sender.sendMessage("");
            farmControl.getLanguageManager().sendMessage(sender, "command.status.total_entities", Placeholder.parsed("count", String.valueOf(entityCount.get())));
            farmControl.getLanguageManager().sendMessage(sender, "command.status.affected_entities", Placeholder.parsed("count", String.valueOf(affectedCount.get())));
            if (affectedCount.get() > 0) {
                sender.sendMessage("");
                farmControl.getLanguageManager().sendMessage(sender, "command.status.breakdown_header");
                for (Map.Entry<String, AtomicInteger> entry : actionCount.entrySet()) {
                    farmControl.getLanguageManager().sendMessage(
                        sender,
                        "command.status.breakdown_item",
                        Placeholder.parsed("action", entry.getKey()),
                        Placeholder.parsed("count", String.valueOf(entry.getValue().get()))
                    );
                }
            }
        });
        return true;
    }
}
