package the_fireplace.moreanvils.gui;

import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.input.Keyboard;
import the_fireplace.moreanvils.blocks.MaterialAnvil;
import the_fireplace.moreanvils.container.ContainerMaterialAnvil;
import the_fireplace.moreanvils.network.PacketDispatcher;
import the_fireplace.moreanvils.network.UpdateRenameMessage;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.HashMap;

@SideOnly(Side.CLIENT)
public class GuiMaterialAnvil extends GuiContainer implements IContainerListener {
    private static final ResourceLocation anvilResource = new ResourceLocation("textures/gui/container/anvil.png");
    public static HashMap<String, ResourceLocation> hammers = Maps.newHashMap();
    private ContainerMaterialAnvil anvil;
    private GuiTextField nameField;
    public InventoryPlayer playerInventory;
    public World anvWorld;
    public MaterialAnvil matAnv;

    public GuiMaterialAnvil(InventoryPlayer inventoryIn, World worldIn, MaterialAnvil anvil) {
        super(new ContainerMaterialAnvil(inventoryIn, worldIn, Minecraft.getMinecraft().player, anvil));
        this.playerInventory = inventoryIn;
        this.anvil = (ContainerMaterialAnvil) this.inventorySlots;
        this.anvWorld = worldIn;
        this.matAnv = anvil;
    }

    @Override
    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.nameField = new GuiTextField(0, this.fontRenderer, i + 62, j + 24, 103, 12);
        this.nameField.setTextColor(-1);
        this.nameField.setDisabledTextColour(-1);
        this.nameField.setEnableBackgroundDrawing(false);
        this.nameField.setMaxStringLength(30);
        this.inventorySlots.removeListener(this);
        this.inventorySlots.addListener(this);
    }

    @Override
    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.inventorySlots.removeListener(this);
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.fontRenderer.drawString(I18n.format("container.repair"), 60, 6, 4210752);

        if (this.anvil.maximumCost > 0) {
            int i = 8453920;
            boolean flag = true;
            String s = I18n.format("container.repair.cost", this.anvil.maximumCost);

            if (this.anvil.maximumCost >= this.anvil.maximumCap && !this.mc.player.capabilities.isCreativeMode) {
                s = I18n.format("container.repair.expensive");
                i = 16736352;
            } else if (!this.anvil.getSlot(2).getHasStack()) {
                flag = false;
            } else if (!this.anvil.getSlot(2).canTakeStack(this.playerInventory.player)) {
                i = 16736352;
            }

            if (flag) {
                int j = -16777216 | (i & 16579836) >> 2 | i & -16777216;
                int k = this.xSize - 8 - this.fontRenderer.getStringWidth(s);
                int l = 67;

                if (this.fontRenderer.getUnicodeFlag()) {
                    drawRect(k - 3, l - 2, this.xSize - 7, l + 10, -16777216);
                    drawRect(k - 2, l - 1, this.xSize - 8, l + 9, -12895429);
                } else {
                    this.fontRenderer.drawString(s, k, l + 1, j);
                    this.fontRenderer.drawString(s, k + 1, l, j);
                    this.fontRenderer.drawString(s, k + 1, l + 1, j);
                }

                this.fontRenderer.drawString(s, k, l, i);
            }
        }

        GlStateManager.enableLighting();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (this.nameField.textboxKeyTyped(typedChar, keyCode)) {
            this.renameItem();
        } else {
            super.keyTyped(typedChar, keyCode);
        }
    }

    private void renameItem() {
        String s = this.nameField.getText();
        //TODO: Make sure this works
        PacketDispatcher.sendToServer(new UpdateRenameMessage(s));
        Slot slot = this.anvil.getSlot(0);

        if (slot != null && slot.getHasStack() && !slot.getStack().hasDisplayName() && s.equals(slot.getStack().getDisplayName())) {
            s = "";
        }

        this.anvil.updateItemName(s);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        this.nameField.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        GlStateManager.disableLighting();
        GlStateManager.disableBlend();
        this.nameField.drawTextBox();
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        this.mc.getTextureManager().bindTexture(anvilResource);
        int i = (this.width - this.xSize) / 2;
        int j = (this.height - this.ySize) / 2;
        this.drawTexturedModalRect(i, j, 0, 0, this.xSize, this.ySize);
        this.drawTexturedModalRect(i + 59, j + 20, 0, this.ySize + (this.anvil.getSlot(0).getHasStack() ? 0 : 16), 110, 16);

        if ((this.anvil.getSlot(0).getHasStack() || this.anvil.getSlot(1).getHasStack()) && !this.anvil.getSlot(2).getHasStack()) {
            this.drawTexturedModalRect(i + 99, j + 45, this.xSize, 0, 28, 21);
        }
        hammers.putIfAbsent(anvil.getName(), new ResourceLocation("moreanvils:textures/gui/"+anvil.getName().toLowerCase()+"_hammer.png"));

        this.mc.getTextureManager().bindTexture(hammers.get(anvil.getName()));
        drawModalRectWithCustomSizedTexture(i+25, j+7, 0, 0, 22, 22, 22, 22);
    }

    @Override
    public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {
        this.sendSlotContents(containerToSend, 0, containerToSend.getSlot(0).getStack());
    }

    @Override
    public void sendSlotContents(@Nonnull Container containerToSend, int slotInd, @Nonnull ItemStack stack) {
        if (slotInd == 0) {
            this.nameField.setText(stack.isEmpty() ? "" : stack.getDisplayName());
            this.nameField.setEnabled(!stack.isEmpty());

            if (!stack.isEmpty()) {
                this.renameItem();
            }
        }
    }

    @Override
    public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {
    }

    @Override
    public void sendAllWindowProperties(@Nonnull Container containerIn, @Nonnull IInventory inventory) {
    }
}