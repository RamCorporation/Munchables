package net.ramgames.munchables;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import oshi.util.tuples.Triplet;

import java.util.HashMap;
import java.util.Set;
import java.util.function.Supplier;

public class Munchables implements ModInitializer {
    public static final String MOD_ID = "munchables";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final HashMap<String, Integer> eatTimes = new HashMap<>();

    protected static void addEatTime(Pair<Boolean, String> pair, Integer value) {
        LOGGER.info("adding!");
        String key = pair.getRight();
        if(!eatTimes.containsKey(key)) {
            eatTimes.put(key, value);
            return;
        }
        if(pair.getLeft()) eatTimes.put(key, value);
    }
    protected static void emptyEatTimes() {
        Set.copyOf(eatTimes.keySet()).forEach(eatTimes::remove);
    }
    @Override
    public void onInitialize() {
        LOGGER.info("Munchables is ready to eat!");
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ModResourceLoader("munchables", (id, json) -> {
            boolean replace = json.get("replace").getAsBoolean();
            String item = json.get("item").getAsString();
            int timing = json.get("eatTime").getAsInt();
            return new Triplet<>(replace, item, timing);
        }));
    }

    public static int getTiming(NbtCompound nbt, String key, Supplier<Integer> defaultValue) {
        if(nbt != null && nbt.contains("EatTime")) return nbt.getInt("EatTime");
        return eatTimes.getOrDefault(key, defaultValue.get());
    }
}
