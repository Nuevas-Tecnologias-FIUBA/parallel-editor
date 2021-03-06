/*
 *  A real-time collaborative tool to develop files over the network.
 *  Copyright (C) 2010  Mauro Ciancio and Leandro Gilioli
 *                      {maurociancio,legilioli} at gmail dot com
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package ar.noxit.paralleleditor.kernel

import actors.converter.DefaultRemoteMessageConverter
import actors.{ClientActor, KernelActor}
import basic.BasicKernel
import messages.{UserLoggedOut, NewUserLoggedIn}
import org.junit._
import org.scalatest.junit.AssertionsForJUnit
import org.easymock.EasyMock._
import ar.noxit.paralleleditor.common.network.SenderActor
import ar.noxit.paralleleditor.common.remote.{TerminateActor, NetworkActors, Peer}
import ar.noxit.paralleleditor.common.messages._

@Test
class NotificationTest extends AssertionsForJUnit {
    var kernel: BasicKernel = _

    @Before
    def setUp: Unit = {
        kernel = new BasicKernel
        kernel.timeout = 5000
    }

    @Test
    def testNotificationAtLogin: Unit = {
        val callback1 = createMock(classOf[UpdateCallback])
        callback1 update NewUserLoggedIn("username2")
        replay(callback1)

        val session1 = kernel.login("username1")
        session1.installOnUpdateCallback(callback1)

        val session2 = kernel.login("username2")

        verify(callback1)
    }

    @Test
    def testNotificationAtLoginAndLogout: Unit = {
        val callback1 = createMock(classOf[UpdateCallback])
        callback1 update NewUserLoggedIn("username2")
        callback1 update UserLoggedOut("username2")
        replay(callback1)

        val session1 = kernel.login("username1")
        session1.installOnUpdateCallback(callback1)

        val session2 = kernel.login("username2")
        session2.logout

        verify(callback1)
    }

    @Test
    def testNotificationAtLoginAndLogoutWithMoreThanOneUser: Unit = {
        val callback1 = createMock(classOf[UpdateCallback])
        callback1 update NewUserLoggedIn("username2")
        callback1 update NewUserLoggedIn("username3")
        callback1 update NewUserLoggedIn("username4")
        callback1 update UserLoggedOut("username2")
        callback1 update UserLoggedOut("username3")
        callback1 update UserLoggedOut("username4")
        replay(callback1)

        val session1 = kernel.login("username1")
        session1.installOnUpdateCallback(callback1)

        val session2 = kernel.login("username2")
        val session3 = kernel.login("username3")
        val session4 = kernel.login("username4")
        session2.logout
        session3.logout
        session4.logout

        verify(callback1)
    }

    object NullPeer extends Peer {
        def disconnect = {}
    }

    @Test
    def testNotificationWithActors: Unit = {
        val ka = new KernelActor(kernel)
        ka.start

        // cliente 1
        val gateway1 = createStrictMock(classOf[SenderActor])
        gateway1 ! RemoteLoginOkResponse()
        gateway1 ! RemoteNewUserLoggedIn("username2")
        gateway1 ! RemoteUserLoggedOut("username2")
        replay(gateway1)

        val client1 = new ClientActor(ka, NullPeer)
        client1.remoteConverter = new DefaultRemoteMessageConverter
        client1.start

        client1 ! NetworkActors(gateway1, null)
        client1 ! RemoteLoginRequest("username1")

        // cliente 2
        val gateway2 = createStrictMock(classOf[SenderActor])
        gateway2 ! RemoteLoginOkResponse()
        gateway2 ! TerminateActor()
        replay(gateway2)

        Thread.sleep(1000)
        val client2 = new ClientActor(ka, NullPeer)
        client2.remoteConverter = new DefaultRemoteMessageConverter
        client2.start

        client2 ! NetworkActors(gateway2, null)
        client2 ! RemoteLoginRequest("username2")
        client2 ! RemoteLogoutRequest()

        Thread.sleep(1000)
        verify(gateway1, gateway2)
    }
}
