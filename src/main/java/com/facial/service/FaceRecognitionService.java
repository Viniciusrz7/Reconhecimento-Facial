package com.facial.service; // Define o pacote desta classe

// Importações necessárias para o OpenCV e Java
import org.bytedeco.opencv.opencv_core.*; // Importa classes base como Mat, Point, Rect
import org.bytedeco.opencv.opencv_dnn.*; // Importa classes de redes neurais (Deep Learning)
import org.bytedeco.opencv.global.opencv_core; // Funções globais do núcleo OpenCV
import org.bytedeco.opencv.global.opencv_dnn; // Funções globais de DNN
import java.io.File; // Importa classe para manipulação de arquivos
import java.util.HashMap; // Importa HashMap para armazenar dados em memória
import java.util.Map; // Importa interface Map

/**
 * Serviço de Reconhecimento Facial.
 * Versão Final: Sem logs de debug e totalmente comentada.
 */
public class FaceRecognitionService {

    private Net faceDetector; // Variável para a rede neural de detecção facial
    private Net faceRecognizer; // Variável para a rede neural de reconhecimento facial

    // Define os nomes dos arquivos de modelo
    private static final String PROTO_FILE = "deploy.prototxt"; // Configuração da estrutura da rede de detecção
    private static final String MODEL_FILE = "res10_300x300_ssd_iter_140000.caffemodel"; // Pesos treinados da detecção
    private static final String EMBEDDING_MODEL = "openface_nn4.small2.v1.t7"; // Arquivo da rede Torch para
                                                                               // reconhecimento

    // Banco de dados em memória: Nome da Pessoa -> Assinatura Facial (Matriz)
    private Map<String, Mat> registeredFaces = new HashMap<>();

    // Construtor da classe
    public FaceRecognitionService() {
        checkAndLoadModels(); // Ao iniciar, verifica e carrega os modelos
    }

    // Método para checar arquivos e carregar as IAs
    private void checkAndLoadModels() {
        String prefix = ""; // Prefixo para caso os arquivos estejam em subpasta

        // Verifica se o arquivo não existe na raiz, mas existe na pasta do projeto
        if (!new File(PROTO_FILE).exists() && new File("Reconhecimento-Facial/" + PROTO_FILE).exists()) {
            prefix = "Reconhecimento-Facial/"; // Define o prefixo correto
        }

        // Monta os caminhos finais dos arquivos
        String realProto = prefix + PROTO_FILE; // Caminho do prototxt
        String realModel = prefix + MODEL_FILE; // Caminho do caffemodel
        String realEmbed = prefix + EMBEDDING_MODEL; // Caminho do t7

        // Se algum arquivo não existir, encerra o carregamento com segurança
        if (!new File(realProto).exists() || !new File(realModel).exists() || !new File(realEmbed).exists()) {
            return; // Sai do método sem tentar carregar (evita crash)
        }

        try {
            // Carrega a rede de detecção facial usando Caffe
            faceDetector = opencv_dnn.readNetFromCaffe(realProto, realModel);

            // Carrega a rede de reconhecimento facial usando Torch
            faceRecognizer = opencv_dnn.readNetFromTorch(realEmbed);

        } catch (Exception e) {
            e.printStackTrace(); // Em caso de erro técnico, imprime a pilha de erro (necessário para debug
                                 // crítico)
        }
    }

    // Método que recebe uma imagem e tenta localizar um rosto nela
    public Mat detectFace(Mat frame) {
        if (faceDetector == null)
            return null; // Se a IA não carregou, retorna nulo

        // Prepara a imagem para a IA: redimensiona para 300x300 e normaliza cores
        Mat blob = opencv_dnn.blobFromImage(frame, 1.0, new Size(300, 300),
                new Scalar(104.0, 177.0, 123.0, 0.0),
                false, false, opencv_core.CV_32F);

        faceDetector.setInput(blob); // Envia a imagem processada para a rede neural
        Mat detections = faceDetector.forward(); // Executa o processamento e pega o resultado

        // Reorganiza o resultado em uma matriz legível
        Mat result = detections.reshape(1, (int) detections.total() / 7);

        float maxConfidence = 0; // Variável para guardar a maior confiança encontrada
        int bestIdx = -1; // Índice do melhor rosto encontrado na lista

        // Percorre todas as possíveis detecções encontradas pela IA
        for (int i = 0; i < result.rows(); i++) {
            float confidence = result.ptr(i, 2).getFloat(); // Pega o nível de confiança (0 a 1)

            // Filtra: só aceita se a confiança for maior que 30% (0.3)
            if (confidence > 0.3) {
                // Se essa detecção for mais confiável que a anterior...
                if (confidence > maxConfidence) {
                    maxConfidence = confidence; // Atualiza a maior confiança
                    bestIdx = i; // Armazena o índice desse rosto como o principal
                }
            }
        }

        // Se encontrou algum rosto válido...
        if (bestIdx >= 0) {
            // Calcula as coordenadas reais do retângulo (box) do rosto na imagem original
            int xLeftBottom = (int) (result.ptr(bestIdx, 3).getFloat() * frame.cols()); // X Esquerda
            int yLeftBottom = (int) (result.ptr(bestIdx, 4).getFloat() * frame.rows()); // Y Base
            int xRightTop = (int) (result.ptr(bestIdx, 5).getFloat() * frame.cols()); // X Direita
            int yRightTop = (int) (result.ptr(bestIdx, 6).getFloat() * frame.rows()); // Y Topo

            // Garante que as coordenadas não saiam da imagem (matemática segura)
            xLeftBottom = Math.max(0, xLeftBottom); // Não pode ser menor que 0
            yLeftBottom = Math.max(0, yLeftBottom); // Não pode ser menor que 0
            xRightTop = Math.min(frame.cols() - 1, xRightTop); // Não pode passar da largura
            yRightTop = Math.min(frame.rows() - 1, yRightTop); // Não pode passar da altura

            // Se o retângulo formado for válido...
            if (xRightTop > xLeftBottom && yRightTop > yLeftBottom) {
                // Cria o objeto Rect com as dimensões calculadas
                Rect faceRegion = new Rect(xLeftBottom, yLeftBottom, xRightTop - xLeftBottom, yRightTop - yLeftBottom);
                return new Mat(frame, faceRegion); // Recorta a imagem original na área do rosto e retorna
            }
        }
        return null; // Retorna nulo se nenhum rosto foi detectado na imagem
    }

    // Método que transforma a imagem do rosto em números (assinatura digital)
    public Mat getEmbedding(Mat faceCrop) {
        if (faceRecognizer == null || faceCrop == null)
            return null; // Validação de segurança

        // Prepara a imagem do rosto: redimensiona para 96x96 (padrão OpenFace) e escala
        // pixels
        Mat blob = opencv_dnn.blobFromImage(faceCrop, 1.0 / 255, new Size(96, 96),
                new Scalar(0, 0, 0, 0), true, false, opencv_core.CV_32F);

        faceRecognizer.setInput(blob); // Envia para a rede de reconhecimento
        return faceRecognizer.forward().clone(); // Retorna o clone do vetor de características (embedding)
    }

    // Método para salvar um novo usuário na memória
    public void registerUser(String name, Mat faceEmbedding) {
        registeredFaces.put(name, faceEmbedding); // Adiciona no mapa: Nome -> Assinatura
    }

    // Método para identificar quem é o dono do rosto atual
    public String identifyUser(Mat currentEmbedding) {
        if (currentEmbedding == null || registeredFaces.isEmpty())
            return "Desconhecido"; // Se não tem dados, retorna desconhecido

        String bestMatch = "Desconhecido"; // Nome padrão inicial
        double minDistance = 0.5; // Distância de corte: 0.5 (alta precisão). Menor = mais rigoroso.

        // Percorre todos os usuários cadastrados no sistema
        for (Map.Entry<String, Mat> entry : registeredFaces.entrySet()) {
            String nomeSalvo = entry.getKey(); // Pega o nome salvo
            Mat embeddingSalvo = entry.getValue(); // Pega a assinatura salva

            // Calcula a diferença matemática (distância euclidiana) entre os rostos
            double distance = opencv_core.norm(embeddingSalvo, currentEmbedding, opencv_core.NORM_L2, null);

            // Se a diferença for menor (ou seja, mais parecido) que o mínimo aceito...
            if (distance < minDistance) {
                minDistance = distance; // Atualiza a nova menor distância
                bestMatch = nomeSalvo; // Define esse usuário como o provável dono do rosto
            }
        }
        return bestMatch; // Retorna o nome da pessoa identificada (ou Desconhecido)
    }
}
