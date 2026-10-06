package com.opcratesworks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.*;
import org.bukkit.command.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.*;

public class OPCratesWorks extends JavaPlugin implements Listener, CommandExecutor {

    private NamespacedKey itemType;
    private NamespacedKey crateKey;
    private final Random random = new Random();
    private static final String CRATE_TITLE = "§5§lOP CRATE";

    @Override
    public void onEnable() {
        saveDefaultConfig();
        itemType = new NamespacedKey(this, "item_type");
        crateKey = new NamespacedKey(this, "op_crate_key");
        getServer().getPluginManager().registerEvents(this, this);
        Objects.requireNonNull(getCommand("opcrates")).setExecutor(this);
        getServer().getScheduler().runTaskTimer(this, this::applyArmorPowers, 20L, 20L);
        getLogger().info("OPCratesWorks enabled.");
    }

    private Component text(String s) {
        return LegacyComponentSerializer.legacySection().deserialize(s.replace("&", "§"));
    }

    private ItemStack tagged(Material material, String name, List<String> lore, String tag, int amount) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(text(name));
        List<Component> lines = new ArrayList<>();
        for (String l : lore) lines.add(text(l));
        meta.lore(lines);
        meta.getPersistentDataContainer().set(itemType, PersistentDataType.STRING, tag);
        item.setItemMeta(meta);
        return item;
    }

    public ItemStack key(int amount) {
        return tagged(Material.TRIPWIRE_HOOK,
                getConfig().getString("crate.key-name", "&d&lOP CRATE KEY"),
                getConfig().getStringList("crate.key-lore"),
                "op_key", amount);
    }

    public ItemStack saber() {
        ItemStack i = tagged(Material.NETHERITE_SWORD,
                getConfig().getString("items.saber-name", "&c&lVOID SABER"),
                getConfig().getStringList("items.saber-lore"),
                "void_saber", 1);
        ItemMeta m = i.getItemMeta();
        m.setCustomModelData(9001);
        m.addEnchant(Enchantment.SHARPNESS, 5, true);
        m.addEnchant(Enchantment.UNBREAKING, 5, true);
        m.addEnchant(Enchantment.FIRE_ASPECT, 2, true);
        m.setUnbreakable(true);
        i.setItemMeta(m);
        return i;
    }

    public ItemStack armor(Material mat, String name, String slot) {
        ItemStack i = tagged(mat, name,
                List.of("&7Custom OP armor.", "&8Set: &5Void Set"),
                "void_" + slot, 1);
        ItemMeta m = i.getItemMeta();
        int model = switch (slot) {
            case "helmet" -> 9002;
            case "chest" -> 9003;
            case "legs" -> 9004;
            case "boots" -> 9005;
            default -> 9000;
        };
        m.setCustomModelData(model);
        if (m instanceof org.bukkit.inventory.meta.LeatherArmorMeta leather) {
            leather.setColor(org.bukkit.Color.fromRGB(255, 255, 255));
        }
        m.addEnchant(Enchantment.PROTECTION, 5, true);
        m.addEnchant(Enchantment.UNBREAKING, 5, true);
        m.setUnbreakable(true);
        i.setItemMeta(m);
        return i;
    }

    private boolean hasTag(ItemStack i, String tag) {
        if (i == null || !i.hasItemMeta()) return false;
        String s = i.getItemMeta().getPersistentDataContainer().get(itemType, PersistentDataType.STRING);
        return tag.equals(s);
    }

    @EventHandler
    public void onKeyUse(PlayerInteractEvent e) {
        if (e.getAction() != Action.RIGHT_CLICK_BLOCK || e.getClickedBlock() == null) return;
        ItemStack hand = e.getItem();
        if (!hasTag(hand, "op_key")) return;
        e.setCancelled(true);

        Player p = e.getPlayer();
        hand.setAmount(hand.getAmount() - 1);
        openCrate(p);
    }

    private void openCrate(Player p) {
        Inventory inv = Bukkit.createInventory(null, 27, text(CRATE_TITLE));
        for (int slot : new int[]{0,1,2,3,5,6,7,8,9,17,18,19,20,21,22,23,24,25,26})
            inv.setItem(slot, filler());

        inv.setItem(10, saber());
        inv.setItem(12, armor(Material.LEATHER_HELMET, getConfig().getString("items.helmet-name"), "helmet"));
        inv.setItem(13, armor(Material.LEATHER_CHESTPLATE, getConfig().getString("items.chest-name"), "chest"));
        inv.setItem(14, armor(Material.LEATHER_LEGGINGS, getConfig().getString("items.legs-name"), "legs"));
        inv.setItem(16, armor(Material.LEATHER_BOOTS, getConfig().getString("items.boots-name"), "boots"));
        inv.setItem(11, tagged(Material.NETHER_STAR, "&e&lOPEN CRATE", List.of("&7Click any reward slot", "&7to roll for a random reward."), "roll_button", 1));
        inv.setItem(15, tagged(Material.ENCHANTED_GOLDEN_APPLE, "&6&lGOD APPLE",
                List.of("&7Rare OP crate reward."), "god_apple_reward", 2));
        p.openInventory(inv);
    }

    private ItemStack filler() {
        ItemStack i = new ItemStack(Material.PURPLE_STAINED_GLASS_PANE);
        ItemMeta m = i.getItemMeta();
        m.displayName(Component.empty());
        i.setItemMeta(m);
        return i;
    }

    @EventHandler
    public void onGuiClick(InventoryClickEvent e) {
        if (!e.getView().title().equals(text(CRATE_TITLE))) return;
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player p)) return;
        ItemStack clicked = e.getCurrentItem();
        if (clicked == null || clicked.getType() == Material.AIR) return;

        String tag = clicked.hasItemMeta()
                ? clicked.getItemMeta().getPersistentDataContainer().get(itemType, PersistentDataType.STRING)
                : null;
        if (tag == null) return;

        // Clicking the center button area gives a random OP reward.
        // The item buttons are only previews.
        List<ItemStack> rewards = List.of(
                saber(),
                armor(Material.LEATHER_HELMET, getConfig().getString("items.helmet-name"), "helmet"),
                armor(Material.LEATHER_CHESTPLATE, getConfig().getString("items.chest-name"), "chest"),
                armor(Material.LEATHER_LEGGINGS, getConfig().getString("items.legs-name"), "legs"),
                armor(Material.LEATHER_BOOTS, getConfig().getString("items.boots-name"), "boots"),
                new ItemStack(Material.ENCHANTED_GOLDEN_APPLE, 2)
        );

        // Reward only when a reward slot is clicked.
        if (!tag.startsWith("void_") && !tag.equals("god_apple_reward") && !tag.equals("roll_button")) return;
        ItemStack reward = rewards.get(random.nextInt(rewards.size()));

        p.closeInventory();
        HashMap<Integer, ItemStack> left = p.getInventory().addItem(reward);
        left.values().forEach(x -> p.getWorld().dropItemNaturally(p.getLocation(), x));
        p.sendMessage(text("&d&lOP CRATE &8» &fYou won &d" + reward.getType().name() + "&f!"));
        p.getWorld().playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1.2f);
    }

    @EventHandler
    public void onSaberHit(org.bukkit.event.entity.EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (!hasTag(p.getInventory().getItemInMainHand(), "void_saber")) return;
        if (random.nextInt(5) == 0) {
            if (e.getEntity() instanceof org.bukkit.entity.LivingEntity target) {
                target.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 60, 0));
                target.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 40, 0));
                p.getWorld().spawnParticle(Particle.PORTAL, target.getLocation().add(0,1,0), 30, .4,.6,.4,.1);
                p.getWorld().playSound(target.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, .7f, 1.5f);
            }
        }
    }

    @EventHandler
    public void onArmorTick(org.bukkit.event.player.PlayerItemHeldEvent e) {
        // Passive armor is applied through the inventory-click/equipment-safe repeating task below.
    }

    private void applyArmorPowers() {
        for (Player p : Bukkit.getOnlinePlayers()) {
            PlayerInventory inv = p.getInventory();
            boolean full = hasTag(inv.getHelmet(),"void_helmet")
                    && hasTag(inv.getChestplate(),"void_chest")
                    && hasTag(inv.getLeggings(),"void_legs")
                    && hasTag(inv.getBoots(),"void_boots");
            if (full) {
                p.addPotionEffect(new PotionEffect(PotionEffectType.RESISTANCE, 60, 0, true, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, 1, true, false, true));
                p.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 60, 0, true, false, true));
            }
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] a) {
        if (!sender.hasPermission("opcrates.admin")) {
            sender.sendMessage(text("&cNo permission."));
            return true;
        }
        if (a.length == 0) {
            sender.sendMessage(text("&d&lOPCratesWorks &7- /opcrates help"));
            return true;
        }

        switch (a[0].toLowerCase()) {
            case "help" -> {
                sender.sendMessage(text("&d&lOP CRATES &7commands:"));
                sender.sendMessage(text("&f/opcrates givekey <player> [amount]"));
                sender.sendMessage(text("&f/opcrates crate <player>"));
                sender.sendMessage(text("&f/opcrates saber <player>"));
                sender.sendMessage(text("&f/opcrates armor <player>"));
                sender.sendMessage(text("&f/opcrates reload"));
            }
            case "givekey" -> {
                if (a.length < 2) return usage(sender, "/opcrates givekey <player> [amount]");
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target == null) { sender.sendMessage(text("&cPlayer not found.")); return true; }
                int amount = a.length >= 3 ? Math.max(1, Integer.parseInt(a[2])) : 1;
                target.getInventory().addItem(key(amount));
                sender.sendMessage(text("&aGave &d" + amount + " &aOP key(s) to &f" + target.getName()));
            }
            case "crate" -> {
                if (a.length < 2) return usage(sender, "/opcrates crate <player>");
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target != null) openCrate(target);
            }
            case "saber" -> {
                if (a.length < 2) return usage(sender, "/opcrates saber <player>");
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target != null) target.getInventory().addItem(saber());
            }
            case "armor" -> {
                if (a.length < 2) return usage(sender, "/opcrates armor <player>");
                Player target = Bukkit.getPlayerExact(a[1]);
                if (target != null) {
                    target.getInventory().addItem(armor(Material.LEATHER_HELMET, getConfig().getString("items.helmet-name"), "helmet"));
                    target.getInventory().addItem(armor(Material.LEATHER_CHESTPLATE, getConfig().getString("items.chest-name"), "chest"));
                    target.getInventory().addItem(armor(Material.LEATHER_LEGGINGS, getConfig().getString("items.legs-name"), "legs"));
                    target.getInventory().addItem(armor(Material.LEATHER_BOOTS, getConfig().getString("items.boots-name"), "boots"));
                }
            }
            case "reload" -> {
                reloadConfig();
                sender.sendMessage(text("&aOPCratesWorks config reloaded."));
            }
            default -> sender.sendMessage(text("&cUnknown command. &f/opcrates help"));
        }
        return true;
    }

    private boolean usage(CommandSender s, String u) {
        s.sendMessage(text("&cUsage: " + u));
        return true;
    }

    @Override
    public void onDisable() {
        getLogger().info("OPCratesWorks disabled.");
    }
}
