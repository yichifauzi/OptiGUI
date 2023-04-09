package opekope2.optigui.internal.optifinecompat

import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Nameable
import opekope2.filter.*
import opekope2.optifinecompat.FilterBuilder
import opekope2.optifinecompat.properties.BeaconProperties
import opekope2.optigui.mixin.BeaconBlockEntityAccessorMixin
import opekope2.optigui.resource.Resource
import opekope2.optigui.service.RegistryLookupService
import opekope2.optigui.service.getService
import opekope2.util.NumberOrRange
import opekope2.util.TexturePath
import opekope2.util.splitIgnoreEmpty

private const val CONTAINER = "beacon"
private val texture = TexturePath.BEACON

fun createBeaconFilter(resource: Resource): FilterInfo? {
    if (resource.properties["container"] != CONTAINER) return null
    val replacement = findReplacementTexture(resource) ?: return null

    val filter = FilterBuilder.build(resource) {
        setReplaceableTextures(texture)
        addGeneralFilters<BeaconProperties>()
        addFilterForProperty("levels", { it.splitIgnoreEmpty(*delimiters) }) { levels ->
            PreProcessorFilter.nullGuarded(
                { (it.data as? BeaconProperties)?.level },
                FilterResult.Mismatch(),
                DisjunctionFilter(levels.mapNotNull { NumberOrRange.tryParse(it)?.toFilter() })
            )
        }
    }

    return FilterInfo(
        PostProcessorFilter(filter, replacement),
        setOf(texture)
    )
}

internal fun processBeacon(beacon: BlockEntity): Any? {
    if (beacon !is BeaconBlockEntity) return null
    val lookup = getService<RegistryLookupService>()

    val world = beacon.world ?: return null

    return BeaconProperties(
        name = (beacon as? Nameable)?.customName?.string,
        biome = lookup.lookupBiomeId(world, beacon.pos),
        height = beacon.pos.y,
        level = (beacon as BeaconBlockEntityAccessorMixin).level
    )
}