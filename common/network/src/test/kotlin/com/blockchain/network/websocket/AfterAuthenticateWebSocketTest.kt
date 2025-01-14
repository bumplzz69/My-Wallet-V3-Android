package com.blockchain.network.websocket

import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.rxjava3.disposables.Disposable
import org.amshove.kluent.`should be equal to`
import com.nhaarman.mockitokotlin2.mock
import org.junit.Test

class AfterAuthenticateWebSocketTest {

    @Test
    fun `method runs after successful authentication`() {
        var counter = 0
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                counter++
                mock()
            }
        counter `should be equal to` 0
        webSocket.open()
        mockConnection.simulateAuthenticated()
        counter `should be equal to` 1
    }

    @Test
    fun `method doesn't run after non authentication connection events`() {
        var counter = 0
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                counter++
                mock()
            }
        counter `should be equal to` 0
        webSocket.open()
        mockConnection.simulateSuccess()
        mockConnection.simulateFailure()
        mockConnection.simulateDisconnect()
        counter `should be equal to` 0
    }

    @Test
    fun `close closes other`() {
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                mock()
            }
        webSocket.close()
        verify(mockConnection.mock).close()
    }

    @Test
    fun `after close the method is not called on successful authentication`() {
        var counter = 0
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                counter++
                mock()
            }
        counter `should be equal to` 0
        webSocket.open()
        webSocket.close()
        mockConnection.simulateAuthenticated()
        counter `should be equal to` 0
    }

    @Test
    fun `after close and open, the method is called on successful authentication`() {
        var counter = 0
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                counter++
                mock()
            }
        counter `should be equal to` 0
        webSocket.open()
        webSocket.close()
        webSocket.open()
        mockConnection.simulateAuthenticated()
        counter `should be equal to` 1
    }

    @Test
    fun `after close the result of the after open is disposed`() {
        val disposable = mock<Disposable>()
        val mockConnection = MockConnection()
        val webSocket = (mock<WebSocket<String, String>>() + mockConnection)
            .afterAuthenticate {
                disposable
            }
        webSocket.open()
        mockConnection.simulateAuthenticated()
        verify(disposable, never()).dispose()
        webSocket.close()
        verify(disposable).dispose()
    }
}
