package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;
import dev.latvian.mods.kubejs.script.ScriptType;
import dev.latvian.mods.rhino.util.wrap.TypeWrappers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;

public class OEVPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        OEVEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("OEV$ItemValueManager", ItemValueManager.instance);
    }

    //todo:等待写pjs兼容
    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(RecipeType.class, o -> {
            if(o instanceof RecipeType){
                return (RecipeType) o;
            }
            return ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation(o.toString()));
        });
    }

}
