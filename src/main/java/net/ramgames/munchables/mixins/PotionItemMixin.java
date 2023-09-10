package net.ramgames.munchables.mixins;

import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.registry.Registries;
import net.ramgames.munchables.Munchables;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionItemMixin {

    @Inject(method = "getMaxUseTime", at = @At("HEAD"), cancellable = true)
    private void setMaxUseTime(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        String stringId = Registries.ITEM.getId(stack.getItem()).toString();
        cir.setReturnValue(Munchables.getTiming(stack.getNbt(), stringId, () -> 32));
    }
}
