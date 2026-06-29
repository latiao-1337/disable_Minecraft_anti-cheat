package me.latiao.mixin;

import net.minecraft.network.chat.Component;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerCommonPacketListenerImpl.class)
public class BlockKick {


    @Inject(
            method = "disconnect(Lnet/minecraft/network/chat/Component;)V",
            at = @At("HEAD"),
            cancellable = true
    )
    private void onDisconnect(Component reason, CallbackInfo ci) {
        // 1. 将踢出原因输出到控制台，方便了解是什么触发了踢出
        System.out.println("Intercepted disconnect, reason: " + reason.toString());

        // 2. 取消原始 disconnect 方法的执行，使玩家不会被踢出
        ci.cancel();
    }
}