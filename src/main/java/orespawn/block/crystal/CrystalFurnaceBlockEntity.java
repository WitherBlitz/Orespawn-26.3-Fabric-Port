package orespawn.block.crystal;

import com.mojang.serialization.Codec;
import it.unimi.dsi.fastutil.objects.Reference2IntMap;
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CookingFuel;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.loot.providers.number.ints.ResolvableInt;
import net.minecraft.world.phys.Vec3;
import orespawn.content.Crystal;
import org.jspecify.annotations.Nullable;

import java.util.Map;

/**
 * OreSpawn's TileEntityCrystalFurnace: input, fuel and output slots like a furnace, smelting every furnace recipe in a
 * fixed 150 ticks. Fuel burns as long as a furnace's would (wooden things, coal, lava...), except Crystal Energy, which
 * burns for 20000 ticks. As in OreSpawn, fuel is only lit while something can be smelted, and the progress drops back
 * to nothing the moment it can't. Hoppers fill the input from above and the fuel from the sides, and empty the output
 * (and empty buckets) from below. Taking the output gives the recipes' experience, as a furnace does.
 */
public class CrystalFurnaceBlockEntity extends BaseContainerBlockEntity implements WorldlyContainer {
	public static final int COOK_TIME = 150;
	public static final int CRYSTAL_ENERGY_BURN_TIME = 20000;
	private static final int[] SLOTS_UP = {0};
	private static final int[] SLOTS_DOWN = {2, 1};
	private static final int[] SLOTS_SIDES = {1};
	private static final Codec<Map<ResourceKey<Recipe<?>>, Integer>> RECIPES_USED_CODEC = Codec.unboundedMap(Recipe.KEY_CODEC, Codec.INT);

	private NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
	private int burnTime;
	private int currentItemBurnTime;
	private int cookTime;
	private final Reference2IntOpenHashMap<ResourceKey<Recipe<?>>> recipesUsed = new Reference2IntOpenHashMap<>();
	private final RecipeManager.CachedCheck<SingleRecipeInput, ? extends AbstractCookingRecipe> quickCheck = RecipeManager.createCheck(RecipeType.SMELTING);

	/** What the menu shows: 0 cook time, 1 burn time left, 2 the current fuel's burn time. */
	final ContainerData data = new ContainerData() {
		@Override
		public int get(int id) {
			return switch (id) {
				case 0 -> CrystalFurnaceBlockEntity.this.cookTime;
				case 1 -> CrystalFurnaceBlockEntity.this.burnTime;
				case 2 -> CrystalFurnaceBlockEntity.this.currentItemBurnTime;
				default -> 0;
			};
		}

		@Override
		public void set(int id, int value) {
			switch (id) {
				case 0 -> CrystalFurnaceBlockEntity.this.cookTime = value;
				case 1 -> CrystalFurnaceBlockEntity.this.burnTime = value;
				case 2 -> CrystalFurnaceBlockEntity.this.currentItemBurnTime = value;
				default -> {
				}
			}
		}

		@Override
		public int getCount() {
			return 3;
		}
	};

	public CrystalFurnaceBlockEntity(BlockPos pos, BlockState state) {
		super(Crystal.CRYSTAL_FURNACE_ENTITY, pos, state);
	}

	/** Whether the item burns in the Crystal Furnace (TileEntityCrystalFurnace.isItemFuel). */
	public static boolean isFuel(ItemStack stack) {
		return stack.is(Crystal.CRYSTAL_ENERGY.asItem()) || stack.has(DataComponents.COOKING_FUEL);
	}

	/** getItemBurnTime: Crystal Energy 20000 ticks, anything else a furnace's time. */
	private int burnDuration(ServerLevel level, ItemStack stack) {
		if (stack.isEmpty()) return 0;
		if (stack.is(Crystal.CRYSTAL_ENERGY.asItem())) return CRYSTAL_ENERGY_BURN_TIME;
		return ResolvableInt.getFromItem(stack, DataComponents.COOKING_FUEL, CookingFuel::burnTime, this.getLootContext(level), 0);
	}

	@Override
	protected Component getDefaultName() {
		return Component.translatable("container.furnace");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.items;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> items) {
		this.items = items;
	}

	@Override
	public int getContainerSize() {
		return this.items.size();
	}

	@Override
	protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
		return new CrystalFurnaceMenu(containerId, inventory, this, this.data);
	}

	@Override
	protected void loadAdditional(ValueInput input) {
		super.loadAdditional(input);
		this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(input, this.items);
		this.burnTime = input.getIntOr("BurnTime", 0);
		this.cookTime = input.getIntOr("CookTime", 0);
		this.currentItemBurnTime = input.getIntOr("CurrentItemBurnTime", 0);
		this.recipesUsed.clear();
		this.recipesUsed.putAll(input.read("RecipesUsed", RECIPES_USED_CODEC).orElse(Map.of()));
	}

	@Override
	protected void saveAdditional(ValueOutput output) {
		super.saveAdditional(output);
		output.putInt("BurnTime", this.burnTime);
		output.putInt("CookTime", this.cookTime);
		output.putInt("CurrentItemBurnTime", this.currentItemBurnTime);
		ContainerHelper.saveAllItems(output, this.items);
		output.store("RecipesUsed", RECIPES_USED_CODEC, this.recipesUsed);
	}

	/** updateEntity. */
	public static void serverTick(ServerLevel level, BlockPos pos, BlockState state, CrystalFurnaceBlockEntity e) {
		boolean wasBurning = e.burnTime > 0;
		boolean changed = false;
		if (e.burnTime > 0) e.burnTime--;
		RecipeHolder<? extends AbstractCookingRecipe> recipe = e.recipe(level);
		if (e.burnTime == 0 && recipe != null) {
			ItemStack fuel = e.items.get(1);
			e.currentItemBurnTime = e.burnTime = e.burnDuration(level, fuel);
			if (e.burnTime > 0) {
				changed = true;
				Item fuelItem = fuel.getItem();
				ItemStackTemplate remainder = fuelItem.getCraftingRemainder();
				fuel.shrink(1);
				if (fuel.isEmpty()) {
					e.items.set(1, remainder != null ? remainder.create() : ItemStack.EMPTY);
				} else if (remainder != null) {
					Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), remainder.create());
				}
			}
		}
		if (e.burnTime > 0 && recipe != null) {
			e.cookTime++;
			if (e.cookTime == COOK_TIME) {
				e.cookTime = 0;
				e.smelt(level, recipe);
				changed = true;
			}
		} else {
			e.cookTime = 0;
		}
		boolean burning = e.burnTime > 0;
		if (wasBurning != burning) {
			changed = true;
			state = state.setValue(CrystalFurnaceBlock.LIT, burning);
			level.setBlock(pos, state, 3);
		}
		if (changed) setChanged(level, pos, state);
	}

	/** canSmelt: the input's smelting recipe, if its result fits in the output slot. */
	private @Nullable RecipeHolder<? extends AbstractCookingRecipe> recipe(ServerLevel level) {
		ItemStack input = this.items.get(0);
		if (input.isEmpty()) return null;
		SingleRecipeInput in = new SingleRecipeInput(input);
		RecipeHolder<? extends AbstractCookingRecipe> recipe = this.quickCheck.getRecipeFor(in, level).orElse(null);
		if (recipe == null) return null;
		ItemStack result = recipe.value().assemble(in);
		if (result.isEmpty()) return null;
		ItemStack output = this.items.get(2);
		if (output.isEmpty()) return recipe;
		if (!ItemStack.isSameItemSameComponents(output, result)) return null;
		int total = output.getCount() + result.getCount();
		return total <= this.getMaxStackSize() && total <= result.getMaxStackSize() ? recipe : null;
	}

	/** smeltItem. */
	private void smelt(ServerLevel level, RecipeHolder<? extends AbstractCookingRecipe> recipe) {
		ItemStack input = this.items.get(0);
		ItemStack result = recipe.value().assemble(new SingleRecipeInput(input));
		ItemStack output = this.items.get(2);
		if (output.isEmpty()) {
			this.items.set(2, result.copy());
		} else {
			output.grow(result.getCount());
		}
		input.shrink(1);
		this.recipesUsed.addTo(recipe.id(), 1);
	}

	/** Gives the experience of everything smelted since it was last emptied (SlotFurnace.onCrafting). */
	public void awardUsedRecipesAndPopExperience(ServerPlayer player) {
		ServerLevel level = player.level();
		Vec3 at = player.position();
		for (Reference2IntMap.Entry<ResourceKey<Recipe<?>>> entry : this.recipesUsed.reference2IntEntrySet()) {
			level.recipeAccess().byKey(entry.getKey()).ifPresent(holder -> {
				player.awardRecipes(java.util.List.of(holder));
				if (holder.value() instanceof AbstractCookingRecipe cooking) {
					float xp = entry.getIntValue() * cooking.experience();
					int whole = Mth.floor(xp);
					float frac = Mth.frac(xp);
					if (frac != 0.0F && level.getRandom().nextFloat() < frac) whole++;
					ExperienceOrb.award(level, at, whole);
				}
			});
		}
		this.recipesUsed.clear();
	}

	@Override
	public boolean canPlaceItem(int slot, ItemStack stack) {
		if (slot == 2) return false;
		return slot != 1 || isFuel(stack);
	}

	@Override
	public int[] getSlotsForFace(Direction direction) {
		return direction == Direction.DOWN ? SLOTS_DOWN : direction == Direction.UP ? SLOTS_UP : SLOTS_SIDES;
	}

	@Override
	public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction direction) {
		return this.canPlaceItem(slot, stack);
	}

	@Override
	public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
		return direction != Direction.DOWN || slot != 1 || stack.is(Items.BUCKET);
	}
}
