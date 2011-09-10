/*
 * Copyright (C) 2011 Keyle
 *
 * This file is part of MyWolf.
 *
 * MyWolf is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyWolf is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MyWolf. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyWolf;

import de.Keyle.MyWolf.Listeners.*;
import de.Keyle.MyWolf.MyWolf.BehaviorState;
import de.Keyle.MyWolf.MyWolf.WolfState;
import de.Keyle.MyWolf.Skill.MyWolfExperience;
import de.Keyle.MyWolf.Skill.MyWolfSkill;
import de.Keyle.MyWolf.Skill.Skills.*;
import de.Keyle.MyWolf.chatcommands.*;
import de.Keyle.MyWolf.util.MyWolfConfig;
import de.Keyle.MyWolf.util.MyWolfLanguage;
import de.Keyle.MyWolf.util.MyWolfPermissions;
import de.Keyle.MyWolf.util.MyWolfPermissions.PermissionsType;
import de.Keyle.MyWolf.util.MyWolfUtil;
import net.minecraft.server.ItemStack;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.Event;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.config.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;

public class MyWolfPlugin extends JavaPlugin
{
    public static MyWolfPlugin Plugin;

    public void onDisable()
    {
        SaveWolves(ConfigBuffer.WolvesConfig);
        for (String owner : ConfigBuffer.mWolves.keySet())
        {
            if (ConfigBuffer.mWolves.get(owner).Status == WolfState.Here)
            {
                ConfigBuffer.mWolves.get(owner).removeWolf();
            }
        }
        getServer().getScheduler().cancelTasks(this);
        ConfigBuffer.mWolves.clear();
        ConfigBuffer.WolfChestOpened.clear();

        MyWolfUtil.Log.info("[MyWolf] Disabled");
    }

    public void onEnable()
    {
        Plugin = this;

        MyWolfPlayerListener playerListener = new MyWolfPlayerListener();
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_PORTAL, playerListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_MOVE, playerListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.PLAYER_INTERACT_ENTITY, playerListener, Event.Priority.Normal, this);

        MyWolfVehicleListener vehicleListener = new MyWolfVehicleListener();
        getServer().getPluginManager().registerEvent(Event.Type.VEHICLE_ENTER, vehicleListener, Event.Priority.Low, this);

        MyWolfWorldListener worldListener = new MyWolfWorldListener();
        getServer().getPluginManager().registerEvent(Event.Type.CHUNK_UNLOAD, worldListener, Event.Priority.Normal, this);

        MyWolfEntityListener entityListener = new MyWolfEntityListener();
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_TARGET, entityListener, Event.Priority.Normal, this);
        getServer().getPluginManager().registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Normal, this);

        MyWolfLevelUpListener levelupListener = new MyWolfLevelUpListener();
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, levelupListener, Event.Priority.Normal, this);

        MyWolfInventoryListener inventoryListener = new MyWolfInventoryListener();
        getServer().getPluginManager().registerEvent(Event.Type.CUSTOM_EVENT, inventoryListener, Event.Priority.Normal, this);



        getCommand("wolfname").setExecutor(new MyWolfName());
        getCommand("wolfcall").setExecutor(new MyWolfCall());
        getCommand("wolfstop").setExecutor(new MyWolfStop());
        getCommand("wolfrelease").setExecutor(new MyWolfRelease());
        getCommand("wolf").setExecutor(new MyWolfHelp());
        getCommand("wolfinventory").setExecutor(new MyWolfInventory());
        getCommand("wolfpickup").setExecutor(new MyWolfPickup());
        getCommand("wolfbehavior").setExecutor(new MyWolfBehavior());
        getCommand("wolfcompass").setExecutor(new MyWolfCompass());
        getCommand("wolfinfo").setExecutor(new MyWolfInfo());
        getCommand("wolfskin").setExecutor(new MyWolfSkin());

        if (MyWolfConfig.LevelSystem)
        {
            getCommand("wolfexp").setExecutor(new MyWolfEXP());
        }

        new Inventory();
        new HP();
        new HPregeneration();
        new Pickup();
        new Behavior();
        new Damage();

        new MyWolfSkill("Control").registerSkill();

        MyWolfConfig.Config = this.getConfiguration();
        MyWolfConfig.setStandart();
        MyWolfConfig.loadVariables();

        if(MyWolfConfig.PermissionsBukkit == false)
        {
            MyWolfPermissions.setup();
        }
        else
        {
            MyWolfPermissions.setup(PermissionsType.BukkitPermissions);
        }

        ConfigBuffer.lv = new MyWolfLanguage(new Configuration(new File(this.getDataFolder().getPath() + File.separator + "lang.yml")));
        ConfigBuffer.lv.setStandart();
        ConfigBuffer.lv.loadVariables();

        File WolvesConfigFile = new File(this.getDataFolder().getPath() + File.separator + "Wolves.yml");
        ConfigBuffer.WolvesConfig = new Configuration(WolvesConfigFile);
        ConfigBuffer.WolvesConfig.load();
        List<String> WolfList = ConfigBuffer.WolvesConfig.getKeys("Wolves");
        if (WolfList != null)
        {
            for (String ownername : WolfList)
            {
                if (ConfigBuffer.WolvesConfig.getKeys("Wolves." + ownername + ".inventory") != null)
                {
                    try
                    {
                        copyFile(WolvesConfigFile, new File(this.getDataFolder().getPath() + File.separator + "Wolves.yml.backup"));
                        MyWolfUtil.Log.info("[MyWolf] Created backup. See this file if players want their items back");
                        break;
                    }
                    catch (Exception e)
                    {
                        MyWolfUtil.Log.info("[MyWolf] Can not create backup. Sorry for lost items :´(");
                    }
                }
            }
        }

        LoadWolves(ConfigBuffer.WolvesConfig);

        if (MyWolfConfig.LevelSystem)
        {
            try
            {
                MyWolfExperience.JSreader = MyWolfUtil.readFileAsString(MyWolfPlugin.Plugin.getDataFolder().getPath() + File.separator + "exp.js");
            }
            catch (Exception e)
            {
                MyWolfExperience.JSreader = null;
                MyWolfUtil.Log.info("[MyWolf] EXP-Script not found (exp.js). Working with factor.");
            }
        }

        for (Player p : this.getServer().getOnlinePlayers())
        {
            if (ConfigBuffer.mWolves.containsKey(p.getName()) && p.isOnline())
            {
                ConfigBuffer.mWolves.get(p.getName()).createWolf(ConfigBuffer.mWolves.get(p.getName()).isSitting());
            }
        }
        MyWolfUtil.Log.info("[" + ConfigBuffer.pdfFile.getName() + "] version " + ConfigBuffer.pdfFile.getVersion() + " ENABLED");
    }

    public void LoadWolves(Configuration Config)
    {
        int anzahlWolves = 0;

        Config.load();
        List<String> WolfList = Config.getKeys("Wolves");
        if (WolfList != null)
        {
            for (String ownername : WolfList)
            {
                double WolfX = Config.getDouble("Wolves." + ownername + ".loc.X", 0);
                double WolfY = Config.getDouble("Wolves." + ownername + ".loc.Y", 0);
                double WolfZ = Config.getDouble("Wolves." + ownername + ".loc.Z", 0);
                double WolfEXP = Config.getDouble("Wolves." + ownername + ".exp", 0);
                String WolfWorld = Config.getString("Wolves." + ownername + ".loc.world", getServer().getWorlds().get(0).getName());
                int WolfHealthNow = Config.getInt("Wolves." + ownername + ".health.now", 6);
                int WolfRespawnTime = Config.getInt("Wolves." + ownername + ".health.respawntime", 0);
                String WolfName = Config.getString("Wolves." + ownername + ".name", "Wolf");
                String WolfSkin = Config.getString("Wolves." + ownername + ".skin", "");
                boolean WolfSitting = Config.getBoolean("Wolves." + ownername + ".sitting", false);
                BehaviorState WolfBehavior = BehaviorState.valueOf(Config.getString("Wolves." + ownername + ".behavior", "Normal"));
                boolean WolfPickup = Config.getBoolean("Wolves." + ownername + ".pickup", false);

                if (getServer().getWorld(WolfWorld) == null)
                {
                    MyWolfUtil.Log.info("[MyWolf] World \"" + WolfWorld + "\" for " + ownername + "'s wolf \"" + WolfName + "\" not found - skiped wolf");
                    continue;
                }

                MyWolf Wolf = new MyWolf(ownername);

                ConfigBuffer.mWolves.put(ownername, Wolf);



                Wolf.setLocation(new Location(this.getServer().getWorld(WolfWorld), WolfX, WolfY, WolfZ));

                Wolf.setHealth(WolfHealthNow);
                Wolf.RespawnTime = WolfRespawnTime;
                if (WolfRespawnTime > 0)
                {
                    Wolf.Status = WolfState.Dead;
                }
                else
                {
                    Wolf.Status = WolfState.Despawned;
                }
                Wolf.SetName(WolfName);
                Wolf.setSitting(WolfSitting);
                Wolf.Experience.setExp(WolfEXP);
                Wolf.setTameSkin(WolfSkin);
                Wolf.Behavior = WolfBehavior;
                Wolf.isPickup = WolfPickup;

                String inv;
                if (Config.getKeys("Wolves." + ownername + ".inventory") != null)
                {
                    String inv1 = Config.getString("Wolves." + ownername + ".inventory.1", "");
                    String inv2 = Config.getString("Wolves." + ownername + ".inventory.2", "");
                    inv = inv1 + (!inv2.equals("") ? ";" + inv2 : "");
                    inv = inv.replaceAll(";,,;", ";");
                    inv = inv.replaceAll(";,,", "");
                }
                else
                {
                    inv = Config.getString("Wolves." + ownername + ".inventory", ",,");
                }
                String[] invSplit = inv.split(";");
                for (int i = 0 ; i < invSplit.length ; i++)
                {
                    if (i < Wolf.inv.getSize())
                    {
                        String[] itemvalues = invSplit[i].split(",");
                        if (itemvalues.length == 3 && MyWolfUtil.isInt(itemvalues[0]) && MyWolfUtil.isInt(itemvalues[1]) && MyWolfUtil.isInt(itemvalues[2]))
                        {
                            if (Material.getMaterial(Integer.parseInt(itemvalues[0])) != null)
                            {
                                if (Integer.parseInt(itemvalues[1]) <= 64)
                                {
                                    Wolf.inv.setItem(i, new ItemStack(Integer.parseInt(itemvalues[0]), Integer.parseInt(itemvalues[1]), Integer.parseInt(itemvalues[2])));
                                }
                            }
                        }
                    }
                }
                anzahlWolves++;
            }
        }
        MyWolfUtil.Log.info("[" + this.getDescription().getName() + "] " + anzahlWolves + " wolf/wolves loaded");
    }

    public void SaveWolves(Configuration Config)
    {
        Config.removeProperty("Wolves");
        for (String owner : ConfigBuffer.mWolves.keySet())
        {
            MyWolf wolf = ConfigBuffer.mWolves.get(owner);
            String Items = "";
            for (int i = 0 ; i < wolf.inv.getSize() ; i++)
            {
                ItemStack Item = wolf.inv.getItem(i);
                if (Item != null)
                {
                    Items += Item.id + "," + Item.count + "," + Item.damage + ";";
                }
                else
                {
                    Items += ",,;";
                }
            }
            Items = !Items.equals("") ? Items.substring(0, Items.length() - 1) : Items;
            Config.setProperty("Wolves." + owner + ".inventory", Items);
            Config.setProperty("Wolves." + owner + ".loc.X", wolf.getLocation().getX());
            Config.setProperty("Wolves." + owner + ".loc.Y", wolf.getLocation().getY());
            Config.setProperty("Wolves." + owner + ".loc.Z", wolf.getLocation().getZ());
            Config.setProperty("Wolves." + owner + ".loc.world", wolf.getLocation().getWorld().getName());

            Config.setProperty("Wolves." + owner + ".health.now", wolf.getHealth());
            Config.setProperty("Wolves." + owner + ".health.respawntime", wolf.RespawnTime);
            Config.setProperty("Wolves." + owner + ".name", wolf.Name);
            Config.setProperty("Wolves." + owner + ".sitting", wolf.isSitting());
            Config.setProperty("Wolves." + owner + ".exp", wolf.Experience.getExp());
            Config.setProperty("Wolves." + owner + ".skin", wolf.SkinURL);
            Config.setProperty("Wolves." + owner + ".behavior", wolf.Behavior.name());
            Config.setProperty("Wolves." + owner + ".pickup", wolf.isPickup);
        }
        Config.save();
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static boolean isMyWolf(Wolf wolf)
    {
        for (MyWolf w : ConfigBuffer.mWolves.values())
        {
            if (w.getID() == wolf.getEntityId())
            {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static MyWolf getMyWolf(Wolf wolf)
    {
        for (MyWolf w : ConfigBuffer.mWolves.values())
        {
            if (w.getID() == wolf.getEntityId())
            {
                return w;
            }
        }
        return null;
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public static MyWolf getMyWolf(Player player)
    {
        return ConfigBuffer.mWolves.containsKey(player.getName()) ? ConfigBuffer.mWolves.get(player.getName()) : null;
    }

    public static void copyFile(File in, File out) throws Exception
    {
        FileInputStream fis = new FileInputStream(in);
        FileOutputStream fos = new FileOutputStream(out);
        try
        {
            byte[] buf = new byte[1024];
            int i;
            while ((i = fis.read(buf)) != -1)
            {
                fos.write(buf, 0, i);
            }
        }
        finally
        {
            fis.close();
            fos.close();
        }
    }
}