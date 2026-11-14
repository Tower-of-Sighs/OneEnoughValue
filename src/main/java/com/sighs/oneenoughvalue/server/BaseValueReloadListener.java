package com.sighs.oneenoughvalue.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sighs.oneenoughvalue.OneEnoughValue;
import com.sighs.oneenoughvalue.manager.ItemValueManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.profiling.ProfilerFiller;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * 基础价值的数据包来源：读取 data/&lt;namespace&gt;/oev/values/*.json。<br>
 * 文件格式与内置默认一致（扁平映射，# 前缀表示标签）。
 * <p>
 * 用 {@code listPacks()} 而不是 {@code listResources()}：前者按数据包优先级依次返回，
 * 后者不保证顺序。同一 key 后读到的覆盖先读到的，低优先级数据包因此能被高优先级覆盖。
 */
public class BaseValueReloadListener extends SimplePreparableReloadListener<List<BaseValueReloadListener.Entry>> {
    public static final String DIRECTORY = "oev/values";
    public static final String SUFFIX = ".json";

    public record Entry(String key, int value) {
    }

    @Override
    protected List<Entry> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        List<Entry> entries = new ArrayList<>();
        // listPacks() 的顺序即数据包优先级，低优先级在前，保证后写覆盖
        resourceManager.listPacks().forEach(pack -> readPack(pack, entries));
        OneEnoughValue.LOGGER.info("从数据包读取基础价值 {} 条", entries.size());
        return entries;
    }

    private static void readPack(PackResources pack, List<Entry> entries) {
        for (String namespace : pack.getNamespaces(PackType.SERVER_DATA)) {
            pack.listResources(PackType.SERVER_DATA, namespace, DIRECTORY, (location, supplier) -> {
                if (!location.getPath().endsWith(SUFFIX)) {
                    return;
                }
                try (InputStream stream = supplier.get();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                    readFile(location, reader, entries);
                } catch (Exception e) {
                    OneEnoughValue.LOGGER.error("读取基础价值数据包文件失败: {} (来自 {})",
                            location, pack.packId(), e);
                }
            });
        }
    }

    private static void readFile(ResourceLocation location, BufferedReader reader, List<Entry> entries) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        json.asMap().forEach((key, element) -> {
            try {
                entries.add(new Entry(key, element.getAsInt()));
            } catch (RuntimeException e) {
                OneEnoughValue.LOGGER.error("{} 中 {} 的数值不是整数: {}", location, key, element);
            }
        });
    }

    @Override
    protected void apply(List<Entry> entries, ResourceManager resourceManager, ProfilerFiller profiler) {
        for (Entry entry : entries) {
            String key = entry.key();
            if (key.startsWith("#")) {
                ItemValueManager.instance.registerValue(
                        ItemTags.create(new ResourceLocation(key.substring(1))), entry.value());
            } else {
                ItemValueManager.instance.registerValue(key, entry.value());
            }
        }
    }
}
