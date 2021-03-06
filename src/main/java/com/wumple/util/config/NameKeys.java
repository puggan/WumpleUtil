package com.wumple.util.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import com.wumple.util.base.misc.Util;
import com.wumple.util.misc.TypeIdentifier;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.state.IProperty;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.registries.IForgeRegistryEntry;

/*
 * Generate a set of strings that represent the queried object
 */
public class NameKeys
{
    // ----------------------------------------------------------------------
    // Utility
    
    /**
     * Generate a set of strings that represent the queried ItemStack
     * @see TypeIdentifier for opposite direction but similiar code
     * @param itemStack for which to get namekeys for lookup
     * @return namekeys to search config for, in order
     */
    static public ArrayList<String> getNameKeys(ItemStack itemStack)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (itemStack == null)
        {
            return nameKeys;
        }

        Item item = itemStack.getItem();
        
        addNameKeysResLoc(nameKeys, item);
        addNameKeysItemTags(nameKeys, item);
        addNameKeysObject(nameKeys, (Object)itemStack);
        addNameKeysObject(nameKeys, (Object)item);
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(Item item)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (item == null)
        {
            return nameKeys;
        }

        addNameKeysResLoc(nameKeys, item);
        addNameKeysItemTags(nameKeys, item);
        addNameKeysObject(nameKeys, (Object)item);
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(Block block)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        addNameKeysResLoc(nameKeys, block);
        addNameKeysBlockTags(nameKeys, block);
        addNameKeysObject(nameKeys, (Object)block);
        
        return nameKeys;
    }

    static public ArrayList<String> getNameKeys(Entity entity)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (entity == null)
        {
            return nameKeys;
        }

        addNameKeysResLoc(nameKeys, entity.getType());
        addNameKeysEntityTags(nameKeys, entity);
        addNameKeysObject(nameKeys, (Object)entity.getType());
        
        return nameKeys;
    }
    
    static public ArrayList<String> getNameKeys(TileEntity entity)
    {
        ArrayList<String> nameKeys = new ArrayList<String>();
        
        if (entity == null)
        {
            return nameKeys;
        }

        // OLD: addNameKeysProperty(nameKeys, entity, BlockCrops.AGE);
        addNameKeysResLoc(nameKeys, entity.getType());
        addNameKeysObject(nameKeys, (Object)entity);
        
        return nameKeys;
    }
    
    // ------------------------------------------------------------------------
    // builders
    
    static public ArrayList<String> addNameKeysResLoc(ArrayList<String> nameKeys, IForgeRegistryEntry<?> entry)
    {
        String name = (entry == null) ? null : entry.getRegistryName().toString();

        if (name != null)
        {
            nameKeys.add(name);
        }
        
        return nameKeys;
    }

    static public <T extends Comparable<T>> ArrayList<String> addNameKeysProperty(ArrayList<String> nameKeys, TileEntity te, IProperty<T> property)
    {
        ResourceLocation loc = (te == null) ? null : te.getType().getRegistryName();
        if (loc == null)
        {
            return nameKeys;
        }
        
        BlockPos pos = te.getPos();
        World world = te.getWorld();
        BlockState state = (world != null) ? world.getBlockState(pos) : null;
        Collection<IProperty<?>> props = (state != null) ? state.getProperties() : null;
        if ((props != null) && (props.contains(property)))
        {
            T value = state.get(property);
            nameKeys.add(loc.toString() + "[" + property.getName() + "=" + value.toString() + "]" );
        }
            
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeys(ArrayList<String> nameKeys, Collection<ResourceLocation> tags)
    {
    	for (ResourceLocation tag : tags)
        {
            nameKeys.add("#" + tag.toString());
        }

        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysItemTags(ArrayList<String> nameKeys, Item item)
    {
    	return (item != null) ? addNameKeys(nameKeys, ItemTags.getCollection().getOwningTags(item) ) : nameKeys;
    }
    
    static public ArrayList<String> addNameKeysBlockTags(ArrayList<String> nameKeys, Block block)
    {
    	return addNameKeys(nameKeys, BlockTags.getCollection().getOwningTags(block));
    }
    
    static public ArrayList<String> addNameKeysEntityTags(ArrayList<String> nameKeys, Entity it)
    {
    	return addNameKeys(nameKeys, EntityTypeTags.getCollection().getOwningTags(it.getType()));
    }
    
    static protected boolean ConfigAddClassNames()
    {
    	// TODO
    	return false;
    }
    
    static public ArrayList<String> addNameKeysObject(ArrayList<String> nameKeys, Object object)
    {   
        addNameKeysSpecial(nameKeys, object);
        
        if (ConfigAddClassNames())
        {
            addNameKeysClasses(nameKeys, object);
        }
        
        return nameKeys;
    }
    
    static public ArrayList<String> addNameKeysClasses(ArrayList<String> nameKeys, Object object)
    {   
        // class names for dynamic matching
        Class<?> c = object.getClass();
        while (c != null)
        {
            String classname = c.getName();
            nameKeys.add(classname);
            c = c.getSuperclass();
        }
        
        return nameKeys;
    }

    static public ArrayList<String> addNameKeysSpecial(ArrayList<String> nameKeys, Object object)
    {   
    	
    	for (Function<Object, String> func : specialFuncs)
    	{
    		String nameKey = func.apply(object);
    		if (nameKey != null)
    		{
    			nameKeys.add(nameKey);
    		}
    	}
        
        return nameKeys;
    }
    
    // ------------------------------------------------------------------------
    // User definable builder support
    
    static public List< Function<Object, String> > specialFuncs = new ArrayList< Function<Object, String> >();
        
    static public void addNameKeySpecialCheck(Function<Object, String> func)
    {
    	specialFuncs.add(func);
    }
    
    // ------------------------------------------------------------------------
    // Special user defined builder example

    // Constants
	public static String foodSpecial = "$food";
    
    static public String checkFoodNameKey(Object x)
    {
		Item item = Util.as(x, Item.class);
		return (item != null) && (item.isFood()) ? foodSpecial : null;
    }
    
    static 
    {
    	addNameKeySpecialCheck( NameKeys::checkFoodNameKey );
    }
}
