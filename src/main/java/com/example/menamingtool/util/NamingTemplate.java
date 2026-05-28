package com.example.menamingtool.util;

import net.minecraft.nbt.NBTTagCompound;

public class NamingTemplate {

    private String template = "接口{n}";
    private int currentValue = 1;
    private int startValue = 1;
    private int step = 1;
    private String format = "%d";

    public String generateCurrentName() {
        return template.replace("{n}", formatValue(currentValue));
    }

    public void advance() {
        currentValue += step;
    }

    public void reset() {
        currentValue = startValue;
    }

    public String previewNext() {
        return template.replace("{n}", formatValue(currentValue));
    }

    private String formatValue(int value) {
        try {
            return String.format(format, value);
        } catch (Exception e) {
            return String.valueOf(value);
        }
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Template", template);
        tag.setInteger("CurrentValue", currentValue);
        tag.setInteger("StartValue", startValue);
        tag.setInteger("Step", step);
        tag.setString("Format", format);
        return tag;
    }

    public void readFromNBT(NBTTagCompound tag) {
        if (tag == null) return;
        if (tag.hasKey("Template")) template = tag.getString("Template");
        if (tag.hasKey("CurrentValue")) currentValue = tag.getInteger("CurrentValue");
        if (tag.hasKey("StartValue")) startValue = tag.getInteger("StartValue");
        if (tag.hasKey("Step")) step = tag.getInteger("Step");
        if (tag.hasKey("Format")) format = tag.getString("Format");
    }

    public String getTemplate() {
        return template;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentValue(int currentValue) {
        this.currentValue = currentValue;
    }

    public int getStartValue() {
        return startValue;
    }

    public void setStartValue(int startValue) {
        this.startValue = startValue;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
