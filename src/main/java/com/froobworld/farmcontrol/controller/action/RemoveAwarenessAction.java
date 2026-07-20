package com.froobworld.farmcontrol.controller.action;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;

public class RemoveAwarenessAction extends Action {

    public RemoveAwarenessAction() {
        super("remove-awareness", Mob.class, false, true, true);
    }

    @Override
    public void doAction(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        mob.setAware(false);
    }

    @Override
    public void undoAction(Entity entity) {
        if (!(entity instanceof Mob mob)) {
            return;
        }
        mob.setAware(true);
        if (entity instanceof org.bukkit.entity.Villager villager) {
            try {
                // In 1.21+, restoring awareness requires a refresh of the brain/goal selectors.
                // Toggling AI state forces this update, allowing Villagers to restock.
                boolean hadAi = villager.hasAI();
                villager.setAI(!hadAi);
                villager.setAI(hadAi);
            } catch (Throwable ignored) {}
        }
    }
}
