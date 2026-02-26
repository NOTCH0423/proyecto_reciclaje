package com.reciclaje.ai;

import com.reciclaje.util.VectorizerUtil;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * Gestor de aprendizaje conversacional
 * Mantiene historial de conversaciones y entrena el modelo de forma incremental
 */
public class ConversationLearner {
    
    private NeuralModel model;
    private VectorizerUtil vectorizer;
    private List<ConversationPair> conversationHistory;
    private String dataPath;
    private static final int OUTPUT_SIZE = 8; // Número de categorías de respuesta
    private Map<String, Integer> categoryIndex;
    
    /**
     * Constructor
     * @param model modelo neuronal a entrenar
     * @param dataPath ruta donde guardar datos de conversaciones
     */
    public ConversationLearner(NeuralModel model, String dataPath) {
        this.model = model;
        this.dataPath = dataPath;
        this.vectorizer = new VectorizerUtil();
        this.conversationHistory = new ArrayList<>();
        this.categoryIndex = new HashMap<>();
        
        initializeCategories();
        createDataDirectory();
        loadConversationHistory();
    }
    
    /**
     * Inicializa las categorías de respuesta
     */
    private void initializeCategories() {
        String[] categories = {
            "general",        // 0 - preguntas generales
            "plastico",       // 1 - sobre plástico
            "papel",          // 2 - sobre papel
            "vidrio",         // 3 - sobre vidrio
            "organico",       // 4 - sobre residuos orgánicos
            "electronico",    // 5 - sobre residuos electrónicos
            "peligroso",      // 6 - sobre materiales peligrosos
            "desconocido"     // 7 - respuesta no clasificada
        };
        
        for (int i = 0; i < categories.length; i++) {
            categoryIndex.put(categories[i], i);
        }
        
        System.out.println("[ConversationLearner] Categorías inicializadas: " + categoryIndex.size());
    }
    
    /**
     * Crea el directorio de datos si no existe
     */
    private void createDataDirectory() {
        try {
            Path path = Paths.get(dataPath);
            Files.createDirectories(path);
            System.out.println("[ConversationLearner] Directorio de datos: " + dataPath);
        } catch (IOException e) {
            System.err.println("Error creando directorio: " + e.getMessage());
        }
    }
    
    /**
     * Entrena el modelo con el historial de conversaciones
     */
    public void trainModel() {
        if (conversationHistory.isEmpty()) {
            System.out.println("[ConversationLearner] No hay conversaciones para entrenar");
            return;
        }
        
        // Construir vocabulario
        List<String> allTexts = new ArrayList<>();
        for (ConversationPair pair : conversationHistory) {
            allTexts.add(pair.userInput);
            allTexts.add(pair.response);
        }
        vectorizer.buildVocabulary(allTexts);
        
        // Preparar datos de entrenamiento
        double[][] inputs = new double[conversationHistory.size()][];
        double[][] outputs = new double[conversationHistory.size()][];
        
        for (int i = 0; i < conversationHistory.size(); i++) {
            ConversationPair pair = conversationHistory.get(i);
            inputs[i] = vectorizer.vectorize(pair.userInput);
            outputs[i] = createOutputVector(pair.category);
        }
        
        // Convertir a NDArray
        INDArray inputArray = Nd4j.create(inputs);
        INDArray outputArray = Nd4j.create(outputs);
        
        // Entrenar
        System.out.println("[ConversationLearner] Entrenando con " + conversationHistory.size() + " muestras...");
        model.train(inputArray, outputArray, 10);
        model.save();
        
        System.out.println("[ConversationLearner] Entrenamiento completado");
    }
    
    /**
     * Entrena el modelo de forma incremental (una conversación a la vez)
     * @param userInput entrada del usuario
     * @param response respuesta del bot
     * @param category categoría de la respuesta
     */
    public void learnFromConversation(String userInput, String response, String category) {
        // Crear par de conversación
        ConversationPair pair = new ConversationPair(userInput, response, category);
        conversationHistory.add(pair);
        
        // Actualizar vocabulario
        List<String> texts = Arrays.asList(userInput, response);
        vectorizer.buildVocabulary(texts);
        
        // Vectorizar
        double[] input = vectorizer.vectorize(userInput);
        double[] output = createOutputVector(category);
        
        // Convertir a NDArray
        INDArray inputArray = Nd4j.create(input).reshape(1, input.length);
        INDArray outputArray = Nd4j.create(output).reshape(1, output.length);
        
        // Entrenar de forma incremental
        System.out.println("[ConversationLearner] Aprendiendo de nueva interacción - Categoría: " + category);
        model.trainIncremental(inputArray, outputArray);
        
        // Guardar periódicamente (cada 5 interacciones)
        if (conversationHistory.size() % 5 == 0) {
            model.save();
            saveConversationHistory();
        }
    }
    
    /**
     * Predice la categoría de una entrada de usuario
     * @param userInput entrada del usuario
     * @return categoría predicha
     */
    public String predictCategory(String userInput) {
        double[] inputVector = vectorizer.vectorize(userInput);
        INDArray input = Nd4j.create(inputVector).reshape(1, inputVector.length);
        INDArray output = model.predict(input);
        
        // Obtener índice de máxima probabilidad
        int maxIdx = output.argMax(1).toIntVector()[0];
        
        // Encontrar categoría correspondiente
        for (Map.Entry<String, Integer> entry : categoryIndex.entrySet()) {
            if (entry.getValue() == maxIdx) {
                double confidence = output.getDouble(maxIdx);
                System.out.println("[ConversationLearner] Predicción: " + entry.getKey() + 
                                 " (confianza: " + String.format("%.2f%%", confidence * 100) + ")");
                return entry.getKey();
            }
        }
        
        return "desconocido";
    }
    
    /**
     * Guarda el historial de conversaciones en archivo
     */
    public void saveConversationHistory() {
        String filePath = Paths.get(dataPath, "conversation_history.csv").toString();
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // Encabezado
            writer.println("USER_INPUT,RESPONSE,CATEGORY,TIMESTAMP");
            
            // Datos
            for (ConversationPair pair : conversationHistory) {
                String userInput = pair.userInput.replaceAll(",", ";");
                String response = pair.response.replaceAll(",", ";");
                writer.println(userInput + "," + response + "," + pair.category + "," + pair.timestamp);
            }
            
            System.out.println("[ConversationLearner] Historial guardado: " + filePath);
        } catch (IOException e) {
            System.err.println("Error guardando historial: " + e.getMessage());
        }
    }
    
    /**
     * Carga el historial de conversaciones desde archivo
     */
    public void loadConversationHistory() {
        String filePath = Paths.get(dataPath, "conversation_history.csv").toString();
        File file = new File(filePath);
        
        if (!file.exists()) {
            System.out.println("[ConversationLearner] No hay historial previo");
            return;
        }
        
        conversationHistory.clear();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Saltar encabezado
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 3) {
                    String userInput = parts[0];
                    String response = parts[1];
                    String category = parts[2];
                    ConversationPair pair = new ConversationPair(userInput, response, category);
                    conversationHistory.add(pair);
                }
            }
            
            System.out.println("[ConversationLearner] Historial cargado: " + conversationHistory.size() + " conversaciones");
        } catch (IOException e) {
            System.err.println("Error cargando historial: " + e.getMessage());
        }
    }
    
    /**
     * Crea un vector de salida para una categoría
     */
    private double[] createOutputVector(String category) {
        double[] vector = new double[OUTPUT_SIZE];
        Integer categoryIdx = categoryIndex.get(category);
        
        if (categoryIdx != null) {
            vector[categoryIdx] = 1.0;
        } else {
            vector[OUTPUT_SIZE - 1] = 1.0; // desconocido
        }
        
        return vector;
    }
    
    /**
     * Obtiene estadísticas de aprendizaje
     */
    public void printStatistics() {
        System.out.println("[ConversationLearner] Estadísticas:");
        System.out.println("  - Conversaciones aprendidas: " + conversationHistory.size());
        System.out.println("  - Tamaño vocabulario: " + vectorizer.getVocabularySize());
        System.out.println("  - Categorías: " + categoryIndex.size());
        model.printModelInfo();
    }
    
    /**
     * Clase interna para representar un par de conversación
     */
    public static class ConversationPair {
        public String userInput;
        public String response;
        public String category;
        public long timestamp;
        
        public ConversationPair(String userInput, String response, String category) {
            this.userInput = userInput;
            this.response = response;
            this.category = category;
            this.timestamp = System.currentTimeMillis();
        }
    }
    
    /**
     * Obtiene el vectorizador
     */
    public VectorizerUtil getVectorizer() {
        return vectorizer;
    }
    
    /**
     * Obtiene el número de conversaciones aprendidas
     */
    public int getConversationCount() {
        return conversationHistory.size();
    }
}
