package cc.sighs.oneenoughvalue.manager;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import cc.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import cc.sighs.oneenoughvalue.kubejs.events.OEVInitRecipeHandleEventJS;
import cc.sighs.oneenoughvalue.kubejs.events.OEVInitValueEventJS;
import cc.sighs.oneenoughvalue.server.recipe.RecipeHandlerManager;
import dev.latvian.mods.kubejs.typings.Info;
import dev.latvian.mods.rhino.util.HideFromJS;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPredicate;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.HashMap;
import java.util.Map;

public class ItemValueManager {
    public static ItemValueManager instance = new ItemValueManager();
    public Map<ResourceLocation, Integer> recipesGenValue = new HashMap<>();
    public Map<ResourceLocation, Integer> baseValueMap = new HashMap<>();
    public Map<DataComponentPredicate, Integer> extraValueMap = new HashMap<>();

    public void init() {
        //todo 增加更多预设价值（可能？
        JsonObject defValue;
        {
            defValue = JsonParser.parseString("""
                          {
                          "#forge:crops/beetroot": 64,
                          "#forge:crops/carrot": 64,
                          "#forge:crops/nether_wart": 24,
                          "#forge:crops/potato": 64,
                          "#forge:crops/wheat": 24,
                          "#forge:dusts/glowstone": 384,
                          "#forge:dusts/redstone": 64,
                          "#forge:gems/diamond": 8192,
                          "#forge:gems/emerald": 16384,
                          "#forge:gems/quartz": 256,
                          "#forge:nether_stars": 139264,
                          "#forge:rods/wooden": 4,
                          "#forge:seeds/beetroot": 16,
                          "#forge:seeds/wheat": 16,
                          "#minecraft:decorated_pot_sherds": 216,
                          "#minecraft:leaves": 1,
                          "#minecraft:logs": 32,
                          "#minecraft:music_discs": 2048,
                          "#minecraft:planks": 8,
                          "#minecraft:saplings": 32,
                          "#minecraft:small_flowers": 16,
                          "#minecraft:tall_flowers": 32,
                          "#minecraft:wool": 48,
                          "#forge:gems/amethyst": 32,
                          "#forge:gems/peridot": 2048,
                          "#forge:gems/ruby": 2048,
                          "#forge:gems/sapphire": 2048,
                          "#forge:ingots/iron": 256,
                          "#forge:ingots/uranium": 4096,
                          "minecraft:andesite": 16,
                          "minecraft:apple": 128,
                          "minecraft:bamboo": 32,
                          "minecraft:basalt": 4,
                          "minecraft:beef": 64,
                          "minecraft:big_dripleaf": 32,
                          "minecraft:blackstone": 4,
                          "minecraft:blaze_rod": 1536,
                          "minecraft:bone": 144,
                          "minecraft:brain_coral": 16,
                          "minecraft:brain_coral_block": 64,
                          "minecraft:brain_coral_fan": 16,
                          "minecraft:brown_mushroom": 32,
                          "minecraft:bubble_coral": 16,
                          "minecraft:bubble_coral_block": 64,
                          "minecraft:bubble_coral_fan": 16,
                          "minecraft:cactus": 8,
                          "minecraft:calcite": 32,
                          "minecraft:chicken": 64,
                          "minecraft:chorus_flower": 96,
                          "minecraft:chorus_fruit": 192,
                          "minecraft:chorus_plant": 64,
                          "minecraft:clay_ball": 16,
                          "minecraft:coal": 128,
                          "minecraft:coast_armor_trim_smithing_template": 12271,
                          "minecraft:cobbled_deepslate": 2,
                          "minecraft:cobblestone": 1,
                          "minecraft:cobweb": 12,
                          "minecraft:cocoa_beans": 64,
                          "minecraft:cod": 64,
                          "minecraft:creeper_head": 256,
                          "minecraft:crimson_fungus": 32,
                          "minecraft:crimson_roots": 1,
                          "minecraft:crying_obsidian": 768,
                          "minecraft:dead_brain_coral": 1,
                          "minecraft:dead_brain_coral_block": 4,
                          "minecraft:dead_brain_coral_fan": 1,
                          "minecraft:dead_bubble_coral": 1,
                          "minecraft:dead_bubble_coral_block": 4,
                          "minecraft:dead_bubble_coral_fan": 1,
                          "minecraft:dead_bush": 1,
                          "minecraft:dead_fire_coral": 1,
                          "minecraft:dead_fire_coral_block": 4,
                          "minecraft:dead_fire_coral_fan": 1,
                          "minecraft:dead_horn_coral": 1,
                          "minecraft:dead_horn_coral_block": 4,
                          "minecraft:dead_horn_coral_fan": 1,
                          "minecraft:dead_tube_coral": 1,
                          "minecraft:dead_tube_coral_block": 4,
                          "minecraft:dead_tube_coral_fan": 1,
                          "minecraft:diorite": 16,
                          "minecraft:dirt": 1,
                          "minecraft:dragon_egg": 262144,
                          "minecraft:dune_armor_trim_smithing_template": 53898,
                          "minecraft:echo_shard": 192,
                          "minecraft:egg": 32,
                          "minecraft:end_stone": 1,
                          "minecraft:ender_pearl": 1024,
                          "minecraft:eye_armor_trim_smithing_template": 23017,
                          "minecraft:feather": 48,
                          "minecraft:fern": 1,
                          "minecraft:filled_map": 1472,
                          "minecraft:fire_coral": 16,
                          "minecraft:fire_coral_block": 64,
                          "minecraft:fire_coral_fan": 16,
                          "minecraft:flint": 4,
                          "minecraft:ghast_tear": 4096,
                          "minecraft:glow_berries": 16,
                          "minecraft:glow_lichen": 8,
                          "minecraft:granite": 16,
                          "minecraft:grass": 1,
                          "minecraft:gravel": 4,
                          "minecraft:gunpowder": 192,
                          "minecraft:heart_of_the_sea": 32768,
                          "minecraft:honey_bottle": 48,
                          "minecraft:horn_coral": 16,
                          "minecraft:horn_coral_block": 64,
                          "minecraft:horn_coral_fan": 16,
                          "minecraft:host_armor_trim_smithing_template": 10176,
                          "minecraft:ice": 1,
                          "minecraft:ink_sac": 16,
                          "minecraft:kelp": 1,
                          "minecraft:lapis_lazuli": 864,
                          "minecraft:large_fern": 1,
                          "minecraft:lily_pad": 16,
                          "minecraft:magma_block": 128,
                          "minecraft:mangrove_roots": 4,
                          "minecraft:melon_slice": 16,
                          "minecraft:moss_block": 12,
                          "minecraft:mutton": 64,
                          "minecraft:name_tag": 192,
                          "minecraft:nautilus_shell": 1024,
                          "minecraft:nether_sprouts": 1,
                          "minecraft:netherite_scrap": 12288,
                          "minecraft:netherite_upgrade_smithing_template": 7497,
                          "minecraft:netherrack": 1,
                          "minecraft:obsidian": 64,
                          "minecraft:phantom_membrane": 192,
                          "minecraft:piglin_banner_pattern": 512,
                          "minecraft:piglin_head": 256,
                          "minecraft:pink_petals": 4,
                          "minecraft:pointed_dripstone": 16,
                          "minecraft:poisonous_potato": 64,
                          "minecraft:porkchop": 64,
                          "minecraft:prismarine_crystals": 512,
                          "minecraft:prismarine_shard": 256,
                          "minecraft:pufferfish": 64,
                          "minecraft:pumpkin": 144,
                          "minecraft:rabbit": 64,
                          "minecraft:rabbit_foot": 128,
                          "minecraft:rabbit_hide": 16,
                          "minecraft:raiser_armor_trim_smithing_template": 10176,
                          "minecraft:red_mushroom": 32,
                          "minecraft:red_sand": 1,
                          "minecraft:rib_armor_trim_smithing_template": 10310,
                          "minecraft:rotten_flesh": 32,
                          "minecraft:saddle": 192,
                          "minecraft:salmon": 64,
                          "minecraft:sand": 1,
                          "minecraft:sculk_catalyst": 8040,
                          "minecraft:sculk_vein": 4,
                          "minecraft:scute": 96,
                          "minecraft:sea_pickle": 16,
                          "minecraft:seagrass": 1,
                          "minecraft:sentry_armor_trim_smithing_template": 57345,
                          "minecraft:shaper_armor_trim_smithing_template": 10176,
                          "minecraft:shulker_shell": 2048,
                          "minecraft:silence_armor_trim_smithing_template": 39465,
                          "minecraft:skeleton_skull": 256,
                          "minecraft:slime_ball": 32,
                          "minecraft:small_dripleaf": 24,
                          "minecraft:sniffer_egg": 2048,
                          "minecraft:snout_armor_trim_smithing_template": 7533,
                          "minecraft:snow": 1,
                          "minecraft:snowball": 1,
                          "minecraft:soul_sand": 49,
                          "minecraft:spider_eye": 128,
                          "minecraft:spire_armor_trim_smithing_template": 18588,
                          "minecraft:sponge": 128,
                          "minecraft:spore_blossom": 64,
                          "minecraft:string": 12,
                          "minecraft:sugar_cane": 32,
                          "minecraft:sweet_berries": 16,
                          "minecraft:tall_grass": 1,
                          "minecraft:tide_armor_trim_smithing_template": 22116,
                          "minecraft:trident": 16398,
                          "minecraft:tropical_fish": 64,
                          "minecraft:tube_coral": 16,
                          "minecraft:tube_coral_block": 64,
                          "minecraft:tube_coral_fan": 16,
                          "minecraft:tuff": 4,
                          "minecraft:turtle_egg": 192,
                          "minecraft:twisting_vines": 8,
                          "minecraft:vex_armor_trim_smithing_template": 51917,
                          "minecraft:vine": 8,
                          "minecraft:ward_armor_trim_smithing_template": 19677,
                          "minecraft:warped_fungus": 32,
                          "minecraft:warped_roots": 1,
                          "minecraft:wayfinder_armor_trim_smithing_template": 10176,
                          "minecraft:weeping_vines": 8,
                          "minecraft:wild_armor_trim_smithing_template": 42641,
                          "minecraft:zombie_head": 256,
                          "minecraft:gold_ingot": 648,
                          "minecraft:copper_ingot":64
                          }
                    """).getAsJsonObject();
        }
        defValue.asMap().forEach((string, jsonElement) -> {
            if (string.startsWith("#")) {
                registerValue(ItemTags.create(ResourceLocation.parse(string.substring(1))), jsonElement.getAsInt());
            } else {
                registerValue(string, jsonElement.getAsInt());
            }
        });
    }

    public void onReload(RecipeManager recipeManager, RegistryAccess registryAccess) {
        baseValueMap.clear();
        init();
        OEVEvents.ADD_VALUE.post(new OEVInitValueEventJS(this));

        OEVInitRecipeHandleEventJS.modifiers.clear();
        RecipeHandlerManager.INSTANCE.parseAllRecipes(recipeManager, registryAccess);

        Map<ResourceLocation, Integer> result = new HashMap<>();
        /*
        先放人配方生成价值，再加入基础价值，保证基础价值优先级高于配方生成
         */
        result.putAll(recipesGenValue);
        result.putAll(baseValueMap);
        baseValueMap = result;
        recipesGenValue.clear();
    }

    //如果放入了新值则返回true,用于检测配方是否完全处理
    public boolean computeRecipeValue(String type, ItemStack stack, int value) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        value /= stack.getCount();
        for (OEVInitRecipeHandleEventJS.IRecipeModify modifier : OEVInitRecipeHandleEventJS.modifiers) {
            value = modifier.apply(type, value);
        }
        Integer oldValue = recipesGenValue.get(key);
        if (oldValue == null) {
            recipesGenValue.put(key, value);
            return true;
        }
        if (value < 0) {
            recipesGenValue.put(key, 0);
            return true;
        }
        if (value < oldValue) {
            recipesGenValue.put(key, value);
            return true;
        }
        return false;
    }

    @Info("获取一个物品的价值（基础价值+额外价值）")
    public int getValue(ItemStack itemStack) {
        return getBaseValue(itemStack) + getExtraValue(itemStack);
    }

    @HideFromJS//js别看这个，笨牛会找不到方法
    public int getBaseValue(ItemStack itemStack) {
        return getBaseValue(itemStack.getItem());
    }

    @Info("获取一个物品的基础价值")
    public int getBaseValue(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return baseValueMap.getOrDefault(key, -1);
    }

    @Info("获取一个物品的额外价值")
    public int getExtraValue(ItemStack itemStack) {
        int value = 0;
        for (Map.Entry<DataComponentPredicate, Integer> entry : extraValueMap.entrySet()) {
            if (entry.getKey().test(itemStack)) {
                value += entry.getValue();
            }
        }
        return value;
    }

    @HideFromJS
    public void registerValue(ResourceLocation key, Integer value) {
        if (BuiltInRegistries.ITEM.containsKey(key)) {
            baseValueMap.put(key, value);
        }
    }

    @HideFromJS
    public void registerValue(String key, Integer value) {
        ResourceLocation location = ResourceLocation.parse(key);
        if (BuiltInRegistries.ITEM.containsKey(location)) {
            baseValueMap.put(location, value);
        }
    }

    public void registerValue(Item item, Integer value) {
        registerValue(BuiltInRegistries.ITEM.getKey(item), value);
    }

    public void registerValue(TagKey<Item> tag, Integer value) {
        for (Holder<Item> holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            registerValue(holder.value(), value);
        }
    }


    public void registerExtraValue(DataComponentPredicate dataComponentPredicate, Integer value) {
        extraValueMap.put(dataComponentPredicate, value);
    }

    public void registerExtraValue(DataComponentMap dataComponentMap, Integer value) {
        registerExtraValue(DataComponentPredicate.allOf(dataComponentMap), value);
    }

    public void registerExtraValue(ItemStack stack, Integer value) {
        registerExtraValue(stack.getComponents(), value);
    }
}
