package cc.sighs.oneenoughvalue.kubejs;

import cc.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import cc.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.event.EventGroupRegistry;
import dev.latvian.mods.kubejs.plugin.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingRegistry;
import dev.latvian.mods.kubejs.script.TypeWrapperRegistry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;

public class OEVPlugin implements KubeJSPlugin {
    @Override
    public void registerEvents(EventGroupRegistry registry) {
        registry.register(OEVEvents.GROUP);
    }

    @Override
    public void registerBindings(BindingRegistry bindings) {
        bindings.add("OEV$ItemValueManager", ItemValueManager.instance);
    }

    //todo:等待写pjs兼容
    @Override
    public void registerTypeWrappers(TypeWrapperRegistry registry) {
        registry.register(RecipeType.class, o -> {
            if (o instanceof RecipeType) {
                return (RecipeType) o;
            }
            return BuiltInRegistries.RECIPE_TYPE.get(ResourceLocation.parse(o.toString()));
        });
    }

}
