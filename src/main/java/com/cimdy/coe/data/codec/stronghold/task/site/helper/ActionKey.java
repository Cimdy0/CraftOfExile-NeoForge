package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import com.cimdy.coe.client.InputState;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.LinkedList;
import java.util.List;

public class ActionKey {
    private KeyMapping key;
    private List<Modifier> modifiers;

    public ActionKey(KeyMapping key) {
        this.key = key;
        this.modifiers = new LinkedList<>();
    }

    public ActionKey(KeyMapping key, Modifier... modifiers) {
        this.key = key;
        this.modifiers = new LinkedList<>(List.of(modifiers));
    }

    public Component getText() {
        MutableComponent component = Component.empty();
        modifiers.forEach(modifier ->
                component.append(modifier.getText() + " + "));
        return component.append(Component.keybind(key.getName()));
    }

    public boolean isDown() {
        return key.isDown() && modifiers.stream().allMatch(Modifier::isDown);
    }

    public enum Modifier {
        SHIFT("Shift"),
        CTRL("Ctrl"),
        ALT("Alt");

        private final String text;

        Modifier(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public boolean isDown() {
            return switch (this) {
                case SHIFT -> InputState.isHoldingShift();
                case CTRL -> InputState.isHoldingCtrl();
                case ALT -> InputState.isHoldingAlt();
            };
        }
    }
}
