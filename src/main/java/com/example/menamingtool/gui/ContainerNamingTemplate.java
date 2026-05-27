package com.example.menamingtool.gui;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

public class ContainerNamingTemplate extends Container {

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return true;
    }
}
