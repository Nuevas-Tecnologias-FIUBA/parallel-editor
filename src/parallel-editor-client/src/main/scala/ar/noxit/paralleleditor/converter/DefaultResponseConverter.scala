package ar.noxit.paralleleditor.converter

import ar.noxit.paralleleditor.common.messages._
import ar.noxit.paralleleditor._
import common.logger.Loggable

class DefaultResponseConverter extends ResponseConverter with Loggable {
    override def convert(response: Response) = {
        response match {
            case RemoteDocumentSubscriptionResponse(docTitle, initialContent) => {
                trace("RemoteDocumentSubscriptionResponse received")
                DocumentSubscription(docTitle, initialContent)
            }
            case RemoteDocumentSubscriptionAlreadyExists(offenderTitle) => {
                trace("RemoteDocumentSubscriptionAlreadyExists")
                DocumentSubscriptionAlreadyExists(offenderTitle)
            }
            case RemoteDocumentSubscriptionNotExists(offenderTitle) => {
                trace("RemoteDocumentSubscriptionNotExists")
                DocumentSubscriptionNotExists(offenderTitle)
            }
            case RemoteDocumentListResponse(l) => {
                trace("RemoteDocumentListResponse %s", l)
                DocumentListUpdate(l)
            }
            case RemoteDocumentTitleExists(offenderTitle) => {
                trace("RemoteDocumentTitleExists %s", offenderTitle)
                DocumentTitleTaken(offenderTitle)
            }

            case r: RemoteLoginRefusedRemoteResponse => {
                trace("login refused from kernel.")

                r match {
                    case UsernameAlreadyExistsRemoteResponse() => {
                        trace("username already exists")
                        UsernameTaken()
                    }
                }
            }

            case RemoteLoginOkResponse() => {
                trace("login accepted from kernel.")
                LoginOk()
            }
        }
    }
}
