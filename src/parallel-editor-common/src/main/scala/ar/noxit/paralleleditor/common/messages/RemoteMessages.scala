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
package ar.noxit.paralleleditor.common.messages

import scala.serializable

/**
 * Clase base de los mensajes remotos
 */
@serializable
sealed trait BaseRemoteMessage

/**
 * Mensajes que van hacia el kernel y que deben convertirse antes de ser enviados
 */
@serializable
sealed trait ToKernel

/**
 * Mensajes enviados desde el cliente que tienen destino al kernel, son todos request
 */
@serializable
sealed trait Request

/**
 * Mensaje proveniente del kernel que son respuestas a mensajes
 */
@serializable
sealed trait Response

/**
 * Nivel documento
 */

/**
 * Clase base para las operaciones sobre documentos
 */
sealed trait RemoteOperation extends BaseRemoteMessage

/**
 * Agregar texto
 */
case class RemoteAddText(val text: String, val startPos: Int, val pword: List[Int]) extends RemoteOperation

/**
 * Borrar texto
 */
case class RemoteDeleteText(val startPos: Int, val size: Int) extends RemoteOperation

/**
 * Null operation
 */
case class RemoteNullOpText() extends RemoteOperation

/**
 * A nivel Sincronismo
 */

/**
 * información de sincronización del documento
 */
@serializable
case class SyncStatus(val myMsgs: Int, val otherMessages: Int)

/**
 * Sincronizar operaciones
 */
case class SyncOperation(val syncSatus: SyncStatus, val payload: RemoteOperation) extends BaseRemoteMessage

/**
 * A nivel kernel
 */

/**
 * Operacion sobre un determinado documento
 */
case class RemoteDocumentOperation(val docTitle: String, val payload: SyncOperation) extends BaseRemoteMessage


/**
 * Pide un nuevo documento
 */
case class RemoteNewDocumentRequest(val title: String, val initialContent: String = "") extends BaseRemoteMessage with ToKernel with Request

/**
 * Suscribirse a un doc existe
 */
case class RemoteSubscribeRequest(val title: String) extends BaseRemoteMessage with ToKernel with Request

/**
 *
 */
case class RemoteDocumentTitleExists(val offenderTitle: String) extends BaseRemoteMessage with Response

/**
 * Suscripcion aceptada
 */
case class RemoteDocumentSubscriptionResponse(val docTitle: String, val initialContent: String) extends BaseRemoteMessage with Response

/**
 * Ya existe subscripcion
 */
case class RemoteDocumentSubscriptionAlreadyExists(val offenderTitle: String) extends BaseRemoteMessage with Response

/**
 * No existe subscripcion
 */
case class RemoteDocumentSubscriptionNotExists(val offenderTitle: String) extends BaseRemoteMessage with Response

/**
 * No se puede borrar el documento, está en uso
 */
case class RemoteDocumentInUse(val docTitle: String) extends BaseRemoteMessage with Response

/**
 * El doc se borró ok
 */
case class RemoteDocumentDeletedOk(val docTitle: String) extends BaseRemoteMessage with Response

/**
 * El titulo no existe
 */
case class RemoteDocumentDeletionTitleNotExists(val docTitle: String) extends BaseRemoteMessage with Response

/**
 * Pide listado de usuarios
 */
case class RemoteUserListRequest() extends BaseRemoteMessage with Request with ToKernel

/**
 * Pide listado de documentos
 */
case class RemoteDocumentListRequest() extends BaseRemoteMessage with ToKernel with Request

/**
 * Respuesta listado de usuarios
 */
case class RemoteUserListResponse(val usernames: Map[String, List[String]]) extends BaseRemoteMessage with Response

/**
 * Respuesta de listado de documentos
 */
case class RemoteDocumentListResponse(val docList: List[String]) extends BaseRemoteMessage with Response

/**
 * Pide desuscriberse a un doc
 */
case class RemoteUnsubscribeRequest(val title: String) extends BaseRemoteMessage with Request

/**
 * Pide al kernel que borre un documento
 */
case class RemoteDeleteDocumentRequest(val docTitle: String) extends BaseRemoteMessage with Request with ToKernel

/**
 * Pide login
 */
case class RemoteLoginRequest(val username: String) extends BaseRemoteMessage with Request

/**
 * Rta de login OK
 */
case class RemoteLoginOkResponse() extends BaseRemoteMessage with Response

/**
 * Rta de Login Erroneo
 */
trait RemoteLoginRefusedRemoteResponse extends BaseRemoteMessage with Response

/**
 * Nombre de usuario tomado
 */
case class UsernameAlreadyExistsRemoteResponse() extends RemoteLoginRefusedRemoteResponse with Response

/**
 * Pedido de logout
 */
case class RemoteLogoutRequest() extends BaseRemoteMessage

/**
 * Nueva sesión iniciada
 */
case class RemoteNewUserLoggedIn(val username: String) extends BaseRemoteMessage with Response

/**
 * Sesión cerrada
 */
case class RemoteUserLoggedOut(val username: String) extends BaseRemoteMessage with Response

/**
 * Nueva suscripcion a un documento
 */
case class RemoteNewSubscriberToDocument(val username: String, val docTitle: String) extends BaseRemoteMessage with Response

/**
 * Desuscripcion a un documento
 */
case class RemoteSubscriberLeftDocument(val username: String, val docTitle: String) extends BaseRemoteMessage with Response

/**
 * desuscripcion del document ok
 */
case class RemoteSubscriptionCancelled(val docTitle: String) extends BaseRemoteMessage with Response

/**
 * Cuando no existe documento en la suscripcion
 */
case class RemoteDocumentNotExists(val offenderTitle: String) extends BaseRemoteMessage with Response

/**
 * Mensaje de chat de otro usuario
 */
case class RemoteChatMessage(val username: String, val message: String) extends BaseRemoteMessage with Response

/**
 * Send message
 */
case class RemoteSendChatMessage(val message: String) extends BaseRemoteMessage with Request with ToKernel
