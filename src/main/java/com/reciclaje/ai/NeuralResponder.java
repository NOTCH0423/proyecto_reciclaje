package com.reciclaje.ai;

import com.reciclaje.core.Responder;
import com.reciclaje.core.RuleBasedResponder;

/**
 * Responder mejorado con Red Neuronal
 * Utiliza un modelo neuronal entrenado para mejorar la inteligencia del bot
 * Aprende de cada interacción del usuario
 */
public class NeuralResponder implements Responder {
    
    private NeuralModel model;
    private ConversationLearner learner;
    private RuleBasedResponder baseResponder;
    private boolean isEnabled;
    // Rutas para guardar el modelo y los datos de conversación DENTRO del proyecto
    private static final String DATA_PATH = "base_data/neural-model";
    private static final String MODEL_PATH = DATA_PATH + "/recycling-model.zip";
    
    /**
     * Constructor
     */
    public NeuralResponder() {
        this.baseResponder = new RuleBasedResponder();
        this.isEnabled = false;
        initializeModel();
    }
    
    /**
     * Inicializa el modelo neuronal
     */
    private void initializeModel() {
        try {
            System.out.println("[NeuralResponder] Inicializando modelo neuronal...");
            
            // Crear modelo
            model = new NeuralModel(200, 8, MODEL_PATH);
            
            // Cargar modelo existente o crear uno nuevo
            model.load();
            
            // Crear gestor de aprendizaje
            learner = new ConversationLearner(model, DATA_PATH);
            
            System.out.println("[NeuralResponder] Modelo neural cargado exitosamente");
            System.out.println("[NeuralResponder] Conversaciones previas: " + learner.getConversationCount());
            
            isEnabled = true;
        } catch (Exception e) {
            System.err.println("[NeuralResponder] Error inicializando modelo: " + e.getMessage());
            isEnabled = false;
        }
    }
    
    @Override
    public String respond(String userInput) {
        if (!isEnabled || model == null || learner == null) {
            return baseResponder.respond(userInput);
        }
        
        try {
            // Predecir categoría con red neuronal
            String predictedCategory = learner.predictCategory(userInput);
            
            // Obtener respuesta base
            String baseResponse = baseResponder.respond(userInput);
            
            // Mejorar respuesta basándose en categoría predicha
            String enhancedResponse = enhanceResponse(baseResponse, predictedCategory, userInput);
            
            // Aprender de la interacción
            learner.learnFromConversation(userInput, enhancedResponse, predictedCategory);
            
            return enhancedResponse;
            
        } catch (Exception e) {
            System.err.println("[NeuralResponder] Error en predicción: " + e.getMessage());
            return baseResponder.respond(userInput);
        }
    }
    
    /**
     * Mejora la respuesta basándose en la categoría predicha
     */
    private String enhanceResponse(String baseResponse, String category, String userInput) {
        StringBuilder enhanced = new StringBuilder(baseResponse);
        
        switch (category.toLowerCase()) {
            case "plastico":
                enhanced.append("\n\n💡 Consejo especial sobre PLÁSTICO: ");
                enhanced.append("Recuerda que no todos los plásticos se reciclan igual. ");
                enhanced.append("Verifica el símbolo de reciclaje en el producto (1-7).");
                break;
                
            case "papel":
                enhanced.append("\n\n📄 Consejo especial sobre PAPEL: ");
                enhanced.append("El papel mojado o con comida no se puede reciclar. ");
                enhanced.append("Compacta el papel para ahorrar espacio en los contenedores.");
                break;
                
            case "vidrio":
                enhanced.append("\n\n🔷 Consejo especial sobre VIDRIO: ");
                enhanced.append("Asegúrate de que el vidrio esté limpio antes de reciclarlo. ");
                enhanced.append("El cristal y el espejo NO se reciclan con vidrio común.");
                break;
                
            case "organico":
                enhanced.append("\n\n🌿 Consejo especial sobre RESIDUOS ORGÁNICOS: ");
                enhanced.append("Los residuos orgánicos pueden compostarse en casa. ");
                enhanced.append("Esto reduce la basura enviada a rellenos sanitarios.");
                break;
                
            case "electronico":
                enhanced.append("\n\n🔌 Consejo especial sobre ELECTRÓNICOS: ");
                enhanced.append("Los aparatos electrónicos contienen materiales tóxicos. ");
                enhanced.append("Llévelos a puntos de recogida especializados, no a basura común.");
                break;
                
            case "peligroso":
                enhanced.append("\n\n⚠️ Consejo importante sobre MATERIALES PELIGROSOS: ");
                enhanced.append("Maneja con cuidado. Contacta con autoridades locales para ");
                enhanced.append("disposición segura de materiales peligrosos.");
                break;
                
            default:
                enhanced.append("\n\n📚 Si tienes dudas específicas, te recomendamos ");
                enhanced.append("categorizar mejor tu pregunta por tipo de residuo.");
        }
        
        return enhanced.toString();
    }
    
    /**
     * Entrena el modelo con datos históricos
     */
    public void trainModel() {
        if (!isEnabled || learner == null) {
            System.err.println("[NeuralResponder] Modelo no inicializado");
            return;
        }
        
        System.out.println("[NeuralResponder] Iniciando entrenamiento del modelo...");
        learner.trainModel();
        System.out.println("[NeuralResponder] Entrenamiento completado");
    }
    
    /**
     * Guarda el estado actual del modelo
     */
    public void saveModel() {
        if (!isEnabled || model == null) {
            return;
        }
        
        model.save();
        learner.saveConversationHistory();
        System.out.println("[NeuralResponder] Modelo guardado");
    }
    
    /**
     * Obtiene estadísticas del aprendizaje
     */
    public void printStatistics() {
        if (!isEnabled || learner == null) {
            System.out.println("[NeuralResponder] Modelo no disponible");
            return;
        }
        
        learner.printStatistics();
    }
    
    /**
     * Verifica si el módulo neural está habilitado
     */
    public boolean isNeuralEnabled() {
        return isEnabled;
    }
    
    /**
     * Obtiene el número de conversaciones aprendidas
     */
    public int getLearnedConversationCount() {
        if (learner != null) {
            return learner.getConversationCount();
        }
        return 0;
    }
}
