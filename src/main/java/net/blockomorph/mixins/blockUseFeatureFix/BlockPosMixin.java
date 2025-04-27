package net.blockomorph.mixins.blockUseFeatureFix;

import net.blockomorph.utils.accessors.BlockPosAccessor;
import net.blockomorph.utils.use.UseController;
import net.minecraft.core.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(BlockPos.class)
public abstract class BlockPosMixin implements BlockPosAccessor {
    @Unique
    UseController controller;
    //@Unique
    //private boolean getter;

    public BlockPos setUseController(UseController ctr) {
        this.controller = ctr;
        return (BlockPos)((Object) this);
    }

    public UseController getController() {
        return this.controller;
    }

    @Override
    public String toString() {
        if (this.controller != null) {
            BlockPos pos = this.controller.getOffset();
            return "Morphed player " + this.controller.getOwner() + " bounded blockpos, position in local coordinate system: " + pos.getX() + " " + pos.getY() + " " + pos.getZ();
        }
        return super.toString();
    }

    /*@Unique
    public void enableXYZdynamicGetter(boolean yes) {
        if (this.controller != null) {
            this.getter = yes;
        }
    }

    public int getX() {
        return 0;
    }

    public int getY() {
        return 0;
    }

    public int getZ() {
        return 0;
    }

    private void checkGetter(Runnable runnable) {
        if (this.controller != null) {
            runnable.run();
        } else this.getter = false;
    }*/
}
