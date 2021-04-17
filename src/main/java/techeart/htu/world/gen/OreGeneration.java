package techeart.htu.world.gen;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.registries.ForgeRegistries;
import techeart.htu.MainClass;

@Mod.EventBusSubscriber(modid = MainClass.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class OreGeneration
{


    @SubscribeEvent
    public static void generateOres(FMLLoadCompleteEvent event)
    {
        for(Biome biome : ForgeRegistries.BIOMES)
        {
            //nether generation
            if(biome.getCategory() == Biome.Category.NETHER)
            {

            }
            //end generation
            else if(biome.getCategory() == Biome.Category.THEEND)
            {

            }
            //overworld generation
            else
            {

            }
        }
    }

    private static void generateOre(Biome biome, int veinCount, int bottomOffset, int topOffset, int maxHeight, int veinSize, OreFeatureConfig.FillerBlockType fillerBlock, BlockState ore)
    {

    }
}
