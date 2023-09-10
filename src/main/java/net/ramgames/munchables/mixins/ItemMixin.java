package net.ramgames.munchables.mixins;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.Registries;
import net.ramgames.munchables.Munchables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Item.class)
public abstract class ItemMixin {

    @Inject(method = "getMaxUseTime", at = @At("HEAD"),cancellable = true)
    private void setEatTimes(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        String stringId = Registries.ITEM.getId(stack.getItem()).toString();
        cir.setReturnValue(Munchables.getTiming(stack.getNbt(), stringId, () -> {
            if(stack.isFood()) return stack.getItem().getFoodComponent().isSnack() ? 16 : 32;
            return 0;
        }));
    }
}
