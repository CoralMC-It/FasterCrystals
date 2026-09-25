/*
 * Copyright (C) 2023-2026 Jyguy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package xyz.reknown.fastercrystals.listener.bukkit;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.world.WorldUnloadEvent;
import xyz.reknown.fastercrystals.FasterCrystals;
import xyz.reknown.fastercrystals.repository.CrystalRepository;

public class CrystalStateListener implements Listener {

    private static final long DELAY = 40L;
    private final FasterCrystals plugin;
    private final CrystalRepository crystalRepository;

    public CrystalStateListener() {
        this.plugin = FasterCrystals.getInstance();
        this.crystalRepository = plugin.getCrystalRepository();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntityType() != EntityType.END_CRYSTAL) return;
        crystalRepository.add((EnderCrystal) event.getEntity());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRemoveFromWorld(EntityRemoveFromWorldEvent event) {
        if (event.getEntityType() != EntityType.END_CRYSTAL) return;

        EnderCrystal crystal = (EnderCrystal) event.getEntity();
        int entityId = crystal.getEntityId();

        // add delay so that it is detected as a crystal when interact happens after destruction.
        // Not the entity scheduler: Folia retires it right after this event, which would drop the task and leak the
        // crystal (and with it the crystal's whole world).
        FoliaScheduler.getGlobalRegionScheduler().runDelayed(plugin, task -> crystalRepository.remove(entityId, crystal), DELAY);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onWorldUnload(WorldUnloadEvent event) {
        crystalRepository.unloadWorldCrystals(event.getWorld());
    }
}
