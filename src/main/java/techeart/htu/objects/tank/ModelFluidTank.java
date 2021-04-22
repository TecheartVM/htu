package techeart.htu.objects.tank;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.model.*;
import net.minecraft.util.ResourceLocation;
import techeart.htu.MainClass;
import java.util.function.Function;

public class ModelFluidTank extends Model
{
    private static final ResourceLocation TANK_TEXTURE = new ResourceLocation(MainClass.MODID, "blocks/fluid_tank.png");

    private final ModelRenderer tank;
    private final ModelRenderer fluid;
    //TODO add supports models

    public ModelFluidTank(Function<ResourceLocation, RenderType> renderTypeIn)
    {
        super(renderTypeIn);
        tank = new ModelRenderer(this, 0, 0);
        tank.addBox(0f, 0f, 0f, 12f, 16f, 12f, false);
        tank.setRotationPoint(-6f, 0f, -6f);
        tank.setTextureSize(16, 32);

        fluid = new ModelRenderer(this, 0, 0);
        fluid.addBox(0f, 0f, 0f, 11.9f, 15.9f, 11.9f, false);
        fluid.setRotationPoint(-5.95f, 0f, -5.95f);
        fluid.setTextureSize(16, 32);
    }

    public void setRotateAngle(ModelRenderer modelRenderer, float x, float y, float z)
    {
        modelRenderer.rotateAngleX = x;
        modelRenderer.rotateAngleY = y;
        modelRenderer.rotateAngleZ = z;
    }

    @Override
    public void render(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        tank.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
        renderFluid(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    protected void renderFluid(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {
        fluid.render(matrixStackIn, bufferIn, packedLightIn, packedOverlayIn, red, green, blue, alpha);
    }

    protected void renderSupports(MatrixStack matrixStackIn, IVertexBuilder bufferIn, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha)
    {

    }
}
