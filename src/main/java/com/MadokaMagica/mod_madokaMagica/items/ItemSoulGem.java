package com.MadokaMagica.mod_madokaMagica.items;

import java.util.Random;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.google.common.collect.Sets;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.DamageSource;

import net.minecraftforge.common.MinecraftForge;

import com.MadokaMagica.mod_madokaMagica.trackers.PMDataTracker;
import com.MadokaMagica.mod_madokaMagica.managers.MadokaMagicaEventManager;
import com.MadokaMagica.mod_madokaMagica.managers.PlayerDataTrackerManager;
import com.MadokaMagica.mod_madokaMagica.managers.ItemSoulGemManager;
import com.MadokaMagica.mod_madokaMagica.events.MadokaMagicaWitchTransformationEvent;
import com.MadokaMagica.mod_madokaMagica.items.ItemSoulGem;
import com.MadokaMagica.mod_madokaMagica.items.ItemGriefSeed;
import com.MadokaMagica.mod_madokaMagica.util.Helper;

public class ItemSoulGem extends Item{
    public final static float MAX_DESPAIR = 100.0F;
    public final static float CREATE_WEAPON_DESPAIR = 10.0F;
    public final static float BUFF_PLAYER_DESPAIR = 20.0F;
    public final static float AMBIENT_DESPAIR = 1.0F;

    // small explosion
    public final static float SOUL_GEM_SHATTER_EXPLOSION_RADIUS = 1.0F;

    public final static float BLOCK_HURT_DAMAGE_AMOUNT = 4.0F; // 2 hearts of damage

    public static final Set softBlocks = Sets.newHashSet(new Block[] {  Blocks.sand,
                                                                        Blocks.leaves,
                                                                        Blocks.sponge,
                                                                        Blocks.web,
                                                                        Blocks.tallgrass,
                                                                        Blocks.deadbush,
                                                                        Blocks.wool,
                                                                        Blocks.yellow_flower,
                                                                        Blocks.red_flower,
                                                                        Blocks.redstone_wire,
                                                                        Blocks.wheat,
                                                                        Blocks.redstone_torch,
                                                                        Blocks.unlit_redstone_torch,
                                                                        Blocks.snow,
                                                                        Blocks.reeds,
                                                                        Blocks.portal,
                                                                        Blocks.cake,
                                                                        Blocks.vine,
                                                                        Blocks.waterlily,
                                                                        Blocks.end_portal,
                                                                        Blocks.cocoa,
                                                                        Blocks.tripwire,
                                                                        Blocks.carrots,
                                                                        Blocks.potatoes,
                                                                        Blocks.carpet,
                                                                     }
                                                        );
    public static final Set hurtBlocks = Sets.newHashSet(new Block[] {Blocks.fire});

    protected float despair;
    public EntityPlayer player;
    public PMDataTracker playerData;
    public Random random;

    public ItemSoulGem(){
        super();
        random = new Random();
        // setTextureName();
    }

    public ItemSoulGem(EntityPlayer entityPlayer, PMDataTracker pmPlayerData){
        super();
        player = entityPlayer;
        playerData = pmPlayerData;
        despair = 0;
        random = new Random();
    }

    public float getDespair(ItemStack stack){
        return stack.getTagCompound().getFloat("SG_DESPAIR");
    }

    public void setDespair(ItemStack stack, float val){
        stack.getTagCompound().setFloat("SG_DESPAIR",val);
    }
    
    public void addDespair(ItemStack stack, float val){
        this.setDespair(stack,val+this.getDespair(stack));
    }

/*
    public PMWeapon createWeapon(){
        // Do some stuff to create a weapon
        despair += ItemSoulGem.CREATE_WEAPON_DESPAIR;
    }
*/

    // TODO: Make this method smarter
    public void randomBuffPlayer(ItemStack stack){
        this.addDespair(stack,ItemSoulGem.BUFF_PLAYER_DESPAIR);
        switch(random.nextInt()*2){
            case 0:
                boostJump();
            case 1:
                boostRun();
        }
    }

    protected void boostJump(){

    }

    protected void boostRun(){

    }

    public void cleanse(ItemGriefSeed gs){
        System.out.println("Calling Deprecated method ItemSoulGem().cleanse(ItemGriefSeed)\nPlease call static method ItemSoulGem.cleanse(ItemStack,ItemStack)");
        float subtract = ItemSoulGem.MAX_DESPAIR*0.1F; // 10% of MAX_DESPAIR
        if(subtract > despair) subtract = despair;
        gs.addDespair(subtract);
        despair -= subtract;
    }

    public static void cleanse(ItemStack soulgem,ItemStack griefseed){
        float subtract = ItemSoulGem.MAX_DESPAIR*0.1F; // 10% of MAX_DESPAIR
        float despair_sg = soulgem.getTagCompound().getFloat("SG_DESPAIR");
        float despair_gs = griefseed.getTagCompound().getFloat("SG_DESPAIR");
        if(subtract > despair_sg) subtract = despair_sg;
        griefseed.getTagCompound().setFloat("SG_DESPAIR",despair_gs+subtract);
        soulgem.getTagCompound().setFloat("SG_DESPAIR",despair_sg-subtract);
    }

    @Deprecated
    public void addDespair(float despair){
        this.despair += despair;
    }

    @Deprecated
    public boolean canTransformIntoWitch(){
        return despair >= ItemSoulGem.MAX_DESPAIR;
    }

    public boolean canTransformIntoWitch(ItemStack stack){
        return this.getDespair(stack) >= ItemSoulGem.MAX_DESPAIR;
    }

    @Deprecated
    protected void transformIntoWitch(){
        // MadokaMagicaEventManager.getInstance().startEvent(new MadokaMagicaWitchTransformationEvent(this.playerData));
        // MadokaMagicaWitchTransformationEvent.getInstance().activate(this.playerData);
        MinecraftForge.EVENT_BUS.post(new MadokaMagicaWitchTransformationEvent(this.playerData));
    }

    protected void transformIntoWitch(ItemStack stack){
        MinecraftForge.EVENT_BUS.post(new MadokaMagicaWitchTransformationEvent(this.getPlayerDataTrackerFor(stack)));
    }

    // Returns a PMDataTracker for a player tied to ItemStack if one exists. Otherwise, returns null
    public PMDataTracker getPlayerDataTrackerFor(ItemStack stack){
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt != null){
            if(nbt.hasKey("PLAYER_UUID_MOST_SIG") && nbt.hasKey("PLAYER_UUID_LEAST_SIG")){
                return PlayerDataTrackerManager.getInstance().getTrackerByPlayer(
                        Helper.getPlayerOnServerByUUID(
                            new UUID(
                                nbt.getLong("PLAYER_UUID_MOST_SIG"),
                                nbt.getLong("PLAYER_UUID_LEAST_SIG")
                                )
                            )
                        );
            }
        }
        System.out.println("No Player exists for this stack.");
        return null;
    }

    // If the soul gem gets destroyed, kill the player
    // We kill the bat man
    public void destroySoulGem(ItemStack stack){
        if(this.player != null){
            this.player.worldObj.playSoundAtEntity(this.player,"madokamagica:shatter",50,1.0F);
            // TODO: What does the last boolean mean?
            this.player.worldObj.createExplosion(this.player,this.player.posX,this.player.posY,this.player.posZ,ItemSoulGem.SOUL_GEM_SHATTER_EXPLOSION_RADIUS,true);
            stack.damageItem(stack.getMaxDamage()+1,this.player); // Add 1 as well as the max damage to ensure that it is broken
            this.player.setDead();
        }
        System.out.println("Player is null! Skipping.");
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity,int par4,boolean bool){
        super.onUpdate(stack,world,entity,par4,bool);

        float worldTime = player.worldObj.getTotalWorldTime();
        if(worldTime % 23000 == 0){
            despair += AMBIENT_DESPAIR; // This may no longer be needed
            // Update the despair in the ItemStack
            int nbtDespair = stack.getTagCompound().getInteger("SG_DESPAIR");
            stack.getTagCompound().setFloat("SG_DESPAIR",nbtDespair+AMBIENT_DESPAIR);
        }

        // TODO: Fix this random number thing
        if(this.canTransformIntoWitch(stack) && (this.random.nextInt(100) == despair))
            this.transformIntoWitch();
    }

    public boolean isBlockSoft(Block block){
        return this.softBlocks.contains(block);
    }

    public boolean isBlockHurt(Block block){
        return this.hurtBlocks.contains(block);
    }

    @Override
    public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity){
        this.destroySoulGem(stack);
        return true;
    }

    @Override
    public boolean onBlockDestroyed(ItemStack stack, World world, Block block, int x, int y, int z,EntityLivingBase entity){
        if(!isBlockSoft(block))
            this.destroySoulGem(stack);
        else if(isBlockHurt(block))
            this.player.attackEntityFrom(new DamageSource("Damaged Soul Gem").setDamageBypassesArmor().setMagicDamage(),ItemSoulGem.BLOCK_HURT_DAMAGE_AMOUNT);
        else
            stack.damageItem(1,entity);

        // IMPORTANT NOTE: If this item becomes too damaged, then it will be destroyed through the destroySoulGem method
        // Unfortunately, there is no easy way to do this through the Item class.
        // So, we handle an event called PlayerDestroyItemEvent, and call the correct method in PMEventHandler where this event is handled

        return true;
    }

    @Override
    public void onCreated(ItemStack stack, World world, EntityPlayer player){
        super.onCreated(stack,world,player);
        NBTTagCompound nbt = stack.getTagCompound();
        if(nbt == null)
            nbt = new NBTTagCompound();

        nbt.setLong("PLAYER_UUID_MOST_SIG",player.getUniqueID().getMostSignificantBits());
        nbt.setLong("PLAYER_UUID_LEAST_SIG",player.getUniqueID().getLeastSignificantBits());
        nbt.setFloat("SG_DESPAIR",0);

        stack.setTagCompound(nbt);

        PMDataTracker tracker = PlayerDataTrackerManager.getInstance().getTrackerByPlayer(player);

        ItemSoulGemManager.getInstance().registerSoulGem(stack,tracker);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void getSubItems(Item item, CreativeTabs tab, List subItems){
        subItems.add(new ItemStack(this,1));
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer player, List list, boolean bool){
        String sgORgs = (this instanceof ItemGriefSeed)?"Grief Seed":"Soul Gem";
        list.add(sgORgs + " of " + playerData.getEntityName());
        list.add("Despair: " + this.getDespair(stack) + "%");

        if((this.getDespair(stack)/MAX_DESPAIR)*100 > 75)
            addDespairTooHighInformation(list);
    }

    protected void addDespairTooHighInformation(List list){
        list.add("Despair is at dangerous levels! Acquire a Grief Seed before it is too late!");
    }
}
