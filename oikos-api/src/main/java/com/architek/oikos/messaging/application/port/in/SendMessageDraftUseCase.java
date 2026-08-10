package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.SendMessageDraftCommand;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;

public interface SendMessageDraftUseCase {

    /** Promotes the draft into a real GROUP conversation or the property's BROADCAST channel
     * (whichever the draft's own isBroadcast flag says), deletes the draft, and returns the
     * resulting conversation id - same reference shape startConversation/broadcast already
     * return, so the frontend can navigate identically. */
    ConversationId send(SendMessageDraftCommand command);
}
