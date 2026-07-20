package com.froobworld.farmcontrol.controller;

import com.froobworld.farmcontrol.FarmControl;
import com.froobworld.farmcontrol.config.FcConfig;
import com.froobworld.farmcontrol.utils.EntityCategory;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

import java.util.*;

public class SpawnLimiter implements Listener {
    private final FarmControl farmControl;
    private final Map<World, List<SpawnLimitRule>> ruleCache = new HashMap<>();

    public SpawnLimiter(FarmControl farmControl) {
        this.farmControl = farmControl;
        Bukkit.getPluginManager().registerEvents(this, farmControl);
    }

    public void clearCache() {
        ruleCache.clear();
    }

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        Location location = event.getLocation();
        World world = location.getWorld();
        if (world == null) {
            return;
        }

        List<SpawnLimitRule> rules = ruleCache.computeIfAbsent(world, w -> {
            List<SpawnLimitRule> parsedRules = new ArrayList<>();
            FcConfig.WorldSettings settings = farmControl.getFcConfig().worldSettings.of(w);
            if (settings != null) {
                List<String> limitStrings = settings.spawnLimits.get();
                if (limitStrings != null) {
                    for (String str : limitStrings) {
                        try {
                            String[] parts = str.split(";");
                            if (parts.length >= 3) {
                                String entityType = parts[0].trim();
                                int limit = Integer.parseInt(parts[1].trim());
                                String distance = parts[2].trim();
                                Set<String> reasons = new HashSet<>();
                                if (parts.length >= 4) {
                                    for (String reason : parts[3].split(",")) {
                                        reasons.add(reason.trim().toLowerCase());
                                    }
                                }
                                parsedRules.add(new SpawnLimitRule(entityType, limit, distance, reasons));
                            }
                        } catch (Exception e) {
                            farmControl.getLogger().warning("Invalid spawn limit entry: '" + str + "'. Error: " + e.getMessage());
                        }
                    }
                }
            }
            return parsedRules;
        });

        if (rules.isEmpty()) {
            return;
        }

        for (SpawnLimitRule rule : rules) {
            // Check entity type/category match
            EntityCategory category = EntityCategory.ofName(rule.entityType);
            boolean typeMatch = false;
            if (category != null) {
                if (category.isMember(event.getEntityType())) {
                    typeMatch = true;
                }
            } else {
                if (event.getEntityType().name().equalsIgnoreCase(rule.entityType)) {
                    typeMatch = true;
                }
            }
            if (!typeMatch) {
                continue;
            }

            // Check spawn reason match
            if (rule.reasons != null && !rule.reasons.isEmpty()) {
                String reasonStr = event.getSpawnReason().name().toLowerCase();
                if (!rule.reasons.contains(reasonStr) && !rule.reasons.contains("*")) {
                    continue;
                }
            }

            // Count existing matching entities
            int count = 0;
            if ("same-chunk".equalsIgnoreCase(rule.distance)) {
                for (Entity entity : location.getChunk().getEntities()) {
                    if (entity.isDead() || entity == event.getEntity()) {
                        continue;
                    }
                    if (category != null) {
                        if (category.isMember(entity.getType())) {
                            count++;
                        }
                    } else {
                        if (entity.getType() == event.getEntityType()) {
                            count++;
                        }
                    }
                }
            } else {
                double r = rule.distanceSquared > 0 ? Math.sqrt(rule.distanceSquared) : 0;
                if (r > 0) {
                    Collection<Entity> nearby = world.getNearbyEntities(location, r, r, r);
                    for (Entity entity : nearby) {
                        if (entity.isDead() || entity == event.getEntity()) {
                            continue;
                        }
                        if (category != null) {
                            if (category.isMember(entity.getType())) {
                                count++;
                            }
                        } else {
                            if (entity.getType() == event.getEntityType()) {
                                count++;
                            }
                        }
                    }
                }
            }

            if (count >= rule.limit) {
                event.setCancelled(true);
                return; // Cancel spawn and stop processing rules
            }
        }
    }

    private static class SpawnLimitRule {
        public final String entityType;
        public final int limit;
        public final String distance;
        public final double distanceSquared;
        public final Set<String> reasons;

        public SpawnLimitRule(String entityType, int limit, String distance, Set<String> reasons) {
            this.entityType = entityType;
            this.limit = limit;
            this.distance = distance;
            if (!"same-chunk".equalsIgnoreCase(distance)) {
                double dist = Double.parseDouble(distance);
                this.distanceSquared = dist * dist;
            } else {
                this.distanceSquared = -1;
            }
            this.reasons = reasons;
        }
    }
}
