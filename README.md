## One Enough Value

本模组提供了与等价交换相似的物品价值系统，用于提供便捷而拓展性强的物品价值参考。

需要前置KubeJS。

### 基础价值

图片

本模组默认为绝大多数基础材料提供了基础价值。

除此之外，你也可以用以下方法为物品添加或修改基础价值：
```javascript
OEVEvents.addItemValue(event => {
    // 增加基础价值。
    event.addBaseItemValue('minecraft:iron_nugget', 10);
    // 通过匹配nbt增加额外价值，如tacz的枪械。
    event.addExtraItemValue('{GunId:"tacz:m700"}', 500000);
});
```
如果一个物品没有显示价值，那么它的价值就为0，上面的方法都是将价值+10。该方法支持负数。

物品价值会向下取整，如0.5会向下取整为0。

如果你需要与其它模组进行联动，随时读取价值，可以使用价值管理器：
```javascript
// 玩家右键物品时输出物品的价值。
ItemEvents.rightClicked(event => {
    event.player.tell(OEV$ItemValueManager.getValue(event.item));
});
```

### 加工价值

如果一个物品没有基础价值，存在以它为产物的配方，那么该物品的价值就会默认取配方原料价值之和的最小值。

例如红色染料可以由价值16的红色郁金香合成，也可以由价值64的甜菜根合成，那么默认价值就会取16。

对于产物数量不为1的配方，价值也会自动除以数量，如默认情况下价值648的金锭可以合成9个价值72的金粒。

原版的配方都默认设置了最简单的求和处理。

对于其它模组注册的新配方类型，可以用以下方法设置最简单的求和处理：

```javascript
let $RecipeType = Java.loadClass("net.minecraft.world.item.crafting.RecipeType");
OEVEvents.addRecipeHandler(event => {
    event.addSimpleRecipeHandler($RecipeType.BLASTING);
});
```

### 自定义加工

以下是一个简单的模板，可以让合成得到的产物价值是原料价值之和的十倍。
```javascript
let $RecipeType = Java.loadClass("net.minecraft.world.item.crafting.RecipeType");
OEVEvents.addRecipeHandler(event => {
    // 添加自定义配方加工价值处理器，遍历所有配方。
    event.addCustomRecipeHandler($RecipeType.CRAFTING, recipe => {
        let value = 0;
        // 遍历配方中的各项原料。
        for (let ingredient of recipe.getIngredients()) {
            // 获取单个原料的最小价值，如对于标签匹配的原料，如煤炭128，木炭32，制作火把时取32.
            let ingredientMinValue = event.getMinIngredientValue(ingredient);
            // 处理原料为空和原料包含物品没有对应价值的情况，照抄即可。
            if (ingredientMinValue === event.getMaxInteger()) continue;
            else if (ingredientMinValue === -1) return true;
            value += ingredientMinValue;
        }
        value *= 10;
        // 设置单条配方的最终价值价值。
        return event.defaultSetRecipeValue(recipe, value);
    });
    // 辅助方法，输出一个数组，包含所有已注册的配方类型。
    console.log(event.getAllRecipeType());
});
```

### 后续计划

- 移植到其它版本