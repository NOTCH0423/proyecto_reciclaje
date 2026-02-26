package com.reciclaje.util;

import java.util.*;

/**
 * Utilidad para convertir texto en vectores numéricos (embeddings)
 * Implementa un enfoque simple pero efectivo de vectorización
 */
public class VectorizerUtil {
    
    private static final int VECTOR_SIZE = 200;
    private Set<String> vocabulary;
    private Map<String, Integer> wordIndex;
    private static final String[] STOP_WORDS = {
        "el", "la", "de", "que", "y", "a", "en", "un", "es", "se",
        "no", "por", "con", "para", "una", "su", "al", "lo", "como", "más",
        "o", "pero", "sus", "le", "ya", "o", "fue", "este", "ha", "sí",
        "porque", "esta", "son", "entre", "está", "cuando", "muy", "sin",
        "sobre", "ser", "tiene", "también", "me", "hasta", "hay", "donde",
        "han", "quien", "están", "estado", "desde", "todo", "nos", "durante",
        "estados", "todos", "uno", "les", "ni", "contra", "otros", "fueron",
        "ese", "eso", "había", "ante", "ellos", "e", "esto", "mí", "antes",
        "algunos", "qué", "unos", "yo", "otro", "otras", "otra", "él", "tanto",
        "esa", "estos", "mucho", "quién", "esas", "este", "sois", "mío",
        "vosotros", "os", "mía", "tú", "te", "ti", "vuestra", "vuestro"
    };
    private static Set<String> stopWordsSet = new HashSet<>(Arrays.asList(STOP_WORDS));
    
    /**
     * Constructor
     */
    public VectorizerUtil() {
        this.vocabulary = new HashSet<>();
        this.wordIndex = new HashMap<>();
    }
    
    /**
     * Construye el vocabulario a partir de documentos
     * @param documents lista de documentos de texto
     */
    public void buildVocabulary(List<String> documents) {
        vocabulary.clear();
        wordIndex.clear();
        
        for (String doc : documents) {
            String[] words = tokenize(doc);
            for (String word : words) {
                vocabulary.add(word);
            }
        }
        
        int index = 0;
        for (String word : vocabulary) {
            wordIndex.put(word, index++);
        }
        
        System.out.println("[VectorizerUtil] Vocabulario construido con " + vocabulary.size() + " palabras");
    }
    
    /**
     * Convierte un texto en vector numérico
     * @param text texto a vectorizar
     * @return array de doubles representando el vector
     */
    public double[] vectorize(String text) {
        double[] vector = new double[VECTOR_SIZE];
        
        String[] words = tokenize(text);
        Map<Integer, Integer> wordCounts = new HashMap<>();
        
        // Contar ocurrencias de palabras
        for (String word : words) {
            if (wordIndex.containsKey(word)) {
                int idx = wordIndex.get(word);
                wordCounts.put(idx, wordCounts.getOrDefault(idx, 0) + 1);
            }
        }
        
        // Crear vector usando TF-IDF simple
        for (Map.Entry<Integer, Integer> entry : wordCounts.entrySet()) {
            int idx = entry.getKey() % VECTOR_SIZE;
            double frequency = (double) entry.getValue() / Math.max(1, words.length);
            vector[idx] += frequency;
        }
        
        // Normalizar vector
        double norm = 0.0;
        for (double v : vector) {
            norm += v * v;
        }
        norm = Math.sqrt(norm);
        
        if (norm > 0) {
            for (int i = 0; i < vector.length; i++) {
                vector[i] /= norm;
            }
        }
        
        return vector;
    }
    
    /**
     * Tokeniza un texto (divide en palabras)
     * @param text texto a tokenizar
     * @return array de palabras
     */
    public String[] tokenize(String text) {
        text = text.toLowerCase()
                   .replaceAll("[^a-záéíóúñ ]", "")
                   .trim();
        
        String[] words = text.split("\\s+");
        List<String> filtered = new ArrayList<>();
        
        for (String word : words) {
            if (!word.isEmpty() && !stopWordsSet.contains(word) && word.length() > 2) {
                filtered.add(word);
            }
        }
        
        return filtered.toArray(new String[0]);
    }
    
    /**
     * Obtiene el tamaño del vector
     */
    public int getVectorSize() {
        return VECTOR_SIZE;
    }
    
    /**
     * Obtiene el tamaño del vocabulario
     */
    public int getVocabularySize() {
        return vocabulary.size();
    }
    
    /**
     * Calcula similitud coseno entre dos vectores
     * @param v1 vector 1
     * @param v2 vector 2
     * @return similitud entre 0 y 1
     */
    public static double cosineSimilarity(double[] v1, double[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Los vectores deben tener el mismo tamaño");
        }
        
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        
        norm1 = Math.sqrt(norm1);
        norm2 = Math.sqrt(norm2);
        
        if (norm1 == 0.0 || norm2 == 0.0) {
            return 0.0;
        }
        
        return dotProduct / (norm1 * norm2);
    }
    
    /**
     * Encuentra el palabra más similar en el vocabulario
     * @param text texto a buscar
     * @return palabra más similar
     */
    public String findMostSimilarWord(String text) {
        String[] words = tokenize(text);
        if (words.length == 0) return "";
        
        double[] textVector = vectorize(text);
        String mostSimilar = "";
        double maxSimilarity = -1;
        
        for (String word : vocabulary) {
            double[] wordVector = vectorize(word);
            double similarity = cosineSimilarity(textVector, wordVector);
            if (similarity > maxSimilarity) {
                maxSimilarity = similarity;
                mostSimilar = word;
            }
        }
        
        return mostSimilar;
    }
    
    /**
     * Obtiene información sobre el vectorizador
     */
    public void printInfo() {
        System.out.println("[VectorizerUtil]");
        System.out.println("  - Tamaño vector: " + VECTOR_SIZE);
        System.out.println("  - Tamaño vocabulario: " + vocabulary.size());
        System.out.println("  - Palabras vacías eliminadas: " + STOP_WORDS.length);
    }
}
