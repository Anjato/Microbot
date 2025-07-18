/*
 * Copyright (c) 2021, Zoinkwiz
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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.recipefordisaster;

import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestHelperQuest;
import net.runelite.client.plugins.microbot.questhelper.questinfo.QuestVarbits;
import net.runelite.client.plugins.microbot.questhelper.requirements.Requirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.quest.QuestRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.util.Operation;
import net.runelite.client.plugins.microbot.questhelper.requirements.var.VarbitRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ExperienceReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.UnlockReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.Client;
import net.runelite.api.QuestState;
import net.runelite.api.Skill;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;
import net.runelite.api.gameval.VarbitID;

import java.util.*;

public class RFDGoblins extends BasicQuestHelper
{
	ItemRequirement bread, orange, knife, blueGreenPurpledye, spice, fishingBait, bucketOfWater,
		breadHighlighted, orangeHighlighted, knifeHighlighted, blueGreenPurpledyeHighlighted,
		spiceHighlighted, fishingBaitHighlighted, bucketOfWaterHighlighted, charcoal, dyedOrange,
		spicedBait, wetBread, teleportLumbridge, teleportFalador, slop, slopHighlighted,
		orangeSliceHighlighted;

	Requirement inDiningRoom, inCookRoom;

	QuestStep enterDiningRoom, inspectGoblin, goDownToKitchen, talkToCook, talkToCookAfterChar,
		sliceOrange, dyeOrange, spiceBait, useWaterOnBread, enterDiningRoomAgain,
		useSlopOnGoblin;

	ConditionalStep goTalkCook1, goTalkCook2, goTalkCook3, goTalkCook4;

	//Zones
	Zone diningRoom, cookRoom, cookRoomDestroyed;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();
		setupConditions();
		setupSteps();
		Map<Integer, QuestStep> steps = new HashMap<>();

		ConditionalStep goInspectGoblin = new ConditionalStep(this, enterDiningRoom);
		goInspectGoblin.addStep(inDiningRoom, inspectGoblin);
		steps.put(0, goInspectGoblin);

		steps.put(5, goTalkCook1);
		steps.put(10, goTalkCook2);
		steps.put(15, goTalkCook3);

		ConditionalStep goGetIngredients = new ConditionalStep(this, useWaterOnBread);
		goGetIngredients.addStep(new Conditions(wetBread, dyedOrange, spicedBait),
			goTalkCook4);
		goGetIngredients.addStep(new Conditions(wetBread, dyedOrange), spiceBait);
		goGetIngredients.addStep(new Conditions(wetBread, orangeSliceHighlighted), dyeOrange);
		goGetIngredients.addStep(wetBread, sliceOrange);
		steps.put(30, goGetIngredients);

		ConditionalStep goGiveSlop = new ConditionalStep(this, enterDiningRoomAgain);
		goGiveSlop.addStep(inDiningRoom, useSlopOnGoblin);
		steps.put(35, goGiveSlop);

		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		bread = new ItemRequirement("Bread", ItemID.BREAD);
		orange = new ItemRequirement("Orange", ItemID.ORANGE);
		knife = new ItemRequirement("Knife", ItemID.KNIFE).isNotConsumed();
		blueGreenPurpledye = new ItemRequirement("A blue, green, or purple dye", ItemID.BLUEDYE);
		blueGreenPurpledye.addAlternates(ItemID.GREENDYE, ItemID.PURPLEDYE);
		spice = new ItemRequirement("Spice or gnome spice", ItemID.SPICESPOT).isNotConsumed();
		spice.setTooltip("You can get some from the Culinaromancer's chest, or a Gnome Spice from the Tree Gnome " +
			"Stronghold");
		spice.addAlternates(ItemID.GNOME_SPICE);
		fishingBait = new ItemRequirement("Fishing bait", ItemID.FISHING_BAIT);
		bucketOfWater = new ItemRequirement("Bucket of water", ItemID.BUCKET_WATER);

		breadHighlighted = bread.highlighted();
		orangeHighlighted = orange.highlighted();
		knifeHighlighted = knife.highlighted();
		blueGreenPurpledyeHighlighted = blueGreenPurpledye.highlighted();
		spiceHighlighted = spice.highlighted();
		fishingBaitHighlighted = fishingBait.highlighted();
		bucketOfWaterHighlighted = bucketOfWater.highlighted();

		orangeSliceHighlighted = new ItemRequirement("Orange slices", ItemID.ORANGE_SLICES);
		orangeSliceHighlighted.setHighlightInInventory(true);

		charcoal = new ItemRequirement("Charcoal", ItemID.CHARCOAL);
		charcoal.setTooltip("Can be bought from the general store in Shilo Village or the one northwest of Tai Bwo Wannai");
		charcoal.addAlternates(ItemID.GROUND_CHARCOAL);

		dyedOrange = new ItemRequirement("Dyed orange", ItemID._100GOBLIN_DYED_ORANGES);
		spicedBait = new ItemRequirement("Spicy maggots", ItemID._100GOBLIN_SPICEY_MAGGOTS);
		wetBread = new ItemRequirement("Soggy bread", ItemID._100GOBLIN_SOGGY_BREAD);
		slop = new ItemRequirement("Slop of compromise", ItemID._100GOBLIN_COMPROMISE_MUSH);
		slopHighlighted = new ItemRequirement("Slop of compromise", ItemID._100GOBLIN_COMPROMISE_MUSH);
		slopHighlighted.setHighlightInInventory(true);

		teleportFalador = new ItemRequirement("Teleport to Falador", ItemID.POH_TABLET_FALADORTELEPORT);
		teleportLumbridge = new ItemRequirement("Teleport to Lumbridge", ItemID.POH_TABLET_LUMBRIDGETELEPORT);
	}

	@Override
	protected void setupZones()
	{
		diningRoom = new Zone(new WorldPoint(1856, 5313, 0), new WorldPoint(1870, 5333, 0));
		cookRoom = new Zone(new WorldPoint(2974, 9906, 0), new WorldPoint(2990, 9917, 0));
		cookRoomDestroyed = new Zone(new WorldPoint(2974, 9866, 0), new WorldPoint(2990, 9877, 0));
	}

	public void setupConditions()
	{
		inDiningRoom = new ZoneRequirement(diningRoom);
		inCookRoom = new ZoneRequirement(cookRoom, cookRoomDestroyed);
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
				new ExperienceReward(Skill.COOKING, 1000),
				new ExperienceReward(Skill.FARMING, 1000),
				new ExperienceReward(Skill.CRAFTING, 1000));
	}

	@Override
	public List<UnlockReward> getUnlockRewards()
	{
		return Collections.singletonList(new UnlockReward("Increased access to the Culinaromancer's Chest"));
	}

	public void setupSteps()
	{
		enterDiningRoom = new ObjectStep(this, ObjectID.HUNDRED_LUMBRIDGE_DOUBLEDOORL, new WorldPoint(3213, 3221, 0),
			"Go inspect Wartface or Bentnoze.");
		inspectGoblin = new ObjectStep(this, ObjectID.HUNDRED_GOBLIN1_BASE, new WorldPoint(1862, 5325, 0),
			"Inspect Wartface or Bentnoze.");
		inspectGoblin.addSubSteps(enterDiningRoom);

		goDownToKitchen = new ObjectStep(this, ObjectID._100_GOBLIN_LADDER_DOWN, new WorldPoint(2960, 3507, 0),
			"");

		talkToCook = new NpcStep(this, NpcID.GOBLIN_COOK, new WorldPoint(2981, 9909, 0), "");
		((NpcStep)talkToCook).addAlternateNpcs(NpcID.GOBLIN_COOK_ON_WALL);
		talkToCookAfterChar = new NpcStep(this, NpcID.GOBLIN_COOK_CHARRED, new WorldPoint(2981, 9868, 0),
			"");

		useWaterOnBread = new DetailedQuestStep(this, "Use a bucket of water on some bread.", bucketOfWaterHighlighted,
			breadHighlighted);

		sliceOrange = new DetailedQuestStep(this, "Use a knife on an orange and SLICE it.", knifeHighlighted,
			orangeHighlighted);
		dyeOrange = new DetailedQuestStep(this, "Use a dye on the sliced orange.", blueGreenPurpledyeHighlighted,
			orangeSliceHighlighted);
		spiceBait = new DetailedQuestStep(this, "Use spices on the fishing bait.", spiceHighlighted, fishingBaitHighlighted);

		enterDiningRoomAgain = new ObjectStep(this, ObjectID.HUNDRED_LUMBRIDGE_DOUBLEDOORL, new WorldPoint(3213, 3221, 0),
			"Go use the slop on Wartface or Bentnoze in the Lumbridge Banquet room.", slop);
		useSlopOnGoblin = new ObjectStep(this, ObjectID.HUNDRED_GOBLIN1_BASE, new WorldPoint(1862, 5325, 0),
			"Use the slop on Wartface or Bentnoze.", slopHighlighted);
		useSlopOnGoblin.addSubSteps(enterDiningRoomAgain);
		useSlopOnGoblin.addIcon(ItemID._100GOBLIN_COMPROMISE_MUSH);

		goTalkCook1 = new ConditionalStep(this, goDownToKitchen, "Go talk to the Goblin Cook down the ladder in the Goblin " +
			"Village.", bread, orange, knife, blueGreenPurpledye, spice, fishingBait,
			bucketOfWater, charcoal);
		goTalkCook1.addStep(inCookRoom, talkToCook);
		goTalkCook1.addDialogSteps("I need your help...", "What do you need? Maybe I can get it for you.");

		goTalkCook2 = new ConditionalStep(this, goDownToKitchen, "Give the Goblin Cook some charcoal.", charcoal);
		goTalkCook2.addStep(inCookRoom, talkToCook);
		goTalkCook2.addDialogSteps("I've got the charcoal you were after.");

		goTalkCook3 = new ConditionalStep(this, goDownToKitchen, "Talk to the Goblin Cook again.",
			bread, orange, knife, blueGreenPurpledye, spice, fishingBait, bucketOfWater);
		goTalkCook3.addStep(inCookRoom, talkToCookAfterChar);
		goTalkCook3.addDialogSteps("I need your help...");

		goTalkCook4 = new ConditionalStep(this, goDownToKitchen, "Talk to the Goblin Cook again with the ingredients.",
			wetBread, dyedOrange, spicedBait);
		goTalkCook4.addStep(inCookRoom, talkToCookAfterChar);
		goTalkCook4.addDialogStep("I've got the ingredients we need...");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		return Arrays.asList(bread, orange, knife, blueGreenPurpledye, spice, fishingBait, bucketOfWater, charcoal);
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		return Arrays.asList(teleportFalador, teleportLumbridge);
	}

	@Override
	public List<Requirement> getGeneralRequirements()
	{
		ArrayList<Requirement> reqs = new ArrayList<>();
		reqs.add(new VarbitRequirement(QuestVarbits.QUEST_RECIPE_FOR_DISASTER.getId(), Operation.GREATER_EQUAL, 3,
			"Started Recipe for Disaster"));
		reqs.add(new QuestRequirement(QuestHelperQuest.GOBLIN_DIPLOMACY, QuestState.FINISHED));

		return reqs;

	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();
		allSteps.add(new PanelDetails("Saving the Generals",
			Arrays.asList(inspectGoblin, goTalkCook1, goTalkCook2, goTalkCook3,
				useWaterOnBread, sliceOrange, dyeOrange, spiceBait, goTalkCook4,
				useSlopOnGoblin),
			bread, orange, knife, blueGreenPurpledye, spice, fishingBait, bucketOfWater, charcoal));
		return allSteps;
	}

	@Override
	public QuestState getState(Client client)
	{
		int questState = client.getVarbitValue(VarbitID._100GOBLIN);
		if (questState == 0)
		{
			return QuestState.NOT_STARTED;
		}

		if (questState < 40)
		{
			return QuestState.IN_PROGRESS;
		}

		return QuestState.FINISHED;
	}

	@Override
	public boolean isCompleted()
	{
		return (getQuest().getVar(client) >= 40 || client.getVarbitValue(VarbitID.HUNDRED_MAIN_QUEST_VAR) < 3);
	}
}
