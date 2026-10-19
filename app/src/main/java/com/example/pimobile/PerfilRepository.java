package com.example.pimobile;

public class PerfilRepository {

    // TODO: trocar por dados reais do Firebase:
    // FirebaseAuth para saber quem está logado (uid, email) e
    // Firestore (coleção "usuarios", documento = uid) para nome e tipo de conta.
    // A PerfilActivity só usa este método, então nada mais precisa mudar.
    public static Perfil getPerfilAtual() {
        return new Perfil("Ana Souza", "ana.souza@email.com", "Freelancer", true, 2);
    }
}