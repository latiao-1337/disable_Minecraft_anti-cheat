package me.latiao.mixin;

import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 *
 */
@Mixin(ServerGamePacketListenerImpl.class)
public abstract class BlockCheckPlayerMovement {
    // === 基础字段 ===
    @Shadow
    public ServerPlayer player;

    // 注意：server 字段在新版本可能不存在，通过 player.server 获取
    // @Shadow @Final private MinecraftServer server; // 移除这个

//    @Shadow
//    private int tickCount;

    @Shadow
    private double firstGoodX;
    @Shadow
    private double firstGoodY;
    @Shadow
    private double firstGoodZ;

    @Shadow
    private double lastGoodX;
    @Shadow
    private double lastGoodY;
    @Shadow
    private double lastGoodZ;

//    @Shadow
//    private int receivedMovePacketCount;
//
//    @Shadow
//    private int knownMovePacketCount;
//
//    @Shadow
//    private boolean clientIsFloating;

    // === 方法 Shadow ===
//    @Shadow
//    public abstract void disconnect(Component reason);

//    @Shadow
//    protected abstract void resetPosition();
//
//    @Shadow
//    protected abstract boolean hasClientLoaded();
//
//    @Shadow
//    protected abstract boolean updateAwaitingTeleport();

//    @Shadow
//    protected abstract void teleport(double x, double y, double z, float yRot, float xRot);

//    @Shadow
//    protected abstract boolean shouldCheckPlayerMovement(boolean isFallFlying);
//
//    @Shadow
//    protected abstract boolean noBlocksAround(Entity entity);

//    @Shadow
//    protected abstract boolean isEntityCollidingWithAnythingNew(ServerLevel level, Entity entity, AABB oldAABB, double newX, double newY, double newZ);

    // === 静态方法 Shadow ===
    @Shadow
    protected static double clampHorizontal(double value) {
        throw new AssertionError();
    }

    @Shadow
    protected static double clampVertical(double value) {
        throw new AssertionError();
    }

    @Shadow
    protected abstract void handlePlayerKnownMovement(Vec3 deltaMovement);

//    @Shadow
//    protected static boolean containsInvalidValues(double x, double y, double z, float yRot, float xRot) {
//        throw new AssertionError();
//    }


    @Inject(method = "handleMovePlayer", at = @At("HEAD"), cancellable = true)
    private void onHandleMovePlayer(ServerboundMovePlayerPacket packet, CallbackInfo ci) {
        ci.cancel();
//        PacketUtils.ensureRunningOnSameThread(packet, this, this.player.level());
//        if (containsInvalidValues(packet.getX(0.0), packet.getY(0.0), packet.getZ(0.0), packet.getYRot(0.0F), packet.getXRot(0.0F))) {
//            this.disconnect(Component.translatable("multiplayer.disconnect.invalid_player_movement"));
//        } else {
        ServerLevel level = this.player.level();
//            if (!this.player.wonGame) {
//                if (this.tickCount == 0) {
//                    this.resetPosition();
//                }

//                if (this.hasClientLoaded()) {
        float targetYRot = Mth.wrapDegrees(packet.getYRot(this.player.getYRot()));
        float targetXRot = Mth.wrapDegrees(packet.getXRot(this.player.getXRot()));
//                    if (this.updateAwaitingTeleport()) {
//                        this.player.absSnapRotationTo(targetYRot, targetXRot);
//                    } else {
        double targetX = clampHorizontal(packet.getX(this.player.getX()));
        double targetY = clampVertical(packet.getY(this.player.getY()));
        double targetZ = clampHorizontal(packet.getZ(this.player.getZ()));
//        if (this.player.isPassenger()) {
//            this.player.absSnapTo(this.player.getX(), this.player.getY(), this.player.getZ(), targetYRot, targetXRot);
//            this.player.level().getChunkSource().move(this.player);
//        } else {
        double startX = this.player.getX();
        double startY = this.player.getY();
        double startZ = this.player.getZ();
        double xDist = targetX - this.firstGoodX;
        double yDist = targetY - this.firstGoodY;
        double zDist = targetZ - this.firstGoodZ;
//                            double expectedDist = this.player.getDeltaMovement().lengthSqr();
//                            double movedDist = xDist * xDist + yDist * yDist + zDist * zDist;
//                            if (this.player.isSleeping()) {
//                                if (movedDist > 1.0) {
//                                    this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), targetYRot, targetXRot);
//                                }
//                            } else {
//                                boolean isFallFlying = this.player.isFallFlying();
//                                if (level.tickRateManager().runsNormally()) {
//                                    this.receivedMovePacketCount++;
//                                    int deltaPackets = this.receivedMovePacketCount - this.knownMovePacketCount;
//                                    if (deltaPackets > 5) {
//                                        LOGGER.debug("{} is sending move packets too frequently ({} packets since last tick)", this.player.getPlainTextName(), deltaPackets);
//                                        deltaPackets = 1;
//                                    }

//                                    if (this.shouldCheckPlayerMovement(isFallFlying)) {
//                                        float metersPerTick = isFallFlying ? 300.0F : 100.0F;
//                                        if (movedDist - expectedDist > metersPerTick * deltaPackets) {
////                                            LOGGER.warn("{} moved too quickly! {},{},{}", this.player.getPlainTextName(), xDist, yDist, zDist);
//                                            this.teleport(this.player.getX(), this.player.getY(), this.player.getZ(), this.player.getYRot(), this.player.getXRot());
//                                            return;
//                                        }
//                                    }
//                                }

//                                AABB oldAABB = this.player.getBoundingBox();
        xDist = targetX - this.lastGoodX;
        yDist = targetY - this.lastGoodY;
        zDist = targetZ - this.lastGoodZ;
        boolean movedUpwards = yDist > 0.0;
        if (this.player.onGround() && !packet.isOnGround() && movedUpwards) {
            this.player.jumpFromGround();
        }

//                                boolean playerStandsOnSomething = this.player.verticalCollisionBelow;
        this.player.move(MoverType.PLAYER, new Vec3(xDist, yDist, zDist));
//                                double oyDist = yDist;
//                                xDist = targetX - this.player.getX();
//                                yDist = targetY - this.player.getY();
//                                if (yDist > -0.5 || yDist < 0.5) {
//                                    yDist = 0.0;
//                                }

//                                zDist = targetZ - this.player.getZ();
//                                movedDist = xDist * xDist + yDist * yDist + zDist * zDist;
//                                boolean fail = false;
//                                if (!this.player.isChangingDimension()
//                                        && movedDist > 0.0625
//                                        && !this.player.isSleeping()
//                                        && !this.player.isCreative()
//                                        && !this.player.isSpectator()
//                                        && !this.player.isInPostImpulseGraceTime()) {
//                                    fail = true;
//                                    LOGGER.warn("{} moved wrongly!", this.player.getPlainTextName());
//                                }

//                                if (this.player.noPhysics
//                                        || this.player.isSleeping()
//                                        || (!fail || !level.noCollision(this.player, oldAABB))
//                                        && !this.isEntityCollidingWithAnythingNew(level, this.player, oldAABB, targetX, targetY, targetZ)) {
        this.player.absSnapTo(targetX, targetY, targetZ, targetYRot, targetXRot);
//                                    boolean isAutoSpinAttack = this.player.isAutoSpinAttack();
//                                    this.clientIsFloating = oyDist >= -0.03125
//                                            && !playerStandsOnSomething
//                                            && !this.player.isSpectator()
//                                            && !this.server.allowFlight()
//                                            && !this.player.getAbilities().mayfly
//                                            && !this.player.hasEffect(MobEffects.LEVITATION)
//                                            && !isFallFlying
//                                            && !isAutoSpinAttack
//                                            && this.noBlocksAround(this.player);
        this.player.level().getChunkSource().move(this.player);
        Vec3 clientDeltaMovement = new Vec3(this.player.getX() - startX, this.player.getY() - startY, this.player.getZ() - startZ);
        this.player.setOnGroundWithMovement(packet.isOnGround(), packet.horizontalCollision(), clientDeltaMovement);
        this.player.doCheckFallDamage(clientDeltaMovement.x, clientDeltaMovement.y, clientDeltaMovement.z, packet.isOnGround());
        this.handlePlayerKnownMovement(clientDeltaMovement);
        if (movedUpwards) {
            this.player.resetFallDistance();
        }

//                                    if (packet.isOnGround()
//                                            || this.player.hasLandedInLiquid()
//                                            || this.player.onClimbable()
//                                            || this.player.isSpectator()
//                                            || isFallFlying
//                                            || isAutoSpinAttack) {
//                                        this.player.tryResetCurrentImpulseContext();
//                                    }

        this.player.checkMovementStatistics(this.player.getX() - startX, this.player.getY() - startY, this.player.getZ() - startZ);
        this.lastGoodX = this.player.getX();
        this.lastGoodY = this.player.getY();
        this.lastGoodZ = this.player.getZ();
//                                } else {
//                                    this.teleport(startX, startY, startZ, targetYRot, targetXRot);
//                                    this.player.doCheckFallDamage(this.player.getX() - startX, this.player.getY() - startY, this.player.getZ() - startZ, packet.isOnGround());
//                                    this.player.removeLatestMovementRecording();

//                                }
//                            }
//                        }
//                    }
//                }
//            }
    }

}

//}