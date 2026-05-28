package com.example.menamingtool.item;

import com.example.menamingtool.MENamingToolMod;
import com.example.menamingtool.util.NamingTemplate;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.helpers.ICustomNameObject;
import appeng.tile.AEBaseTile;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.lang.reflect.Field;
import java.util.List;

public class ItemMENamingTool extends Item {

    public ItemMENamingTool() {
        setUnlocalizedName("me_naming_tool");
        setTextureName("menamingtool:me_naming_tool");
        setMaxStackSize(1);
        setCreativeTab(MENamingToolMod.creativeTab);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (player.isSneaking()) {
            if (world.isRemote) {
                MovingObjectPosition mop = player.rayTrace(5.0D, 1.0F);
                boolean targetingBlock = mop != null
                        && mop.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK;
                if (!targetingBlock) {
                    player.openGui(MENamingToolMod.instance, MENamingToolMod.GUI_ID_TEMPLATE, world, 0, 0, 0);
                }
            }
            return itemStack;
        }
        return super.onItemRightClick(itemStack, world, player);
    }

    @Override
    public boolean onItemUseFirst(ItemStack stack, EntityPlayer player, World world,
                                  int x, int y, int z, int side,
                                  float hitX, float hitY, float hitZ) {
        if (world.isRemote) return false;

        TileEntity te = world.getTileEntity(x, y, z);
        if (te == null) return false;

        NamingTemplate template = loadTemplateFromNBT(stack);
        String name = template.generateCurrentName();
        boolean named = false;

        // 处理线缆上的部件形态接口（PartInterface 等）
        // TileCableBus 包含 Part，名字存在 Part 的 ItemStack 上而非 TileEntity 字段
        if (te instanceof IPartHost) {
            named = tryNamePart((IPartHost) te, side, name);
        }

        // 处理方块形态的 AE2 设备（TileInterface 等）
        if (!named && te instanceof AEBaseTile) {
            setInterfaceCustomName(te, name);
            named = true;
        }

        if (!named) return false;

        template.advance();
        saveTemplateToNBT(stack, template);

        player.addChatMessage(new ChatComponentText(
                EnumChatFormatting.GREEN +
                StatCollector.translateToLocalFormatted("menamingtool.msg.named", name)
        ));

        return true;
    }

    private boolean tryNamePart(IPartHost host, int clickedSide, String name) {
        // 先尝试玩家点击的面
        IPart part = host.getPart(ForgeDirection.getOrientation(clickedSide));
        if (part instanceof ICustomNameObject) {
            applyPartName(part, host, name);
            return true;
        }

        // 点击面没找到则遍历所有方向（包括 UNKNOWN 中心线缆位置）
        for (ForgeDirection dir : ForgeDirection.values()) {
            if (dir.ordinal() == clickedSide) continue;
            part = host.getPart(dir);
            if (part instanceof ICustomNameObject) {
                applyPartName(part, host, name);
                return true;
            }
        }

        return false;
    }

    private void applyPartName(IPart part, IPartHost host, String name) {
        ((ICustomNameObject) part).setCustomName(name);
        // 标记方块需要保存，确保持久化
        if (host instanceof AEBaseTile) {
            ((AEBaseTile) host).saveChanges();
            ((AEBaseTile) host).markForUpdate();
        }
    }

    private void setInterfaceCustomName(TileEntity te, String name) {
        AEBaseTile aeTile = (AEBaseTile) te;

        // Forge 的 TileEntity.customName（getCommandSenderName 直接读取此字段用于 GUI 标题）
        try {
            Field field = TileEntity.class.getDeclaredField("customName");
            field.setAccessible(true);
            field.set(aeTile, name);
        } catch (Exception ignored) {
        }

        // AE2 的 AEBaseTile.customName（接口终端通过 getTermName() → getCustomName() 读取）
        aeTile.setCustomName(name);

        aeTile.saveChanges();
        aeTile.markForUpdate();
    }

    public static void saveTemplateToNBT(ItemStack stack, NamingTemplate template) {
        if (stack == null) return;
        if (!stack.hasTagCompound()) {
            stack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound subTag = template.writeToNBT();
        stack.getTagCompound().setTag("MENamingTool", subTag);
    }

    public static NamingTemplate loadTemplateFromNBT(ItemStack stack) {
        NamingTemplate template = new NamingTemplate();
        if (stack == null || !stack.hasTagCompound()) return template;

        NBTTagCompound stackTag = stack.getTagCompound();
        if (stackTag.hasKey("MENamingTool")) {
            NBTTagCompound subTag = stackTag.getCompoundTag("MENamingTool");
            template.readFromNBT(subTag);
        }
        return template;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean advanced) {
        NamingTemplate tpl = loadTemplateFromNBT(stack);

        tooltip.add(EnumChatFormatting.GRAY +
                StatCollector.translateToLocalFormatted("menamingtool.tip.template", tpl.getTemplate()));
        tooltip.add(EnumChatFormatting.GRAY +
                StatCollector.translateToLocalFormatted("menamingtool.tip.format",
                        tpl.getFormat(), tpl.getStartValue(), tpl.getStep()));
        tooltip.add(EnumChatFormatting.AQUA +
                StatCollector.translateToLocalFormatted("menamingtool.tip.next", tpl.previewNext()));
        tooltip.add(EnumChatFormatting.DARK_GRAY +
                StatCollector.translateToLocal("menamingtool.tip.usage1"));
        tooltip.add(EnumChatFormatting.DARK_GRAY +
                StatCollector.translateToLocal("menamingtool.tip.usage2"));

        super.addInformation(stack, player, tooltip, advanced);
    }
}
