package com.wumple.util.capability.targetcrafting;

import net.minecraft.nbt.NBTBase;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;

public class CapStorage<U extends IContainerCraftingOwner> implements IStorage<U>
{
    @Override
    public NBTBase writeNBT(Capability<U> capability, U instance, EnumFacing side)
    {
        if (instance != null)
        {
            return instance.serializeNBT();
        }

        return null;
    }

    @Override
    public void readNBT(Capability<U> capability, U instance, EnumFacing side, NBTBase nbt)
    {
        if ((nbt != null) && (instance != null))
        {
            instance.deserializeNBT(nbt);
        }
    }
}