package opekope2.optigui.internal.selector

import opekope2.optigui.annotation.Selector
import opekope2.optigui.api.interaction.Interaction
import opekope2.optigui.api.selector.ISelector
import opekope2.optigui.filter.DisjunctionFilter
import opekope2.optigui.filter.IFilter
import opekope2.optigui.filter.PreProcessorFilter
import opekope2.optigui.properties.IBeaconProperties
import opekope2.util.*


@Selector("beacon.levels")
object BeaconLevelSelector : ISelector {
    override fun createFilter(selector: String): IFilter<Interaction, *>? =
        selector.splitIgnoreEmpty(*delimiters)
            ?.assertNotEmpty()
            ?.map(NumberOrRange::tryParse) {
                throw RuntimeException("Invalid beacon levels: ${joinNotFound(it)}")
            }
            ?.assertNotEmpty()
            ?.let { levels ->
                PreProcessorFilter.nullGuarded(
                    { (it.data as? IBeaconProperties)?.level },
                    IFilter.Result.mismatch(),
                    DisjunctionFilter(levels.map { it.toFilter() })
                )
            }
}