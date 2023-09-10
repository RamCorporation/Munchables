package net.ramgames.munchables;

import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.registry.Registry;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReloader;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Pair;
import net.minecraft.util.profiler.Profiler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Triplet;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;

public class ModResourceLoader implements SimpleResourceReloadListener<ModResourceLoader.ResourceLoader>{
    public static final Logger LOGGER = LoggerFactory.getLogger("Munchables API");
    private final String path;
    private final BiFunction<Identifier, JsonObject, Triplet<Boolean, String, Integer>> codec;

    public ModResourceLoader(String path, BiFunction<Identifier, JsonObject, Triplet<Boolean, String, Integer>> codec) {
        this.path = path;
        this.codec = codec;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier("munchables", "api_" + path + "_loader");
    }

    @Override
    public CompletableFuture<ResourceLoader> load(ResourceManager manager, Profiler profiler, Executor executor) {
        return CompletableFuture.supplyAsync(() -> new ResourceLoader(manager, profiler, path, codec), executor);
    }

    @Override
    public CompletableFuture<Void> apply(ResourceLoader data, ResourceManager manager, Profiler profiler, Executor executor) {
        data.getModules().forEach(Munchables::addEatTime);
        return CompletableFuture.runAsync(() -> {
        });
    }

    @Override
    public CompletableFuture<Void> reload(ResourceReloader.Synchronizer helper, ResourceManager manager, Profiler loadProfiler, Profiler applyProfiler, Executor loadExecutor, Executor applyExecutor) {
        Munchables.emptyEatTimes();
        return SimpleResourceReloadListener.super.reload(helper, manager, loadProfiler, applyProfiler, loadExecutor, applyExecutor);
    }

    @Override
    public String getName() {
        return SimpleResourceReloadListener.super.getName();
    }

    static class ResourceLoader {
        private final ResourceManager manager;
        private final Profiler profiler;
        private final String path;
        private final HashMap<Pair<Boolean, String>, Integer> modules = new HashMap<>();
        private final BiFunction<Identifier, JsonObject, Triplet<Boolean, String, Integer>> codec;

        public ResourceLoader(ResourceManager manager, Profiler profiler, String path, BiFunction<Identifier, JsonObject, Triplet<Boolean, String, Integer>> codec) {
            this.manager = manager;
            this.profiler = profiler;
            this.path = path;
            this.codec = codec;
            loadData();
        }

        private void loadData() {
            profiler.push("Load API_"+path);
            LOGGER.info("loading " + path + " resources");
            Map<Identifier, Resource> resources = manager.findResources(path, id -> id.getPath().endsWith(".json"));
            resources.forEach((key, value) -> {
                try {
                    Triplet<Boolean, String, Integer> result = codec.apply(key, JsonHelper.deserialize(value.getReader()).getAsJsonObject());
                    modules.put(new Pair<>(result.getA(), result.getB()), result.getC());
                } catch (Exception e) {
                    LOGGER.error("failed to load resource " + key + ":" + e.getMessage());
                }

            });
            profiler.pop();
        }

        public HashMap<Pair<Boolean, String>, Integer> getModules() {
            return this.modules;
        }
    }
}
