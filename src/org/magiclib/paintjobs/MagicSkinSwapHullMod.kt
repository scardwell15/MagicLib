package org.magiclib.paintjobs

import com.fs.starfarer.api.campaign.CampaignUIAPI
import com.fs.starfarer.api.campaign.econ.MarketAPI
import com.fs.starfarer.api.combat.BaseHullMod
import com.fs.starfarer.api.combat.ShipAPI
import com.fs.starfarer.api.ui.TooltipMakerAPI

class MagicSkinSwapHullMod : BaseHullMod() {
    companion object {
        const val ID = "ML_skinSwap"
        const val PAINTJOB_TAG_PREFIX = "ML_paintjob-"
    }

    override fun applyEffectsAfterShipCreation(ship: ShipAPI?, id: String?) {
        super.applyEffectsAfterShipCreation(ship, id)
        ship ?: return
        id ?: return

        if (!MagicPaintjobManager.isEnabled) {
            return
        }

        val paintjob = getAppliedPaintjob(ship) ?: return

        MagicPaintjobManager.applyPaintjob(null, ship, paintjob)
    }

    private fun getAppliedPaintjob(ship: ShipAPI): MagicPaintjobSpec? {
        // (the tag should only be on the variant, not the ship, but I don't trust myself)
        val tag = (ship.tags + ship.variant.tags).firstOrNull { it.startsWith(PAINTJOB_TAG_PREFIX) }
        return tag?.removePrefix(PAINTJOB_TAG_PREFIX)?.let { paintjobId ->
            MagicPaintjobManager.getPaintjob(paintjobId)
        }
    }

    override fun canBeAddedOrRemovedNow(
        ship: ShipAPI?,
        marketOrNull: MarketAPI?,
        mode: CampaignUIAPI.CoreUITradeMode?
    ): Boolean = ship != null && getAppliedPaintjob(ship)?.isPermament != true

    override fun addPostDescriptionSection(
        tooltip: TooltipMakerAPI?,
        hullSize: ShipAPI.HullSize?,
        ship: ShipAPI?,
        width: Float,
        isForModSpec: Boolean
    ) {
        super.addPostDescriptionSection(tooltip, hullSize, ship, width, isForModSpec)
        ship ?: return

        val skins = MagicPaintjobManager.getPaintjobsForHull(ship.hullSpec.hullId)
        skins.forEach { skin ->
            tooltip?.addPara(skin.name, 10f)
        }
    }
}