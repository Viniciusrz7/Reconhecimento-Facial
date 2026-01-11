package com.facial; // Pacote principal do aplicativo

import com.facial.ui.LoginFrame; // Importa a janela de login
import javax.swing.SwingUtilities; // Importa utilitário de thread do Swing

/**
 * Classe principal que inicia o programa.
 * Simples e direta.
 */
public class Main {
    public static void main(String[] args) {
        // SwingUtilities.invokeLater garante que a interface gráfica rode na thread
        // correta de eventos.
        // Isso evita erros de concorrência e travamentos visuais.
        SwingUtilities.invokeLater(() -> {
            LoginFrame frame = new LoginFrame(); // Cria a janela de login
            frame.setVisible(true); // Torna a janela visível na tela
        });
    }
}
