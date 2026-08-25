package com.architek.oikos.messaging.application.port.in;

import com.architek.oikos.messaging.application.command.MarkConversationUnreadCommand;

/**
 * Repasse une conversation en non lue pour l'appelant - l'inverse de
 * MarkConversationReadUseCase, et le geste qu'on attend d'une boîte mail quand
 * on veut retomber dessus plus tard.
 */
public interface MarkConversationUnreadUseCase {

    void markUnread(MarkConversationUnreadCommand command);
}
