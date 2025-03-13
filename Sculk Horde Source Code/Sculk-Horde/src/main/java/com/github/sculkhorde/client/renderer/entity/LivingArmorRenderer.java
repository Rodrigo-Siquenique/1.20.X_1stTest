package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.LivingArmorModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.LivingArmorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;


public class LivingArmorRenderer extends GeoEntityRenderer<LivingArmorEntity> {

    private static final String LEFT_HAND = "leftItem";
    private static final String RIGHT_HAND = "rightItem";
    private static final String LEFT_BOOT = "leftBoot";
    private static final String RIGHT_BOOT = "rightBoot";
    private static final String LEFT_ARMOR_LEG = "leftLeg";
    private static final String RIGHT_ARMOR_LEG = "rightLeg";
    private static final String CHESTPLATE = "body";
    private static final String RIGHT_SLEEVE = "rightArm";
    private static final String LEFT_SLEEVE = "leftArm";
    private static final String HELMET = "head";

    protected ItemStack mainHandItem;
    protected ItemStack offhandItem;

    public LivingArmorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingArmorModel());

        this.addRenderLayer(new ItemArmorGeoLayer<LivingArmorEntity>(this) {
            @Nullable
            protected ItemStack getArmorItemForBone(GeoBone bone, LivingArmorEntity animatable) {
                ItemStack itemStack;
                switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT:
                        itemStack = this.bootsStack;
                        break;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG:
                        itemStack = this.leggingsStack;
                        break;
                    case CHESTPLATE, RIGHT_SLEEVE, LEFT_SLEEVE:
                        itemStack = this.chestplateStack;
                        break;
                    case HELMET:
                        itemStack = this.helmetStack;
                        break;
                    default:
                        itemStack = null;
                }

                return itemStack;
            }

            @Nonnull
            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, LivingArmorEntity animatable) {
                EquipmentSlot var10000;
                switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT:
                        var10000 = EquipmentSlot.FEET;
                        break;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG:
                        var10000 = EquipmentSlot.LEGS;
                        break;
                    case RIGHT_SLEEVE:
                        var10000 = !animatable.isLeftHanded() ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
                        break;
                    case LEFT_SLEEVE:
                        var10000 = animatable.isLeftHanded() ? EquipmentSlot.OFFHAND : EquipmentSlot.MAINHAND;
                        break;
                    case CHESTPLATE:
                        var10000 = EquipmentSlot.CHEST;
                        break;
                    case HELMET:
                        var10000 = EquipmentSlot.HEAD;
                        break;
                    default:
                        var10000 = super.getEquipmentSlotForBone(bone, stack, animatable);
                }

                return var10000;
            }

            @Nonnull
            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, LivingArmorEntity animatable, HumanoidModel<?> baseModel) {
                ModelPart var10000;
                switch (bone.getName()) {
                    case LEFT_BOOT, LEFT_ARMOR_LEG:
                        var10000 = baseModel.leftLeg;
                        break;
                    case RIGHT_BOOT, RIGHT_ARMOR_LEG:
                        var10000 = baseModel.rightLeg;
                        break;
                    case RIGHT_SLEEVE:
                        var10000 = baseModel.rightArm;
                        break;
                    case LEFT_SLEEVE:
                        var10000 = baseModel.leftArm;
                        break;
                    case CHESTPLATE:
                        var10000 = baseModel.body;
                        break;
                    case HELMET:
                        var10000 = baseModel.head;
                        break;
                    default:
                        var10000 = super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                }

                return var10000;
            }
        });

        this.addRenderLayer(new BlockAndItemGeoLayer<LivingArmorEntity>(this) {
            @Nullable
            protected ItemStack getStackForBone(GeoBone bone, LivingArmorEntity animatable) {
                ItemStack var10000;
                switch (bone.getName()) {
                    case LEFT_HAND:
                        var10000 = animatable.isLeftHanded() ? LivingArmorRenderer.this.mainHandItem : LivingArmorRenderer.this.offhandItem;
                        break;
                    case RIGHT_HAND:
                        var10000 = animatable.isLeftHanded() ? LivingArmorRenderer.this.offhandItem : LivingArmorRenderer.this.mainHandItem;
                        break;
                    default:
                        var10000 = null;
                }

                return var10000;
            }

            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, LivingArmorEntity animatable) {
                ItemDisplayContext displayContext;
                switch (bone.getName()) {
                    case LEFT_HAND:
                        displayContext = ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                        break;
                    case RIGHT_HAND:
                        displayContext = ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                        break;
                    default:
                        displayContext = ItemDisplayContext.NONE;
                }

                return displayContext;
            }

            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, LivingArmorEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                if (stack == LivingArmorRenderer.this.mainHandItem) {
                    //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    if (stack.getItem() instanceof ShieldItem) {
                        //poseStack.translate(0, 0.13, -1.25);
                        poseStack.translate(0.0, 0.125, -0.25);
                    }
                } else if (stack == LivingArmorRenderer.this.offhandItem) {
                    //poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    if (stack.getItem() instanceof ShieldItem) {
                        //poseStack.translate(0, 0.13, -1.25);
                        poseStack.translate(0.0, 0.125, 0.25);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay);
            }
        });

    }

    public void preRender(PoseStack poseStack, LivingArmorEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        this.mainHandItem = animatable.getMainHandItem();
        this.offhandItem = animatable.getOffhandItem();
    }

}
