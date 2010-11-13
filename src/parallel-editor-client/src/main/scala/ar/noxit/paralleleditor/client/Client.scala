package ar.noxit.paralleleditor.client

import actors.Actor
import converter.DefaultResponseConverter
import remote.RemoteServerProxy
import java.net.Socket
import ar.noxit.paralleleditor.common.network.SocketNetworkConnection
import ar.noxit.paralleleditor.common.converter.{DefaultRemoteOperationConverter, DefaultMessageConverter}

trait Documents {
    /**
     * See ClientMessages.scala
     */
    def process(msg: CommandFromKernel)
}

trait LocalClientActorFactory {
    def newLocalClientActor: Actor
}

class InternalClientActorFactory(private val docs: Documents) extends LocalClientActorFactory {
    val clientActor = new ClientActor(docs)
    clientActor.responseConverter = new DefaultResponseConverter
    clientActor.converter = new DefaultMessageConverter(new DefaultRemoteOperationConverter)

    override def newLocalClientActor = clientActor
}

object SessionFactory {
    def newSession(host: String, port: Int, adapter: Documents): Actor = {
        val socket = new Socket(host, port)
        val factory = new InternalClientActorFactory(adapter)

        // TODO resolver el tema de la conexión, que se cierra on disconnect
        new RemoteServerProxy(new SocketNetworkConnection(socket), factory)
        factory.clientActor
    }
}
