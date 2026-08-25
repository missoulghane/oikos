package com.architek.oikos.messaging.application.query;

/**
 * Le filtre lu / non lu de la boîte : UNREAD ne garde que les conversations
 * portant au moins un message non lu par l'appelant, READ celles où il n'en
 * reste aucun. null = pas de filtre. Se lit sur le même compteur que la
 * pastille de la liste (ConversationSummaryView.unreadCount), pour que filtrer
 * ne montre jamais autre chose que ce que la pastille annonçait.
 */
public enum ConversationReadState {
    READ,
    UNREAD
}
