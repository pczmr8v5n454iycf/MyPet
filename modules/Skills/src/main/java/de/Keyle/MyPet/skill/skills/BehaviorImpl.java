/*
 * This file is part of MyPet
 *
 * Copyright © 2011-2018 Keyle
 * MyPet is licensed under the GNU Lesser General Public License.
 *
 * MyPet is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MyPet is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package de.Keyle.MyPet.skill.skills;

import com.google.common.collect.Iterables;
import de.Keyle.MyPet.MyPetApi;
import de.Keyle.MyPet.api.Util;
import de.Keyle.MyPet.api.entity.MyPet;
import de.Keyle.MyPet.api.player.Permissions;
import de.Keyle.MyPet.api.skill.skills.Behavior;
import de.Keyle.MyPet.api.util.locale.Translation;
import org.bukkit.ChatColor;
import org.json.simple.JSONObject;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import static de.Keyle.MyPet.api.skill.skills.Behavior.BehaviorMode.*;

public class BehaviorImpl implements Behavior {
    protected MyPet myPet;
    protected Set<BehaviorMode> activeBehaviors = new HashSet<>();
    protected BehaviorMode selectedBehavior = BehaviorMode.Normal;
    protected static Random random = new Random();
    Iterator<BehaviorMode> behaviorCycler;

    public BehaviorImpl(MyPet myPet) {
        this.myPet = myPet;
        behaviorCycler = Iterables.cycle(activeBehaviors).iterator();
    }

    public MyPet getMyPet() {
        return myPet;
    }

    public boolean isActive() {
        return activeBehaviors.size() > 0;
    }

    @Override
    public void reset() {
        activeBehaviors.clear();
    }

    public void setBehavior(BehaviorMode mode) {
        selectedBehavior = mode;
    }

    public void enableBehavior(BehaviorMode mode) {
        activeBehaviors.add(mode);
    }

    public void disableBehavior(BehaviorMode mode) {
        activeBehaviors.remove(mode);
    }

    public BehaviorMode getBehavior() {
        return selectedBehavior;
    }

    public boolean isModeUsable(BehaviorMode mode) {
        return activeBehaviors.contains(mode);
    }

    public String toPrettyString() {
        String activeModes = ChatColor.GOLD + Translation.getString("Name.Normal", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        if (activeBehaviors.contains(Friendly)) {
            activeModes += ", " + ChatColor.GOLD + Translation.getString("Name.Friendly", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        }
        if (activeBehaviors.contains(Aggressive)) {
            if (!activeModes.equalsIgnoreCase("")) {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + Translation.getString("Name.Aggressive", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        }
        if (activeBehaviors.contains(Farm)) {
            if (!activeModes.equalsIgnoreCase("")) {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + Translation.getString("Name.Farm", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        }
        if (activeBehaviors.contains(Raid)) {
            if (!activeModes.equalsIgnoreCase("")) {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + Translation.getString("Name.Raid", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        }
        if (activeBehaviors.contains(Duel)) {
            if (!activeModes.equalsIgnoreCase("")) {
                activeModes += ", ";
            }
            activeModes += ChatColor.GOLD + Translation.getString("Name.Duel", myPet.getOwner().getLanguage()) + ChatColor.RESET;
        }
        return Translation.getString("Name.Modes", myPet.getOwner().getLanguage()) + ": " + activeModes;
    }

    public boolean activate() {
        if (isActive()) {
            while (true) {
                selectedBehavior = behaviorCycler.next();
                if (selectedBehavior != Normal) {
                    if (Permissions.has(myPet.getOwner().getPlayer(), "MyPet.extended.behavior." + selectedBehavior.name().toLowerCase())) {
                        break;
                    }
                } else {
                    break;
                }
            }
            myPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.Skill.Behavior.NewMode", myPet.getOwner().getLanguage()), myPet.getPetName(), Translation.getString("Name." + selectedBehavior.name(), myPet.getOwner().getPlayer())));
            return true;
        } else {
            myPet.getOwner().sendMessage(Util.formatText(Translation.getString("Message.No.Skill", myPet.getOwner().getLanguage()), myPet.getPetName(), this.getName(myPet.getOwner().getLanguage())));
            return false;
        }
    }

    public JSONObject save() {
        return new JSONObject();
    }

    @Override
    public void load(JSONObject o) {
    }

    public void schedule() {
        if (selectedBehavior == Aggressive && random.nextBoolean() && myPet.getStatus() == MyPet.PetState.Here) {
            MyPetApi.getPlatformHelper().playParticleEffect(myPet.getLocation().get().add(0, myPet.getEntity().get().getEyeHeight(), 0), "VILLAGER_ANGRY", 0.2F, 0.2F, 0.2F, 0.5F, 1, 20);
        }
    }

    @Override
    public String toString() {
        return "BehaviorImpl{" +
                "activeBehaviors=" + activeBehaviors +
                ", selectedBehavior=" + selectedBehavior +
                '}';
    }
}