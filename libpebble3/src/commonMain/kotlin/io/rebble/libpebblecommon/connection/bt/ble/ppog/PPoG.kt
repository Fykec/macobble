package io.rebble.libpebblecommon.connection.bt.ble.ppog

import androidx.compose.ui.util.fastForEachReversed
import co.touchlab.kermit.Logger
import io.ktor.utils.io.writeByteArray
import io.rebble.libpebblecommon.BleConfigFlow
import io.rebble.libpebblecommon.connection.ConnectionException
import io.rebble.libpebblecommon.connection.ConnectionFailureReason
import io.rebble.libpebblecommon.connection.PebbleProtocolStreams
import io.rebble.libpebblecommon.connection.bt.ble.BlePlatformConfig
import io.rebble.libpebblecommon.di.ConnectionCoroutineScope
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.selects.select
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.min
import kotlin.time.Duration.Companion.seconds

interface PPoGPacketSender {
    suspend fun sendPacket(packet: ByteArray): Boolean
    fun wasRestoredWithSubscribedCentral(): Boolean
}

class PPoGStream(val inboundPPoGBytesChannel: Channel<ByteArray> = Channel(capacity = 100))

class PPoG(
    private val pebbleProtocolStreams: PebbleProtocolStreams,
    private val pPoGStream: PPoGStream,
    private val pPoGPacketSender: PPoGPacketSender,
    private val bleConfig: BleConfigFlow,
    private val blePlatformConfig: BlePlatformConfig,
    private val scope: ConnectionCoroutineScope,
) {
    private val logger = Logger.withTag("PPoG")
    private var mtu: Int = blePlatformConfig.initialMtu
    private var closed = false

    fun run(reversed: Boolean = false) {
        scope.launch {
            val params = if (reversed) {
                // Reversed PPoG: the watch is server-side and, once the phone
                // subscribes to the notify characteristic, sits in
                // AwaitingResetRequest waiting for us to initiate. Skip the
                // initWaitingForResetRequest phase entirely.
                withTimeoutOrNull(12.seconds) { initWithResetRequest() }
                    ?: throw ConnectionException(ConnectionFailureReason.TimeoutInitializingPpog)
            } else {
                withTimeoutOrNull(12.seconds) {
                    initWaitingForResetRequest()
                } ?: withTimeoutOrNull(5.seconds) {
                    if (blePlatformConfig.fallbackToResetRequest) {
                        initWithResetRequest()
                    } else {
                        null
                    }
                } ?: throw ConnectionException(ConnectionFailureReason.TimeoutInitializingPpog)
            }
            runConnection(params)
        }
    }

    private fun verboseLog(message: () -> String) {
        if (bleConfig.value.verbosePpogLogging) {
            logger.v(message = message)
        }
    }

    suspend fun close() {
        if (closed) return
        closed = true
        logger.d("close")
        if (blePlatformConfig.sendPpogResetOnDisconnection) {
            // This really for iOS, where the "connection" will stay alive when the app "disconnects",
            // but we need to get the watch's PPoG state machine into a "need to reconnect" state.
            try {
                sendPacketImmediately(PPoGPacket.ResetRequest(0, PPoGVersion.ONE), PPoGVersion.ONE)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.w("couldn't send PPoG reset on close", e)
            }
        }
    }

    private suspend inline fun <reified T : PPoGPacket> waitForPacket(): T {
        // Wait for reset request from watch
        while (true) {
            val packet = pPoGStream.inboundPPoGBytesChannel.receive().asPPoGPacket()
            if (packet !is T) {
                // Not expected, but can happen (i.e. don't crash out): if a watch reconnects
                // really quickly then we can see stale packets come through.
                logger.w("unexpected packet $packet waiting for ${T::class}")
                continue
            }
            return packet
        }
    }

    private suspend fun initWithResetRequest(): PPoGConnectionParams? {
        logger.d("initWithResetRequest")
        // Reversed PPoG doesn't have a meta characteristic, so we have to assume.
        val ppogVersion = PPoGVersion.ONE

        // Send reset request
        try {
            sendPacketImmediately(
                packet = PPoGPacket.ResetRequest(
                    sequence = 0,
                    ppogVersion = ppogVersion,
                ),
                version = ppogVersion,
            )
        } catch (e: CancellationException) {
            throw  e
        } catch (e: Exception) {
            logger.e("error sending reset request", e)
        }

        // The watch gives up on our reset request after a few seconds and starts its own. Answer
        // that instead of waiting for a ResetComplete that is never coming.
        while (true) {
            val packet = pPoGStream.inboundPPoGBytesChannel.receive().asPPoGPacket()
            when (packet) {
                is PPoGPacket.ResetComplete -> {
                    logger.d("got $packet")
                    sendResetComplete(ppogVersion)
                    return connectionParams(packet, ppogVersion)
                }

                is PPoGPacket.ResetRequest -> {
                    logger.d("got $packet while waiting for ResetComplete; responding")
                    return respondToResetRequest(packet)
                }

                else -> logger.w("unexpected packet $packet waiting for ResetComplete")
            }
        }
    }

    // Negotiate connection
    private suspend fun initWaitingForResetRequest(): PPoGConnectionParams {
        logger.d("initWaitingForResetRequest")

        val resetRequest = waitForPacket<PPoGPacket.ResetRequest>()
        logger.d("got $resetRequest")
        return respondToResetRequest(resetRequest)
    }

    private suspend fun respondToResetRequest(resetRequest: PPoGPacket.ResetRequest): PPoGConnectionParams {
        sendResetComplete(resetRequest.ppogVersion)

        // Wait for reset complete confirmation
        val resetComplete = waitForPacket<PPoGPacket.ResetComplete>()
        logger.d("got $resetComplete")

        return connectionParams(resetComplete, resetRequest.ppogVersion)
    }

    private suspend fun sendResetComplete(version: PPoGVersion) {
        sendPacketImmediately(
            packet = PPoGPacket.ResetComplete(
                sequence = 0,
                rxWindow = min(blePlatformConfig.desiredRxWindow, MAX_SUPPORTED_WINDOW_SIZE),
                txWindow = min(blePlatformConfig.desiredTxWindow, MAX_SUPPORTED_WINDOW_SIZE),
            ),
            version = version,
        )
    }

    private fun connectionParams(
        resetComplete: PPoGPacket.ResetComplete,
        version: PPoGVersion,
    ) = PPoGConnectionParams(
        rxWindow = min(min(resetComplete.txWindow, blePlatformConfig.desiredTxWindow), MAX_SUPPORTED_WINDOW_SIZE),
        txWindow = min(min(resetComplete.rxWindow, blePlatformConfig.desiredRxWindow), MAX_SUPPORTED_WINDOW_SIZE),
        pPoGversion = version,
    )

    /**
     * Runs PPoG sessions. The watch resets the session mid-transfer when its own ack timeouts run
     * out, so renegotiate and resend the packets it never acked rather than dropping the connection:
     * a reset then costs a round trip instead of the whole transfer.
     */
    private suspend fun runConnection(initialParams: PPoGConnectionParams) {
        var params = initialParams
        var resendAfterReset: List<ByteArray> = emptyList()
        var resets = 0
        while (true) {
            when (val outcome = runSession(params, resendAfterReset)) {
                is SessionOutcome.Closed -> return
                is SessionOutcome.Reset -> {
                    resets++
                    if (resets > MAX_SESSION_RESETS) {
                        logger.w("PPoG reset $resets times; giving up")
                        throw IllegalStateException("Watch keeps resetting PPoG - disconnect and reconnect")
                    }
                    logger.w(
                        "in-session ${outcome.packet} from watch; renegotiating and resending " +
                                "${outcome.unackedPayloads.size} unacked packets"
                    )
                    resendAfterReset = outcome.unackedPayloads
                    params = when (val packet = outcome.packet) {
                        is PPoGPacket.ResetRequest -> respondToResetRequest(packet)
                        is PPoGPacket.ResetComplete -> {
                            sendResetComplete(params.pPoGversion)
                            connectionParams(packet, params.pPoGversion)
                        }

                        else -> error("unexpected reset packet $packet")
                    }
                }
            }
        }
    }

    // No need for any locking - state is only accessed/mutated within this method (except for mtu
    // which can only increase).
    private suspend fun runSession(
        params: PPoGConnectionParams,
        resendAfterReset: List<ByteArray>,
    ): SessionOutcome {
        logger.d("runSession: $params")

        val outboundSequence = Sequence()
        val inboundSequence = Sequence()
        val outboundDataQueue = ArrayDeque<PacketToSend>()
        val inflightPackets = ArrayDeque<PacketToSend>()
        val onTimeout = Channel<Unit>()
        var timeoutJob: Job? = null
        var lastSentAck: PPoGPacket.Ack? = null
        var lastReceivedAck: PPoGPacket.Ack? = null

        // Data the watch never acked before it reset the session, already split into chunks.
        resendAfterReset.forEach { payload ->
            outboundDataQueue.addLast(
                PacketToSend(
                    packet = PPoGPacket.Data(sequence = outboundSequence.getThenIncrement(), data = payload),
                    attemptCount = 0,
                )
            )
        }

        fun cancelTimeout() {
            timeoutJob?.cancel()
            timeoutJob = null
        }

        fun rescheduleTimeout() {
            cancelTimeout()
            timeoutJob = scope.launch {
                delay(RESET_REQUEST_TIMEOUT)
                logger.w("Packet timeout")
                onTimeout.send(Unit)
                timeoutJob = null
            }
        }

        // Moves packets back to the queue in place (rather than copying) so that every payload is
        // in exactly one of the queue and the in-flight list - that invariant is what lets a reset
        // resend precisely the unacked data.
        fun resendInflightPackets() {
            inflightPackets.fastForEachReversed { packet ->
                packet.attemptCount++
                if (packet.attemptCount > MAX_NUM_RETRIES) {
                    logger.w("Exceeded max retries")
                    throw IllegalStateException("Exceeded max retries")
                }
                outboundDataQueue.addFirst(packet)
            }
            inflightPackets.clear()
        }

        fun removeResendsUpTo(sequence: Int) {
            while (true) {
                val sendPacket = outboundDataQueue.firstOrNull()
                if (sendPacket == null || sendPacket.attemptCount == 0 || sendPacket.packet.sequence > sequence) {
                    break
                }
                outboundDataQueue.removeFirst()
            }
        }

        while (true) {
            // Drain send queue. Runs before waiting so a session that starts with packets to
            // resend (after a watch-initiated reset) gets going immediately.
            while (inflightPackets.size < params.txWindow && !outboundDataQueue.isEmpty()) {
                if (closed) return SessionOutcome.Closed
                val packet = outboundDataQueue.removeFirst()
                sendPacketImmediately(packet.packet, params.pPoGversion)
                rescheduleTimeout()
                inflightPackets.add(packet)
            }
            var resetOutcome: SessionOutcome.Reset? = null
            select {
                onTimeout.onReceive {
                    resendInflightPackets()
                }
                pebbleProtocolStreams.outboundPPBytes.onReceive { bytes ->
                    bytes.asList().chunked(maxDataBytes())
                        .map { chunk ->
                            PacketToSend(
                                packet = PPoGPacket.Data(
                                    sequence = outboundSequence.getThenIncrement(),
                                    data = chunk.toByteArray()
                                ),
                                attemptCount = 0,
                            )
                        }
                        .forEach {
                            outboundDataQueue.addLast(it)
                        }
                }
                pPoGStream.inboundPPoGBytesChannel.onReceive { bytes ->
                    val packet = bytes.asPPoGPacket()
                    verboseLog { "received packet: $packet" }
                    when (packet) {
                        is PPoGPacket.Ack -> {
                            removeResendsUpTo(packet.sequence)
                            if (packet == lastReceivedAck) {
                                logger.w("Received duplicate ACK; resending inflight packets")
                                resendInflightPackets()
                            }
                            // TODO remove resends of this packet from send queue (+ also remove up-to-them, which OG code didn't do?)

                            // Remove from in-flight packets, up until (including) this packet
                            // TODO warn if we don't have that packet inflight?
                            while (true) {
                                val inflightPacket = inflightPackets.removeFirstOrNull() ?: break
                                if (inflightPacket.packet.sequence == packet.sequence) break
                            }
                            if (inflightPackets.isEmpty()) {
                                cancelTimeout()
                            } else {
                                // Measure the timeout from the last ack progress, not the last send:
                                // a busy link keeps acking and must not look stalled.
                                rescheduleTimeout()
                            }
                            lastReceivedAck = packet
                        }

                        is PPoGPacket.Data -> {
                            if (packet.sequence != inboundSequence.get()) {
                                logger.w("data out of sequence; resending last ack")
                                lastSentAck?.let { sendPacketImmediately(it, params.pPoGversion) }
                            } else {
                                pebbleProtocolStreams.inboundPPBytes.writeByteArray(packet.data)
                                pebbleProtocolStreams.inboundPPBytes.flush()
                                inboundSequence.increment()
                                // TODO coalesced ACKing
                                lastSentAck = PPoGPacket.Ack(sequence = packet.sequence)
                                    .also { sendPacketImmediately(it, params.pPoGversion) }
                            }
                        }

                        // The watch sends these when its ack timeouts have run out: it has dropped
                        // its side of the session, so renegotiate and resend what it never acked.
                        is PPoGPacket.ResetComplete, is PPoGPacket.ResetRequest -> {
                            resetOutcome = SessionOutcome.Reset(
                                packet = packet,
                                unackedPayloads = inflightPackets.map { it.packet.data } +
                                        outboundDataQueue.map { it.packet.data },
                            )
                        }
                    }
                }
            }
            resetOutcome?.let { return it }
        }
    }

    private fun maxDataBytes() = mtu - DATA_HEADER_OVERHEAD_BYTES

    private suspend fun sendPacketImmediately(packet: PPoGPacket, version: PPoGVersion) {
        if (packet is PPoGPacket.Data || packet is PPoGPacket.Ack) {
            verboseLog { "sendPacketImmediately: $packet" }
        } else {
            logger.d { "sendPacketImmediately: $packet" }
        }
        if (!pPoGPacketSender.sendPacket(packet.serialize(version))) {
            logger.e("Couldn't send packet!")
            throw IllegalStateException("Couldn't send packet!")
        }
    }

    fun updateMtu(mtu: Int) {
        if (mtu < this.mtu) throw IllegalStateException("Can't reduce MTU")
        this.mtu = mtu
    }
}

private const val DATA_HEADER_OVERHEAD_BYTES = 1 + 3
private const val MAX_SEQUENCE = 32
private const val MAX_NUM_RETRIES = 2

// Renegotiations tolerated within one connection before giving up and letting the connection drop.
private const val MAX_SESSION_RESETS = 3
private val RESET_REQUEST_TIMEOUT = 10.seconds

private data class PPoGConnectionParams(
    val rxWindow: Int,
    val txWindow: Int,
    val pPoGversion: PPoGVersion,
)

private data class PacketToSend(
    val packet: PPoGPacket.Data,
    var attemptCount: Int,
)

private sealed class SessionOutcome {
    object Closed : SessionOutcome()

    /** The watch reset PPoG mid-session; [unackedPayloads] still have to reach it. */
    data class Reset(
        val packet: PPoGPacket,
        val unackedPayloads: List<ByteArray>,
    ) : SessionOutcome()
}

private class Sequence {
    private var sequence = 0

    fun getThenIncrement(): Int {
        val currentSequence = sequence
        increment()
        return currentSequence
    }

    fun get(): Int = sequence

    fun increment() {
        sequence = (sequence + 1) % MAX_SEQUENCE
    }
}
