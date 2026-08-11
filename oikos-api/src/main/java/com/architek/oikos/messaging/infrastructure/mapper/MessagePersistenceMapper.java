package com.architek.oikos.messaging.infrastructure.mapper;

import org.mapstruct.Mapper;

import com.architek.oikos.messaging.domain.model.Message;
import com.architek.oikos.messaging.domain.valueobject.ConversationId;
import com.architek.oikos.messaging.domain.valueobject.MessageBody;
import com.architek.oikos.messaging.domain.valueobject.MessageId;
import com.architek.oikos.messaging.infrastructure.persistence.MessageEntity;
import com.architek.oikos.shared.domain.valueobject.EntityId;

@Mapper(componentModel = "spring")
public interface MessagePersistenceMapper {

    default MessageEntity toEntity(Message message) {
        MessageEntity entity = new MessageEntity();
        entity.setId(message.getId().asUuid());
        entity.setConversationId(message.getConversationId().asUuid());
        entity.setSenderId(message.getSenderId().value());
        entity.setSenderIdentity(message.getSenderIdentity());
        entity.setBody(message.getBody().value());
        entity.setCreatedDate(message.getCreatedDate());
        return entity;
    }

    default Message toDomain(MessageEntity entity) {
        return Message.reconstruct(MessageId.of(entity.getId()), ConversationId.of(entity.getConversationId()),
                EntityId.of(entity.getSenderId()), entity.getSenderIdentity(), MessageBody.of(entity.getBody()),
                entity.getCreatedDate());
    }
}
