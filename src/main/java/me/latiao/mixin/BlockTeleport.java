package me.latiao.mixin;

import net.minecraft.server.network.ServerGamePacketListenerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 监听并记录所有服务端对玩家执行的传送操作。
 * 可用于调试：当反作弊或其他系统触发传送回弹时，通过控制台输出目标坐标和朝向。
 * 目前仅打印信息，未取消传送（可通过取消注释 {@code ci.cancel()} 来完全阻止传送）。
 */
@Mixin(ServerGamePacketListenerImpl.class)
public class BlockTeleport {

    /**
     * 在 teleport 方法执行前注入，打印传送的目标坐标与朝向。
     * 方法签名对应原版的 {@code teleport(double, double, double, float, float)}。
     *
     * @param x    目标 X 坐标
     * @param y    目标 Y 坐标
     * @param z    目标 Z 坐标
     * @param yRot 目标水平旋转角度（偏航角）
     * @param xRot 目标垂直旋转角度（俯仰角）
     * @param ci   回调信息，可用于取消原方法执行
     */
    @Inject(
            method = "teleport(DDDFF)V",   // 方法描述符：double x, double y, double z, float yRot, float xRot
            at = @At("HEAD")
            // cancellable = true  // 如需阻止传送，取消此行注释并启用下方 ci.cancel()
    )
    private void onTeleport(double x, double y, double z, float yRot, float xRot, CallbackInfo ci) {
        // 输出格式化的传送信息，方便在控制台追踪传送原因和位置
        System.out.printf("Teleport command detected: target=(%.2f, %.2f, %.2f) yRot=%.2f xRot=%.2f%n",
                x, y, z, yRot, xRot);

        // 若需要完全阻止传送（例如完全接管传送逻辑），可取消注释下面一行：
        // ci.cancel();
    }
}