package net.blockomorph.utils.accessors;

import net.blockomorph.utils.use.UseController;

public interface MenuAccessor {
    void boundToPlayer(UseController pl);
    UseController getPlayer();
}
