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
package net.runelite.client.plugins.microbot.questhelper.helpers.quests.piratestreasure;

import net.runelite.client.plugins.microbot.questhelper.collections.ItemCollections;
import net.runelite.client.plugins.microbot.questhelper.panel.PanelDetails;
import net.runelite.client.plugins.microbot.questhelper.questhelpers.BasicQuestHelper;
import net.runelite.client.plugins.microbot.questhelper.requirements.conditional.Conditions;
import net.runelite.client.plugins.microbot.questhelper.requirements.item.ItemRequirement;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.Zone;
import net.runelite.client.plugins.microbot.questhelper.requirements.zone.ZoneRequirement;
import net.runelite.client.plugins.microbot.questhelper.rewards.ItemReward;
import net.runelite.client.plugins.microbot.questhelper.rewards.QuestPointReward;
import net.runelite.client.plugins.microbot.questhelper.steps.*;
import net.runelite.api.coords.WorldPoint;
import net.runelite.api.gameval.ItemID;
import net.runelite.api.gameval.NpcID;
import net.runelite.api.gameval.ObjectID;

import java.util.*;

public class PiratesTreasure extends BasicQuestHelper
{
	//ItemRequirements
	ItemRequirement sixtyCoins, spade, pirateMessage, chestKey;

	private NpcStep speakToRedbeard;

	private RumSmugglingStep smuggleRum;

	private QuestStep readPirateMessage;

	private ObjectStep openChest, climbStairs;

	private QuestStep digUpTreasure;

	Zone blueMoonFirst;

	ZoneRequirement inBlueMoonFirst;

	@Override
	public Map<Integer, QuestStep> loadSteps()
	{
		initializeRequirements();

		Map<Integer, QuestStep> steps = new HashMap<>();

		speakToRedbeard = new NpcStep(this, NpcID.REDBEARD_FRANK, new WorldPoint(3053, 3251, 0),
			"Talk to Redbeard Frank in Port Sarim.");
		speakToRedbeard.addDialogSteps("I'm in search of treasure.", "Yes.");

		steps.put(0, speakToRedbeard);

		smuggleRum = new RumSmugglingStep(this);

		steps.put(1, smuggleRum);

		readPirateMessage = new DetailedQuestStep(this, "Read the Pirate message.", pirateMessage.highlighted());
		climbStairs = new ObjectStep(this, ObjectID.FAI_VARROCK_STAIRS, new WorldPoint(3228, 3393, 0),
			"Climb up the stairs in The Blue Moon Inn in Varrock.");
		openChest = new ObjectStep(this, ObjectID.PIRATECHEST, new WorldPoint(3219, 3396, 1),
			"Open the chest by using the key on it.", chestKey.highlighted());
		openChest.addDialogStep("Ok thanks, I'll go and get it.");
		openChest.addIcon(ItemID.CHEST_KEY);

		blueMoonFirst = new Zone(new WorldPoint(3213, 3405, 1), new WorldPoint(3234, 3391, 1));
		inBlueMoonFirst = new ZoneRequirement(blueMoonFirst);

		ConditionalStep getTreasureMap = new ConditionalStep(this, climbStairs);
		getTreasureMap.addStep(new Conditions(chestKey, inBlueMoonFirst), openChest);
		getTreasureMap.addStep(pirateMessage, readPirateMessage);

		steps.put(2, getTreasureMap);

		digUpTreasure = new DigStep(this, new WorldPoint(2999, 3383, 0),
			"Dig in the middle of the cross in Falador Park, and kill the Gardener (level 4) who appears. Once killed, dig again.");

		steps.put(3, digUpTreasure);
		return steps;
	}

	@Override
	protected void setupRequirements()
	{
		sixtyCoins = new ItemRequirement("Coins", ItemCollections.COINS, 60);
		spade = new ItemRequirement("Spade", ItemID.SPADE).isNotConsumed();
		pirateMessage = new ItemRequirement("Pirate message", ItemID.PIRATEMESSAGE);
		chestKey = new ItemRequirement("Chest key", ItemID.CHEST_KEY);
		chestKey.setTooltip("You can get another one from Redbeard Frank");
	}

	@Override
	public List<ItemRequirement> getItemRequirements()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(sixtyCoins);
		reqs.add(spade);

		return reqs;
	}

	@Override
	public List<ItemRequirement> getItemRecommended()
	{
		ArrayList<ItemRequirement> reqs = new ArrayList<>();
		reqs.add(new ItemRequirement("A teleport to Varrock", ItemID.POH_TABLET_VARROCKTELEPORT));
		reqs.add(new ItemRequirement("A teleport to Falador", ItemID.POH_TABLET_FALADORTELEPORT));
		reqs.add(new ItemRequirement("Bananas (obtainable in quest)", ItemID.BANANA, 10));

		return reqs;
	}

	@Override
	public List<String> getCombatRequirements()
	{
		return Collections.singletonList("Gardener (level 4)");
	}

	@Override
	public QuestPointReward getQuestPointReward()
	{
		return new QuestPointReward(2);
	}

	@Override
	public List<ItemReward> getItemRewards()
	{
		return Arrays.asList(
				new ItemReward("A Gold Ring", ItemID.GOLD_RING, 1),
				new ItemReward("An Emerald", ItemID.EMERALD, 1),
				new ItemReward("Coins", ItemID.COINS, 450));
	}

	@Override
	public List<PanelDetails> getPanels()
	{
		List<PanelDetails> allSteps = new ArrayList<>();

		allSteps.add(new PanelDetails("Talk to Redbeard Frank", Collections.singletonList(speakToRedbeard), sixtyCoins));
		allSteps.addAll(smuggleRum.panelDetails());
		allSteps.add(new PanelDetails("Discover the treasure", Arrays.asList(climbStairs, openChest, readPirateMessage,
			digUpTreasure), spade));

		return allSteps;
	}
}
