package com.sighs.oneenoughvalue.server;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sighs.oneenoughvalue.api.OEVLog;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
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

    /** 单例：注册与读取共用同一个实例，标签就绪后由 ItemValueManager 取结果。 */
    public static final BaseValueReloadListener INSTANCE = new BaseValueReloadListener();

    public record Entry(String key, int value) {
    }

    /** 最近一次资源重载解析出的条目，供 TagsUpdatedEvent 阶段注册。 */
    private volatile List<Entry> entries = new ObjectArrayList<>();

    @Override
    protected List<Entry> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        entries.clear();
        // listPacks() 的顺序即数据包优先级，低优先级在前，保证后写覆盖
        resourceManager.listPacks().forEach(pack -> readPack(pack, entries));
        OEVLog.LOGGER.info("从数据包读取基础价值 {} 条", entries.size());
        return entries;
    }

    /**
     * 只保存解析结果，不当场注册：apply 还在资源重载阶段，此时标签尚未绑定，
     * 在这里展开 #标签 会拿到空集合，导致所有标签价值被静默丢弃。
     * 真正的注册在 ItemValueManager.onReload（TagsUpdatedEvent）里做。
     */
    @Override
    protected void apply(List<Entry> parsed, ResourceManager resourceManager, ProfilerFiller profiler) {
        entries = parsed;
    }

    public List<Entry> getEntries() {
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
                    OEVLog.LOGGER.error("读取基础价值数据包文件失败: {} (来自 {})",
                            location, pack.packId(), e);
                }
            });
        }
    }

    private static void readFile(Identifier location, BufferedReader reader, List<Entry> entries) {
        JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
        json.asMap().forEach((key, element) -> {
            try {
                entries.add(new Entry(key, element.getAsInt()));
            } catch (RuntimeException e) {
                OEVLog.LOGGER.error("{} 中 {} 的数值不是整数: {}", location, key, element);
            }
        });
    }
}
