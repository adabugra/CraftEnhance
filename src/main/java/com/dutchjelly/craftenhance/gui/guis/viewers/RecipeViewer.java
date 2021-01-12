package com.dutchjelly.craftenhance.gui.guis.viewers;

import com.dutchjelly.craftenhance.crafthandling.recipes.EnhancedRecipe;
import com.dutchjelly.craftenhance.exceptions.ConfigError;
import com.dutchjelly.craftenhance.crafthandling.recipes.WBRecipe;
import com.dutchjelly.craftenhance.gui.GuiManager;
import com.dutchjelly.craftenhance.gui.guis.GUIElement;
import com.dutchjelly.craftenhance.gui.templates.GuiTemplate;
import com.dutchjelly.craftenhance.gui.util.GuiUtil;
import com.dutchjelly.craftenhance.gui.util.InfoItemPlaceHolders;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RecipeViewer<RecipeT extends EnhancedRecipe> extends GUIElement {
    private Inventory inventory;

    @Getter
    private RecipeT recipe;

    public RecipeViewer(GuiManager manager, GuiTemplate template, GUIElement previous, Player p, RecipeT recipe) {
        super(manager, template, previous, p);
        this.recipe = recipe;

        inventory = GuiUtil.CopyInventory(getTemplate().getTemplate(), getTemplate().getInvTitle(), this);
        updateRecipeDisplay();
        updatePlaceHolders();
    }

    private void updateRecipeDisplay(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        if (fillSpace.size() != recipe.getContent().length + 1)
            throw new ConfigError("fill space of recipe viewer must be " + (recipe.getContent().length + 1));
        for (int i = 0; i < recipe.getContent().length; i++) {
            if (fillSpace.get(i) >= inventory.getSize())
                throw new ConfigError("fill space spot " + fillSpace.get(i) + " is outside of inventory");
            inventory.setItem(fillSpace.get(i), recipe.getContent()[i]);
        }
        if (fillSpace.get(9) >= inventory.getSize())
            throw new ConfigError("fill space spot " + fillSpace.get(recipe.getContent().length) + " is outside of inventory");
        inventory.setItem(fillSpace.get(recipe.getContent().length), recipe.getResult());
    }

    protected abstract Map<String,String> getPlaceHolders();

    private void updatePlaceHolders(){
        List<Integer> fillSpace = getTemplate().getFillSpace();
        ItemStack[] template = getTemplate().getTemplate();
        Map<String, String> placeHolders = new HashMap<String,String>(){{
            put(InfoItemPlaceHolders.Key.getPlaceHolder(), recipe.getKey() == null ? "null" : recipe.getKey());
            put(InfoItemPlaceHolders.MatchMeta.getPlaceHolder(), recipe.isMatchMeta() ? "match meta" : "only match type");
            put(InfoItemPlaceHolders.Permission.getPlaceHolder(), recipe.getPermissions() == null ? "null" : recipe.getPermissions());
        }};

        for(int i = 0; i < template.length; i++){
            if(fillSpace.contains(i)) continue;
            if(template[i] == null) continue;
            inventory.setItem(i, GuiUtil.ReplaceAllPlaceHolders(template[i].clone(), placeHolders));
        }
    }

    @Override
    public Inventory getInventory() {
        return inventory;
    }

    @Override
    public void handleEventRest(InventoryClickEvent e) {
        int clickedSlot = e.getSlot();
        if (getTemplate().getFillSpace().contains(clickedSlot)) {
            int translatedSlot = -1;
            for (int i = 0; i < getTemplate().getFillSpace().size(); i++) {
                if (getTemplate().getFillSpace().get(i) == clickedSlot) {
                    translatedSlot = i;
                    break;
                }
            }
            if (translatedSlot == -1) return;

            EnhancedRecipe clickedItemRecipe = getManager().getMain().getFm().getRecipes().stream().filter(x -> x.getResult().equals(e.getCurrentItem())).findFirst().orElse(null);
            if (clickedItemRecipe == null || clickedItemRecipe.equals(recipe)) return;

            if (clickedItemRecipe instanceof WBRecipe)
                getManager().openGUI(getPlayer(), new WBRecipeViewer(getManager(), getTemplate(), this, getPlayer(), (WBRecipe) clickedItemRecipe));
        }
    }

    @Override
    public boolean isCancelResponsible() {
        return false;
    }
}