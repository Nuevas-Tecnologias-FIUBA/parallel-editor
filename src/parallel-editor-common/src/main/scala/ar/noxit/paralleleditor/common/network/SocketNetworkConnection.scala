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
package ar.noxit.paralleleditor.common.network

import java.net.Socket
import java.io.{ObjectOutputStream, OutputStream, ObjectInputStream, InputStream}
import ar.noxit.paralleleditor.common.logger.Loggable

class SocketNetworkConnection(private val socket: Socket) extends NetworkConnection with Loggable {
    trace("Socket network connection created")

    override def messageOutput = new SocketMessageOutput(socket.getOutputStream)

    override def messageInput = new SocketMessageInput(socket.getInputStream)

    override def close = {
        trace("Network connection closed")
        socket.close
    }
}

class SocketMessageInput(private val inputStream: InputStream) extends MessageInput {
    private val objectInput = new ObjectInputStream(inputStream)

    override def readMessage = objectInput readObject
}

class SocketMessageOutput(private val outputStream: OutputStream) extends MessageOutput {
    private val objectOutput = new ObjectOutputStream(outputStream)

    override def writeMessage(message: Any) = objectOutput writeObject message
}
