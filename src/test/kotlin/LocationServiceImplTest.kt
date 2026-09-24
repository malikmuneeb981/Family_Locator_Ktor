package com.example

import com.example.data.LocationServiceImpl
import domain.models.Latlng
import domain.models.MyLatLng
import io.ktor.websocket.CloseReason
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketExtension
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlinx.coroutines.CompletableJob
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.coroutines.CoroutineContext
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class FakeWebSocketSession : WebSocketSession {
    val outgoingFrames = mutableListOf<Frame>()
    private val outgoingChannel = Channel<Frame>(Channel.UNLIMITED)
    private val incomingChannel = Channel<Frame>(Channel.UNLIMITED)

    override val coroutineContext: CoroutineContext = Job()
    override val extensions: List<WebSocketExtension<*>> = emptyList()
    override val incoming: ReceiveChannel<Frame> = incomingChannel
    override var masking: Boolean = false
    override var maxFrameSize: Long = Long.MAX_VALUE
    override val outgoing: SendChannel<Frame> = outgoingChannel

    override suspend fun flush() {}

    @Deprecated("Use cancel() instead", ReplaceWith("cancel()"))
    override fun terminate() {}

    override suspend fun send(frame: Frame) {
        outgoingFrames.add(frame)
    }
}

class LocationServiceImplTest {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    @Test
    fun `test multi-user group location broadcasting`() = runBlocking {
        val service = LocationServiceImpl()

        val aliceSession = FakeWebSocketSession()
        val bobSession = FakeWebSocketSession()
        val charlieSession = FakeWebSocketSession()

        // 3 users join the same family group
        service.onJoin("alice", "s1", aliceSession, "family_1")
        service.onJoin("bob", "s2", bobSession, "family_1")
        service.onJoin("charlie", "s3", charlieSession, "family_1")

        // Alice sends her location
        val aliceLocation = MyLatLng(
            groupId = "family_1",
            latlng = Latlng(37.7749, -122.4194)
        )
        service.broadcastLocation("alice", aliceLocation)

        // Alice shouldn't receive her own broadcast
        assertEquals(0, aliceSession.outgoingFrames.size)

        // Bob and Charlie should receive Alice's location
        assertEquals(1, bobSession.outgoingFrames.size)
        assertEquals(1, charlieSession.outgoingFrames.size)

        val bobReceived = json.decodeFromString<MyLatLng>((bobSession.outgoingFrames[0] as Frame.Text).readText())
        assertEquals("alice", bobReceived.senderName)
        assertEquals(37.7749, bobReceived.latlng?.latitude)
        assertEquals(-122.4194, bobReceived.latlng?.longitude)

        // Bob disconnects
        service.onDisconnect("bob")

        // Charlie broadcasts his location
        val charlieLocation = MyLatLng(
            groupId = "family_1",
            latlng = Latlng(40.7128, -74.0060)
        )
        service.broadcastLocation("charlie", charlieLocation)

        // Alice should receive Charlie's location (total 1 frame)
        assertEquals(1, aliceSession.outgoingFrames.size)
        // Bob shouldn't receive anything new because he disconnected (still 1 frame)
        assertEquals(1, bobSession.outgoingFrames.size)
    }
}

