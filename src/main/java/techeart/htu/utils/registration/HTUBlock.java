package techeart.htu.utils.registration;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class HTUBlock extends Block
{
    public HTUBlock(Properties props)
    {
        super(props);
    }

    public static class Builder
    {
        @Nullable
        private Block.Properties properties = AbstractBlock.Properties.create(Material.AIR);

        @Nullable
        private Supplier<? extends Block> block;

        public HTUBlock.Builder withProperties(Block.Properties properties) {
            this.properties = properties;
            return this;
        }
        public HTUBlock.Builder setBlock(Supplier<? extends Block> block) {// HTUBlock.class
            this.block = block;
            return this;
        }
//TODO: Make this thing more flexible
        public Supplier<? extends Block> build() {
            if(block == null)
                return () -> new Block(properties);
            return block;
        }
    }
}
