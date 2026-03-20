package ru.kredwi.berrybush.bush;

import lombok.val;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import ru.kredwi.berrybush.tracking.TrackingSession;

import java.util.Collection;
import java.util.function.BiConsumer;

public interface BushAction extends BiConsumer<TrackingSession, Player> {
    default void addAttribute(ItemStack s, Plugin plugin) {
        ItemMeta meta = s.getItemMeta();
        if (meta != null) {
            PersistentDataContainer container = meta.getPersistentDataContainer();
            container.set(new NamespacedKey(plugin, "other_berry"), PersistentDataType.STRING, "true");
            s.setItemMeta(meta);
        }
    }

    @NotNull
    default Collection<ItemStack> getDrop(@NotNull Block block) {
        val original = block.getBlockData();
        val mature = block.getBlockData().clone();

        val ageable = (Ageable) mature;
        ageable.setAge(ageable.getMaximumAge());
        block.setBlockData(mature);
        Collection<ItemStack> drops = block.getDrops();
        block.setBlockData(original);
        return drops;
    }
}
