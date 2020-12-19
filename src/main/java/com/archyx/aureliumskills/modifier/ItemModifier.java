package com.archyx.aureliumskills.modifier;

import com.archyx.aureliumskills.lang.CommandMessage;
import com.archyx.aureliumskills.lang.Lang;
import com.archyx.aureliumskills.stats.Stat;
import com.archyx.aureliumskills.util.ItemUtils;
import com.archyx.aureliumskills.util.LoreUtil;
import de.tr7zw.changeme.nbtapi.NBTCompound;
import de.tr7zw.changeme.nbtapi.NBTItem;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class ItemModifier {

    private static final NumberFormat nf = new DecimalFormat("#.#");

    public static ItemStack addItemModifier(ItemStack item, Stat stat, double value) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getItemModifiersCompound(nbtItem);
        compound.setDouble(stat.name(), value);
        return nbtItem.getItem();
    }

    public static ItemStack convertToNewModifiers(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getItemModifiersCompound(nbtItem);
        for (StatModifier modifier : getOldItemModifiers(item)) {
            compound.setDouble(modifier.getStat().name(), modifier.getValue());
        }
        for (String key : nbtItem.getKeys()) {
            if (key.startsWith("skillsmodifier-item-")) {
                nbtItem.removeKey(key);
            }
        }
        return nbtItem.getItem();
    }

    public static ItemStack removeItemModifier(ItemStack item, Stat stat) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getItemModifiersCompound(nbtItem);
        compound.removeKey(stat.name());
        return nbtItem.getItem();
    }

    public static ItemStack removeAllItemModifiers(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        NBTCompound compound = ItemUtils.getItemModifiersCompound(nbtItem);
        for (String key : compound.getKeys()) {
            nbtItem.removeKey(key);
        }
        return nbtItem.getItem();
    }

    public static List<StatModifier> getOldItemModifiers(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        List<StatModifier> modifiers = new ArrayList<>();
        for (String key : nbtItem.getKeys()) {
            if (key.contains("skillsmodifier-item-")) {
                String[] keySplit = key.split("-");
                if (keySplit.length == 3) {
                    Stat stat = Stat.valueOf(key.split("-")[2].toUpperCase());
                    int value = nbtItem.getInteger(key);
                    modifiers.add(new StatModifier(key, stat, value));
                }
            }
        }
        return modifiers;
    }

    public static List<StatModifier> getItemModifiers(ItemStack item) {
        NBTItem nbtItem = new NBTItem(item);
        List<StatModifier> modifiers = new ArrayList<>();
        NBTCompound compound = ItemUtils.getItemModifiersCompound(nbtItem);
        for (String key : compound.getKeys()) {
            Stat stat = Stat.valueOf(key);
            double value = compound.getDouble(key);
            modifiers.add(new StatModifier("AureliumSkills.Modifiers.Item." + key, stat, value));
        }
        return modifiers;
    }

    public static void addLore(ItemStack item, Stat stat, double value, Locale locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore;
            if (meta.getLore() != null) {
                if (meta.getLore().size() > 0) {
                    lore = meta.getLore();
                }
                else {
                    lore = new LinkedList<>();
                }
            }
            else {
                lore = new LinkedList<>();
            }
            lore.add(0, LoreUtil.replace(Lang.getMessage(CommandMessage.ITEM_MODIFIER_ADD_LORE, locale),
                    "{stat}", stat.getDisplayName(locale),
                    "{value}", nf.format(value),
                    "{color}", stat.getColor(locale)));
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

    public static void removeLore(ItemStack item, Stat stat, Locale locale) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            List<String> lore = meta.getLore();
            if (lore != null && lore.size() > 0) {
                lore.removeIf(line -> line.contains(stat.getDisplayName(locale)));
            }
            meta.setLore(lore);
        }
        item.setItemMeta(meta);
    }

}
