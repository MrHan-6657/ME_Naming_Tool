package com.example.menamingtool;

import com.example.menamingtool.gui.GuiHandler;
import com.example.menamingtool.item.ItemMENamingTool;
import com.example.menamingtool.network.PacketHandler;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mod(modid = MENamingToolMod.MODID,
     name = MENamingToolMod.NAME,
     version = MENamingToolMod.VERSION,
     dependencies = "required-after:appliedenergistics2")
public class MENamingToolMod {

    public static final String MODID = "menamingtool";
    public static final String NAME = "ME Naming Tool";
    public static final String VERSION = "1.2.0";

    @Mod.Instance(MODID)
    public static MENamingToolMod instance;

    @SidedProxy(
            clientSide = "com.example.menamingtool.ClientProxy",
            serverSide = "com.example.menamingtool.CommonProxy")
    public static CommonProxy proxy;

    public static final int GUI_ID_TEMPLATE = 0;

    public static Item meNamingTool;

    public static CreativeTabs creativeTab = new CreativeTabs("menamingtool") {
        @Override
        @SideOnly(Side.CLIENT)
        public Item getTabIconItem() {
            return meNamingTool;
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit();

        meNamingTool = new ItemMENamingTool();
        GameRegistry.registerItem(meNamingTool, "me_naming_tool");

        PacketHandler.init();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(this, new GuiHandler());

        // 合成配方：石英 + 命名牌 + 纸 + 铁锭 → ME命名工具
        GameRegistry.addRecipe(new ItemStack(meNamingTool),
                " Q ",
                "N P",
                " I ",
                'Q', Items.quartz,
                'N', Items.name_tag,
                'P', Items.paper,
                'I', Items.iron_ingot);
    }
}
