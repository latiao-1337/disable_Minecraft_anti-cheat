package me.latiao.mixin;

import net.minecraft.advancements.triggers.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundUseItemOnPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

//@Mixin(ServerGamePacketListenerImpl.class)
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BlockCheckUseItemOn {
    @Shadow
    public ServerPlayer player;

    @Shadow
    public abstract void ackBlockChangesUpTo(int sequence);
//    @Shadow
//    private Connection connection;

    @Inject(method = "handleUseItemOn", at = @At("HEAD"), cancellable = true)
    private void onHandleUseItemOn(ServerboundUseItemOnPacket packet, CallbackInfo ci) {
        ci.cancel();
//        ServerGamePacketListenerImpl listener = (ServerGamePacketListenerImpl) (Object) this;
//        PacketUtils.ensureRunningOnSameThread(packet, listener, this.player.level());
        this.ackBlockChangesUpTo(packet.getSequence());

        ServerLevel level = this.player.level();
        InteractionHand hand = packet.getHand();
        ItemStack itemStack = this.player.getItemInHand(hand);
        BlockHitResult blockHit = packet.getHitResult();
        BlockPos pos = blockHit.getBlockPos();
        Direction direction = blockHit.getDirection();
//        System.out.println(level.getBlockState(pos).getBlock());
        InteractionResult interactionResult = this.player.gameMode.useItemOn(this.player, level, itemStack, hand, blockHit);
        if (interactionResult.consumesAction()) {
            CriteriaTriggers.ANY_BLOCK_USE.trigger(this.player, blockHit.getBlockPos(), itemStack);
        }
//        this.player.sendSpawnProtectionMessage(pos);
        this.player.swing(hand, true);
        this.player.connection.send(new ClientboundBlockUpdatePacket(level, pos));
        this.player.connection.send(new ClientboundBlockUpdatePacket(level, pos.relative(direction)));

    }
}