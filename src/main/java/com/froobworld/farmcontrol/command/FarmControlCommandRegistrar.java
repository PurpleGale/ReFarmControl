package com.froobworld.farmcontrol.command;

import com.froobworld.farmcontrol.FarmControl;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FarmControlCommandRegistrar {

    private static class DummyCommand extends Command {
        protected DummyCommand(@NotNull String name) {
            super(name);
        }

        @Override
        public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
            return true;
        }
    }

    public static void register(FarmControl plugin) {
        final DummyCommand dummyCommand = new DummyCommand("farmcontrol");
        final FarmControlCommand mainCommand = new FarmControlCommand(plugin);
        final ReloadCommand reloadCommand = new ReloadCommand(plugin);
        final NotifyCommand notifyCommand = new NotifyCommand(plugin);
        final HistoryCommand historyCommand = new HistoryCommand(plugin);
        final StatusCommand statusCommand = new StatusCommand(plugin);

        try {
            plugin.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
                final Commands commands = event.registrar();

                LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("farmcontrol")
                        .requires(src -> src.getSender().hasPermission("farmcontrol.command.farmcontrol"))
                        .executes(ctx -> {
                            mainCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[0]);
                            return 1;
                        })
                        .then(Commands.literal("reload")
                                .requires(src -> src.getSender().hasPermission("farmcontrol.command.reload"))
                                .executes(ctx -> {
                                    reloadCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[]{"reload"});
                                    return 1;
                                }))
                        .then(Commands.literal("notify")
                                .requires(src -> src.getSender().hasPermission("farmcontrol.command.notify"))
                                .executes(ctx -> {
                                    notifyCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[]{"notify"});
                                    return 1;
                                }))
                        .then(Commands.literal("history")
                                .requires(src -> src.getSender().hasPermission("farmcontrol.command.history"))
                                .executes(ctx -> {
                                    historyCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[]{"history"});
                                    return 1;
                                }))
                        .then(Commands.literal("status")
                                .requires(src -> src.getSender().hasPermission("farmcontrol.command.status"))
                                .executes(ctx -> {
                                    statusCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[]{"status"});
                                    return 1;
                                })
                                .then(Commands.argument("world", ArgumentTypes.world())
                                        .executes(ctx -> {
                                            World world = ctx.getArgument("world", World.class);
                                            statusCommand.onCommand(ctx.getSource().getSender(), dummyCommand, "farmcontrol", new String[]{"status", world.getName()});
                                            return 1;
                                        })));

                commands.register(root.build(), "FarmControl main command", List.of("fc"));
            });
        } catch (Throwable t) {
            // Fallback for environments without Paper Brigadier lifecycle events
            plugin.registerCommands();
        }
    }
}
