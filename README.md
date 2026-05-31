# 👤 Reconhecimento Facial em Java

Uma aplicação desktop moderna para detecção e reconhecimento facial em tempo real, desenvolvida em **Java** com o poder do **OpenCV (via JavaCV)**. O sistema utiliza redes neurais profundas (DNN) para localizar rostos na webcam e extrair assinaturas digitais (embeddings), permitindo o cadastro e login automático de usuários.

## ✨ Funcionalidades

* **Detecção em Tempo Real:** Captura de vídeo da webcam e processamento contínuo (aprox. 30 FPS).
* **Inteligência Artificial:** Utiliza modelos pré-treinados (Caffe e Torch) para alta precisão na detecção e reconhecimento.
* **Cadastro Simples:** Permite registrar novos rostos diretamente pela interface com apenas um clique.
* **Interface Moderna:** Construída em Swing, mas aprimorada com o tema escuro **FlatLaf** e guias visuais interativos na tela.
* **Feedback Visual:** Textos dinâmicos que indicam o status do sistema ("Procurando rosto...", "Acesso Negado", "Olá, [Nome]!").

## 🛠️ Tecnologias Utilizadas

* **Java (JDK 8 ou superior)** - Linguagem principal.
* **JavaCV / Bytedeco OpenCV** - Wrapper para utilizar as bibliotecas nativas de visão computacional em Java.
* **OpenCV DNN (Deep Neural Networks)** - Módulo para execução das redes neurais.
* **Swing & AWT** - Para a interface gráfica de usuário (GUI).
* **FlatLaf** - Biblioteca para o tema escuro (Dark Mode).

## 🧠 Modelos de IA Necessários

Para que a aplicação funcione, é obrigatório ter os arquivos dos modelos pré-treinados na raiz do projeto (ou na pasta `Reconhecimento-Facial/`). **Você precisa baixar os seguintes arquivos:**

1. `deploy.prototxt` (Estrutura da rede de detecção)
2. `res10_300x300_ssd_iter_140000.caffemodel` (Pesos da rede Caffe para detecção facial)
3. `openface_nn4.small2.v1.t7` (Rede Torch OpenFace para geração da assinatura do rosto)

## 🚀 Como Executar o Projeto

### Pré-requisitos

* Java JDK instalado e configurado nas variáveis de ambiente.
* Webcam conectada e funcionando (o sistema tenta abrir a câmera padrão `0`).
* Uma IDE Java (IntelliJ IDEA, Eclipse, NetBeans) ou gerenciador de dependências (Maven/Gradle) para importar as bibliotecas do JavaCV e FlatLaf.

### Passos

1. **Clone o repositório:**
```bash
git clone https://github.com/seu-usuario/seu-repositorio.git
cd seu-repositorio

```


2. **Adicione os Modelos de IA:**
Coloque os arquivos `.prototxt`, `.caffemodel` e `.t7` na pasta raiz do projeto.
3. **Execute a Aplicação:**
Rode a classe `Main.java`.
4. **Como usar:**
* Posicione seu rosto dentro da elipse azul na tela.
* Aguarde o status mudar (inicialmente aparecerá "Acesso Negado" pois você não está no banco de dados).
* Clique em **"Cadastrar Meu Rosto"**, digite seu nome e confirme.
* Pronto! A câmera agora deverá exibir "Olá, [Seu Nome]!" na cor verde.



## 📂 Estrutura do Código

O projeto está dividido em pacotes focados na responsabilidade de cada classe:

* `com.facial.Main`: Ponto de entrada da aplicação, responsável por instanciar a interface gráfica na thread correta.
* `com.facial.ui.LoginFrame`: Controla a janela gráfica, inicializa a câmera, gerencia os eventos de clique e desenha os guias de alinhamento por cima do vídeo.
* `com.facial.service.FaceRecognitionService`: O "cérebro" da aplicação. Carrega as redes neurais, recorta o rosto da imagem original, gera o vetor de características (embedding) e calcula a distância euclidiana para identificar usuários.

## ⚠️ Observações Importantes

* **Armazenamento em Memória:** Atualmente, o sistema usa um `HashMap` para salvar os rostos. Isso significa que **os cadastros são perdidos quando a aplicação é fechada**. Para uso em produção, recomenda-se plugar um banco de dados (SQL, MongoDB) para salvar o array de *embeddings*.
* **Iluminação e Posição:** Redes neurais de reconhecimento facial são sensíveis à luz e ângulos extremos. Para melhores resultados, mantenha o rosto reto e bem iluminado.
