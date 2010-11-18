package ar.noxit.paralleleditor.kernel.actors.converter

import ar.noxit.paralleleditor.kernel.actors.RemoteMessageConverter
import ar.noxit.paralleleditor.kernel.messages._
import ar.noxit.paralleleditor.common.messages._

class DefaultRemoteMessageConverter extends RemoteMessageConverter {
    override def convert(remote: ToRemote) = {
        remote match {
            case SubscriptionAlreadyExists(offenderTitle) =>
                RemoteDocumentSubscriptionAlreadyExists(offenderTitle)

            case SubscriptionNotExists(offenderTitle) =>
                RemoteDocumentSubscriptionNotExists(offenderTitle)

            case DocumentListResponse(docList) =>
                RemoteDocumentListResponse(docList)

            case SubscriptionResponse(docSession, initialContent) =>
                RemoteDocumentSubscriptionResponse(docSession.title, initialContent)

            case DocumentTitleExists(offenderTitle) =>
                RemoteDocumentTitleExists(offenderTitle)

            case DocumentInUse(docTitle) =>
                RemoteDocumentInUse(docTitle)

            case DocumentDeletedOk(docTitle) =>
                RemoteDocumentDeletedOk(docTitle)

            case DocumentDeletionTitleNotExists(docTitle) =>
                RemoteDocumentDeletionTitleNotExists(docTitle)

            case UserListResponse(usernames) =>
                RemoteUserListResponse(usernames)

            case NewUserLoggedIn(username) =>
                RemoteNewUserLoggedIn(username)

            case UserLoggedOut(username) =>
                RemoteUserLoggedOut(username)

            case NewSubscriberToDocument(username, docTitle) =>
                RemoteNewSubscriberToDocument(username, docTitle)

            case SubscriberLeftDocument(username, docTitle) =>
                RemoteSubscriberLeftDocument(username, docTitle)

            case DocumentNotExists(offenderTitle) =>
                RemoteDocumentNotExists(offenderTitle)

            case SubscriptionCancelled(docTitle) =>
                RemoteSubscriptionCancelled(docTitle)
        }
    }
}
