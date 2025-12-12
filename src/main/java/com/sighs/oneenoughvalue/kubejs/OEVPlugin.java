package com.sighs.oneenoughvalue.kubejs;

import com.sighs.oneenoughvalue.kubejs.events.OEVEvents;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.script.BindingsEvent;

public class OEVPlugin extends KubeJSPlugin {
    @Override
    public void registerEvents() {
        OEVEvents.GROUP.register();
    }

    @Override
    public void registerBindings(BindingsEvent event) {
        event.add("OEV$ItemValueManager", ItemValueManager.instance);
    }
/*
    todo:等哪天写了pjs兼容再使用
    @Override
    public void registerTypeWrappers(ScriptType type, TypeWrappers typeWrappers) {
        typeWrappers.registerSimple(RecipeType.class,o -> {
            if(o instanceof RecipeType){
                return (RecipeType) o;
            }
            return ForgeRegistries.RECIPE_TYPES.getValue(new ResourceLocation(o.toString()));
        });
    }
 */
}
