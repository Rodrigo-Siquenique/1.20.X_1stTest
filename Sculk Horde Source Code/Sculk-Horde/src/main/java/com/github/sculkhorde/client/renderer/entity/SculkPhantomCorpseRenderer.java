package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkPhantomCorpseModel;
import com.github.sculkhorde.common.entity.SculkPhantomCorpseEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SculkPhantomCorpseRenderer extends GeoEntityRenderer<SculkPhantomCorpseEntity> {

    public SculkPhantomCorpseRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkPhantomCorpseModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

}
