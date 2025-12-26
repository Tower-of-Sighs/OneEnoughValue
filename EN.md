## One Enough Value

This mod provides an item value system similar to ProjectE, offering a convenient and highly extensible reference for item values.

Requires KubeJS as a prerequisite.

### Base Value

Image

By default, this mod provides base values for most basic materials.

Additionally, you can add or modify base values for items using the following methods:

```javascript
OEVEvents.addItemValue(event => {
    // Add base value.
    event.addBaseItemValue('minecraft:iron_nugget', 10);
    // Add extra value by matching NBT, such as Tacz's firearms.
    event.addExtraItemValue('{GunId:"tacz:m700"}', 500000);
});
```

If an item does not show a value, its value is 0. The methods above add a value of +10 (e.g., 0 becomes 10). These methods support negative numbers.

Item values are rounded down (e.g., 0.5 becomes 0).

If you need to interact with other mods and read values at any time, you can use the Value Manager:

```javascript
// Output the item's value when the player right-clicks the item.
ItemEvents.rightClicked(event => {
    event.player.tell(OEV$ItemValueManager.getValue(event.item));
});
```

### Processing Value

If an item has no base value, but there is a recipe that produces it, the item's value will default to the minimum sum of the ingredient values from the recipes.

For example, red dye can be crafted from a red tulip (value 16) or a beetroot (value 64), so the default value will be 16.

For recipes that produce more than one item, the value is automatically divided by the quantity. For instance, a gold ingot (default value 648) can craft 9 gold nuggets, each with a value of 72.

Vanilla recipes are set by default to use a simple summation method.

For new recipe types registered by other mods, you can set up a simple summation method using the following approach:

```javascript
let $RecipeType = Java.loadClass("net.minecraft.world.item.crafting.RecipeType");
OEVEvents.addRecipeHandler(event => {
    event.addSimpleRecipeHandler($RecipeType.BLASTING);
});
```

### Custom Processing

Here is a simple template to make the value of a crafted item ten times the sum of the ingredient values:

```javascript
let $RecipeType = Java.loadClass("net.minecraft.world.item.crafting.RecipeType");
OEVEvents.addRecipeHandler(event => {
    // Add a custom recipe handler for processing value, iterating through all recipes.
    event.addCustomRecipeHandler($RecipeType.CRAFTING, recipe => {
        let value = 0;
        // Iterate through each ingredient in the recipe.
        for (let ingredient of recipe.getIngredients()) {
            // Get the minimum value of a single ingredient. For example, for an ingredient matching a tag like coal (value 128) or charcoal (value 32), it takes 32 when crafting a torch.
            let ingredientMinValue = event.getMinIngredientValue(ingredient);
            // Handle cases where the ingredient is empty or has no corresponding value. Just copy this as is.
            if (ingredientMinValue === event.getMaxInteger()) continue;
            else if (ingredientMinValue === -1) return true;
            value += ingredientMinValue;
        }
        value *= 10;
        // Set the final value for a single recipe.
        return event.defaultSetRecipeValue(recipe, value);
    });
    // Helper method to output an array containing all registered recipe types.
    console.log(event.getAllRecipeType());
});
```

### Future Plans

- Port to other versions.