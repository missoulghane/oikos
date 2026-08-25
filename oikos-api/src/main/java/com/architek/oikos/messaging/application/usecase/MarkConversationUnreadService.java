package com.architek.oikos.messaging.application.usecase;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.architek.oikos.messaging.application.command.MarkConversationUnreadCommand;
import com.architek.oikos.messaging.application.port.in.MarkConversationUnreadUseCase;
import com.architek.oikos.messaging.domain.model.ConversationReadMarker;
import com.architek.oikos.messaging.domain.repository.ConversationReadMarkerRepository;

/**
 * Remet le curseur de lecture a zero : lastReadMessageId redevient null, donc
 * tous les messages de la conversation recomptent comme non lus - exactement
 * l'etat d'une conversation jamais ouverte (voir ConversationReadMarker.unread,
 * dont c'est deja la representation, si bien qu'aucun cas particulier n'est a
 * ajouter au comptage).
 *
 * <p>Ecrit la ligne plutot que de la supprimer : le depot fait un upsert, et
 * une ligne "jamais lue" se lit comme une absence de ligne partout ailleurs.
 */
@Component
public class MarkConversationUnreadService implements MarkConversationUnreadUseCase {

    private final ConversationReadMarkerRepository readMarkerRepository;

    public MarkConversationUnreadService(ConversationReadMarkerRepository readMarkerRepository) {
        this.readMarkerRepository = readMarkerRepository;
    }

    @Override
    @Transactional
    public void markUnread(MarkConversationUnreadCommand command) {
        readMarkerRepository.save(ConversationReadMarker.unread(command.conversationId(), command.userId()));
    }
}
