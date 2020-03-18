package carpet.utils;

import net.minecraft.block.Material;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.BlockState;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.HashMap;

public class WoolTool
{
    private static final HashMap<MaterialColor,DyeColor> Material2Dye = new HashMap<>();
    static
    {
        for (DyeColor color: DyeColor.values())
        {
            Material2Dye.put(color.getMaterialColor(),color);
        }
    }

    public static DyeColor getWoolColorAtPosition(World worldIn, BlockPos pos)
    {
        BlockState state = worldIn.getBlockState(pos);
        if (state.method_26207() != Material.WOOL || !state.method_26212(worldIn, pos)) // getMaterial() //isSimpleFUllBlock
            return null;
        return Material2Dye.get(state.method_26205(worldIn, pos)); //getTopMaterialColor
    }
}
