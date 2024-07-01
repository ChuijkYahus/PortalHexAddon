package net.portalhexaddon.casting.patterns.spells

import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.spell.*
import at.petrak.hexcasting.api.spell.casting.CastingContext
import at.petrak.hexcasting.api.spell.iota.Iota
import com.mojang.math.Vector3f
import net.minecraft.world.phys.Vec3
import net.portalhexaddon.portals.PortalHexUtils
import net.portalhexaddon.portals.PortalHexUtils.Companion.PortalVecRotate
import qouteall.imm_ptl.core.api.PortalAPI
import qouteall.imm_ptl.core.portal.Portal

class OpTwoWayPortal : SpellAction {
    /**
     * The number of arguments from the stack that this action requires.
     */
    override val argc: Int = 4

    /**
     * The method called when this Action is actually executed. Accepts the [args]
     * that were on the stack (there will be [argc] of them), and the [ctx],
     * which contains things like references to the caster, the ServerLevel,
     * methods to determine whether locations and entities are in ambit, etc.
     * Returns a triple of things. The [RenderedSpell] is responsible for the spell actually
     * doing things in the world, the [Int] is how much media the spell should cost,
     * and the [List] of [ParticleSpray] renders particle effects for the result of the SpellAction.
     *
     * The [execute] method should only contain code to find the targets of the spell and validate
     * them. All the code that actually makes changes to the world (breaking blocks, teleporting things,
     * etc.) should be in the private [Spell] data class below.
     */
    override fun execute(args: List<Iota>, ctx: CastingContext): Triple<RenderedSpell, Int, List<ParticleSpray>> {
        val prtPos: Vec3 = args.getVec3(0,argc)
        val prtPosOut: Vec3 = args.getVec3(1,argc)
        val prtRot: Vec3 = args.getVec3(2,argc)
        val prtSize: Double = args.getDoubleBetween(3,1.0/10.0,10.0,argc)

        val cost = (16 * MediaConstants.CRYSTAL_UNIT * prtSize).toInt()

        val prtPos3f = Vector3f(prtPos.x.toFloat(), prtPos.y.toFloat(), prtPos.z.toFloat())

        ctx.assertVecInRange(prtPos)
        ctx.assertVecInRange(prtPosOut)

        return Triple(
            Spell(prtPos3f,prtPosOut,prtRot,prtSize),
            cost,
            listOf(ParticleSpray.burst(ctx.caster.position(), 1.0))
        )

    }

    private data class Spell(val prtPos: Vector3f, val prtPosOut: Vec3, val prtRot: Vec3, val prtSize: Double) : RenderedSpell {
        override fun cast(ctx: CastingContext) {
            val prt: Portal? = Portal.entityType.create(ctx.world)

            prt!!.originPos = Vec3(prtPos)
            prt.setDestinationDimension(ctx.world.dimension())
            prt.setDestination(prtPosOut)
            prt.setOrientationAndSize(
                PortalVecRotate(prtRot)[0],
                PortalVecRotate(prtRot)[1],
                prtSize,
                prtSize
            )
            PortalHexUtils.MakePortalNGon(prt,6, 0.0)

            val portal2 = PortalAPI.createReversePortal(prt) //Reverse makes a portal at the output
            val portal3 = PortalAPI.createFlippedPortal(prt) //Flip rotates the portal
            val portal4 = PortalAPI.createFlippedPortal(portal2)

            prt.level.addFreshEntity(prt)
            prt.level.addFreshEntity(portal2)
            prt.level.addFreshEntity(portal3)
            prt.level.addFreshEntity(portal4)
        }
    }
}
