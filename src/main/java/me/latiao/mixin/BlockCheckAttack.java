package me.latiao.mixin;

import net.minecraft.network.protocol.game.ServerboundAttackPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;


@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BlockCheckAttack {

    // --- 映射目标类的方法和字段 ---
    @Shadow
    public ServerPlayer player;

    @Shadow
    protected abstract boolean hasClientLoaded();

    //    @Shadow public abstract void disconnect(Component reason);
    @Shadow
    @Final
//    private static Logger LOGGER;

    @Inject(method = "handleAttack", at = @At("HEAD"), cancellable = true)
    private void onHandleAttack(ServerboundAttackPacket packet, CallbackInfo ci) {
        ci.cancel();
//            PacketUtils.ensureRunningOnSameThread(packet, this, this.player.level());
//            if (this.hasClientLoaded() && !this.player.isSpectator()) {
        ServerLevel level = this.player.level();
        Entity target = level.getEntityOrPart(packet.entityId());
        this.player.resetLastActionTime();
//                if (target != null && level.getWorldBorder().isWithinBounds(target.blockPosition())) {
//                    AABB targetBounds = target.getBoundingBox();
//                    ItemStack mainHandItem = this.player.getMainHandItem();
//                    if (this.player.isWithinAttackRange(mainHandItem, targetBounds, 3.0)) {
//                        if (!mainHandItem.has(DataComponents.PIERCING_WEAPON)) {
//                            if (target instanceof ItemEntity
//                                    || target instanceof ExperienceOrb
//                                    || target == this.player
//                                    || target instanceof AbstractArrow abstractArrow && !abstractArrow.isAttackable()) {
//                                this.disconnect(Component.translatable("multiplayer.disconnect.invalid_entity_attacked"));
//                                LOGGER.warn("Player {} tried to attack an invalid entity", this.player.getPlainTextName());
//                            } else if (mainHandItem.isItemEnabled(level.enabledFeatures())) {
//                                if (!this.player.cannotAttackWithItem(mainHandItem, 5)) {
        this.player.attack(target);
//                                }
//                            }
//                        }
//                    }
//                }
    }
}

