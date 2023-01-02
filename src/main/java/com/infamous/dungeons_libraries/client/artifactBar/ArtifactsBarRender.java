package com.infamous.dungeons_libraries.client.artifactBar;

import com.infamous.dungeons_libraries.client.gui.elementconfig.GuiElementConfig;
import com.infamous.dungeons_libraries.client.gui.elementconfig.GuiElementConfigRegistry;
import com.infamous.dungeons_libraries.integration.curios.client.CuriosKeyBindings;
import com.infamous.dungeons_libraries.items.artifacts.ArtifactItem;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MainWindow;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Optional;

import static com.infamous.dungeons_libraries.DungeonsLibraries.MODID;
import static com.infamous.dungeons_libraries.items.ItemTagWrappers.CURIOS_ARTIFACTS;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = MODID)
public class ArtifactsBarRender {
    private static final ResourceLocation ARTIFACT_BAR_RESOURCE = new ResourceLocation(MODID, "textures/gui/artifact_bar.png");
    public static final int SOUL_LEVEL_COLOR = 0x10B0E4;

    @SubscribeEvent
    public static void displyArtifactBar(RenderGameOverlayEvent.Post event) {
        final Minecraft mc = Minecraft.getInstance();
        if(mc != null && CURIOS_ARTIFACTS.getValues().isEmpty()) return;

        if (event.getType().equals(RenderGameOverlayEvent.ElementType.HOTBAR) && mc.getCameraEntity() instanceof PlayerEntity) {
            GuiElementConfig guiElementConfig = GuiElementConfigRegistry.getConfig(new ResourceLocation(MODID, "artifact_bar"));
            if(guiElementConfig.isHidden()) return;

            PlayerEntity renderPlayer = (PlayerEntity) mc.getCameraEntity();
            if(renderPlayer == null) return;

            MainWindow sr = event.getWindow();
            int scaledWidth = sr.getGuiScaledWidth();
            int scaledHeight = sr.getGuiScaledHeight();
            int x = guiElementConfig.getXPosition(scaledWidth);
            int y = guiElementConfig.getYPosition(scaledHeight);

            CuriosApi.getCuriosHelper().getCuriosHandler(renderPlayer).ifPresent(iCuriosItemHandler -> {
                renderBar(event.getMatrixStack(), mc, renderPlayer, x, y, iCuriosItemHandler);
            });

            mc.getTextureManager().bind(AbstractGui.GUI_ICONS_LOCATION);
        }

    }

    private static void renderBar(MatrixStack matrixStack, Minecraft mc, PlayerEntity renderPlayer, int x, int y, ICuriosItemHandler iCuriosItemHandler) {
        Optional<ICurioStacksHandler> artifactStackHandler = iCuriosItemHandler.getStacksHandler("artifact");
        if (artifactStackHandler.isPresent()) {
            int slots = artifactStackHandler.get().getStacks().getSlots();
            renderSlotBg(matrixStack, mc, x, y, slots);
            for(int slot = 0; slot < slots; slot++) {
                ItemStack artifact = artifactStackHandler.get().getStacks().getStackInSlot(slot);
                if (!artifact.isEmpty() && artifact.getItem() instanceof ArtifactItem) {
                    int xPos = x + slot * 20 +3;
                    int yPos = y +3;
                    renderSlot(matrixStack, mc, xPos, yPos, renderPlayer, artifact);
                }
                renderSlotKeybind(matrixStack, mc, x, y, slot);
            }
        }
    }

    private static void renderSlot(MatrixStack matrixStack, Minecraft mc,int xPos, int yPos, PlayerEntity renderPlayer, ItemStack artifactStack) {
        if (!artifactStack.isEmpty()) {
            float f = (float)artifactStack.getPopTime() - 0;
            if (f > 0.0F) {
                RenderSystem.pushMatrix();
                float f1 = 1.0F + f / 5.0F;
                RenderSystem.translatef((float)(xPos + 8), (float)(yPos + 12), 0.0F);
                RenderSystem.scalef(1.0F / f1, (f1 + 1.0F) / 2.0F, 1.0F);
                RenderSystem.translatef((float)(-(xPos + 8)), (float)(-(yPos + 12)), 0.0F);
            }

            Minecraft.getInstance().getItemRenderer().renderAndDecorateItem(renderPlayer, artifactStack, xPos, yPos);
            if (f > 0.0F) {
                RenderSystem.popMatrix();
            }

            Minecraft.getInstance().getItemRenderer().renderGuiItemDecorations(Minecraft.getInstance().font, artifactStack, xPos, yPos);
        }
    }

    private static void renderSlotBg(MatrixStack matrixStack, Minecraft mc, int xPos, int yPos, int slots) {
        mc.getTextureManager().bind(ARTIFACT_BAR_RESOURCE);
//        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.enableAlphaTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        AbstractGui.blit(matrixStack, xPos, yPos, 0, 0, 62, 22, 62, 22);
        RenderSystem.disableAlphaTest();
        RenderSystem.disableBlend();
    }

    private static void renderSlotKeybind(MatrixStack matrixStack, Minecraft mc, int x, int y, int slot) {
        String keybind = "";
        if(slot == 0) {
            KeyBinding keybinding = CuriosKeyBindings.activateArtifact1;
            keybind = getString(keybinding);
        }else if(slot == 1) {
            KeyBinding keybinding = CuriosKeyBindings.activateArtifact2;
            keybind = getString(keybinding);
        }else if(slot == 2) {
            KeyBinding keybinding = CuriosKeyBindings.activateArtifact3;
            keybind = getString(keybinding);
        }
        int keybindWidth = mc.font.width(keybind);
        int xPosition = x + 1 + slot * 20 + 18 - keybindWidth;
        int yPosition = y + 3;
        AbstractGui.drawString(matrixStack, mc.font, keybind, xPosition, yPosition, 0xFFFFFF);
    }

    private static String getString(KeyBinding keybinding) {
//        return keybinding.getKeyModifier().getCombinedName(keybinding.getKey(), () -> keybinding.getKey().getDisplayName()).getString();
        return keybinding.getKey().getDisplayName().getString();
    }

}
