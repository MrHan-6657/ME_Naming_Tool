package com.example.menamingtool.gui;

import com.example.menamingtool.item.ItemMENamingTool;
import com.example.menamingtool.network.PacketHandler;
import com.example.menamingtool.network.PacketSyncTemplate;
import com.example.menamingtool.util.NamingTemplate;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

/**
 * 命名模板设置GUI。
 * 玩家潜行右键空气时打开，用于配置命名规则。
 * 界面包含5个文本框和3个按钮，底部实时预览下一个名称。
 */
@SideOnly(Side.CLIENT)
public class GuiNamingTemplate extends GuiScreen {

    private final EntityPlayer player;
    private final NamingTemplate template;

    // 文本框：模板、起始值、当前值、步进、数字格式
    private GuiTextField templateField;
    private GuiTextField startValueField;
    private GuiTextField currentValueField;
    private GuiTextField stepField;
    private GuiTextField formatField;

    // GUI左上角坐标，用于居中定位
    private int guiLeft, guiTop;

    public GuiNamingTemplate(EntityPlayer player) {
        this.player = player;
        this.template = new NamingTemplate();

        // 从手持物品加载已保存的模板
        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() instanceof ItemMENamingTool) {
            NamingTemplate loaded = ItemMENamingTool.loadTemplateFromNBT(held);
            if (loaded != null) {
                this.template.readFromNBT(loaded.writeToNBT());
            }
        }
    }

    @Override
    public void initGui() {
        // 居中计算
        this.guiLeft = (this.width - 240) / 2;
        this.guiTop = (this.height - 180) / 2;

        int x = guiLeft + 10;
        int y = guiTop + 25;
        int fieldWidth = 220;
        int fieldHeight = 20;

        // 第一行：命名模板输入框
        this.templateField = new GuiTextField(this.fontRendererObj, x, y, fieldWidth, fieldHeight);
        this.templateField.setMaxStringLength(64);
        this.templateField.setText(template.getTemplate());

        // 第二行：起始值、当前值、步进（三个紧凑排列）
        y += 35;
        this.startValueField = new GuiTextField(this.fontRendererObj, x, y, 70, fieldHeight);
        this.startValueField.setText(String.valueOf(template.getStartValue()));

        this.currentValueField = new GuiTextField(this.fontRendererObj, x + 75, y, 70, fieldHeight);
        this.currentValueField.setText(String.valueOf(template.getCurrentValue()));

        this.stepField = new GuiTextField(this.fontRendererObj, x + 150, y, 70, fieldHeight);
        this.stepField.setText(String.valueOf(template.getStep()));

        // 第三行：数字格式输入框
        y += 35;
        this.formatField = new GuiTextField(this.fontRendererObj, x, y, fieldWidth, fieldHeight);
        this.formatField.setMaxStringLength(32);
        this.formatField.setText(template.getFormat());

        // 底部三个按钮
        int btnY = guiTop + 145;
        int btnWidth = 70;
        this.buttonList.add(new GuiButton(0, guiLeft + 10, btnY, btnWidth, 20,
                I18n.format("menamingtool.gui.confirm")));
        this.buttonList.add(new GuiButton(1, guiLeft + 85, btnY, btnWidth, 20,
                I18n.format("menamingtool.gui.reset")));
        this.buttonList.add(new GuiButton(2, guiLeft + 160, btnY, btnWidth, 20,
                I18n.format("menamingtool.gui.cancel")));
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();

        int x = guiLeft + 10;
        int y = guiTop + 10;

        // 标签文字
        this.drawString(this.fontRendererObj,
                I18n.format("menamingtool.gui.template"), x, y, 0xFFFFFF);

        y = guiTop + 60;
        this.drawString(this.fontRendererObj,
                I18n.format("menamingtool.gui.start"), x, y, 0xFFFFFF);
        this.drawString(this.fontRendererObj,
                I18n.format("menamingtool.gui.current"), x + 75, y, 0xFFFFFF);
        this.drawString(this.fontRendererObj,
                I18n.format("menamingtool.gui.step"), x + 150, y, 0xFFFFFF);

        y = guiTop + 95;
        this.drawString(this.fontRendererObj,
                I18n.format("menamingtool.gui.format"), x, y, 0xFFFFFF);

        // 下一个名称预览（不递进编号，仅预览）
        y = guiTop + 120;
        String preview = I18n.format("menamingtool.gui.preview") + ": " + template.previewNext();
        this.drawString(this.fontRendererObj, preview, x, y, 0xAAAAAA);

        // 绘制所有文本框
        this.templateField.drawTextBox();
        this.startValueField.drawTextBox();
        this.currentValueField.drawTextBox();
        this.stepField.drawTextBox();
        this.formatField.drawTextBox();

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        switch (button.id) {
            case 0: // 确定：保存模板到物品NBT并关闭GUI
                saveAndClose();
                break;
            case 1: // 重置：恢复默认值并刷新文本框
                template.reset();
                refreshFields();
                break;
            case 2: // 取消：关闭GUI不保存
                this.mc.displayGuiScreen(null);
                break;
        }
    }

    /** 从文本框读取数值，保存到物品NBT，发送同步包到服务端 */
    private void saveAndClose() {
        readFields();

        ItemStack held = player.getHeldItem();
        if (held != null && held.getItem() instanceof ItemMENamingTool) {
            ItemMENamingTool.saveTemplateToNBT(held, template);
            // 同步到服务端，确保服务端使用最新的模板数据
            PacketHandler.INSTANCE.sendToServer(new PacketSyncTemplate(template));
        }

        this.mc.displayGuiScreen(null);
    }

    /** 从文本框读取用户输入并更新模板对象 */
    private void readFields() {
        template.setTemplate(templateField.getText());

        try {
            template.setStartValue(Integer.parseInt(startValueField.getText()));
        } catch (NumberFormatException ignored) {}

        try {
            template.setCurrentValue(Integer.parseInt(currentValueField.getText()));
        } catch (NumberFormatException ignored) {}

        try {
            template.setStep(Integer.parseInt(stepField.getText()));
        } catch (NumberFormatException ignored) {}

        template.setFormat(formatField.getText());
    }

    /** 用模板对象的数据刷新所有文本框内容 */
    private void refreshFields() {
        this.templateField.setText(template.getTemplate());
        this.startValueField.setText(String.valueOf(template.getStartValue()));
        this.currentValueField.setText(String.valueOf(template.getCurrentValue()));
        this.stepField.setText(String.valueOf(template.getStep()));
        this.formatField.setText(template.getFormat());
    }

    /** 将键盘输入优先路由到当前聚焦的文本框 */
    @Override
    protected void keyTyped(char typedChar, int keyCode) {
        if (this.templateField.textboxKeyTyped(typedChar, keyCode)) return;
        if (this.startValueField.textboxKeyTyped(typedChar, keyCode)) return;
        if (this.currentValueField.textboxKeyTyped(typedChar, keyCode)) return;
        if (this.stepField.textboxKeyTyped(typedChar, keyCode)) return;
        if (this.formatField.textboxKeyTyped(typedChar, keyCode)) return;
        super.keyTyped(typedChar, keyCode);
    }

    /** 将鼠标点击优先路由到对应的文本框 */
    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        this.templateField.mouseClicked(mouseX, mouseY, mouseButton);
        this.startValueField.mouseClicked(mouseX, mouseY, mouseButton);
        this.currentValueField.mouseClicked(mouseX, mouseY, mouseButton);
        this.stepField.mouseClicked(mouseX, mouseY, mouseButton);
        this.formatField.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    /** GUI打开时不暂停游戏 */
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
