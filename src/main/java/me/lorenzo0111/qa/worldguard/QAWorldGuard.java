package me.lorenzo0111.qa.worldguard;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagConflictException;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import me.zombie_striker.qg.api.QAWeaponPrepareShootEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

public final class QAWorldGuard extends JavaPlugin implements Listener {
    private StateFlag use;

    @Override
    public void onLoad() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            use = new StateFlag("qa-use", true);
            registry.register(use);
        } catch (FlagConflictException ignored) {
            Flag<?> existing = registry.get("qa-use");
            if (existing instanceof StateFlag) {
                use = (StateFlag) existing;
            }
        }

        if (use == null) {
            this.setEnabled(false);
        }
    }

    @Override
    public void onEnable() {
        Bukkit.getPluginManager().registerEvents(this,this);
        this.getLogger().info("Successfully hooked QualityArmory with WorldGuard.");
    }

    @EventHandler
    public void onInteract(QAWeaponPrepareShootEvent event) {
        Player player = event.getPlayer();

        LocalPlayer localplayer = WorldGuardPlugin.getPlugin(WorldGuardPlugin.class).wrapPlayer(player);
        boolean canBypass = WorldGuard.getInstance().getPlatform().getSessionManager().hasBypass(localplayer, BukkitAdapter.adapt(player.getWorld()));
        if (canBypass) return;

        RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
        RegionQuery query = container.createQuery();

        if (!query.testState(BukkitAdapter.adapt(event.getPlayer().getLocation()), localplayer, use)) {
            event.setCancelled(true);
        }
    }
}