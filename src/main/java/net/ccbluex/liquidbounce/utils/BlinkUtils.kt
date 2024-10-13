/*
 * FDPClient Hacked Client
 * A free open source mixin-based injection hacked client for Minecraft using Minecraft Forge by LiquidBounce.
 * https://github.com/SkidderMC/FDPClient/
 */
package net.ccbluex.liquidbounce.utils

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPacket
import net.ccbluex.liquidbounce.utils.PacketUtils.sendPackets
import net.ccbluex.liquidbounce.utils.misc.RandomUtils
import net.minecraft.client.entity.EntityOtherPlayerMP
import net.minecraft.network.Packet
import net.minecraft.network.handshake.client.C00Handshake
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.C01PacketChatMessage
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S02PacketChat
import net.minecraft.network.play.server.S29PacketSoundEffect
import net.minecraft.network.status.client.C00PacketServerQuery
import net.minecraft.network.status.client.C01PacketPing
import net.minecraft.util.Vec3
import java.math.BigInteger
import java.util.*

object BlinkUtils : MinecraftInstance() {
    val publicPacket: Packet<*>? = null
    val packets = mutableListOf<Packet<*>>()
    val packetsReceived = mutableListOf<Packet<*>>()
    private var fakePlayer: EntityOtherPlayerMP? = null
    val positions = mutableListOf<Vec3>()
    private val playerBuffer = LinkedList<Packet<INetHandlerPlayServer>>()

    const val Invalid_Type = -301
    const val MisMatch_Type = -302
    var movingPacketStat = false
    var transactionStat = false
    var keepAliveStat = false
    var actionStat = false
    var abilitiesStat = false
    var invStat = false
    var interactStat = false
    var otherPacket = false

    val isBlinking: Boolean
        get() = packets.size + packetsReceived.size > 0

    private var packetToggleStat = BooleanArray(26) { false }

    init {
        setBlinkState(off = true, release = true)
        clearPacket()
    }

    fun blink(packet: Packet<*>, event: PacketEvent, sent: Boolean = true, receive: Boolean = true) {
        val player = mc.thePlayer ?: return

        if (event.isCancelled || player.isDead) return

        if (isBlacklisted(packet.javaClass.simpleName)) return


        when (packet) {
            is C00Handshake, is C00PacketServerQuery, is C01PacketPing, is S02PacketChat, is C01PacketChatMessage -> {
                return
            }
            is S29PacketSoundEffect -> {
                if (packet.soundName == "game.player.hurt") return
            }
        }

        if (mc.currentServerData != null) {
            if (sent && !receive) {
                if (event.eventType == EventState.RECEIVE) {
                    synchronized(packetsReceived) {
                        PacketUtils.queuedPackets.addAll(packetsReceived)
                    }
                    packetsReceived.clear()
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    synchronized(packets) {
                        packets += packet
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) { positions += packetPos }
                    }
                }
            } else if (receive && !sent) {
                if (event.eventType == EventState.RECEIVE && player.ticksExisted > 10) {
                    event.cancelEvent()
                    synchronized(packetsReceived) { packetsReceived += packet }
                }
                if (event.eventType == EventState.SEND) {
                    synchronized(packets) {
                        sendPackets(*packets.toTypedArray(), triggerEvents = false)
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) { positions += packetPos }
                    }
                    packets.clear()
                }
            } else if (sent && receive) {
                if (event.eventType == EventState.RECEIVE && player.ticksExisted > 10) {
                    event.cancelEvent()
                    synchronized(packetsReceived) { packetsReceived += packet }
                }
                if (event.eventType == EventState.SEND) {
                    event.cancelEvent()
                    synchronized(packets) {
                        packets += packet
                    }
                    if (packet is C03PacketPlayer && packet.isMoving) {
                        val packetPos = Vec3(packet.x, packet.y, packet.z)
                        synchronized(positions) { positions += packetPos }
                    }
                }
            }
        }

        if (!sent && !receive)
            unblink()
    }

    fun releasePacket(packetType: String? = null, onlySelected: Boolean = false, amount: Int = -1, minBuff: Int = 0) {
        var count = 0
        when (packetType) {
            null -> {
                for (packet in playerBuffer) {
                    val packetID = BigInteger(packet.javaClass.simpleName.substring(1..2), 16).toInt()
                    if (packetToggleStat[packetID] || !onlySelected) {
                        sendPacket(packet)
                    }
                }
            }
            else -> {
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packet in playerBuffer) {
                    if (packet.javaClass.simpleName.equals(packetType, ignoreCase = true)) {
                        tempBuffer.add(packet)
                    }
                }
                while (tempBuffer.size > minBuff && (count < amount || amount <= 0)) {
                    sendPacket(tempBuffer.pop())
                    count++
                }
            }
        }
        clearPacket(packetType, onlySelected, count)
    }

    fun clearPacket(packetType: String? = null, onlySelected: Boolean = false, amount: Int = -1) {
        when (packetType) {
            null -> {
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packet in playerBuffer) {
                    val packetID = BigInteger(packet.javaClass.simpleName.substring(1..2), 16).toInt()
                    if (!packetToggleStat[packetID] && onlySelected) tempBuffer.add(packet)
                }
                playerBuffer.clear()
                playerBuffer.addAll(tempBuffer)
            }
            else -> {
                var count = 0
                val tempBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
                for (packet in playerBuffer) {
                    if (!packet.javaClass.simpleName.equals(packetType, ignoreCase = true)) {
                        tempBuffer.add(packet)
                    } else if (count++ > amount) {
                        tempBuffer.add(packet)
                    }
                }
                playerBuffer.clear()
                playerBuffer.addAll(tempBuffer)
            }
        }
    }

    fun pushPacket(packet: Packet<*>): Boolean {
        val packetID = BigInteger(packet.javaClass.simpleName.substring(1..2), 16).toInt()
        return if (packetToggleStat[packetID] && !isBlacklisted(packet.javaClass.simpleName)) {
            playerBuffer.add(packet as Packet<INetHandlerPlayServer>)
            true
        } else false
    }

    private fun isBlacklisted(packetType: String): Boolean {
        return when (packetType) {
            "C00Handshake", "C00PacketLoginStart", "C00PacketServerQuery", "C01PacketChatMessage", "C01PacketEncryptionResponse", "C01PacketPing" -> true
            else -> false
        }
    }

    fun setBlinkState(off: Boolean = false, release: Boolean = false, all: Boolean = false) {
        if (release) releasePacket()
        movingPacketStat = (movingPacketStat && !off) || all
        transactionStat = (transactionStat && !off) || all
        keepAliveStat = (keepAliveStat && !off) || all
        actionStat = (actionStat && !off) || all
        abilitiesStat = (abilitiesStat && !off) || all
        invStat = (invStat && !off) || all
        interactStat = (interactStat && !off) || all
        otherPacket = (otherPacket && !off) || all
        for (i in packetToggleStat.indices) {
            packetToggleStat[i] = all || when (i) {
                0x00 -> keepAliveStat
                0x01, 0x11, 0x12, 0x14, 0x15, 0x17, 0x18, 0x19 -> otherPacket
                0x03, 0x04, 0x05, 0x06 -> movingPacketStat
                0x0F -> transactionStat
                0x02, 0x09, 0x0A, 0x0B -> actionStat
                0x0C, 0x13 -> abilitiesStat
                0x0D, 0x0E, 0x10, 0x16 -> invStat
                0x07, 0x08 -> interactStat
                else -> false
            }
        }
    }

    fun bufferSize(packetType: String? = null): Int {
        return if (packetType == null) playerBuffer.size else {
            val tempBuffer = playerBuffer.filter { it.javaClass.simpleName.equals(packetType, ignoreCase = true) }
            if (tempBuffer.isNotEmpty()) tempBuffer.size else MisMatch_Type
        }
    }

    fun syncSent() {
        synchronized(packetsReceived) {
            PacketUtils.queuedPackets.addAll(packetsReceived)
            packetsReceived.clear()
        }
    }

    fun syncReceived() {
        synchronized(packets) {
            sendPackets(*packets.toTypedArray(), triggerEvents = false)
            packets.clear()
        }
    }

    fun cancel() {
        val player = mc.thePlayer ?: return
        val firstPosition = positions.firstOrNull() ?: return

        player.setPositionAndUpdate(firstPosition.xCoord, firstPosition.yCoord, firstPosition.zCoord)

        synchronized(packets) {
            val iterator = packets.iterator()
            while (iterator.hasNext()) {
                val packet = iterator.next()
                if (packet is C03PacketPlayer) {
                    iterator.remove()
                } else {
                    sendPacket(packet)
                    iterator.remove()
                }
            }
        }

        synchronized(positions) {
            positions.clear()
        }

        // Remove fake player
        fakePlayer?.apply {
            fakePlayer?.entityId?.let { mc.theWorld?.removeEntityFromWorld(it) }
            fakePlayer = null
        }
    }

    fun unblink() {
        synchronized(packetsReceived) { PacketUtils.queuedPackets.addAll(packetsReceived) }
        synchronized(packets) { sendPackets(*packets.toTypedArray(), triggerEvents = false) }
        clear()

        fakePlayer?.apply {
            mc.theWorld?.removeEntityFromWorld(entityId)
            fakePlayer = null
        }
    }

    fun clear() {
        synchronized(packetsReceived) { packetsReceived.clear() }
        synchronized(packets) { packets.clear() }
        synchronized(positions) { positions.clear() }
    }

    fun addFakePlayer() {
        val player = mc.thePlayer ?: return
        val world = mc.theWorld ?: return

        fakePlayer = EntityOtherPlayerMP(world, player.gameProfile).apply {
            rotationYawHead = player.rotationYawHead
            renderYawOffset = player.renderYawOffset
            copyLocationAndAnglesFrom(player)
            rotationYawHead = player.rotationYawHead
            inventory = player.inventory
            world.addEntityToWorld(RandomUtils.nextInt(Int.MIN_VALUE, Int.MAX_VALUE), this)
        }
    }

    @EventTarget
    fun onWorld(event: WorldEvent) {
        if (event.worldClient == null) {
            clear()
        }
    }
}
