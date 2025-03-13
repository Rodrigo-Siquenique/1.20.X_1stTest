package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkRavagerModel;
import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SculkRavagerRenderer extends GeoEntityRenderer<SculkRavagerEntity>
{
    public SculkRavagerRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkRavagerModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }
}
