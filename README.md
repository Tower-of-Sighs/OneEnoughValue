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
    // 第一个参数为RecipeType，你可以使用字符串来代表
    // 也可以去loadClass获取RecipeType实例
    // 辅助方法，输出一个数组，包含所有已注册的配方类型。
    console.log(event.getAllRecipeType());
    
    event.addCustomRecipeHandler("crafting",
        // 获取输入物品，正常你应该不需要改
        event.defaultRecipeInputGetter,
        // 设置输出物品，多物品输出你可能需要重写这部分
        event.defaultRecipeOutputGetter,
        // 设置配方的额外价值，例如熔炉燃烧时间提供额外价值
        event.defaultRecipeExtraValueGetter,
        // 配方价值设置，单输出情况下你不需要管
        // 多物品输入你需要自行分配每个输出物品的价值，不然会只给第一个物品设置
        event.defaultRecipeValueSetter
    )
    
    event.addCustomRecipeHandler($RecipeType.CRAFTING,
        event.defaultRecipeInputGetter,
        event.defaultRecipeOutputGetter,
        event.defaultRecipeExtraValueGetter,
        (recipe, stacks, totalValue, setter) => {
            // 配方十倍价值
            setter.set(recipe, stacks.get(0), totalValue * 10);
        }
    )

});
```

### 后续计划

- 移植到其它版本