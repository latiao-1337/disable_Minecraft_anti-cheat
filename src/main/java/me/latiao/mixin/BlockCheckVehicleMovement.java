package me.latiao.mixin;

import net.minecraft.network.protocol.game.ServerboundMoveVehiclePacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BlockCheckVehicleMovement {

    // === 字段 Shadows ===
    @Shadow
    public ServerPlayer player;

//    @Shadow
//    @Final
//    private MinecraftServer server;

    @Shadow
    private Entity lastVehicle;

    @Shadow
    private double vehicleFirstGoodX;

    @Shadow
    private double vehicleFirstGoodY;

    @Shadow
    private double vehicleFirstGoodZ;

    @Shadow
    private double vehicleLastGoodX;

    @Shadow
    private double vehicleLastGoodY;

    @Shadow
    private double vehicleLastGoodZ;

    @Shadow
    private boolean clientVehicleIsFloating;

//    @Shadow
//    private static final Logger LOGGER = null; // 实际不需要初始化，Mixin 会处理

    // === 方法 Shadows ===
//    @Shadow
//    public abstract void disconnect(Component reason);

//    @Shadow
//    protected abstract boolean updateAwaitingTeleport();

//    @Shadow
//    protected abstract boolean hasClientLoaded();

    @Shadow
    protected static double clampHorizontal(double value) {
        throw new AssertionError();
    }

    @Shadow
    protected static double clampVertical(double value) {
        throw new AssertionError();
    }

//    @Shadow
//    protected abstract boolean isSingleplayerOwner();

//    @Shadow
//    public abstract void send(net.minecraft.network.protocol.Packet<?> packet);

//    @Shadow
//    protected abstract boolean containsInvalidValues(double x, double y, double z, float yRot, float xRot);

//    @Shadow
//    protected abstract boolean isEntityCollidingWithAnythingNew(ServerLevel level, Entity entity, AABB oldAABB, double newX, double newY, double newZ);

    @Shadow
    protected abstract void handlePlayerKnownMovement(Vec3 deltaMovement);

//    @Shadow
//    protected abstract boolean noBlocksAround(Entity entity);

    @Inject(method = "handleMoveVehicle", at = @At("HEAD"), cancellable = true)
    private void onHandleMoveVehicle(ServerboundMoveVehiclePacket packet, CallbackInfo ci) {
        ci.cancel();
//        PacketUtils.ensureRunningOnSameThread(packet, (ServerGamePacketListenerImpl) (Object) this, this.player.level());

//        if (containsInvalidValues(packet.position().x(), packet.position().y(), packet.position().z(), packet.yRot(), packet.xRot())) {
//            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_vehicle_movement"));
//        } else
//            if (!this.updateAwaitingTeleport() && this.hasClientLoaded())
        {
            Entity vehicle = this.player.getRootVehicle();
//            if (vehicle != this.player && vehicle.getControllingPassenger() == this.player && vehicle == this.lastVehicle) {
            ServerLevel level = this.player.level();
            double oldX = vehicle.getX();
            double oldY = vehicle.getY();
            double oldZ = vehicle.getZ();
            double targetX = clampHorizontal(packet.position().x());
            double targetY = clampVertical(packet.position().y());
            double targetZ = clampHorizontal(packet.position().z());
            float targetYRot = Mth.wrapDegrees(packet.yRot());
            float targetXRot = Mth.wrapDegrees(packet.xRot());
            double xDist = targetX - this.vehicleFirstGoodX;
            double yDist = targetY - this.vehicleFirstGoodY;
            double zDist = targetZ - this.vehicleFirstGoodZ;
//            double expectedDist = vehicle.getDeltaMovement().lengthSqr();
//            double movedDist = xDist * xDist + yDist * yDist + zDist * zDist;

//                if (movedDist - expectedDist > 100.0 && !this.isSingleplayerOwner()) {
////                    LOGGER.warn("{} (vehicle of {}) moved too quickly! {},{},{}",
////                            vehicle.getPlainTextName(), this.player.getPlainTextName(), xDist, yDist, zDist;)
//                    this.player.connection.send(ClientboundMoveVehiclePacket.fromEntity(vehicle));
//                    ci.cancel();
//                    return;
//                }

//            AABB oldAABB = vehicle.getBoundingBox();
            xDist = targetX - this.vehicleLastGoodX;
            yDist = targetY - this.vehicleLastGoodY;
            zDist = targetZ - this.vehicleLastGoodZ;
//            boolean vehicleRestsOnSomething = vehicle.verticalCollisionBelow;

            if (vehicle instanceof LivingEntity livingVehicle && livingVehicle.onClimbable()) {
                livingVehicle.resetFallDistance();
            }

            vehicle.move(MoverType.PLAYER, new Vec3(xDist, yDist, zDist));
//            double oyDist = yDist;
//            xDist = targetX - vehicle.getX();
//            yDist = targetY - vehicle.getY();
//
//                if (yDist > -0.5 || yDist < 0.5) {
//                    yDist = 0.0;
//                }

//            zDist = targetZ - vehicle.getZ();
//            movedDist = xDist * xDist + yDist * yDist + zDist * zDist;
//                boolean fail = movedDist > 0.0625;

            //                    LOGGER.warn("{} (vehicle of {}) moved wrongly! {}",
            //                            vehicle.getPlainTextName(), this.player.getPlainTextName(), Math.sqrt(movedDist));
            //
//                if (fail && level.noCollision(vehicle, oldAABB) ||
//                        this.isEntityCollidingWithAnythingNew(level, vehicle, oldAABB, targetX, targetY, targetZ)) {
//                    vehicle.absSnapTo(oldX, oldY, oldZ, targetYRot, targetXRot);
//                    this.player.connection.send(ClientboundMoveVehiclePacket.fromEntity(vehicle));
//                    vehicle.removeLatestMovementRecording();
//                    ci.cancel();
//                    return;
//                }

            vehicle.absSnapTo(targetX, targetY, targetZ, targetYRot, targetXRot);
            this.player.level().getChunkSource().move(this.player);
            Vec3 clientDeltaMovement = new Vec3(vehicle.getX() - oldX, vehicle.getY() - oldY, vehicle.getZ() - oldZ);
            this.handlePlayerKnownMovement(clientDeltaMovement);
            vehicle.setOnGroundWithMovement(packet.onGround(), clientDeltaMovement);
            vehicle.doCheckFallDamage(clientDeltaMovement.x, clientDeltaMovement.y, clientDeltaMovement.z, packet.onGround());
            this.player.checkMovementStatistics(clientDeltaMovement.x, clientDeltaMovement.y, clientDeltaMovement.z);

//                this.clientVehicleIsFloating = oyDist >= -0.03125
//                        && !vehicleRestsOnSomething
//                        && !this.server.allowFlight()
//                        && !vehicle.isFlyingVehicle()
//                        && !vehicle.isNoGravity()
//                        && this.noBlocksAround(vehicle);

            this.vehicleLastGoodX = vehicle.getX();
            this.vehicleLastGoodY = vehicle.getY();
            this.vehicleLastGoodZ = vehicle.getZ();
        }


    }
}