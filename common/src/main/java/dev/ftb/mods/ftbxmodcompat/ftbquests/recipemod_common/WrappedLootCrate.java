package dev.ftb.mods.ftbxmodcompat.ftbquests.recipemod_common;

import dev.ftb.mods.ftblibrary.icon.ItemIcon;
import dev.ftb.mods.ftbquests.quest.loot.LootCrate;
import dev.ftb.mods.ftbquests.quest.loot.WeightedReward;
import net.minecraft.nbt.StringTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WrappedLootCrate {
	public static final int ITEMSX = 8;
	public static final int ITEMSY = 6;
	public static final int ITEMS = ITEMSX * ITEMSY;

	public final LootCrate crate;
	public final ItemStack crateStack;
	public final List<WeightedReward> sortedRewards;
	public final List<ItemStack> outputs;
	private final List<List<ItemStack>> cycledOutputs;

	public WrappedLootCrate(LootCrate c) {
		crate = c;
		crateStack = crate.createStack();
		outputs = new ArrayList<>(c.table.rewards.size());
		sortedRewards = c.table.rewards.stream().sorted(WeightedReward::compareTo).toList();

		for (WeightedReward reward : sortedRewards) {
			Object object = reward.reward.getIngredient();
			ItemStack stack = object instanceof ItemStack ? (ItemStack) object : ItemStack.EMPTY;

			if (!stack.isEmpty()) {
				outputs.add(stack.copy());
			} else if (reward.reward.getIcon() instanceof ItemIcon) {
				stack = ((ItemIcon) reward.reward.getIcon()).getStack().copy();
				stack.setHoverName(reward.reward.getTitle());
				outputs.add(stack);
			} else {
				stack = new ItemStack(Items.PAINTING);
				stack.setHoverName(reward.reward.getTitle());
				stack.addTagElement("icon", StringTag.valueOf(reward.reward.getIcon().toString()));
				outputs.add(stack);
			}
		}

		if (outputs.size() <= ITEMS) {
			cycledOutputs = new ArrayList<>(outputs.size());

			for (ItemStack stack : outputs) {
				cycledOutputs.add(Collections.singletonList(stack));
			}
		} else {
			// too many items to fit in display; cycle them
			cycledOutputs = new ArrayList<>(ITEMS);

			for (int i = 0; i < ITEMS; i++) {
				cycledOutputs.add(new ArrayList<>());
			}

			for (int i = 0; i < outputs.size(); i++) {
				cycledOutputs.get(i % ITEMS).add(outputs.get(i));
			}
		}
	}

	public List<Ingredient> inputIngredients() {
		return List.of(Ingredient.of(crateStack));
	}

	public List<Ingredient> outputIngredients() {
		return cycledOutputs.stream().map(items -> Ingredient.of(items.toArray(new ItemStack[0]))).toList();
	}
}