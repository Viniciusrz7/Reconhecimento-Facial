#  Projeto de Login com Reconhecimento Facial 

Seus principais recursos são:

1.  **Câmera ao Vivo**: Mostra sua imagem em tempo real na tela.
2.  **Detecção de Rosto**: Um quadrado invisível que acha onde está seu rosto na imagem.
3.  **Guia Visual**: Um desenho (círculo azul) na tela para ajudar você a se posicionar (estilo Gov.br).
4.  **Cadastro Rápido**: Você clica, digita seu nome e o sistema "decora" seu rosto.
5.  **Reconhecimento Inteligente**: Se você (ou outra pessoa) aparecer na câmera depois, o sistema diz o nome ou "Acesso Negado".

# Tecnologias Usadas

Para fazer isso funcionar, usamos ferramentas profissionais de mercado:

- **Java 21**: A linguagem de programação moderna usada para controlar tudo.
- **OpenCV (via JavaCV)**: A biblioteca de Visão Computacional mais famosa do mundo. É ela que "enxerga" a imagem.
- **Deep Learning (Inteligência Artificial)**:
  - Usamos **Redes Neurais** (cérebros artificiais) pré-treinados.
  - Não usamos algoritmos antigos (como Eigenfaces), usamos tecnologia de ponta (**ResNet SSD** e **OpenFace**) que é muito mais precisa.
- **Swing + FlatLaf**: Para criar a janela preta moderna (Modo Escuro) e bonita no Windows.
- **Maven**: Para baixar todas essas peças (bibliotecas) automaticamente da internet.

# Como Funciona a "Mágica"? (Passo a Passo)

Aqui está a explicação humana do processo que acontece milissegundos a cada quadro do vídeo:

# 1. Captura da Imagem 

O computador liga a webcam e tira "fotos" (quadros) 30 vezes por segundo. Usamos um driver especial (**DirectShow**) para garantir que a câmera funcione bem no Windows sem travar.

# 2. Detecção do Rosto 

Antes de reconhecer _quem_ é, precisamos saber _se_ tem alguém ali.

- A IA olha a foto e procura padrões de olhos, nariz e boca.
- Se ela tiver mais de **30% de certeza** que achou um rosto, ela recorta apenas essa parte da imagem.

# 3. Criação da Assinatura Digital

Esta é a parte mais inteligente.

- O sistema pega o recorte do seu rosto e passa por uma segunda Rede Neural (**OpenFace**).
- Essa rede transforma a imagem do seu rosto em uma lista de **128 números**.
- **Exemplo**: O sistema não salva sua foto, ele salva algo como `[0.5, -1.2, 0.9, ...]`. Isso é único para você, como uma impressão digital. Se você usar óculos ou mudar o cabelo, os números mudam muito pouco, por isso a IA ainda te reconhece!

# 4. O Teste Final 

Quando a câmera vê um rosto novo, ela gera os números dele e compara com os números que estão salvos na memória (quando você se cadastrou).

- O sistema usa matemática (Distância Euclidiana) para ver a **diferença** entre os números.
- **Diferença Baixa (Ex: 0.3)**: Os números são quase iguais. É você! ✅
- **Diferença Alta (Ex: 1.1)**: Os números não batem. É outra pessoa! ❌
  - _Configuramos o sistema para ser bem rigoroso (0.5), para não confundir você com parentes parecidos!_

# A Matemática por Trás

O usuário perguntou: _"Qual algoritmo matemático é usado e como funciona?"_
A resposta principal é: **Distância Euclidiana (Norma L2) em um Espaço Vetorial de 128 Dimensões**.

# 1. O Conceito de Vetor ("Embedding")

Imagine que descrevemos uma pessoa com apenas 2 números (2 dimensões): **[Altura, Peso]**.

- **Vinicius**: `[1.80, 80]`
- **Adriana**: `[1.65, 60]`
  Se desenharmos isso num papel (gráfico X e Y), são dois pontos distantes um do outro.

A nossa IA faz algo parecido, mas muito mais complexo. Ela descreve seu rosto usando **128 números**.

- **Seu Rosto (Computador vê)** = `[0.10, -0.55, 0.91, 2.33 ...]` (e assim por diante até 128 números)
  Isso se chama **Vetor de Características** (ou _Embedding_).

# 2. A Fórmula da Comparação (Distância Euclidiana)

Para saber se o _Rosto A_ é igual ao _Rosto B_, o computador calcula a distância "em linha reta" entre esses dois pontos.

A fórmula usada no código (`opencv_core.norm`) é esta:

$$ d(p, q) = \sqrt{(q*1 - p_1)^2 + (q_2 - p_2)^2 + \dots + (q*{128} - p\_{128})^2} $$

Simplificando:

1.  O computador pega o 1º número do seu rosto e subtrai do 1º número do rosto salvo.
2.  Faz isso para todos os 128 números.
3.  Eleva tudo ao quadrado (para não ter números negativos).
4.  Soma tudo.
5.  Tira a raiz quadrada do total.

# Resumo Simples

- **Resultado próximo de 0 (ex: 0.3):** A distância é curtíssima. Os pontos estão colados. **É a mesma pessoa!** ✅
- **Resultado longe de 0 (ex: 1.2):** A distância é grande. Os pontos estão longe. **São pessoas diferentes!** ❌

# Estrutura do Código

Para facilitar seus estudos, o código foi dividido em 3 partes principais:

1.  **`Main.java`**: É só o botão de "Ligar". Ele inicia a janela.
2.  **`LoginFrame.java` (A Tela)**: Cuida de tudo que você vê.
    - Desenha a janela preta.
    - Liga a câmera.
    - Desenha o círculo azul "Encaixe seu rosto aqui".
    - Escreve "Bem-vindo" ou "Acesso Negado".
3.  **`FaceRecognitionService.java` (O Cérebro)**: Cuida de tudo que pensa.
    - Carrega os arquivos pesados de IA (`.caffemodel`, `.t7`).
    - Faz os cálculos matemáticos para comparar os rostos.

# Explicação dos Arquivos

1.  **`deploy.prototxt`** (O Esqueleto 💀)
    - É a **planta baixa** da rede neural. Ele descreve a arquitetura: quantas camadas existem, como elas se conectam. Sem ele, o sistema não sabe como montar o cérebro.
2.  **`res10_300x300_ssd_iter_140000.caffemodel`** (O Cérebro 🧠)
    - Contém os **pesos matemáticos** aprendidos após treinar com milhões de fotos. É aqui que está a "inteligência" para saber o que é um rosto.
3.  **`openface_nn4.small2.v1.t7`** (O Identificador 🆔)
    - É uma segunda rede neural especializada em transformar rostos em números (assinatura).

**Resumo**: O `.prototxt` diz _como montar_ a IA, e o `.caffemodel` diz _o que_ a IA sabe. Um não funciona sem o outro!
