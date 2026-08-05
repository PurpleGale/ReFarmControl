package com.froobworld.farmcontrol.controller.tracker;

import com.froobworld.farmcontrol.controller.action.Action;
import com.froobworld.farmcontrol.controller.entity.SnapshotEntity;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.entity.EntityType;

import java.util.*;
import java.util.stream.Collectors;

public class CycleStats {
    private final long startTime;
    private final long endTime;
    private final int entitiesAffected;
    private final int entitiesRemoved;
    private final Map<Action, Map<EntityType, Integer>> actionCounts;

    private CycleStats(long startTime, long endTime, int entitiesAffected, int entitiesRemoved, Map<Action, Map<EntityType, Integer>> actionCounts) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.entitiesAffected = entitiesAffected;
        this.entitiesRemoved = entitiesRemoved;
        this.actionCounts = actionCounts;
    }

    public long getStartTimeMillis() {
        return startTime;
    }

    public long getDurationMillis() {
        return startTime - endTime;
    }

    public int getAffectedEntityCount() {
        return entitiesAffected;
    }

    public int getEntitiesRemoved() {
        return entitiesRemoved;
    }

    public Component getBreakdownComponent() {
        Component result = Component.empty();
        boolean firstAction = true;

        for (Action action : actionCounts.keySet()) {
            int totalCount = actionCounts.get(action).values().stream()
                    .mapToInt(value -> value)
                    .sum();
            String entityBreakdown = actionCounts.get(action).entrySet().stream()
                    .sorted(Map.Entry.<EntityType, Integer>comparingByValue().reversed())
                    .map(entry -> "<red>" + entry.getValue() + "</red> <gray>" + entry.getKey().name().toLowerCase() + "</gray>")
                    .collect(Collectors.joining(", "));
            
            String line = (firstAction ? "" : "\n\n") + "<gold>- " + action.getName() + ": </gold><red>" + totalCount + "</red>\n" + entityBreakdown;
            result = result.append(MiniMessage.miniMessage().deserialize(line));
            firstAction = false;
        }
        return result;
    }

    public static class Builder {
        private long startTime;
        private final Set<SnapshotEntity> affectedEntities = new HashSet<>();
        private final Set<SnapshotEntity> removedEntities = new HashSet<>();
        private final Map<Action, Map<EntityType, Integer>> actionCounts = new HashMap<>();

        private Builder() {}

        public static Builder start(long startTime) {
            Builder builder = new Builder();
            builder.startTime = startTime;
            return builder;
        }

        public synchronized void action(Action action, SnapshotEntity entity) {
            affectedEntities.add(entity);
            if (action.removes()) {
                removedEntities.add(entity);
            }
            actionCounts.computeIfAbsent(action, a -> new HashMap<>())
                    .compute(entity.getEntityType(), (e, count) -> count != null ? count + 1 : 1);
        }

        public CycleStats end(long endTime) {
            return new CycleStats(startTime, endTime, affectedEntities.size(), removedEntities.size(), actionCounts);
        }

    }

}
