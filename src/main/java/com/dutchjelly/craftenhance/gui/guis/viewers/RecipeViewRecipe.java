package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.FurnaceRecipe;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.files.CategoryData;
import com.dutchjelly.craftenhance.files.MenuSettingsCache;
import com.dutchjelly.craftenhance.gui.guis.RecipesViewer;
import com.dutchjelly.craftenhance.gui.templates.MenuTemplate;
import com.dutchjelly.craftenhance.gui.util.ButtonType;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import org.broken.arrow.menu.library.button.MenuButton;
import org.broken.arrow.menu.library.button.logic.ButtonUpdateAction;
import org.broken.arrow.menu.library.button.logic.FillMenuButton;
import org.broken.arrow.menu.library.holder.MenuHolderPage;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static com.dutchjelly.craftenhance.CraftEnhance.self;
import static com.dutchjelly.craftenhance.gui.util.FormatListContents.formatRecipes;

public class RecipeViewRecipe<RecipeT extends EnhancedRecipe> extends MenuHolderPage<ItemStack> {
	private final MenuSettingsCache menuSettingsCache = self().getMenuSettingsCache();
	private final MenuTemplate menuTemplate;
	private final CategoryData categoryData;
	private final RecipeT recipe;

	public RecipeViewRecipe(final CategoryData categoryData, final RecipeT recipe, final String menuType) {
		super(formatRecipes(recipe, null, false));
		this.recipe = recipe;
		this.categoryData = categoryData;
		this.menuTemplate = menuSettingsCache.getTemplates().get(menuType);
		setFillSpace(this.menuTemplate.getFillSlots());
		setTitle(this.menuTemplate.getMenuTitel());
		setMenuSize(27);
		this.setUseColorConversion(true);
	}

	@Override
	public MenuButton getButtonAt(final int slot) {
		if (this.menuTemplate == null) return null;
		for (final Entry<List<Integer>, com.dutchjelly.craftenhance.gui.templates.MenuButton> menuTemplate : this.menuTemplate.getMenuButtons().entrySet()) {
			if (menuTemplate.getKey().contains(slot)) {
				return registerButtons(menuTemplate.getValue());
			}
		}
		return null;
	}


	private MenuButton registerButtons(final com.dutchjelly.craftenhance.gui.templates.MenuButton value) {
		return new MenuButton() {
			@Override
			public void onClickInsideMenu(@Nonnull final Player player, @Nonnull final Inventory menu, @Nonnull final ClickType click, @Nonnull final ItemStack clickedItem) {
				if (run(value, menu, player, click))
					updateButtons();
			}

			@Override
			public ItemStack getItem() {
				final Map<String, String> placeHolders = new HashMap<String, String>() {{
					if (recipe instanceof WBRecipe)
						put(InfoItemPlaceHolders.Shaped.getPlaceHolder(), ((WBRecipe) recipe).isShapeless() ? "shapeless" : "shaped");
					if (recipe instanceof FurnaceRecipe) {
						put(InfoItemPlaceHolders.Exp.getPlaceHolder(), String.valueOf(((FurnaceRecipe) recipe).getExp()));
						put(InfoItemPlaceHolders.Duration.getPlaceHolder(), String.valueOf(((FurnaceRecipe) recipe).getDuration()));
					}
					put(InfoItemPlaceHolders.Key.getPlaceHolder(), recipe.getKey() == null ? "null" : recipe.getKey());
					put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), recipe.getMatchType().getDescription());
					put(InfoItemPlaceHolders.MatchType.getPlaceHolder(), recipe.getMatchType().getDescription());
					put(InfoItemPlaceHolders.Permission.getPlaceHolder(), recipe.getPermissions() == null ? "null" : recipe.getPermissions());
					put(InfoItemPlaceHolders.Worlds.getPlaceHolder(), recipe.getAllowedWorlds() == null || recipe.getAllowedWorlds().isEmpty() ? "all worlds" : recipe.getAllowedWorldsFormatted());
				}};
				return GuiUtil.ReplaceAllPlaceHolders(value.getItemStack().clone(), placeHolders);
			}
		};
	}

	public boolean run(final com.dutchjelly.craftenhance.gui.templates.MenuButton value, final Inventory menu, final Player player, final ClickType click) {
		if (value.getButtonType() == ButtonType.Back) {
			new RecipesViewer(categoryData, "", player).menuOpen(player);
		}
		return false;
	}

	@Override
	public FillMenuButton<ItemStack> createFillMenuButton() {
		return new FillMenuButton<>((player1, itemStacks, clickType, itemStack, recipeT) -> ButtonUpdateAction.NONE,
				(integer, itemStack) -> itemStack);
	}
}
