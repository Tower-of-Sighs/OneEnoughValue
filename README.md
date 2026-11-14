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
});
```
默认价值来自数据包 `data/<命名空间>/oev/values/*.json`，格式是扁平的「物品 id 或标签 → 价值」映射，`#` 前缀表示标签：

```json
{
  "minecraft:apple": 128,
  "#minecraft:logs": 32
}
```

整合包可以放自己的数据包来覆盖默认值：多个文件按数据包优先级依次读取，同一个 key 后读到的覆盖先读到的。没有删除语法，要清掉某条就覆盖成 0，或用下面的 KubeJS 方法。

如果一个物品没有显示价值，那么它的价值就为0，上面的方法都是将价值+10。该方法支持负数。

物品价值会向下取整，如0.5会向下取整为0。

如果你需要与其它模组进行联动，随时读取价值，可以使用价值管理器：
```javascript
// 玩家右键物品时输出物品的价值。
ItemEvents.rightClicked(event => {
    event.player.tell(OEV$ItemValueManager.getValue(event.item));
});
```

### 额外价值

额外价值是 `(标识符, 计算函数)` 两段式，写在 **startup 脚本**里（`kubejs/startup_scripts/`）。计算函数接收 ItemStack 并**返回额外价值**，返回 0 表示不匹配：

```javascript
OEVEvents.addExtraItemValue(event => {
    // 固定值：匹配苹果给 500000。
    event.addExtraValue('apple', stack => stack.id === 'minecraft:apple' ? 500000 : 0);
    // 动态：按 NBT 判断，tacz 的枪械。
    event.addExtraValue('tacz_gun', stack => stack.nbt?.GunId === 'tacz:m700' ? 500000 : 0);
    // 动态：按数量计价，每颗钻石 100。
    event.addExtraValue('diamond_stack', stack => stack.id === 'minecraft:diamond' ? stack.count * 100 : 0);
    // 动态：按附魔等级计价，每级 50。
    event.addExtraValue('sharp', stack => (stack.getEnchantmentLevel('minecraft:sharpness') ?? 0) * 50);
});
```

返回值可以是任意表达式，所以能做按数量、按 NBT、按附魔等级等动态定价；返回 0 即视为不匹配。

标识符用来做管理：同一个 id 重复注册等于覆盖，因此 `/kubejs reload_startup_scripts` 之类重跑脚本的场景不会让价值翻倍。**不同** id 命中了同一个物品时，额外价值**累加**。

如果需要在游戏运行期间增删，用全局的 `OEV$ExtraValueHelper`：

```javascript
// 注意：运行时改动不会同步，多人游戏会导致双端不同步，见方法上的 @Info 提示
OEV$ExtraValueHelper.add('apple', stack => stack.id === 'minecraft:apple' ? 500 : 0);
OEV$ExtraValueHelper.remove('apple');
OEV$ExtraValueHelper.getIds();
```

> 这些方法标记为**不稳定**：额外价值表不做网络同步，运行时只会在调用所在的一侧生效，多人游戏里需要自行保证双端一致。正常玩法建议写在 startup 脚本里。

startup 脚本在客户端与服务端都会执行，所以额外价值两端天然一致，不需要同步。额外价值会在 `FMLCommonSetupEvent` 定型，之后仍然可以动态追加。

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

### 给其它模组用的 Java API

如果你的模组有自己的配方类型，可以在 Java 侧监听 `OEVRegisterRecipeHandlersEvent` 注册处理器：

```java
import com.sighs.oneenoughvalue.api.event.OEVRegisterRecipeHandlersEvent;
import com.sighs.oneenoughvalue.server.recipe.api.RecipeHandler;

@Mod.EventBusSubscriber(modid = "mymod")
public class MyModHooks {
    @SubscribeEvent
    public static void onRegister(OEVRegisterRecipeHandlersEvent event) {
        // 单输出配方最常见的写法
        event.register(MyRecipeTypes.CUSTOM, RecipeHandler.simple(recipe -> 0));
        // 或者完整自定义四个环节
        event.register(MyRecipeTypes.OTHER, RecipeHandler.create(
                IRecipeInputGetter.DEFAULT,
                myOutputGetter,
                myExtraValueGetter,
                IRecipeValueSetter.DEFAULT));
        // 只是想按默认求和规则处理
        event.registerSimple(RecipeType.SMELTING);
    }
}
```

监听器挂在 `MinecraftForge.EVENT_BUS` 上即可。**每轮价值重算都会重新触发该事件**，所以自动支持 `/reload`；不要改在模组构造期做一次性注册，那会在下一次 reload 被清掉。

### 后续计划

- 移植到其它版本