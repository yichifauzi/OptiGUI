package opekope2.optigui.internal.mc_all

import net.minecraft.block.entity.BeaconBlockEntity
import net.minecraft.block.entity.BlockEntity
import net.minecraft.util.Nameable
import opekope2.filter.*
import opekope2.optigui.internal.properties.BeaconProperties
import opekope2.optigui.mixin.BeaconBlockEntityAccessorMixin
import opekope2.optigui.provider.IRegistryLookupProvider
import opekope2.optigui.provider.getProvider
import opekope2.optigui.resource.Resource
import opekope2.util.NumberOrRange
import opekope2.util.TexturePath
import opekope2.util.resolvePath
import opekope2.util.resolveResource
import java.io.File

private const val container = "beacon"
private val texture = TexturePath.BEACON

fun createBeaconFilter(resource: Resource): FilterInfo? {
    if (resource.properties["container"] != container) return null
    val resFolder = File(resource.id.path).parent.replace('\\', '/')
    val replacement = (resource.properties["texture"] as? String)?.let {
        resource.resourceManager.resolveResource(resolvePath(resFolder, it))
    } ?: return null

    val filters = createGeneralFilters(resource, container, texture)

    filters.addForProperty(resource, "levels") { levels ->
        TransformationFilter(
            { (it.data as? BeaconProperties)?.level },
            NullableFilter(
                skipOnNull = false,
                failOnNull = true,
                filter = DisjunctionFilter(levels.split(*delimiters).mapNotNull { NumberOrRange.parse(it)?.toFilter() })
            )
        )
    }

    return FilterInfo(
        OverridingFilter(ConjunctionFilter(filters), replacement),
        setOf(texture)
    )
}

internal fun processBeacon(beacon: BlockEntity): Any? {
    if (beacon !is BeaconBlockEntity) return null
    val lookup = getProvider<IRegistryLookupProvider>()

    val world = beacon.world ?: return null

    return BeaconProperties(
        container = container,
        texture = texture,
        name = (beacon as? Nameable)?.customName?.string,
        biome = lookup.lookupBiome(world, beacon.pos),
        height = beacon.pos.y,
        level = (beacon as BeaconBlockEntityAccessorMixin).level
    )
}