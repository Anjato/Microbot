/*
 * Copyright (c) 2020, Zoinkwiz
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.templeofikov;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.NpcCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.ObjectCondition;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirements;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.FreeInventorySlotRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.SkillRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.player.WeightRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.LogicType;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Operation;
import net.runelite.client.plugins.microbot.questhelper.requirements.widget.WidgetTextRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class TempleOfIkov extends BasicQuestHelper
{
	//Items Required
	ItemRequirement pendantOfLucien, bootsOfLightness, limpwurt20, yewOrBetterBow, knife, lightSource, lever, iceArrows20, iceArrows, shinyKey,
		armadylPendant, staffOfArmadyl, pendantOfLucienEquipped, bootsOfLightnessEquipped, iceArrowsEquipped;

	Requirement emptyInventorySpot;

	Requirement  belowMinus1Weight, below4Weight, inEntryRoom, inNorthRoom, inBootsRoom, dontHaveBoots, inMainOrNorthRoom,
		leverNearby, pulledLever, inArrowRoom, hasEnoughArrows, lesNearby, inLesRoom, inWitchRoom, inDemonArea,
		inArmaRoom;

	QuestStep talkToLucien, prepare, prepareBelow0, enterDungeonForBoots, enterDungeon, goDownToBoots, getBoots, goUpFromBoots, pickUpLever,
		useLeverOnHole, pullLever, enterArrowRoom, returnToMainRoom, goSearchThievingLever, goPullThievingLever, fightLes, tryToEnterWitchRoom,
		enterDungeonKilledLes, enterLesDoor, giveWineldaLimps, talkToWinelda, enterDungeonGivenLimps, enterFromMcgrubbors, pickUpKey, pushWall,
		makeChoice, killLucien, bringStaffToLucien, returnToLucien;

	ObjectStep collectArrows;

	//Zones
	Zone entryRoom1, entryRoom2, northRoom1, northRoom2, bootsRoom, arrowRoom1, arrowRoom2, arrowRoom3, lesRoom, witchRoom, demonArea1, demonArea2, demonArea3,
		demonArea4, armaRoom1, armaRoom2;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		steps.put(0, talkToLucien);

		ConditionalStep getLeverPiece = new ConditionalStep(this, prepare);
		getLeverPiece.addStep(new Conditions(iceArrows, inMainOrNorthRoom), goSearchThievingLever);
		getLeverPiece.addStep(new Conditions(hasEnoughArrows, inArrowRoom), returnToMainRoom);
		getLeverPiece.addStep(inArrowRoom, collectArrows);
		getLeverPiece.addStep(new Conditions(inMainOrNorthRoom, pulledLever), enterArrowRoom);
		getLeverPiece.addStep(leverNearby, pullLever);
		getLeverPiece.addStep(new Conditions(inMainOrNorthRoom, lever), useLeverOnHole);
		getLeverPiece.addStep(new Conditions(inBootsRoom, lever), goUpFromBoots);
		getLeverPiece.addStep(lever, enterDungeon);

		getLeverPiece.addStep(new Conditions(inMainOrNorthRoom, belowMinus1Weight), pickUpLever);
		getLeverPiece.addStep(new Conditions(inBootsRoom, new Conditions(LogicType.OR, belowMinus1Weight, bootsOfLightness)), goUpFromBoots);
		getLeverPiece.addStep(inBootsRoom, getBoots);
		getLeverPiece.addStep(new Conditions(inMainOrNorthRoom, below4Weight, dontHaveBoots), goDownToBoots);
		getLeverPiece.addStep(belowMinus1Weight, enterDungeon);
		getLeverPiece.addStep(new Conditions(below4Weight, bootsOfLightness), prepareBelow0);
		getLeverPiece.addStep(below4Weight, enterDungeonForBoots);

		steps.put(10, getLeverPiece);

		ConditionalStep pullLeverForLes = new ConditionalStep(this, prepare);
		pullLeverForLes.addStep(new Conditions(iceArrows, inMainOrNorthRoom), goPullThievingLever);
		pullLeverForLes.addStep(new Conditions(hasEnoughArrows, inArrowRoom), returnToMainRoom);
		pullLeverForLes.addStep(inArrowRoom, collectArrows);
		pullLeverForLes.addStep(leverNearby, pullLever);
		pullLeverForLes.addStep(new Conditions(inMainOrNorthRoom, lever), useLeverOnHole);
		pullLeverForLes.addStep(new Conditions(inBootsRoom, lever), goUpFromBoots);
		pullLeverForLes.addStep(lever, enterDungeon);

		pullLeverForLes.addStep(new Conditions(inMainOrNorthRoom, belowMinus1Weight), pickUpLever);
		pullLeverForLes.addStep(new Conditions(inBootsRoom, new Conditions(LogicType.OR, belowMinus1Weight, bootsOfLightness)), goUpFromBoots);
		pullLeverForLes.addStep(inBootsRoom, getBoots);
		pullLeverForLes.addStep(new Conditions(inMainOrNorthRoom, below4Weight, dontHaveBoots), goDownToBoots);
		pullLeverForLes.addStep(belowMinus1Weight, enterDungeon);
		pullLeverForLes.addStep(new Conditions(below4Weight, bootsOfLightness), prepareBelow0);
		pullLeverForLes.addStep(below4Weight, enterDungeonForBoots);

		steps.put(20, pullLeverForLes);

		ConditionalStep goFightLes = new ConditionalStep(this, prepare);
		goFightLes.addStep(new Conditions(inLesRoom, lesNearby), fightLes);
		goFightLes.addStep(new Conditions(iceArrows, inMainOrNorthRoom), tryToEnterWitchRoom);
		goFightLes.addStep(new Conditions(hasEnoughArrows, inArrowRoom), returnToMainRoom);
		goFightLes.addStep(inArrowRoom, collectArrows);
		goFightLes.addStep(leverNearby, pullLever);
		goFightLes.addStep(new Conditions(inMainOrNorthRoom, lever), useLeverOnHole);
		goFightLes.addStep(new Conditions(inBootsRoom, lever), goUpFromBoots);
		goFightLes.addStep(lever, enterDungeon);

		goFightLes.addStep(new Conditions(inMainOrNorthRoom, belowMinus1Weight), pickUpLever);
		goFightLes.addStep(new Conditions(inBootsRoom, new Conditions(LogicType.OR, belowMinus1Weight, bootsOfLightness)), goUpFromBoots);
		goFightLes.addStep(inBootsRoom, getBoots);
		goFightLes.addStep(new Conditions(inMainOrNorthRoom, below4Weight, dontHaveBoots), goDownToBoots);
		goFightLes.addStep(belowMinus1Weight, enterDungeon);
		goFightLes.addStep(new Conditions(below4Weight, bootsOfLightness), prepareBelow0);
		goFightLes.addStep(below4Weight, enterDungeonForBoots);

		steps.put(30, goFightLes);

		ConditionalStep goToWitch = new ConditionalStep(this, enterDungeonKilledLes);
		goToWitch.addStep(inWitchRoom, giveWineldaLimps);
		goToWitch.addStep(inMainOrNorthRoom, enterLesDoor);
		steps.put(40, goToWitch);
		steps.put(50, goToWitch);

		// TODO: Verify taking staff doesn't progress quest beyond varp 26 = 60
		ConditionalStep goodOrBadPath = new ConditionalStep(this, enterDungeonGivenLimps);
		goodOrBadPath.addStep(staffOfArmadyl.alsoCheckBank(questBank), bringStaffToLucien);
		goodOrBadPath.addStep(new Conditions(inArmaRoom, shinyKey), makeChoice);
		goodOrBadPath.addStep(new Conditions(inDemonArea, shinyKey), pushWall);
		goodOrBadPath.addStep(new Conditions(LogicType.OR, inArmaRoom, inDemonArea), pickUpKey);
		goodOrBadPath.addStep(new Conditions(LogicType.OR, inMainOrNorthRoom, inWitchRoom), talkToWinelda);
		goodOrBadPath.addStep(shinyKey.alsoCheckBank(questBank), enterFromMcgrubbors);

		steps.put(60, goodOrBadPath);

		steps.put(70, killLucien);
		// Sided against Lucien, quest ends at varp 80

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		pendantOfLucien = new ItemRequirement("Pendant of lucien", ItemID.IKOV_PENDANTOFLUCIEN).isNotConsumed();
		pendantOfLucien.setTooltip("You can get another from Lucien in East Ardougne, near the wall of West Ardougne in the Flying Horse Inn");
		pendantOfLucien.canBeObtainedDuringQuest();
		pendantOfLucienEquipped = pendantOfLucien.equipped();
		bootsOfLightness = new ItemRequirement("Boots of lightness", ItemID.IKOV_BOOTSOFLIGHTNESS).isNotConsumed();
		bootsOfLightnessEquipped = bootsOfLightness.equipped();
		limpwurt20 = new ItemRequirement("Limpwurt (unnoted)", ItemID.LIMPWURT_ROOT, 20);
		yewOrBetterBow = new ItemRequirement("Yew, magic, or dark bow", ItemID.YEW_SHORTBOW).isNotConsumed();
		yewOrBetterBow.addAlternates(ItemID.YEW_LONGBOW, ItemID.TRAIL_COMPOSITE_BOW_YEW, ItemID.MAGIC_SHORTBOW, ItemID.MAGIC_SHORTBOW_I,
			ItemID.MAGIC_LONGBOW, ItemID.DARKBOW);
		knife = new ItemRequirement("Knife to get the boots of lightness", ItemID.KNIFE).isNotConsumed();
		lightSource = new ItemRequirement("A light source to get the boots of lightness", ItemCollections.LIGHT_SOURCES).isNotConsumed();

		iceArrows20 = new ItemRequirement("Ice arrows", ItemID.ICE_ARROW, 20);

		iceArrows = new ItemRequirement("Ice arrows", ItemID.ICE_ARROW);
		iceArrowsEquipped = new ItemRequirement("Ice arrows", ItemID.ICE_ARROW, 1, true);
		lever = new ItemRequirement("Lever", ItemID.IKOV_LEVER);
		lever.setHighlightInInventory(true);

		shinyKey = new ItemRequirement("Shiny key", ItemID.IKOV_SHINYKEY);

		armadylPendant = new ItemRequirement("Armadyl pendant", ItemID.IKOV_PENDANTOFARMARDYL, 1, true);
		armadylPendant.setHighlightInInventory(true);

		staffOfArmadyl = new ItemRequirement("Staff of Armadyl", ItemID.IKOV_STAFFOFARMARDYL);

		emptyInventorySpot = new FreeInventorySlotRequirement(1);
	}

	@Override
	protected void setupZones()
	{
		entryRoom1 = new Zone(new WorldPoint(2647, 9803, 0), new WorldPoint(2680, 9814, 0));
		entryRoom2 = new Zone(new WorldPoint(2670, 9801, 0), new WorldPoint(2680, 9804, 0));
		northRoom1 = new Zone(new WorldPoint(2634, 9815, 0), new WorldPoint(2674, 9857, 0));
		northRoom2 = new Zone(new WorldPoint(2634, 9804, 0), new WorldPoint(2647, 9815, 0));
		bootsRoom = new Zone(new WorldPoint(2637, 9759, 0), new WorldPoint(2654, 9767, 0));
		arrowRoom1 = new Zone(new WorldPoint(2657, 9785, 0), new WorldPoint(2692, 9800, 0));
		arrowRoom2 = new Zone(new WorldPoint(2657, 9799, 0), new WorldPoint(2666, 9802, 0));
		arrowRoom3 = new Zone(new WorldPoint(2682, 9799, 0), new WorldPoint(2749, 9852, 0));
		lesRoom = new Zone(new WorldPoint(2639, 9858, 0), new WorldPoint(2651, 9870, 0));
		witchRoom = new Zone(new WorldPoint(2642, 9871, 0), new WorldPoint(2659, 9879, 0));

		demonArea1 = new Zone(new WorldPoint(2625, 9856, 0), new WorldPoint(2638, 9893, 0));
		demonArea2 = new Zone(new WorldPoint(2639, 9880, 0), new WorldPoint(2664, 9892, 0));
		demonArea3 = new Zone(new WorldPoint(2654, 9893, 0), new WorldPoint(2661, 9896, 0));
		demonArea4 = new Zone(new WorldPoint(2659, 9871, 0), new WorldPoint(2665, 9879, 0));

		armaRoom1 = new Zone(new WorldPoint(2633, 9894, 0), new WorldPoint(2651, 9914, 0));
		armaRoom2 = new Zone(new WorldPoint(2642, 9893, 0), new WorldPoint(2645, 9893, 0));
	}

	public void setupConditions()
	{
		dontHaveBoots = new ItemRequirements(LogicType.NOR, bootsOfLightness);

		below4Weight = new WeightRequirement(4, Operation.LESS_EQUAL);
		belowMinus1Weight = new WeightRequirement(-1, Operation.LESS_EQUAL);
		inEntryRoom = new ZoneRequirement(entryRoom1, entryRoom2);
		inNorthRoom = new ZoneRequirement(northRoom1, northRoom2);
		inLesRoom = new ZoneRequirement(lesRoom);
		inBootsRoom = new ZoneRequirement(bootsRoom);
		inMainOrNorthRoom = new Conditions(LogicType.OR, inEntryRoom, inNorthRoom, inLesRoom);

		pulledLever = new Conditions(true, LogicType.OR, new WidgetTextRequirement(229, 1, "You hear the clunking of some hidden machinery."));
		leverNearby = new ObjectCondition(ObjectID.IKOV_MENDEDLEVER, new WorldPoint(2671, 9804, 0));
		inArrowRoom = new ZoneRequirement(arrowRoom1, arrowRoom2, arrowRoom3);
		hasEnoughArrows = new Conditions(true, LogicType.OR, iceArrows20);
		lesNearby = new NpcCondition(NpcID.IKOV_FIREWARRIOR);
		inWitchRoom = new ZoneRequirement(witchRoom);

		inDemonArea = new ZoneRequirement(demonArea1, demonArea2, demonArea3, demonArea4);
		inArmaRoom = new ZoneRequirement(armaRoom1, armaRoom2);
	}

	public void setupSteps()
	{
		// TODO: Verify which Lucien NPC ID is correct
		talkToLucien = new NpcStep(this, new int[]{NpcID.IKOV_LUCIEN1, NpcID.IKOV_LUCIEN1_VIS, NpcID.IKOV_LUCIEN2_VIS,
				NpcID.IKOV_LUCIEN2_VIS_NOATTACK }, new WorldPoint(2573, 3321, 0), "Talk to Lucien in the pub north of East Ardougne castle.",
				emptyInventorySpot);
		talkToLucien.addDialogSteps("I'm a mighty hero!", "That sounds like a laugh!");
		prepare = new DetailedQuestStep(this,
			"Get your weight below 0kg. You can get boots of lightness from the Temple of Ikov north of East Ardougne for -4.5kg.",
			pendantOfLucienEquipped, limpwurt20, yewOrBetterBow);

		prepareBelow0 = new DetailedQuestStep(this,
			"Get your weight below 0kg.",
			pendantOfLucienEquipped, limpwurt20, yewOrBetterBow);

		prepare.addSubSteps(prepareBelow0);

		enterDungeonForBoots = new ObjectStep(this, ObjectID.LADDER_CELLAR, new WorldPoint(2677, 3405, 0),
			"Enter the Temple of Ikov. You can get Boots of Lightness inside to get -4.5kg.",
			pendantOfLucienEquipped, knife, lightSource, limpwurt20, yewOrBetterBow);

		enterDungeon = new ObjectStep(this, ObjectID.LADDER_CELLAR, new WorldPoint(2677, 3405, 0),
			"Enter the Temple of Ikov north of East Ardougne.", pendantOfLucienEquipped, yewOrBetterBow, limpwurt20);

		enterDungeon.addSubSteps(enterDungeonForBoots);

		goDownToBoots = new ObjectStep(this, ObjectID.IKOV_DARKSTAIRSDOWN, new WorldPoint(2651, 9805, 0), "Go down the west stairs to get boots of lightness.", lightSource, knife);
		getBoots = new ItemStep(this, "Get the boots of lightness in the north east corner.", bootsOfLightnessEquipped);
		goUpFromBoots = new ObjectStep(this, ObjectID.IKOV_DARKSTAIRS, new WorldPoint(2639, 9764, 0), "Go back upstairs.");

		pickUpLever = new DetailedQuestStep(this, new WorldPoint(2637, 9819, 0),
			"Cross the bridge in the north room and pick up the lever whilst weighing less than 0kg.",
			pendantOfLucien.equipped(), lever);
		useLeverOnHole = new ObjectStep(this, ObjectID.IKOV_LEVERBRACKET, new WorldPoint(2671, 9804, 0), "Use the lever on the lever bracket in the entrance room.", lever);

		pullLever = new ObjectStep(this, ObjectID.IKOV_MENDEDLEVER, new WorldPoint(2671, 9804, 0), "Pull the lever.");

		enterArrowRoom = new ObjectStep(this, ObjectID.IKOV_MENDEDLEVERDOORL, new WorldPoint(2662, 9803, 0), "Enter the south gate.");
		collectArrows = new ObjectStep(this, ObjectID.IKOV_CHESTCLOSED, "Search the chests in the ice area of this room until you have at least 20 Ice Arrows or more to be safe. A random chest has the arrows each time.", iceArrows20);
		collectArrows.setHideWorldArrow(true);
		collectArrows.addAlternateObjects(ObjectID.IKOV_CHESTOPEN);

		returnToMainRoom = new ObjectStep(this, ObjectID.IKOV_MENDEDLEVERDOORL, new WorldPoint(2662, 9803, 0), "Return back to the entry room when you have enough arrows.");

		goSearchThievingLever = new ObjectStep(this, ObjectID.IKOV_TRAPLEVER, new WorldPoint(2665, 9855, 0),
			"Go into the north room, then on the north side right-click search the lever there for traps, then pull it.");

		goPullThievingLever = new ObjectStep(this, ObjectID.IKOV_TRAPLEVER, new WorldPoint(2665, 9855, 0),
			"Go into the north room, then on the north side pull the lever.");

		goSearchThievingLever.addSubSteps(goPullThievingLever);

		tryToEnterWitchRoom = new ObjectStep(this, ObjectID.IKOV_FIREWARRIORDOOR, new WorldPoint(2646, 9870, 0),
			"Try to enter the far north door. Be prepared to fight Lesarkus, who can only be hurt by ice arrows.", yewOrBetterBow, iceArrowsEquipped);

		fightLes = new NpcStep(this, NpcID.IKOV_FIREWARRIOR, new WorldPoint(2646, 9866, 0),
			"Kill the Fire Warrior of Lesarkus. He can only be hurt by the ice arrows.", yewOrBetterBow, iceArrowsEquipped);

		enterDungeonKilledLes = new ObjectStep(this, ObjectID.LADDER_CELLAR, new WorldPoint(2677, 3405, 0),
			"Enter the Temple of Ikov north of East Ardougne.", pendantOfLucienEquipped, limpwurt20);

		enterLesDoor = new ObjectStep(this, ObjectID.IKOV_FIREWARRIORDOOR, new WorldPoint(2646, 9870, 0),
			"Go all the way north and talk to Winelda there.", pendantOfLucienEquipped, limpwurt20);

		enterLesDoor.addSubSteps(enterDungeonKilledLes);

		giveWineldaLimps = new NpcStep(this, NpcID.IKOV_WINELDA, new WorldPoint(2655, 9876, 0), "Give Winelda 20 unnoted limpwurts.", limpwurt20);

		enterDungeonGivenLimps = new ObjectStep(this, ObjectID.LADDER_CELLAR, new WorldPoint(2677, 3405, 0),
			"Enter the Temple of Ikov north of East Ardougne. If you got the shiny key, get it and enter via Mcgrubber's wood west of Seers' Village.", pendantOfLucienEquipped);

		enterFromMcgrubbors = new ObjectStep(this, ObjectID.LADDER_CELLAR, new WorldPoint(2659, 3492, 0), "Enter the house at Mcgrubbor's wood and go down the ladder.", shinyKey);


		talkToWinelda = new NpcStep(this, NpcID.IKOV_WINELDA, new WorldPoint(2655, 9876, 0),
			"Talk to Winelda in the far north of the dungeon.");

		pickUpKey = new DetailedQuestStep(this, new WorldPoint(2628, 9859, 0), "Follow the path around past the skeletons and lesser demons until you find a shiny key. Pick it up.", shinyKey);

		pushWall = new ObjectStep(this, ObjectID.SECRETDOOR2, new WorldPoint(2643, 9892, 0), "Push the wall on the north side of this area.");
		pushWall.addSubSteps(enterDungeonGivenLimps, enterFromMcgrubbors);

		makeChoice = new DetailedQuestStep(this,
			"You can now choose to either help Lucien or help the Guardians of Armadyl. To help Lucien, simply pick up the Staff of Armadyl. To help the guardians, remove the pendant of lucien and talk to one of them.");
		makeChoice.addDialogStep("I seek the Staff of Armadyl.");
		makeChoice.addDialogStep("Lucien will give me a grand reward for it!");
		makeChoice.addDialogStep("You're right, it's time for my yearly bath.");
		makeChoice.addDialogStep("Ok! I'll help!");

		killLucien = new NpcStep(this, NpcID.IKOV_LUCIEN2, new WorldPoint(3122, 3484, 0), "Equip the Armadyl Pendant and defeat Lucien in the house " +
				"west of the Grand Exchange.", armadylPendant);
		bringStaffToLucien = new NpcStep(this, NpcID.IKOV_LUCIEN2, new WorldPoint(3122, 3484, 0), "Bring the Staff of Armadyl to Lucien in the house west of the Grand Exchange.", staffOfArmadyl);
		((NpcStep) bringStaffToLucien).addAlternateNpcs(NpcID.IKOV_LUCIEN2_VIS_NOATTACK);

		bringStaffToLucien.addDialogSteps("Yes! Here it is.");

		returnToLucien = new DetailedQuestStep(this, "Either return to Lucien west of the Grand Exchange with the Staff of Armadyl, or kill him whilst wearing the Pendant of Armadyl.");
		returnToLucien.addSubSteps(killLucien, bringStaffToLucien);
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(yewOrBetterBow);
		reqs.add(limpwurt20);
		reqs.add(knife);
		reqs.add(lightSource);
		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		ArrayList<String> reqs = new ArrayList<>();
		reqs.add("Able to survive multiple aggressive spiders (level 61) and demons (level 82)");
		reqs.add("Fire Warrior of Lesarkus (level 84) with ranged");
		reqs.add("If siding with Lucien, Guardian of Armadyl (level 43)");
		return reqs;
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> req = new ArrayList<>();
		req.add(new SkillRequirement(Skill.THIEVING, 42, true));
		req.add(new SkillRequirement(Skill.RANGED, 40));
		return req;
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(1);
	}

	@Override
	public List<ExperienceReward> getExperienceRewards()
	{
		return Arrays.asList(
				new ExperienceReward(Skill.RANGED, 10500),
				new ExperienceReward(Skill.FLETCHING, 8000));
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Collections.singletonList(new ItemReward("Boots of Lightness", ItemID.IKOV_BOOTSOFLIGHTNESS, 1));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Starting off", Collections.singletonList(talkToLucien)));
		allSteps.add(new PanelDetails("Defeat Lesarkus", 
			Arrays.asList(prepare, enterDungeon, goDownToBoots, getBoots, goUpFromBoots, pickUpLever, useLeverOnHole,
				pullLever, enterArrowRoom, collectArrows, returnToMainRoom, goSearchThievingLever,
				tryToEnterWitchRoom, fightLes), pendantOfLucien, yewOrBetterBow, knife, lightSource, limpwurt20));
		allSteps.add(new PanelDetails("Explore deeper", Arrays.asList(enterLesDoor, giveWineldaLimps, pickUpKey, pushWall, makeChoice, returnToLucien)));
		return allSteps;
	}
}
