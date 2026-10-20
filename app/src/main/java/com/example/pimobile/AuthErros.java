package com.example.pimobile;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;

public class AuthErros {

    public static String traduzir(Exception e) {
        if (e instanceof FirebaseAuthUserCollisionException) {
            return "Este email já está cadastrado";
        }
        if (e instanceof FirebaseAuthWeakPasswordException) {
            return "A senha precisa ter pelo menos 6 caracteres";
        }
        if (e instanceof FirebaseAuthInvalidCredentialsException
                || e instanceof FirebaseAuthInvalidUserException) {
            return "Email ou senha incorretos";
        }
        if (e instanceof FirebaseNetworkException) {
            return "Sem conexão com a internet";
        }
        return "Algo deu errado. Tente novamente";
    }
}