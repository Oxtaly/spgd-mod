/*
 * Pixel Dungeon
 * Copyright (C) 2012-2015 Oleg Dolya
 *
 * Shattered Pixel Dungeon
 * Copyright (C) 2014-2024 Evan Debenham
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.shatteredpixel.shatteredpixeldungeon.items.rings;

import com.shatteredpixel.shatteredpixeldungeon.Challenges;
import com.shatteredpixel.shatteredpixeldungeon.Chrome;
import com.shatteredpixel.shatteredpixeldungeon.Dungeon;
import com.shatteredpixel.shatteredpixeldungeon.actors.Char;
import com.shatteredpixel.shatteredpixeldungeon.actors.hero.Hero;
import com.shatteredpixel.shatteredpixeldungeon.effects.Flare;
import com.shatteredpixel.shatteredpixeldungeon.items.Generator;
import com.shatteredpixel.shatteredpixeldungeon.items.Gold;
import com.shatteredpixel.shatteredpixeldungeon.items.Honeypot;
import com.shatteredpixel.shatteredpixeldungeon.items.Item;
import com.shatteredpixel.shatteredpixeldungeon.items.armor.Armor;
import com.shatteredpixel.shatteredpixeldungeon.items.bombs.Bomb;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.PotionOfExperience;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.brews.UnstableBrew;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.ExoticPotion;
import com.shatteredpixel.shatteredpixeldungeon.items.potions.exotic.PotionOfDivineInspiration;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.ScrollOfTransmutation;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ExoticScroll;
import com.shatteredpixel.shatteredpixeldungeon.items.scrolls.exotic.ScrollOfMetamorphosis;
import com.shatteredpixel.shatteredpixeldungeon.items.spells.UnstableSpell;
import com.shatteredpixel.shatteredpixeldungeon.items.stones.StoneOfEnchantment;
import com.shatteredpixel.shatteredpixeldungeon.items.trinkets.ExoticCrystals;
import com.shatteredpixel.shatteredpixeldungeon.items.weapon.Weapon;
import com.shatteredpixel.shatteredpixeldungeon.messages.Messages;
import com.shatteredpixel.shatteredpixeldungeon.scenes.GameScene;
import com.shatteredpixel.shatteredpixeldungeon.sprites.ItemSpriteSheet;
import com.shatteredpixel.shatteredpixeldungeon.utils.GLog;
import com.shatteredpixel.shatteredpixeldungeon.windows.WndBtns_MOD;
import com.watabou.noosa.Visual;
import com.watabou.utils.Bundle;
import com.watabou.utils.DeviceCompat;
import com.watabou.utils.Random;
import com.watabou.utils.Reflection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.function.BiConsumer;

public class RingOfWealth extends Ring {

	{
		icon = ItemSpriteSheet.Icons.RING_WEALTH;
	}

	private float triesToDrop = Float.MIN_VALUE;
	private int dropsToRare = Integer.MIN_VALUE;

	// MOD:
	public static final String DT_DROP_ON_ALL = "DROP_ON_ALL";
	public static final String DT_SHOW_PICKUPS = "SHOW_PICKUPS";
	public static final String AC_SHOW_DEBUG_TOGGLES = "SHOW_DEBUG_TOGGLES";

	public static final String DT_DROP_EQUIPS = "DROP_EQUIPS";
	public static final String DT_DROP_CONSUMABLES = "DROP_CONSUMABLES";

	public static final String DT_DROP_ARMORS = "DROP_ARMORS";
	public static final String DT_DROP_WEAPONS = "DROP_WEAPONS";
	public static final String DT_DROP_WANDS = "DROP_WANDS";
	public static final String DT_DROP_RINGS = "DROP_RINGS";
	public static final String DT_DROP_ARTIFACTS = "DROP_ARTIFACTS";

	public static final String DT_TEST = "TEST";

	public static final LinkedHashMap<String, Boolean> DEBUG_TOGGLES = createToggles();

	private static LinkedHashMap<String, Boolean> createToggles() {
		LinkedHashMap<String, Boolean> map = new LinkedHashMap<>();
		resetDebugToggles(map);
		return map;
	}

	public static void resetDebugToggles() {
		resetDebugToggles(DEBUG_TOGGLES);
	}

	public static void resetDebugToggles(LinkedHashMap<String, Boolean> map) {
		map.put(DT_DROP_ON_ALL, false);

		map.put(DT_DROP_CONSUMABLES, true);
		map.put(DT_DROP_EQUIPS, true);

		map.put(DT_DROP_ARMORS, true);
		map.put(DT_DROP_WEAPONS, true);
		map.put(DT_DROP_WANDS, true);
		map.put(DT_DROP_RINGS, true);
		map.put(DT_DROP_ARTIFACTS, true);

		map.put(DT_SHOW_PICKUPS, false);
	}

	@Override
	public ArrayList<String> actions( Hero hero ) {
		ArrayList<String> actions = super.actions( hero );
		actions.add(AC_SHOW_DEBUG_TOGGLES);
		return actions;
	}

	@Override
	public void execute( Hero hero, String action ) {
		super.execute( hero, action );

        switch (action) {
			case AC_SHOW_DEBUG_TOGGLES:
				LinkedHashMap<String, BiConsumer<Hero, String>> actions = new LinkedHashMap<>();
				for (String toggle : DEBUG_TOGGLES.keySet()) {
					actions.put(toggle, this::execute);
				}
				// actions.put(DT_TEST, this::execute);
				WndBtns_MOD window = new WndBtns_MOD(
						null,
						"Debug Titles",
						"All the debug toggles",
						actions,
						this::actionName,
						(String actioned, Hero heroed) -> {
							if (actioned.equals(DT_DROP_ARTIFACTS)) {
								boolean hasArtifactsLeft = false;
								for ( float prob : Generator.Category.ARTIFACT.probs ) {
									if(Float.compare(prob, 0f) != 0) {
										hasArtifactsLeft = true;
										break;
									}
								}
								if(!hasArtifactsLeft) {
									return Chrome.Type.GREY_BUTTON;
								}
							}
							return null;
						});
				GameScene.show(window);
				return;
            case DT_DROP_ON_ALL:
			case DT_SHOW_PICKUPS:
			case DT_DROP_CONSUMABLES:
			case DT_DROP_EQUIPS:
			case DT_DROP_ARMORS:
			case DT_DROP_WEAPONS:
			case DT_DROP_RINGS:
			case DT_DROP_WANDS:
			case DT_DROP_ARTIFACTS:
				Boolean state = DEBUG_TOGGLES.get(action);
				if(state == null) {
					return;
				}
				if(state.equals(true)) {
					GLog.n("Toggled off %s", action);
					DEBUG_TOGGLES.put(action, false);

					if(action.equals(DT_DROP_CONSUMABLES) && DEBUG_TOGGLES.get(DT_DROP_EQUIPS)) {
						dropsToRare = 0;
					}
					if(action.equals(DT_DROP_EQUIPS) && DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES)) {
						dropsToRare = 10;
					}
					// Toggle off equips if none of the equips are turned on
					if(DEBUG_TOGGLES.get(DT_DROP_EQUIPS) &&
					!DEBUG_TOGGLES.get(DT_DROP_WEAPONS) &&
					!DEBUG_TOGGLES.get(DT_DROP_ARTIFACTS) &&
					!DEBUG_TOGGLES.get(DT_DROP_ARMORS) &&
					!DEBUG_TOGGLES.get(DT_DROP_WANDS) &&
					!DEBUG_TOGGLES.get(DT_DROP_RINGS)) {
						DEBUG_TOGGLES.put(DT_DROP_EQUIPS, false);
					}
				}
				if(state.equals(false)) {
					if(action.equals(DT_DROP_EQUIPS) &&
					!DEBUG_TOGGLES.get(DT_DROP_WEAPONS) &&
					!DEBUG_TOGGLES.get(DT_DROP_ARTIFACTS) &&
					!DEBUG_TOGGLES.get(DT_DROP_ARMORS) &&
					!DEBUG_TOGGLES.get(DT_DROP_WANDS) &&
					!DEBUG_TOGGLES.get(DT_DROP_RINGS)) {
						GLog.w("You can't enable %s without a single equipment enabled!", action);
						return;
					}
					GLog.p("Toggled on %s", action);
					DEBUG_TOGGLES.put(action, true);
					if(action.equals(DT_DROP_ON_ALL)) {
						triesToDrop = 1;
					}
					if(!DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES)
					&& (action.equals(DT_DROP_EQUIPS)
					|| action.equals(DT_DROP_WEAPONS)
					|| action.equals(DT_DROP_ARTIFACTS)
					|| action.equals(DT_DROP_ARMORS)
					|| action.equals(DT_DROP_WANDS)
					|| action.equals(DT_DROP_RINGS))) {
						dropsToRare = 0;
					}
				}
				return;
			case DT_TEST:
				GLog.p("Showcase TEST_LINE");
				GLog.n("Showcase TEST_LINE");
				return;
        }
    }

	@Override
	public String actionName(String action, Hero hero){
		String str = null;
		switch (action) {
			case DT_DROP_ON_ALL:
				if(str == null) str = "DROP ON ALL";
			case DT_SHOW_PICKUPS:
				if(str == null) str = "SHOW PICKUPS";
			case DT_DROP_CONSUMABLES:
				if(str == null) str = "DROP CONSUMABLES";
			case DT_DROP_EQUIPS:
				if(str == null) str = "DROP EQUIPS";
			case DT_DROP_ARMORS:
				if(str == null) str = "DROP ARMORS";
			case DT_DROP_ARTIFACTS:
				if(str == null) {
					str = "DROP ARTIFACTS";
					// boolean hasArtifactsLeft = false;
					// for ( float prob : Generator.Category.ARTIFACT.probs ) {
					// 	if(Float.compare(prob, 0f) != 0) {
					// 		hasArtifactsLeft = true;
					// 		break;
					// 	}
					// }
					// if(!hasArtifactsLeft) {
					// 	str = "§" + str + "§";
 					// }
				}
			case DT_DROP_RINGS:
				if(str == null) str = "DROP RINGS";
			case DT_DROP_WANDS:
				if(str == null) str = "DROP WANDS";
			case DT_TEST:
				if(str == null) str = "!TEST!";
			case DT_DROP_WEAPONS:
				if(str == null) str = "DROP WEAPONS";
			case AC_SHOW_DEBUG_TOGGLES:
				if(str == null) str = "SHOW DEBUG TOGGLES";

				Boolean toggled = DEBUG_TOGGLES.get(action);
				if(toggled == null) toggled = false;
				// String toggleStr = toggled ? "on" : "off";
				return toggled ? "_" + str + "_" : str;
				// return String.format("%s (%s)", str, toggleStr);
			default:
				return Messages.get(this, "ac_" + action);
		}
	}
	// END MOD:

	public String statsInfo() {
		if (isIdentified()){
			String info = Messages.get(this, "stats",
					Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, soloBuffedBonus()) - 1f)));
			if (isEquipped(Dungeon.hero) && soloBuffedBonus() != combinedBuffedBonus(Dungeon.hero)){
				info += "\n\n" + Messages.get(this, "combined_stats",
						Messages.decimalFormat("#.##", 100f * (Math.pow(1.20f, combinedBuffedBonus(Dungeon.hero)) - 1f)));
			}
			return info;
		} else {
			return Messages.get(this, "typical_stats", Messages.decimalFormat("#.##", 20f));
		}
	}

	public String upgradeStat1(int level){
		if (cursed && cursedKnown) level = Math.min(-1, level-3);
		return Messages.decimalFormat("#.##", 100f * (Math.pow(1.2f, level+1)-1f)) + "%";
	}

	private static final String TRIES_TO_DROP = "tries_to_drop";
	private static final String DROPS_TO_RARE = "drops_to_rare";

	@Override
	public void storeInBundle(Bundle bundle) {
		super.storeInBundle(bundle);
		bundle.put(TRIES_TO_DROP, triesToDrop);
		bundle.put(DROPS_TO_RARE, dropsToRare);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
		triesToDrop = bundle.getFloat(TRIES_TO_DROP);
		dropsToRare = bundle.getInt(DROPS_TO_RARE);
	}

	@Override
	protected RingBuff buff( ) {
		return new Wealth();
	}
	
	public static float dropChanceMultiplier( Char target ){
		return (float)Math.pow(1.20, getBuffedBonus(target, Wealth.class));
	}
	
	public static ArrayList<Item> tryForBonusDrop(Char target, int tries ){
		int bonus = getBuffedBonus(target, Wealth.class);

		if (bonus <= 0) return null;
		
		HashSet<Wealth> buffs = target.buffs(Wealth.class);
		float triesToDrop = Float.MIN_VALUE;
		int dropsToEquip = Integer.MIN_VALUE;

		//find the largest count (if they aren't synced yet)
		for (Wealth w : buffs){
			if (w.triesToDrop() > triesToDrop){
				triesToDrop = w.triesToDrop();
				dropsToEquip = w.dropsToRare();
			}
		}

		// MOD:
		// Disables drops early on if neither equips or consumables are enabled
		if(!DEBUG_TOGGLES.get(DT_DROP_EQUIPS) && !DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES)) {
			// GLog.d("No drops!");
			// GLog.newLine();
			// GLog.d("DT_DROP_CONSUMABLES %s", DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES));
			// GLog.newLine();
			// GLog.d("DT_DROP_EQUIPS %s", DEBUG_TOGGLES.get(DT_DROP_EQUIPS));
			return null;
		}

		//reset (if needed), decrement, and store counts
		if (triesToDrop == Float.MIN_VALUE) {
			triesToDrop = DEBUG_TOGGLES.get(DT_DROP_ON_ALL) ? 1 : Random.NormalIntRange(0, 20); // MOD:
			if(!DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES)) { // MOD:
				dropsToEquip = 0;
			}
			else if(!DEBUG_TOGGLES.get(DT_DROP_EQUIPS)) {
				dropsToEquip = 10;
			}
			else {
				dropsToEquip = Random.NormalIntRange(5, 10);
			} // ENDMOD
		}

		//now handle reward logic
		ArrayList<Item> drops = new ArrayList<>();

		triesToDrop -= tries;
		while ( triesToDrop <= 0 ){
			if (dropsToEquip <= 0){
				int equipBonus = 0;

				//A second ring of wealth can be at most +1 when calculating wealth bonus for equips
				//This is to prevent using an upgraded wealth to farm another upgraded wealth and
				//using the two to get substantially more upgrade value than intended
				for (Wealth w : target.buffs(Wealth.class)){
					if (w.buffedLvl() > equipBonus){
						equipBonus = w.buffedLvl() + Math.min(equipBonus, 2);
					} else {
						equipBonus += Math.min(w.buffedLvl(), 2);
					}
				}

				Item i;
				do {
					i = genEquipmentDrop(equipBonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				if(DeviceCompat.isDebug()) { // MOD:
					GLog.d(String.format("Ring of Wealth: Item %s (%s) level %s", i.name(), i.trueName(), i.level()));
				}
				dropsToEquip = !DEBUG_TOGGLES.get(DT_DROP_CONSUMABLES) ? 0 : Random.NormalIntRange(5, 10); // MOD:
			} else {
				Item i;
				do {
					i = genConsumableDrop(bonus - 1);
				} while (Challenges.isItemBlocked(i));
				drops.add(i);
				// MOD:
				// Only decrement dropsToEquip if equips are enabled
				if(DEBUG_TOGGLES.get(DT_DROP_EQUIPS)) dropsToEquip--;
			}
			triesToDrop += DEBUG_TOGGLES.get(DT_DROP_ON_ALL) ? 1 : Random.NormalIntRange(0, 20); // MOD:
		}

		//store values back into rings
		for (Wealth w : buffs){
			w.triesToDrop(triesToDrop);
			w.dropsToRare(dropsToEquip);
		}
		
		return drops;
	}

	//used for visuals
	// 1/2/3 used for low/mid/high tier consumables
	// 3 used for +0-1 equips, 4 used for +2 or higher equips
	private static int latestDropTier = 0;

	public static void showFlareForBonusDrop( Visual vis ){
		if (vis == null || vis.parent == null) return;
		switch (latestDropTier){
			default:
				break; //do nothing
			case 1:
				new Flare(6, 20).color(0x00FF00, true).show(vis, 3f);
				break;
			case 2:
				new Flare(6, 24).color(0x00AAFF, true).show(vis, 3.33f);
				break;
			case 3:
				new Flare(6, 28).color(0xAA00FF, true).show(vis, 3.67f);
				break;
			case 4:
				new Flare(6, 32).color(0xFFAA00, true).show(vis, 4f);
				break;
		}
		latestDropTier = 0;
	}
	
	public static Item genConsumableDrop(int level) {
		float roll = Random.Float();
		//60% chance - 4% per level. Starting from +15: 0%
		if (roll < (0.6f - 0.04f * level)) {
			latestDropTier = 1;
			return genLowValueConsumable();
		//30% chance + 2% per level. Starting from +15: 60%-2%*(lvl-15)
		} else if (roll < (0.9f - 0.02f * level)) {
			latestDropTier = 2;
			return genMidValueConsumable();
		//10% chance + 2% per level. Starting from +15: 40%+2%*(lvl-15)
		} else {
			latestDropTier = 3;
			return genHighValueConsumable();
		}
	}

	private static Item genLowValueConsumable(){
		switch (Random.Int(4)){
			case 0: default:
				Item i = new Gold().random();
				return i.quantity(i.quantity()/2);
			case 1:
				return Generator.randomUsingDefaults(Generator.Category.STONE);
			case 2:
				return Generator.randomUsingDefaults(Generator.Category.POTION);
			case 3:
				return Generator.randomUsingDefaults(Generator.Category.SCROLL);
		}
	}

	private static Item genMidValueConsumable(){
		switch (Random.Int(6)){
			case 0: default:
				Item i = genLowValueConsumable();
				return i.quantity(i.quantity()*2);
			case 1:
				i = Generator.randomUsingDefaults(Generator.Category.POTION);
				if (!(i instanceof ExoticPotion)) {
					return Reflection.newInstance(ExoticPotion.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 2:
				i = Generator.randomUsingDefaults(Generator.Category.SCROLL);
				if (!(i instanceof ExoticScroll)){
					return Reflection.newInstance(ExoticScroll.regToExo.get(i.getClass()));
				} else {
					return Reflection.newInstance(i.getClass());
				}
			case 3:
				return Random.Int(2) == 0 ? new UnstableBrew() : new UnstableSpell();
			case 4:
				return new Bomb();
			case 5:
				return new Honeypot();
		}
	}

	private static Item genHighValueConsumable(){
		switch (Random.Int(4)){
			case 0: default:
				Item i = genMidValueConsumable();
				if (i instanceof Bomb){
					return new Bomb.DoubleBomb();
				} else {
					return i.quantity(i.quantity()*2);
				}
			case 1:
				return new StoneOfEnchantment();
			case 2:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new PotionOfDivineInspiration() : new PotionOfExperience();
			case 3:
				return Random.Float() < ExoticCrystals.consumableExoticChance() ? new ScrollOfMetamorphosis() : new ScrollOfTransmutation();
		}
	}

	private static enum EquipmentDropTypes { // MOD:
 		WEAPON,
		ARMOR,
		RING,
		ARTIFACT,
		WAND
	}

	// MOD: changes the way the items are generated to be able
	// to be toggled off easier
	private static Item genEquipmentDrop( int level ){
		//each upgrade increases depth used for calculating drops by 1
		ArrayList<EquipmentDropTypes> types = new ArrayList<>();

		if(DEBUG_TOGGLES.get(DT_DROP_WEAPONS)) types.add(EquipmentDropTypes.WEAPON);
		if(DEBUG_TOGGLES.get(DT_DROP_WEAPONS)) types.add(EquipmentDropTypes.WEAPON);

		if(DEBUG_TOGGLES.get(DT_DROP_ARMORS)) types.add(EquipmentDropTypes.ARMOR);

		if(DEBUG_TOGGLES.get(DT_DROP_RINGS)) types.add(EquipmentDropTypes.RING);

		if(DEBUG_TOGGLES.get(DT_DROP_ARTIFACTS)) types.add(EquipmentDropTypes.ARTIFACT);

		if(DEBUG_TOGGLES.get(DT_DROP_WANDS)) types.add(EquipmentDropTypes.WAND);

		if(types.isEmpty()) { // Should never happen, but just in case
			return genConsumableDrop(level);
		}
		return genEquipmentDrop(level, types);
	}

	private static Item genEquipmentDrop( int level, ArrayList<EquipmentDropTypes> types ) {
		Item result;

		int floorset = (Dungeon.depth + level)/5;

		EquipmentDropTypes type = types.get(Random.Int(0, types.size() - 1));

		switch (type) { // MOD:
			default: case WEAPON:
				Weapon w = Generator.randomWeapon(floorset, true);
				if (!w.hasGoodEnchant() && Random.Int(10) < level)      w.enchant();
				else if (w.hasCurseEnchant())                           w.enchant(null);
				result = w;
				break;
			case ARMOR:
				Armor a = Generator.randomArmor(floorset);
				if (!a.hasGoodGlyph() && Random.Int(10) < level)        a.inscribe();
				else if (a.hasCurseGlyph())                             a.inscribe(null);
				result = a;
				break;
			case RING:
				result = Generator.randomUsingDefaults(Generator.Category.RING);
				break;
			case ARTIFACT:
				result = Generator.random(Generator.Category.ARTIFACT);
				break;
			case WAND: // MOD: added wands to ring of wealth
				result = Generator.randomUsingDefaults(Generator.Category.WAND);
				break;
		}
		//minimum level is 1/2/3/4/5/6 when ring level is 1/3/5/7/9/11
		if (result.isUpgradable()){
			int minLevel = (level+1)/2;
			if (result.level() < minLevel){
				result.level(minLevel);
			}
		}
		result.cursed = false;
		result.cursedKnown = true;
		if (result.level() >= 2) {
			latestDropTier = 4;
		} else {
			latestDropTier = 3;
		}
		return result;
	}

	public class Wealth extends RingBuff {
		
		private void triesToDrop( float val ){
			triesToDrop = val;
		}
		
		private float triesToDrop(){
			return triesToDrop;
		}

		private void dropsToRare( int val ) {
			dropsToRare = val;
		}

		private int dropsToRare(){
			return dropsToRare;
		}
		
	}
}
