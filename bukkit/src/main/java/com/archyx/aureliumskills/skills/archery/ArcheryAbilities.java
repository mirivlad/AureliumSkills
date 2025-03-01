package com.archyx.aureliumskills.skills.archery;

import com.archyx.aureliumskills.AureliumSkills;
import com.archyx.aureliumskills.ability.Ability;
import com.archyx.aureliumskills.ability.AbilityManager;
import com.archyx.aureliumskills.ability.AbilityProvider;
import com.archyx.aureliumskills.configuration.OptionL;
import com.archyx.aureliumskills.data.PlayerData;
import com.archyx.aureliumskills.skills.Skills;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Random;

public class ArcheryAbilities extends AbilityProvider implements Listener {

    private final Random r = new Random();

    public ArcheryAbilities(AureliumSkills plugin) {
        super(plugin, Skills.ARCHERY);
    }

    public void bowMaster(EntityDamageByEntityEvent event, Player player, PlayerData playerData) {
        if (OptionL.isEnabled(Skills.ARCHERY)) {
            if (plugin.getAbilityManager().isEnabled(Ability.BOW_MASTER)) {
                if (!player.hasPermission("aureliumskills.archery")) {
                    return;
                }
                if (playerData.getAbilityLevel(Ability.BOW_MASTER) > 0) {
                    double multiplier = 1 + (getValue(Ability.BOW_MASTER, playerData) / 100);
                    event.setDamage(event.getDamage() * multiplier);
                }
            }
        }
    }

    public void stun(PlayerData playerData, LivingEntity entity) {
        if (r.nextDouble() < (getValue(Ability.STUN, playerData) / 100)) {
            if (entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED) != null) {
                AttributeInstance speed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                if (speed != null) {
                    //Applies stun
                    double reducedSpeed = speed.getValue() * 0.2;
                    AttributeModifier modifier = new AttributeModifier("AureliumSkills-Stun", -1 * reducedSpeed, AttributeModifier.Operation.ADD_NUMBER);
                    speed.addModifier(modifier);
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            AttributeInstance newSpeed = entity.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
                            if (newSpeed != null) {
                                for (AttributeModifier attributeModifier : newSpeed.getModifiers()) {
                                    if (attributeModifier.getName().equals("AureliumSkills-Stun")) {
                                        newSpeed.removeModifier(attributeModifier);
                                    }
                                }
                            }
                        }
                    }.runTaskLater(plugin, 40L);
                }
            }
        }
    }

    @EventHandler
    public void removeStun(PlayerQuitEvent event) {
        //Removes stun on logout
        AttributeInstance speed = event.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        if (speed != null) {
            for (AttributeModifier attributeModifier : speed.getModifiers()) {
                if (attributeModifier.getName().equals("AureliumSkills-Stun")) {
                    speed.removeModifier(attributeModifier);
                }
            }
        }
    }

    public void piercing(EntityDamageByEntityEvent event, PlayerData playerData, Player player, Arrow arrow) {
        // Disable if enemy is blocking with a shield
        Entity damaged = event.getEntity();
        if (damaged instanceof Player) {
            Player damagedPlayer = (Player) damaged;
            if (damagedPlayer.isBlocking()) {
                return;
            }
        }
        if (r.nextDouble() < (getValue(Ability.PIERCING, playerData) / 100)) {
            arrow.setBounce(false);
            arrow.setPierceLevel(arrow.getPierceLevel() + 1);
        }
    }

    public void pierceInit(PlayerData playerData, Player player, Arrow arrow) {
        if (blockAbility(player)) return;
        if (r.nextDouble() < (getValue(Ability.PIERCING, playerData) / 100)) {
            // Adds 1 pierce to the initial shot otherwise it doesn't pierce on non-lethal damage.
            arrow.setPierceLevel(arrow.getPierceLevel() + 1);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void archeryListener(EntityDamageByEntityEvent event) {
        if (OptionL.isEnabled(Skills.ARCHERY)) {
            if (event.getDamager() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getDamager();
                if (arrow.getShooter() instanceof Player) {
                    Player player = (Player) arrow.getShooter();
                    if (blockAbility(player)) return;
                    // Applies abilities
                    PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    AbilityManager options = plugin.getAbilityManager();
                    if (options.isEnabled(Ability.STUN)) {
                        if (event.getEntity() instanceof LivingEntity) {
                            LivingEntity entity = (LivingEntity) event.getEntity();
                            stun(playerData, entity);
                        }
                    }
                    if (options.isEnabled(Ability.PIERCING)) {
                        piercing(event, playerData, player, arrow);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void pierceListener(EntityShootBowEvent event) {
        if (OptionL.isEnabled(Skills.ARCHERY)) {
            if (event.getProjectile() instanceof Arrow) {
                Arrow arrow = (Arrow) event.getProjectile();
                if (arrow.getShooter() instanceof Player) {
                    Player player = (Player) arrow.getShooter();
                    PlayerData playerData = plugin.getPlayerManager().getPlayerData(player);
                    if (playerData == null) return;
                    AbilityManager options = plugin.getAbilityManager();
                    if (options.isEnabled(Ability.PIERCING)) {
                        pierceInit(playerData, player, arrow);
                    }
                }
            }
        }
    }
}
