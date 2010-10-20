package ar.noxit.paralleleditor.kernel.actors

import ar.noxit.paralleleditor.kernel.callback.ActorCallback
import scala.actors._
import ar.noxit.paralleleditor.common.logger.Loggable
import ar.noxit.paralleleditor.kernel.messages._
import ar.noxit.paralleleditor.common.messages._
import ar.noxit.paralleleditor.kernel.{Session, DocumentSession}
import ar.noxit.paralleleditor.common.remote.{TerminateActor, NetworkActors, Peer}
import ar.noxit.paralleleditor.common.converter._
import ar.noxit.paralleleditor.common.operation.DocumentOperation
import reflect.BeanProperty

class ClientActor(private val kernel: Actor, private val client: Peer) extends Actor with Loggable {
    private var docSessions: List[DocumentSession] = List()
    private val timeout = 5000
    private val maxLoginTries = 5

    private var listener: Actor = _
    private var gateway: Actor = _
    private var session: Session = _

    @BeanProperty
    var converter: RemoteDocumentOperationConverter = _

    override def act = {
        trace("Starting")

        // receive network actors
        val (gateway, listener) = receiveNetworkActors
        this.listener = listener
        this.gateway = gateway

        // wait for session
        session = this.receiveSession(loginTries = 0)

        // process messages
        processMessages
    }

    protected def processMessages() {
        loop {
            react {
                case RemoteNewDocumentRequest(title, initialContent) => {
                    trace("New Document Requested=[%s] content=[%s]", title, initialContent)

                    kernel ! NewDocumentRequest(session, title, initialContent)
                }

                case SubscriptionResponse(docSession, initialContent) => {
                    trace("Received Document Session")

                    docSessions = docSession :: docSessions
                    gateway ! RemoteDocumentSubscriptionResponse(docSession.title, initialContent)
                }

                case RemoteDocumentListRequest() => {
                    trace("Document List Requested")

                    kernel ! DocumentListRequest(session)
                }

                case DocumentListResponse(docList) => {
                    trace("Document List Response")

                    gateway ! RemoteDocumentListResponse(docList)
                }

                case RemoteSubscribeRequest(title) => {
                    trace("RemoteSubscribeRequest")

                    kernel ! SubscribeToDocumentRequest(session, title)
                }
                case RemoteUnsubscribeRequest(title) => {
                    trace("RemoteUnsubscribeRequest")

                    docSessions.find {s => s.title == title}.foreach {docSession => docSession.unsubscribe}
                    docSessions = docSessions.filter {docSession => docSession.title != title}
                }

                case RemoteDocumentOperation(docTitle, payload) => {
                    trace("remove operation received")

                    val converter = new DefaultMessageConverter(new DefaultRemoteOperationConverter)
                    val message = converter.convert(payload)

                    docSessions.find {s => s.title == docTitle}.foreach {ds => ds applyChange message}
                }

                // estos mensajes vienen de los documentos y se deben propagar al cliente
                case PublishOperation(title, m) => {
                    trace("operation received from document")

                    val converted = converter.convert(new DocumentOperation(title, m))
                    gateway ! converted
                }

                case TerminateActor() => {
                    trace("Exit received")
                    doExit
                }

                case RemoteLogoutRequest() => {
                    trace("Logout Requested")
                    doExit
                }

                case message: Any => {
                    trace("unkown message received %s", message)
                }
            }
        }
    }

    private def receiveNetworkActors = {
        trace("Waiting for actors")

        receiveWithin(timeout) {
            case NetworkActors(gateway, listener) => {
                trace("network actors received")
                (gateway, listener)
            }
            case TIMEOUT => doTimeout
        }
    }

    private def receiveSession(loginTries: Int = 0): Session = {
        trace("Waiting for username")

        if (loginTries >= maxLoginTries) {
            warn("max login tries reached")
            doTimeout
        }

        // receive username
        val username = receiveWithin(timeout) {
            case RemoteLoginRequest(username) => {
                trace("Username received=[%s]", username)
                username
            }
            case TIMEOUT => doTimeout
        }

        // login to kernel and wait for response
        trace("Sending login request")

        // logging into the kernel
        kernel ! LoginRequest(username)

        trace("Waiting for session")

        // we expect a session in order to continue
        receiveWithin(timeout) {
            case LoginResponse(session) => {
                trace("Session received")
                // notify client logged in
                gateway ! RemoteLoginOkResponse()

                // install callback
                installCallback(session)

                session
            }
            case UsernameAlreadyExists() => {
                trace("username already exists")

                gateway ! UsernameAlreadyExistsRemoteResponse()
                receiveSession(loginTries + 1)
            }
            case TIMEOUT => doTimeout
        }
    }

    protected def installCallback(session: Session) {
        // install callback
        session.installOnUpdateCallback(new ActorCallback(this))
    }

    private def doTimeout = {
        trace("timeout")
        doExit
    }

    private def doExit = {
        warn("Client actor exiting")

        if (gateway != null)
            gateway ! TerminateActor()
        if (listener != null)
            listener ! TerminateActor()
        if (session != null)
            session.logout

        client.disconnect
        exit
    }
}
