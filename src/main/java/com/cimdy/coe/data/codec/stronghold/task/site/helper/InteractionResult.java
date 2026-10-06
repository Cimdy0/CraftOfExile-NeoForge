package com.cimdy.coe.data.codec.stronghold.task.site.helper;

import com.cimdy.coe.data.codec.stronghold.task.site.Site;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

import java.util.LinkedHashMap;
import java.util.Map;

public class InteractionResult {
    private Site site;
    private ActionResult action;
    private final Map<ActionKey, Component> hints;

    public InteractionResult(Site site) {
        this.site = site;
        this.action = ActionResult.NONE;
        this.hints = new LinkedHashMap<>();
    }

    public InteractionResult() {
        this(null);
    }

    public boolean isChanged() {
        return action != ActionResult.NONE;
    }

    public void setAction(ActionResult action) {
        this.action = action;
    }

    public InteractionResult setSite(Site site) {
        this.site = site;
        return this;
    }

    public Map<ActionKey, Component> getHints() {
        return hints;
    }

    public void putHint(ActionKey key, Component hint) {
        hints.put(key, hint);
    }

    public void putHint(KeyMapping key, Component hint) {
        putHint(new ActionKey(key), hint);
    }

    public void putHint(KeyMapping key, Component hint, ActionKey.Modifier... modifiers) {
        putHint(new ActionKey(key, modifiers), hint);
    }

    public Site getSite() {
        return site;
    }

    public void merge(InteractionResult interactionHint) {
        this.site = interactionHint.site;
        this.action = interactionHint.action;
        this.hints.putAll(interactionHint.hints);
    }
}
