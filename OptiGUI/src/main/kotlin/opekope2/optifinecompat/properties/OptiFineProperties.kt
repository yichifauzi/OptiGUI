package opekope2.optifinecompat.properties

import net.minecraft.util.Identifier

/**
 * General OptiFine container properties (implementation).
 */
data class OptiFineProperties(
    override val name: String?,
    override val biome: Identifier?,
    override val height: Int
) : GeneralProperties(name, biome, height)