package me.latiao.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ServerboundPlayerActionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerPlayerGameMode;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * 移除原版方块破坏的距离、高度与权限检查，直接执行完整的挖掘逻辑。
 * <p>
 * 该类通过 Mixin 注入到 {@link ServerGamePacketListenerImpl} 中，
 * 在 {@code handleBlockBreakAction} 方法头部拦截并取消原版处理，
 * 转而使用自定义的方块破坏流程。
 * </p>
 */
@Mixin(ServerPlayerGameMode.class)
public abstract class BlockCheckBreakAction {

    // === 基础玩家引用 ===

    /**
     * 当前连接对应的服务器玩家实例
     */
    @Shadow
    public ServerPlayer player;

    // === 挖掘状态追踪字段 ===

    /**
     * 服务器当前的游戏刻计数，用于计算挖掘耗时
     */
    @Shadow

    private int gameTicks;

    /**
     * 开始破坏方块时的游戏刻，用于计算挖掘进度
     */
    @Shadow
    private int destroyProgressStart;

    /**
     * 标记玩家当前是否正在破坏方块
     */
    @Shadow
    private boolean isDestroyingBlock;

    /**
     * 当前正在破坏的方块坐标
     */
    @Shadow
    private BlockPos destroyPos;

    /**
     * 上一次发送给客户端的破坏进度状态值（0-9）
     */
    @Shadow
    private int lastSentState;

    /**
     * 是否存在一个延迟的方块破坏操作
     */
    @Shadow
    private boolean hasDelayedDestroy;

    /**
     * 延迟破坏的目标方块坐标
     */
    @Shadow
    private BlockPos delayedDestroyPos;

    /**
     * 延迟破坏开始时的游戏刻
     */
    @Shadow
    private int delayedTickStart;

    // === Shadow 方法 ===

    /**
     * 发送调试日志信息。
     * @param pos      操作的方块坐标
     * @param success  操作是否成功
     * @param sequence 操作序列号
     * @param reason   调试原因描述
     */
//    @Shadow
//    private void debugLogging(BlockPos pos, boolean success, int sequence, String reason) {
//        throw new AssertionError();
//    }
    @Shadow
    private net.minecraft.world.level.GameType gameModeForPlayer;
    /**
     * 玩家所在的服务器世界实例
     */
    @Shadow
    private ServerLevel level;

    /**
     * 执行方块的破坏并发送确认包给客户端。
     *
     * @param pos      要破坏的方块坐标
     * @param sequence 操作序列号
     * @param reason   破坏原因描述
     */
    @Shadow
    private void destroyAndAck(BlockPos pos, int sequence, String reason) {
        throw new AssertionError();
    }

    @Inject(method = "handleBlockBreakAction", at = @At("HEAD"), cancellable = true)

    private void onHandleBlockBreakAction(BlockPos pos, ServerboundPlayerActionPacket.Action action, Direction direction, int maxY, int sequence, CallbackInfo ci) {
        // 【核心拦截】取消原版方法的所有后续处理，完全由当前自定义逻辑接管
        ci.cancel();

        // 检查玩家是否在方块的交互范围内（距离太远则不处理）
//        if (!this.player.isWithinBlockInteractionRange(pos, 1.0)) {
//            this.debugLogging(pos, false, sequence, "too far");
//        }
        // 检查目标方块的高度是否超过了允许的最大高度限制（maxY）
//        else if (pos.getY() > maxY) {
        // 如果超过高度，向客户端发送网络包，将该方块强制恢复为服务端当前的状态（防作弊/同步）
//            this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos)));
//            this.debugLogging(pos, false, sequence, "too high");
//        }
//        else {
        // ==================== 1. 开始挖掘方块 (START_DESTROY_BLOCK) ====================
        if (action == ServerboundPlayerActionPacket.Action.START_DESTROY_BLOCK) {

            // 检查是否处于服务器的出生点保护区域内
//                if (this.level.getServer().isUnderSpawnProtection(this.level, pos, this.player)) {
//                    this.player.sendSpawnProtectionMessage(pos); // 提示玩家处于出生点保护中
//                    this.debugLogging(pos, false, sequence, "spawn protection");
//                    return;
//                }

            // 检查玩家当前是否被允许与该位置的方块交互（例如领地插件、冒险模式限制等）
//                if (!this.level.mayInteract(this.player, pos)) {
//                    this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos))); // 恢复方块状态
//                    this.debugLogging(pos, false, sequence, "may not interact");
//                    return;
//                }

            // 如果玩家是创造模式（具有瞬坏能力），则直接破坏方块并向客户端确认
//                if (this.player.getAbilities().instabuild) {
//                    this.destroyAndAck(pos, sequence, "creative destroy");
//                    return;
//                }

            // 检查玩家的当前游戏模式（GameMode）是否限制了破坏该方块（例如冒险模式下工具不对）
//                if (this.player.blockActionRestricted(this.level, pos, this.gameModeForPlayer)) {
//                    this.player.connection.send(new ClientboundBlockUpdatePacket(pos, this.level.getBlockState(pos))); // 恢复方块状态
//                    this.debugLogging(pos, false, sequence, "block action restricted");
//                    return;
//                }

            // --- 满足基础挖掘条件，开始初始化挖掘逻辑 ---
            this.destroyProgressStart = this.gameTicks; // 记录开始挖掘时的游戏刻（Tick）
            float progress = 1.0F; // 初始化挖掘进度
            BlockState blockState = this.level.getBlockState(pos); // 获取目标位置的方块状态

            // 如果目标不是空气方块，触发攻击方块的相关逻辑
            if (!blockState.isAir()) {
//                    // 触发主手武器/工具上的附魔效果（例如：击中方块时的附魔响应，如风蚀等）
//                    EnchantmentHelper.onHitBlock(
//                            this.level,
//                            this.player.getMainHandItem(),
//                            this.player,
//                            this.player,
//                            EquipmentSlot.MAINHAND,
//                            Vec3.atCenterOf(pos),
//                            blockState,
//                            item -> this.player.onEquippedItemBroken(item, EquipmentSlot.MAINHAND) // 工具损坏时的回调
//                    );
                blockState.attack(this.level, pos, this.player); // 触发方块被左键点击/攻击的事件
                progress = blockState.getDestroyProgress(this.player, this.player.level(), pos); // 计算玩家当前对该方块的单Tick挖掘进度
            }

            // 如果不是空气且计算出的进度大于等于 1.0（说明可以“秒挖”，例如生存模式效率极高或方块极脆弱）
            if (!blockState.isAir() && progress >= 1.0F) {
                this.destroyAndAck(pos, sequence, "insta mine");
            }
            // 否则，进入正常的持续挖掘状态（需要读条）
            else {
                // 如果玩家已经在破坏另一个方块，说明客户端和服务端同步出现偏差（客户端认为可以秒挖，但服务端不同意）
                if (this.isDestroyingBlock) {
                    // 恢复之前那个方块的状态
                    this.player.connection.send(new ClientboundBlockUpdatePacket(this.destroyPos, this.level.getBlockState(this.destroyPos)));
//                        this.debugLogging(pos, false, sequence, "abort destroying since another started (client insta mine, server disagreed)");
                }

                // 标记当前正在挖掘方块，并记录坐标
                this.isDestroyingBlock = true;
                this.destroyPos = pos.immutable();

                // 将进度映射到 0-9 的原版方块裂纹状态（10倍关系），并发送裂纹动画包给周围玩家
                int state = (int) (progress * 10.0F);
                this.level.destroyBlockProgress(this.player.getId(), pos, state);
//                    this.debugLogging(pos, true, sequence, "actual start of destroying");
                this.lastSentState = state; // 记录最后发送的动画状态
//                }
            }
            // ==================== 2. 停止挖掘/完成挖掘 (STOP_DESTROY_BLOCK) ====================
//            else
        }
        if (action == ServerboundPlayerActionPacket.Action.STOP_DESTROY_BLOCK) {
            // 确认客户端结束挖掘的方块就是服务端记录的当前挖掘方块
            if (pos.equals(this.destroyPos)) {
//                int ticksSpentDestroying = this.gameTicks - this.destroyProgressStart; // 计算实际挖掘花费的 Tick 数
                BlockState state = this.level.getBlockState(pos);
//
                    if (!state.isAir()) {
//                        // 计算总挖掘进度：单 Tick 进度 * (花费时间 + 1)
//                float destroyProgress = state.getDestroyProgress(this.player, this.player.level(), pos) * (ticksSpentDestroying + 1);
//
//                        // 【阈值验证】如果总进度达到了 0.7（注意：此处可能针对自定义网络延迟或防作弊进行了放宽，原版通常接近1.0），则认为挖掘有效
//                        if (destroyProgress >= 0.7F) {
                this.isDestroyingBlock = false;
                this.level.destroyBlockProgress(this.player.getId(), pos, -1); // 清除方块裂纹动画
                this.destroyAndAck(pos, sequence, "destroyed"); // 真正执行破坏方块并向客户端确认
//                            return;
//                        }
//
//                        // 如果进度不够，且当前还没有被标记为延迟破坏，则放入延迟破坏队列中（用于处理网络波动导致的短时同步问题）
//                        if (!this.hasDelayedDestroy) {
//                            this.isDestroyingBlock = false;
//                            this.hasDelayedDestroy = true;
//                            this.delayedDestroyPos = pos;
//                            this.delayedTickStart = this.destroyProgressStart;
//                        }
//                    }
                }

//                this.debugLogging(pos, true, sequence, "stopped destroying");
            }
        }
            // ==================== 3. 取消挖掘 (ABORT_DESTROY_BLOCK) ====================
            else
                if (action == ServerboundPlayerActionPacket.Action.ABORT_DESTROY_BLOCK) {
                this.isDestroyingBlock = false; // 移除挖掘状态
//
//                // 如果客户端取消的方块和服务器记录的当前挖掘方块不一致，说明存在方块位置错位
//                if (!Objects.equals(this.destroyPos, pos)) {
////                    LOGGER.warn("Mismatch in destroy block pos: {} {}", this.destroyPos, pos);
//                    this.level.destroyBlockProgress(this.player.getId(), this.destroyPos, -1); // 清除错位方块的裂纹动画
////                    this.debugLogging(pos, true, sequence, "aborted mismatched destroying");
//                }
//
//                // 清除当前被取消方块的裂纹动画
                this.level.destroyBlockProgress(this.player.getId(), pos, -1);
//                this.debugLogging(pos, true, sequence, "aborted destroying");
            }
        }
//    }
//    }
}