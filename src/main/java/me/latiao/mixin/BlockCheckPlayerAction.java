package me.latiao.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.PiercingWeapon;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BlockCheckPlayerAction {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void ackBlockChangesUpTo(int sequence);

    @Inject(method = "handlePlayerAction", at = @At("HEAD"), cancellable = true)
    private void onHandlePlayerAction(ServerboundPlayerActionPacket packet, CallbackInfo ci) {


        ci.cancel();
//        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.level());
//        if (this.hasClientLoaded()) {
        BlockPos pos = packet.getPos();
        this.player.resetLastActionTime();
        ServerboundPlayerActionPacket.Action action = packet.getAction();
        switch (action) {
            case STAB:
//                    if (this.player.isSpectator()) {
//                        return;
//                    } else
            {
                ItemStack itemInHand = this.player.getItemInHand(InteractionHand.MAIN_HAND);
//                        if (this.player.cannotAttackWithItem(itemInHand, 5)) {
//                            return;
//                        }

                PiercingWeapon piercingWeapon = itemInHand.get(DataComponents.PIERCING_WEAPON);
//                        if (piercingWeapon != null) {
                            piercingWeapon.attack(this.player, EquipmentSlot.MAINHAND);
//                        }

                return;
            }
            case SWAP_ITEM_WITH_OFFHAND:
//                    if (!this.player.isSpectator()) {
                        ItemStack swap = this.player.getItemInHand(InteractionHand.OFF_HAND);
                        this.player.setItemInHand(InteractionHand.OFF_HAND, this.player.getItemInHand(InteractionHand.MAIN_HAND));
                        this.player.setItemInHand(InteractionHand.MAIN_HAND, swap);
                        this.player.stopUsingItem();
//                    }

                return;
            case DROP_ITEM:
//                    if (!this.player.isSpectator()) {
                        this.player.drop(false);
//                    }

                return;
            case DROP_ALL_ITEMS:
//                    if (!this.player.isSpectator()) {
                        this.player.drop(true);
//                    }

                return;
            case RELEASE_USE_ITEM:
                this.player.releaseUsingItem();
                return;
            case START_DESTROY_BLOCK:
            case ABORT_DESTROY_BLOCK:
            case STOP_DESTROY_BLOCK:
                this.player.gameMode.handleBlockBreakAction(pos, action, packet.getDirection(), this.player.level().getMaxY(), packet.getSequence());
                this.ackBlockChangesUpTo(packet.getSequence());
                return;
            default:
                throw new IllegalArgumentException("Invalid player action");
//            }
        }
    }
}
