# 🧠 Sistema de Red Neuronal Inteligente para el Bot de Reciclaje

## Descripción General

El bot educativo de reciclaje ahora cuenta con un **sistema de red neuronal integrado** que aprende de cada interacción con los usuarios. El modelo utiliza **Deeplearning4j (DL4J)** para crear una red neuronal que mejora continuamente su capacidad de respuesta.

## 🏗️ Arquitectura del Modelo

### Red Neuronal Profunda (Deep Neural Network)
```
Entrada (200 características)
    ↓
Capa Oculta 1 (128 neuronas) - ReLU
    ↓
Capa Oculta 2 (64 neuronas) - ReLU
    ↓
Capa Oculta 3 (32 neuronas) - ReLU
    ↓
Capa Salida (8 categorías) - Softmax
    ↓
Categorías de Respuesta
```

### Parámetros del Modelo
- **Función de activación**: ReLU (capas ocultas), Softmax (salida)
- **Optimizador**: Adam
- **Tasa de aprendizaje**: 0.01
- **Función de pérdida**: Categorical Cross-Entropy
- **Regularización**: L2 (0.0001)

## 📊 Componentes Principales

### 1. **NeuralModel.java**
Implementa la red neuronal base con DL4J.

**Funcionalidades**:
- Inicialización de la arquitectura de red
- Entrenamiento del modelo (lotes y incremental)
- Predicción y clasificación
- Persistencia (guardar/cargar modelo)

**Ejemplo de uso**:
```java
NeuralModel model = new NeuralModel(200, 8, "data/neural-model/model.zip");
model.initNetwork();
model.train(inputArray, outputArray, 10);
model.save();
```

### 2. **VectorizerUtil.java**
Convierte texto a vectores numéricos (embeddings).

**Características**:
- Tokenización y limpieza de texto
- Eliminación de palabras vacías (stop words)
- Vectorización TF-IDF normalizada
- Similitud coseno entre textos

**Ejemplo**:
```java
VectorizerUtil vectorizer = new VectorizerUtil();
double[] vector = vectorizer.vectorize("¿Cómo reciclo plástico?");
```

### 3. **ConversationLearner.java**
Gestor del aprendizaje conversacional e histórico.

**Funcionalidades**:
- Clasificación automática de preguntas en 8 categorías
- Almacenamiento de historial de conversaciones (CSV)
- Entrenamiento incremental (per-conversation)
- Predicción de categoría de nuevas preguntas

**Categorías**:
1. **general** - Preguntas generales
2. **plastico** - Reciclaje de plástico
3. **papel** - Reciclaje de papel
4. **vidrio** - Reciclaje de vidrio
5. **organico** - Residuos orgánicos
6. **electronico** - Residuos electrónicos
7. **peligroso** - Materiales peligrosos
8. **desconocido** - Respuestas no clasificadas

### 4. **NeuralResponder.java**
Responder mejorado que integra la red neuronal con la base de conocimiento.

**Características**:
- Predicción inteligente de categoría
- Enriquecimiento automático de respuestas
- Aprendizaje de cada interacción
- Guardado periódico de progreso

## 🚀 Cómo Usar

### Acceso por API

#### 1. Enviar Mensaje (Modo Neural - Por Defecto)
```bash
POST http://localhost:5000/api/chat/send
Content-Type: application/json

{
  "message": "¿Cómo reciclo botellas de plástico?",
  "mode": "neural"
}
```

**Respuesta**:
```json
{
  "response": "Las botellas de plástico se deben depositar en el contenedor amarillo...\n\n💡 Consejo especial sobre PLÁSTICO: Recuerda que no todos los plásticos se reciclan igual. Verifica el símbolo de reciclaje en el producto (1-7).",
  "mode": "neural",
  "neural_enabled": "true"
}
```

#### 2. Obtener Estadísticas
```bash
GET http://localhost:5000/api/chat/stats
```

**Respuesta**:
```json
{
  "neural_enabled": true,
  "conversations_learned": 42,
  "model_path": "data/neural-model/recycling-model.zip"
}
```

#### 3. Entrenar Modelo
```bash
POST http://localhost:5000/api/chat/train
```

## 💾 Estructura de Archivos Guardados

```
data/
└── neural-model/
    ├── recycling-model.zip          # Modelo neural entrenado
    └── conversation_history.csv     # Historial de conversaciones
        (columnas: USER_INPUT, RESPONSE, CATEGORY, TIMESTAMP)
```

## 📈 Proceso de Aprendizaje

### 1. **Aprendizaje Incremental Automático**
Cada interacción del usuario:
1. Se vectoriza la entrada
2. Se predice la categoría
3. Se enriquece la respuesta
4. Se entrena el modelo con esta nueva muestra
5. Se guarda periódicamente

### 2. **Entrenamiento por Lotes**
Cuando se desee entrenar con el historial completo:
```bash
POST http://localhost:5000/api/chat/train
```

## 🎯 Beneficios

✅ **Aprendizaje Continuo**: El modelo mejora con cada interacción  
✅ **Respuestas Personalizadas**: Enriquece respuestas según la categoría detectada  
✅ **Clasificación Automática**: Identifica el tipo de pregunta automáticamente  
✅ **Persistencia**: Mantiene el aprendizaje entre sesiones  
✅ **Escalable**: Fácil de expandir a más categorías  

## 🔧 Configuración Avanzada

### Cambiar Hiperparámetros
Editar en `NeuralModel.java`:
```java
private static final double LEARNING_RATE = 0.01;  // Tasa de aprendizaje
private static final int RANDOM_SEED = 42;          // Semilla aleatoria
```

### Cambiar Tamaño de Red
Editar el método `initNetwork()` en `NeuralModel.java`:
```java
.layer(0, new DenseLayer.Builder()
    .nIn(inputSize)
    .nOut(256)  // Aumentar número de neuronas
    ...
```

### Agregar Nuevas Categorías
Editar `ConversationLearner.initializeCategories()`:
```java
String[] categories = {
    "general", "plastico", "papel", "vidrio",
    "organico", "electronico", "peligroso",
    "nueva_categoria"  // Nueva categoría
};
```

## 📊 Monitoreo del Aprendizaje

El modelo registra información sobre el progreso en consola:

```
[NeuralModel] Red neuronal inicializada
  - Entrada: 200
  - Capas ocultas: 128 -> 64 -> 32
  - Salida: 8

[ConversationLearner] Entrenando con 45 muestras...
[NeuralModel] Época 1/10 - Score: 0.8234
[NeuralModel] Época 5/10 - Score: 0.3421
[NeuralModel] Época 10/10 - Score: 0.1234

[ConversationLearner] Estadísticas:
  - Conversaciones aprendidas: 45
  - Tamaño vocabulario: 234
  - Categorías: 8
```

## 🛠️ Depuración

### Ver Información del Modelo
```java
neuralResponder.printStatistics();
```

### Habilitar Logs Detallados
El sistema imprime automáticamente:
- Predicciones de categoría
- Confianza de predicción
- Estado de guardado de modelo
- Progreso de entrenamiento

## 🔐 Almacenamiento en Google Drive

Para sincronizar los datos aprendidos con Google Drive:

1. **Configurar ruta de sincronización**:
   - Guarda los datos en una carpeta de Google Drive
   - Usa Google Drive para Windows/Mac

2. **Ruta actual**: `data/neural-model/`
   - Puedes cambiarla en `NeuralResponder.java` línea 15:
   ```java
   private static final String DATA_PATH = "ruta/a/google/drive";
   ```

## 📝 Próximas Mejoras

- [ ] Word embeddings (Word2Vec)
- [ ] Attention mechanisms
- [ ] Multi-language support
- [ ] Real-time model versioning
- [ ] Análisis de sentimiento
- [ ] Generación de respuestas dinámicas

## ⚠️ Notas Importantes

1. **Primera ejecución**: El modelo tarda en inicializarse (instala las librerías DL4J)
2. **Persistencia**: Los datos se guardan cada 5 interacciones automáticamente
3. **Reentrenamiento**: Se recomienda reentrenar después de ~50 nuevas conversaciones
4. **Memoria**: Para aplicaciones en producción, considerar reducir tamaño de capas

## 📞 Soporte

Si tienes preguntas sobre el módulo neural:
- Revisa los logs en la consola
- Verifica que `data/neural-model/` tenga permisos de escritura
- Asegúrate que Java 17+ esté instalado
