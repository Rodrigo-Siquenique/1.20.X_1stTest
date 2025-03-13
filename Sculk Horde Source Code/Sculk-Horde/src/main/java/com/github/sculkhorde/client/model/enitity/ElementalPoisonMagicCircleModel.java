package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ElementalPoisonMagicCircleAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ElementalPoisonMagicCircleModel extends DefaultedEntityGeoModel<ElementalPoisonMagicCircleAttackEntity> {
    public ElementalPoisonMagicCircleModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "elemental_poison_magic_circle"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ElementalPoisonMagicCircleAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
