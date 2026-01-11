package com.facial.ui; // Define o pacote de Interface Gráfica

// Importações dos serviços de IA, temas visuais e bibliotecas OpenCV/Swing
import com.facial.service.FaceRecognitionService; // Serviço de reconhecimento facial
import com.formdev.flatlaf.FlatDarkLaf; // Tema visual escuro (FlatLaf)
import org.bytedeco.opencv.opencv_core.Mat; // Classe de imagem matriz do OpenCV
import org.bytedeco.opencv.opencv_videoio.VideoCapture; // Classe de controle de vídeo
import org.bytedeco.javacv.Java2DFrameConverter; // Conversor OpenCV -> Java2D
import org.bytedeco.javacv.OpenCVFrameConverter; // Conversor Matriz -> Frame
import org.bytedeco.javacv.Frame; // Classe genérica de Frame de vídeo
import org.bytedeco.opencv.global.opencv_videoio; // Constantes de vídeo (driver)

import javax.swing.*; // Importa componentes de janela (Swing)
import java.awt.*; // Importa componentes gráficos (AWT)
import java.awt.image.BufferedImage; // Classe de imagem padrão do Java
import java.util.concurrent.Executors; // Executor de threads paralelas
import java.util.concurrent.ScheduledExecutorService; // Executor agendado (timer)
import java.util.concurrent.TimeUnit; // Unidade de tempo para o timer

/**
 * Janela Principal de Login com Reconhecimento Facial.
 * Versão Final: Sem logs de debug e totalmente comentada.
 */
public class LoginFrame extends JFrame {

    // Componentes visuais da tela
    private JLabel cameraScreen; // Área onde o vídeo da câmera será exibido
    private JButton btnRegister; // Botão de cadastro de novo usuário
    private JLabel statusLabel; // Texto de status (ex: "Acesso Negado", "Olá fulano")

    private VideoCapture capture; // Controlador físico da webcam
    private ScheduledExecutorService timer; // Agendador do loop de vídeo
    private FaceRecognitionService faceService; // Instância do serviço de IA

    // Conversores de imagem (necessários para mostrar imagem do OpenCV na tela do
    // Java)
    private final OpenCVFrameConverter.ToMat matConverter = new OpenCVFrameConverter.ToMat();
    private final Java2DFrameConverter imageConverter = new Java2DFrameConverter();

    // Variável para guardar temporariamente a assinatura do rosto detectado no
    // momento
    private org.bytedeco.opencv.opencv_core.Mat currentFaceEmbedding = null;

    // Construtor: Iniciado quando a janela é criada
    public LoginFrame() {
        initUI(); // Chama configuração visual
        faceService = new FaceRecognitionService(); // Inicializa o cérebro da IA
        startCamera(); // Liga a câmera
    }

    // Método para desenhar e configurar a janela
    private void initUI() {
        setTitle("Reconhecimento Facial Java"); // Define o título da janela
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define que o X fecha o programa totalmente
        setLayout(new BorderLayout(10, 10)); // Usa layout de bordas com espaçamento de 10px
        setSize(800, 600); // Define tamanho inicial: 800 de largura, 600 de altura
        setLocationRelativeTo(null); // Centraliza a janela na tela do monitor

        try {
            UIManager.setLookAndFeel(new FlatDarkLaf()); // Tenta aplicar o tema escuro moderno
        } catch (Exception ex) {
            // Se falhar o tema, usa o padrão do sistema (não faz nada)
        }

        // Configuração da área de câmera (Centro)
        cameraScreen = new JLabel(); // Cria um rótulo vazio inicialmente
        cameraScreen.setHorizontalAlignment(SwingConstants.CENTER); // Centraliza horizontalmente
        add(cameraScreen, BorderLayout.CENTER); // Adiciona na posição CENTRO do layout

        // Configuração do painel inferior (Botões)
        JPanel controlPanel = new JPanel(); // Cria um painel simples
        controlPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margem interna de 10px

        btnRegister = new JButton("Cadastrar Meu Rosto"); // Cria botão com esse texto
        btnRegister.setFont(new Font("Segoe UI", Font.BOLD, 14)); // Define fonte Segoe UI Negrito 14
        btnRegister.addActionListener(e -> registerUser()); // Ao clicar, executa o método registerUser

        statusLabel = new JLabel("Iniciando..."); // Cria o label de status
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Fonte maior (16)
        statusLabel.setForeground(Color.GRAY); // Cor cinza (neutro) inicial

        controlPanel.add(statusLabel); // Coloca status no painel
        controlPanel.add(Box.createHorizontalStrut(20)); // Coloca um espaço vazio de 20px
        controlPanel.add(btnRegister); // Coloca botão no painel

        add(controlPanel, BorderLayout.SOUTH); // Adiciona o painel na parte de BAIXO (sul) da janela
    }

    // Método para iniciar a captura de vídeo
    private void startCamera() {
        // Tenta abrir a câmera 0 usando DirectShow (driver mais compatível no Windows)
        capture = new VideoCapture(0, opencv_videoio.CAP_DSHOW);

        // Se falhar com DirectShow, tenta o backend padrão automático
        if (!capture.isOpened()) {
            capture = new VideoCapture(0);
        }

        // Se ainda assim não abrir, mostra erro na tela e cancela
        if (!capture.isOpened()) {
            statusLabel.setText("ERRO: Sem câmera!");
            return;
        }

        // Define a tarefa que vai rodar em loop (o "frame grabber")
        Runnable frameGrabber = new Runnable() {
            @Override
            public void run() {
                Mat mat = new Mat(); // Cria matriz vazia

                // Tenta ler o próximo quadro da câmera e jogar na matriz 'mat'
                if (capture.read(mat)) {

                    processFrame(mat); // Processa IA neste quadro (detecta e reconhece)

                    // Converte a matriz do OpenCV para imagem do Java (BufferedImage)
                    Frame frame = matConverter.convert(mat);
                    BufferedImage bufferedImage = imageConverter.convert(frame);

                    if (bufferedImage != null) { // Se a conversão funcionou...

                        // Início do desenho do Overlay (Guia visual)
                        Graphics2D g2 = bufferedImage.createGraphics(); // Pega o pincel de desenho
                        // Ativa anti-aliasing para o desenho não ficar serrilhado
                        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                        int w = bufferedImage.getWidth(); // Largura da imagem
                        int h = bufferedImage.getHeight(); // Altura da imagem

                        g2.setColor(Color.CYAN); // Define cor Ciano (Azul claro)
                        g2.setStroke(new BasicStroke(3)); // Define linha grossa (3px)

                        // Define tamanho da elipse guia
                        int ovalW = 220;
                        int ovalH = 300;
                        // Centraliza a elipse na tela
                        int ovalX = (w - ovalW) / 2;
                        int ovalY = (h - ovalH) / 2;

                        g2.drawOval(ovalX, ovalY, ovalW, ovalH); // Desenha a elipse vazada

                        g2.setFont(new Font("Arial", Font.BOLD, 18)); // Fonte do texto
                        // Escreve o texto um pouco acima da elipse
                        g2.drawString("Encaixe seu rosto aqui", ovalX - 10, ovalY - 10);

                        g2.dispose(); // Libera o pincel de desenho

                        // Fim do desenho do Overlay

                        ImageIcon imageIcon = new ImageIcon(bufferedImage); // Cria ícone Swing
                        cameraScreen.setIcon(imageIcon); // Define no label da tela
                        cameraScreen.repaint(); // Atualiza visualmente
                    }
                }
            }
        };

        // Cria agendador de thread única
        timer = Executors.newSingleThreadScheduledExecutor();
        // Agenda para rodar a cada 33 milissegundos (~30 quadros por segundo)
        timer.scheduleAtFixedRate(frameGrabber, 0, 33, TimeUnit.MILLISECONDS);
    }

    // Método que processa cada quadro de vídeo
    private void processFrame(Mat frame) {
        // Tenta detectar um rosto usando o serviço de IA
        Mat faceCrop = faceService.detectFace(frame);

        if (faceCrop != null) { // Se achou um rosto...

            // Gera a assinatura digital desse rosto
            currentFaceEmbedding = faceService.getEmbedding(faceCrop);

            if (currentFaceEmbedding != null) { // Se conseguiu gerar assinatura...
                // Tenta identificar de quem é
                String user = faceService.identifyUser(currentFaceEmbedding);

                // Atualiza a interface gráfica (SwingUtilities.invokeLater evita travamentos)
                SwingUtilities.invokeLater(() -> {
                    if ("Desconhecido".equals(user)) { // Se não reconheceu ninguem
                        statusLabel.setText("Acesso Negado"); // Texto
                        statusLabel.setForeground(Color.RED); // Cor Vermelha
                    } else { // Se reconheceu alguém
                        statusLabel.setText("Olá, " + user + "!"); // Texto Boas Vindas
                        statusLabel.setForeground(Color.GREEN); // Cor Verde
                    }
                });
            }
        } else { // Se a câmera não está vendo rosto nenhum
            SwingUtilities.invokeLater(() -> {
                statusLabel.setText("Procurando rosto..."); // Texto de espera
                statusLabel.setForeground(Color.YELLOW); // Cor Amarela
            });
            currentFaceEmbedding = null; // Zera a memória de rosto atual
        }
    }

    // Ação do botão "Cadastrar"
    private void registerUser() {
        if (currentFaceEmbedding != null) { // Só permite cadastro se tiver rosto na tela
            // Mostra janela popup pedindo o nome
            String name = JOptionPane.showInputDialog(this, "Nome do usuário:");

            if (name != null && !name.isEmpty()) { // Se digitou algo válido...
                // Manda cadastrar no serviço. Usa .clone() para salvar uma cópia segura da
                // matriz
                faceService.registerUser(name, currentFaceEmbedding.clone());
                JOptionPane.showMessageDialog(this, "Cadastrado!"); // Aviso de sucesso
            }
        } else {
            // Se clicar no botão sem estar na frente da câmera
            JOptionPane.showMessageDialog(this, "Nenhum rosto detectado!", "Aviso", JOptionPane.WARNING_MESSAGE);
        }
    }
}
