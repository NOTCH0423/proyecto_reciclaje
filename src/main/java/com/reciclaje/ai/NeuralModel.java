package com.reciclaje.ai;

import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Modelo de Red Neuronal para el bot educativo de reciclaje
 * Arquitectura: entrada -> capas densas -> salida
 * Usa aprendizaje incremental y persistencia
 */
public class NeuralModel {

    private MultiLayerNetwork network;
    private final int inputSize;
    private final int outputSize;
    private final String modelPath;
    private static final int RANDOM_SEED = 42;
    private static final double LEARNING_RATE = 0.01;
    
    /**
     * Constructor del modelo neural
     * @param inputSize tamaño del vector de entrada (embeddings)
     * @param outputSize tamaño del vector de salida (categorías de respuesta)
     * @param modelPath ruta donde guardar el modelo
     */
    public NeuralModel(int inputSize, int outputSize, String modelPath) {
        this.inputSize = inputSize;
        this.outputSize = outputSize;
        this.modelPath = modelPath;
        
        // Crear directorio si no existe
        Path path = Paths.get(modelPath).getParent();
        try {
            Files.createDirectories(path);
        } catch (IOException e) {
            System.err.println("Error creando directorio para modelo: " + e.getMessage());
        }
    }
    
    /**
     * Inicializa la red neuronal con arquitectura predefinida
     */
    public void initNetwork() {
        MultiLayerConfiguration config = new NeuralNetConfiguration.Builder()
                .seed(RANDOM_SEED)
                .activation(Activation.RELU)
                .weightInit(WeightInit.XAVIER)
                .updater(new Adam(LEARNING_RATE))
                .l2(0.0001)
                .list()
                // Capa entrada -> Primera capa oculta
                .layer(0, new DenseLayer.Builder()
                        .nIn(inputSize)
                        .nOut(128)
                        .activation(Activation.RELU)
                        .build())
                // Primera capa oculta -> Segunda capa oculta
                .layer(1, new DenseLayer.Builder()
                        .nIn(128)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .build())
                // Segunda capa oculta -> Tercera capa oculta
                .layer(2, new DenseLayer.Builder()
                        .nIn(64)
                        .nOut(32)
                        .activation(Activation.RELU)
                        .build())
                // Tercera capa oculta -> Capa salida
                .layer(3, new OutputLayer.Builder()
                        .nIn(32)
                        .nOut(outputSize)
                        .activation(Activation.SOFTMAX)
                        .lossFunction(LossFunctions.LossFunction.MCXENT)
                        .build())
                .build();
        
        network = new MultiLayerNetwork(config);
        network.init();
        
        System.out.println("[NeuralModel] Red neuronal inicializada");
        System.out.println("  - Entrada: " + inputSize);
        System.out.println("  - Capas ocultas: 128 -> 64 -> 32");
        System.out.println("  - Salida: " + outputSize);
    }
    
    /**
     * Entrena el modelo con un lote de datos
     * @param input matriz de entrada (samples x inputSize)
     * @param output matriz de salida (samples x outputSize)
     * @param epochs número de épocas de entrenamiento
     */
    public void train(INDArray input, INDArray output, int epochs) {
        if (network == null) {
            throw new IllegalStateException("Red neural no inicializada. Llama a initNetwork() primero.");
        }
        
        for (int i = 0; i < epochs; i++) {
            network.fit(input, output);
            if ((i + 1) % 5 == 0) {
                System.out.println("[NeuralModel] Época " + (i + 1) + "/" + epochs + 
                                 " - Score: " + network.score());
            }
        }
    }
    
    /**
     * Realiza predicción con la red neuronal
     * @param input vector de entrada
     * @return vector de probabilidades de salida
     */
    public INDArray predict(INDArray input) {
        if (network == null) {
            throw new IllegalStateException("Red neural no inicializada.");
        }
        return network.output(input);
    }
    
    /**
     * Entrena el modelo de forma incremental (una muestra a la vez)
     * Útil para aprendizaje en tiempo real
     * @param input vector de entrada
     * @param output vector esperado de salida
     */
    public void trainIncremental(INDArray input, INDArray output) {
        if (network == null) {
            throw new IllegalStateException("Red neural no inicializada.");
        }
        
        // Asegurar que los datos tengan las dimensiones correctas
        if (input.shape()[0] != 1) {
            input = input.reshape(1, inputSize);
        }
        if (output.shape()[0] != 1) {
            output = output.reshape(1, outputSize);
        }
        
        network.fit(input, output);
    }
    
    /**
     * Guarda el modelo en disco
     */
    public void save() {
        if (network == null) {
            System.err.println("No hay modelo para guardar");
            return;
        }
        
        try {
            network.save(new File(modelPath), true);
            System.out.println("[NeuralModel] Modelo guardado en: " + modelPath);
        } catch (IOException e) {
            System.err.println("Error guardando modelo: " + e.getMessage());
        }
    }
    
    /**
     * Carga el modelo desde disco
     */
    public void load() {
        try {
            File modelFile = new File(modelPath);
            if (modelFile.exists()) {
                network = MultiLayerNetwork.load(modelFile, true);
                System.out.println("[NeuralModel] Modelo cargado desde: " + modelPath);
            } else {
                System.out.println("[NeuralModel] Archivo de modelo no encontrado. Inicializando nuevo modelo.");
                initNetwork();
            }
        } catch (IOException e) {
            System.err.println("Error cargando modelo: " + e.getMessage());
            initNetwork();
        }
    }
    
    /**
     * Obtiene la red neuronal subyacente
     */
    public MultiLayerNetwork getNetwork() {
        return network;
    }
    
    /**
     * Obtiene información del modelo
     */
    public void printModelInfo() {
        if (network != null) {
            System.out.println("[NeuralModel] Información del modelo:");
            System.out.println("  - Parámetros totales: " + network.numParams());
            System.out.println("  - Capas: " + network.getnLayers());
            System.out.println("  - Score actual: " + network.score());
        }
    }
}
