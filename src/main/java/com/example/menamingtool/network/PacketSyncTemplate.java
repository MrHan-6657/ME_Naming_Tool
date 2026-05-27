package com.example.menamingtool.network;

import com.example.menamingtool.item.ItemMENamingTool;
import com.example.menamingtool.util.NamingTemplate;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;

public class PacketSyncTemplate implements IMessage {

    private NBTTagCompound data;

    public PacketSyncTemplate() {}

    public PacketSyncTemplate(NamingTemplate template) {
        this.data = template.writeToNBT();
    }

    @Override
    public void fromBytes(ByteBuf buf) {
        try {
            int length = buf.readInt();
            if (length > 0 && length < 65536) {
                byte[] bytes = new byte[length];
                buf.readBytes(bytes);
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                DataInputStream dis = new DataInputStream(bais);
                this.data = CompressedStreamTools.read(dis);
            } else {
                this.data = new NBTTagCompound();
            }
        } catch (Exception e) {
            this.data = new NBTTagCompound();
        }
    }

    @Override
    public void toBytes(ByteBuf buf) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            DataOutputStream dos = new DataOutputStream(baos);
            CompressedStreamTools.write(data != null ? data : new NBTTagCompound(), dos);
            byte[] bytes = baos.toByteArray();
            buf.writeInt(bytes.length);
            buf.writeBytes(bytes);
        } catch (Exception ignored) {
            buf.writeInt(0);
        }
    }

    public static class Handler implements IMessageHandler<PacketSyncTemplate, IMessage> {
        @Override
        public IMessage onMessage(PacketSyncTemplate message, MessageContext ctx) {
            EntityPlayerMP player = ctx.getServerHandler().playerEntity;
            if (player == null) return null;

            ItemStack held = player.getHeldItem();
            if (held == null || held.getItem() == null) return null;
            if (!(held.getItem() instanceof ItemMENamingTool)) return null;

            NamingTemplate template = new NamingTemplate();
            if (message.data != null) {
                template.readFromNBT(message.data);
            }
            ItemMENamingTool.saveTemplateToNBT(held, template);

            return null;
        }
    }
}
