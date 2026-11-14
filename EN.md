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
});
```

Default values come from datapacks at `data/<namespace>/oev/values/*.json`, using a flat "item id or tag → value" mapping where a `#` prefix means a tag:

```json
{
  "minecraft:apple": 128,
  "#minecraft:logs": 32
}
```

A modpack can ship its own datapack to override the defaults: files are read in datapack priority order and a later key overwrites an earlier one. There is no delete syntax — overwrite a key with 0, or use the KubeJS method below.

If an item does not show a value, its value is 0. The methods above add a value of +10 (e.g., 0 becomes 10). These methods support negative numbers.

Item values are rounded down (e.g., 0.5 becomes 0).

If you need to interact with other mods and read values at any time, you can use the Value Manager:

```javascript
// Output the item's value when the player right-clicks the item.
ItemEvents.rightClicked(event => {
    event.player.tell(OEV$ItemValueManager.getValue(event.item));
});
```

### Extra Value

An extra value is `(id, function)` — the function takes an ItemStack and **returns the extra value**, where 0 means "does not match" — written in a **startup script** (`kubejs/startup_scripts/`):

```javascript
OEVEvents.addExtraItemValue(event => {
    // Fixed value: apples are worth 500000 extra.
    event.addExtraValue('apple', stack => stack.id === 'minecraft:apple' ? 500000 : 0);
    // Dynamic: Tacz's firearms, by NBT.
    event.addExtraValue('tacz_gun', stack => stack.nbt?.GunId === 'tacz:m700' ? 500000 : 0);
    // Dynamic: price per item, 100 each diamond.
    event.addExtraValue('diamond_stack', stack => stack.id === 'minecraft:diamond' ? stack.count * 100 : 0);
    // Dynamic: 50 per Sharpness level.
    event.addExtraValue('sharp', stack => (stack.getEnchantmentLevel('minecraft:sharpness') ?? 0) * 50);
});
```

The function may return any expression, so values can be scaled by stack count, NBT contents, enchantment level and so on; returning 0 means no match.

The id is what makes this manageable: registering the same id again overwrites, so re-running scripts (e.g. `/kubejs reload_startup_scripts`) never doubles a value. **Different** ids that both match one item have their values **added up**.

To add or remove entries while the game is running, use the global `OEV$ExtraValueHelper`:

```javascript
// Note: runtime changes do not sync — see the @Info warning on these methods.
OEV$ExtraValueHelper.add('apple', stack => stack.id === 'minecraft:apple' ? 500 : 0);
OEV$ExtraValueHelper.remove('apple');
OEV$ExtraValueHelper.getIds();
```

> These methods are marked **unstable**: the extra value table is not synced over the network, so a runtime change only affects the side that made it. In multiplayer you must keep both sides consistent yourself. For normal use, register in a startup script instead.

Startup scripts run on both the client and the server, so extra values agree on both sides without any syncing. Extra values are sealed during `FMLCommonSetupEvent`, and can still be added dynamically afterwards.

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

### Java API for Other Mods

If your mod has its own recipe types, register a handler on the Java side by listening to `OEVRegisterRecipeHandlersEvent`:

```java
import com.sighs.oneenoughvalue.api.event.OEVRegisterRecipeHandlersEvent;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeHandler;

@Mod.EventBusSubscriber(modid = "mymod")
public class MyModHooks {
    @SubscribeEvent
    public static void onRegister(OEVRegisterRecipeHandlersEvent event) {
        // The common single-output case
        event.register(MyRecipeTypes.CUSTOM, RecipeHandler.simple(recipe -> 0));
        // Or fully customise all four stages
        event.register(MyRecipeTypes.OTHER, RecipeHandler.create(
                IRecipeInputGetter.DEFAULT,
                myOutputGetter,
                myExtraValueGetter,
                IRecipeValueSetter.DEFAULT));
        // Just use the default summation rule
        event.registerSimple(RecipeType.SMELTING);
    }
}
```

Attach the listener to `MinecraftForge.EVENT_BUS`. The event is **fired again on every value recomputation**, so `/reload` is supported automatically; do not register once during mod construction, because that registration is cleared on the next reload.

### Future Plans

- Port to other versions.