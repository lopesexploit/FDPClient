/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.features.module.modules.movement.flys.ncp

import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.modules.movement.flys.FlyMode
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.features.value.FloatValue
import net.minecraft.network.play.client.C03PacketPlayer.C04PacketPlayerPosition
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import kotlin.math.cos
import kotlin.math.sin

class BlockdropFly : FlyMode("Blockdrop") {
    private val speedValue = FloatValue("${valuePrefix}Speed", 1f, 0.1f, 5f)
    private var startx = 0f
    private var starty = 0f
    private var startz = 0f
    private var startyaw = 0f
    private var startpitch = 0f
    
    override fun onEnable() {
        startx = mc.thePlayer.posX
        starty = mc.thePlayer.posY
        startz = mc.thePlayer.posZ
        startyaw = mc.thePlayer.rotationYaw
        startpitch = mc.thePlayer.rotationPitch
    }
        

    override fun onUpdate(event: UpdateEvent) {
        if (mc.gameSettings.keyBindJump.isKeyDown) mc.thePlayer.motionY = speedValue.get()
        if (mc.gameSettings.keyBindSneak.isKeyDown) mc.thePlayer.motionY = 0f - speedValue.get()
        MovementUtils.strafe(speedValue.get())
        
        repeat(3) {
            PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(startx, starty, startz, startyaw, startpitch, false)
        }
        PacketUtils.sendPacketNoEvent(C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, startyaw, startpitch, false)
    }
    
    override fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(packet is C03PacketPlayer) {
            event.cancelEvent()
        if(packet is S08PacketPlayerPosLook) {
            startx = packet.x
            starty = packet.y
            startz = packet.z
        }
}
