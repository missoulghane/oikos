package com.architek.oikos.shared.exception;

/**
 * Marque une exception qui porte, en plus de son message, un code stable destiné
 * aux clients (web, mobile).
 *
 * <p>Le message d'une exception est écrit pour les logs : il est en anglais, il
 * contient des identifiants techniques, et il change dès qu'on reformule le code.
 * Les interfaces ne l'affichent donc jamais telles quelles - elles affichent leur
 * propre texte, en français, choisi à partir du statut HTTP. Le code est ce qui
 * leur permet de distinguer deux situations que le statut confond, sans jamais
 * comparer des chaînes de message.
 *
 * <p>N'en donner un qu'aux cas où le client doit vraiment réagir différemment :
 * un code par exception serait un second vocabulaire à maintenir pour rien.
 */
public interface CodedException {

    String errorCode();
}
