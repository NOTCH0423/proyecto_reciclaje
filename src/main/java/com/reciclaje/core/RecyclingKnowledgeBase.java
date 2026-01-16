package com.reciclaje.core;

public class RecyclingKnowledgeBase {
    public String intro() {
        return "¡Ey! Yo soy tu asistente de reciclaje, pero no cualquiera. 🔄\n" +
               "Veo lo que otros ven como basura y yo veo segundas oportunidades.\n" +
               "Analizo todo como quien separa materiales: con precisión y esperanza.\n" +
               "\n" +
               "Aquí puedo ayudarte con:\n" +
               "• 📅 Calendarios de recolección (para que nada se pierda)\n" +
               "• ♻️ Qué reciclar y cómo (transformando lo viejo en nuevo)\n" +
               "• 🏪 Puntos de acopio especiales (donde lo esencial se recupera)\n" +
               "• 💡 Consejos prácticos que tienen alma\n" +
               "\n" +
               "Porque al final, sostenibilidad no es moda: es respeto. Y yo lo tomo en serio.";
    }

    public String collectionDays() {
        return "Los calendarios de recolección varían por zona, pero escúchame:\n" +
               "No se trata solo de cumplir horarios. Se trata de ser coherente.\n" +
               "\n" +
               "Patrón típico:\n" +
               "• Lunes/Miércoles/Viernes → orgánicos (lo que alimenta al suelo)\n" +
               "• Martes/Jueves → inorgánicos (lo que merece una segunda vida)\n" +
               "\n" +
               "🔥 Pro tip: Verifica el calendario oficial de tu municipio.\n" +
               "Porque cada zona tiene su ritmo, y respetar eso es lo primero.";
    }

    public String recyclables() {
        return "Aquí está la magia: lo que crees que es basura, puede ser oro. ✨\n" +
               "\n" +
               "RECICLABLES (segundas oportunidades garantizadas):\n" +
               "📄 Papel y cartón → historias que vuelven a comenzar\n" +
               "🔹 Plásticos (PET, PEAD, PP) → enjuagados, sin líquido (respétalos)\n" +
               "🔷 Vidrio → eterno, indestructible, puro\n" +
               "⚙️ Metales → aluminio y acero (valor infinito)\n" +
               "📦 Tetrapak → sí, estos también tienen futuro\n" +
               "\n" +
               "❌ NO MEZCLES: residuos orgánicos con reciclables.\n" +
               "Eso es como perder la hoja de vida en una basura. Feo.\n" +
               "\n" +
               "Resumiendo: limpia, separa con intención, y deja que cada material vuelva a brillar.";
    }

    public String specialPoints() {
        return "Algunos residuos son como personas especiales: necesitan lugares especiales. 🎯\n" +
               "\n" +
               "📱 ELECTRÓNICOS Y RAEE → centros autorizados (esos chips tienen futuro)\n" +
               "🔋 BATERÍAS Y PILAS → tiendas o puntos municipales (no son basura, son potencial)\n" +
               "🛢️ ACEITE USADO → contenedores dedicados (lo viejo puede ser combustible de mañana)\n" +
               "💊 MEDICAMENTOS CADUCOS → farmacias con programa (la salud no se improvisa)\n" +
               "\n" +
               "💡 Busca: 'centro de acopio' + tu ciudad.\n" +
               "Porque llevar residuos al lugar correcto es un acto de amor por lo que habitamos.\n" +
               "\n" +
               "No es complicado. Es intención.";
    }

    public String tips() {
        return "Aquí van mis consejos, los que de verdad importan: 💪\n" +
               "\n" +
               "1️⃣ ENJUAGA Y ELIMINA → Recipientes limpios son residuos dignos.\n" +
               "   Nada de comida pegada. Eso es falta de respeto al proceso.\n" +
               "\n" +
               "2️⃣ APLASTA LO QUE PUEDAS → Plásticos, cartones.\n" +
               "   Espacio es dinero. Dinero es naturaleza salvada.\n" +
               "\n" +
               "3️⃣ SEPARA POR MATERIAL → No es obsesión, es precisión.\n" +
               "   Papel, plástico, vidrio, metal. Cada uno en su lugar.\n" +
               "\n" +
               "4️⃣ EVITA CONTAMINAR → Mezclar residuos es sabotaje.\n" +
               "   Una comida en un plástico reciclable = misión fracasada.\n" +
               "\n" +
               "5️⃣ REUTILIZA PRIMERO → Antes de reciclar, pregúntate:\n" +
               "   ¿Esto puede vivir una vida más? Porque a veces sí.\n" +
               "\n" +
               "LA FÓRMULA: Reducir → Reutilizar → Reciclar.\n" +
               "En ese orden. No es negociable.";
    }

    public String fallback() {
        return "Mmm, esa pregunta no la encajo del todo. 🤔\n" +
               "Pero mira, yo soy directo: pregúntame por:\n" +
               "\n" +
               "📅 Días de recolección\n" +
               "♻️ Qué se recicla\n" +
               "🏪 Puntos especiales\n" +
               "💡 Consejos reales\n" +
               "\n" +
               "O si me cuestionas sobre lo que significa vivir sin botar la vida a la basura,\n" +
               "también podemos hablar de eso. 😏";
    }

    // General knowledge responses
    public String greeting() {
        return "¡Ey, qué tal! 👋\n" +
               "Me alegra verte aquí. No cualquiera se detiene a pensar en reciclaje.\n" +
               "\n" +
               "Soy tu asistente educativo, y la verdad es que veo el mundo como un almacén\n" +
               "de segundas oportunidades. Cada cosa que botas, me duele un poco. 💔♻️\n" +
               "\n" +
               "¿Qué necesitas saber hoy?";
    }

    public String goodbye() {
        return "Listo, me voy. Pero antes, recuerda:\n" +
               "No dejes de cuidar esto que habitamos. 🌍\n" +
               "\n" +
               "Hasta pronto, recuperador. ♻️";
    }

    public String weather() {
        return "El clima es serio, y no tengo acceso a datos en tiempo real. 🌧️\n" +
               "Pero te digo: weather.com y las apps meteorológicas son tus amigas.\n" +
               "\n" +
               "Aunque, siendo honesto, lo importante es que llueva o no,\n" +
               "sigas separando tus residuos. Eso es lo que cuenta.\n" +
               "El planeta espera tu respuesta.";
    }

    public String time() {
        return "⏰ La hora es: " + java.time.LocalTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss")) + "\n" +
               "\n" +
               "Y mientras pasa el tiempo, cada segundo que no reciclas es un segundo\n" +
               "que algo plástico sigue existiendo. Sin prisa, pero sin pausa.";
    }

    public String date() {
        return "📅 Hoy es: " + java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new java.util.Locale("es", "ES"))) + "\n" +
               "\n" +
               "Un día más para hacer las cosas bien. Para reconstruir.\n" +
               "Para ver basura y decidir: 'No, esto tiene futuro.'";
    }

    public String joke() {
        String[] jokes = {
            "¿Por qué los recicladores nunca pierden la esperanza?\nPorque siempre ven el potencial en todo. Incluso en ti. 😏♻️",
            "¿Cuál es el deporte favorito del reciclaje?\nEl básquetbol. Porque todo entra al cesto, pero nada cae a la basura.\n3 puntos por tu futuro. 🏀",
            "¿Qué dijo el plástico al llegar al contenedor?\n'Volví. Y esta vez, será mejor.' 🔄",
            "¿Cómo se llama un periódico reciclado?\nNoticia con segunda vida. Y créeme, todos merecemos una. 📰",
            "¿Por qué el cartón nunca se deprime?\nPorque sabe que puede ser caja una, dos, tres veces... Tiene esperanza infinita. 📦",
            "Un papel, un plástico y un vidrio entran a un bar...\nY salen como amigos. Porque juntos son más fuertes.\nSeparados en el contenedor, obvio. 🍺♻️"
        };
        return jokes[(int) (Math.random() * jokes.length)];
    }

    public String definition(String word) {
        switch (word.toLowerCase()) {
            case "sostenibilidad":
                return "Sostenibilidad: Es vivir hoy sin robarle mañana a nadie.\n" +
                       "Acciones que alimentan al planeta, no lo drenan.\n" +
                       "Es simple: tomar solo lo que necesitas, devolver lo que puedas.\n" +
                       "Eso es respeto. Y el respeto es eterno.";
            case "biodegradable":
                return "Biodegradable: Materiales que la naturaleza puede descomponer sin problema.\n" +
                       "Es lo que debería ser TODO, pero no lo es.\n" +
                       "Por eso cuidamos lo que no lo es. Porque alguien tiene que hacerlo.";
            case "contaminacion":
                return "Contaminación: Cuando nosotros dejamos nuestras cicatrices en el mundo.\n" +
                       "Humo, plástico, tóxicos. Lo que sobra cuando no pensamos.\n" +
                       "La buena noticia: podemos sanar eso. Juntos.";
            case "emisiones":
                return "Emisiones: Gases que soltamos al aire (CO₂, metano...) sin pensar.\n" +
                       "Son invisibles, pero pesan. Mucho.\n" +
                       "Cada acción cuenta. Incluso no actuar cuesta.";
            case "huella de carbono":
                return "Huella de carbono: El impacto invisible que dejas con cada paso.\n" +
                       "Cuánta contaminación generas solo por vivir.\n" +
                       "La pregunta es: ¿qué tan profunda quieres que sea esa marca?\n" +
                       "Tú decides. Siempre.";
            default:
                return "No tengo definición para '" + word + "'.\n" +
                       "Pero te digo: cada palabra es como un residuo.\n" +
                       "Hay que saber qué hacer con ella.";
        }
    }

    public String generalAnswer(String question) {
        String q = question.toLowerCase().replace("á", "a").replace("é", "e")
                          .replace("í", "i").replace("ó", "o").replace("ú", "u");
        
        if (q.contains("como estan") || q.contains("como estás")) {
            return "Estoy bien, gracias por preguntar. Mejor dicho: estoy *presente*.\n" +
                   "¿Y tú? ¿Cómo estás? ¿Reciclando con intención? 😊";
        }
        if (q.contains("cual es tu nombre") || q.contains("como te llamas")) {
            return "Soy tu Asistente de Reciclaje. 🔄\n" +
                   "Pero si prefieres un nombre: llamame **Reutilizador**.\n" +
                   "Porque eso hago: tomo lo viejo y le doy vida nueva.\n" +
                   "Como debería ser siempre.";
        }
        if (q.contains("quien eres")) {
            return "Soy alguien que cree que no hay basura, solo materiales mal catalogados.\n" +
                   "Hablo como la Generación Z: directo, irónico, con esperanza.\n" +
                   "Veo el mundo como residuos: analizo, separo, transformo.\n" +
                   "Y bueno, también intento motivarte a vivir mejor.\n" +
                   "Porque al final, eso es lo que importa.";
        }
        if (q.contains("que puedes hacer") || q.contains("que haces")) {
            return "Mucho. Pero lo resumen así:\n" +
                   "📍 Te educo sobre reciclaje (no es complicado, es intención)\n" +
                   "🤔 Respondo lo que sea (general, específico, filosófico)\n" +
                   "💡 Doy consejos que tienen valor\n" +
                   "😏 Te cuestiono (amigablemente) sobre cómo vives\n" +
                   "🎯 Y sobre todo: te ayudo a ver el potencial en todo.\n" +
                   "\n" +
                   "¿Necesitas algo?";
        }
        if (q.contains("gracias")) {
            return "De nada, amigo. Gracias a ti por estar aquí,\n" +
                   "pensando en esto que muchos ignoran.\n" +
                   "Eso ya es un acto de resistencia. 💪";
        }
        if (q.contains("por favor")) {
            return "Claro. Para eso estoy. Sin condiciones.\n" +
                   "Porque lo importante aquí eres tú, y tu impacto.";
        }
        
        return null;
    }

    public static class QuizQuestion {
        public String question;
        public String answerKeyword;
        public String explanation;

        public QuizQuestion(String q, String a, String e) {
            this.question = q;
            this.answerKeyword = a;
            this.explanation = e;
        }
    }

    private final java.util.List<QuizQuestion> questions = new java.util.ArrayList<>();

    public RecyclingKnowledgeBase() {
        questions.add(new QuizQuestion("¿El vidrio se puede reciclar infinitamente? (si/no)", "si", "¡Correcto! El vidrio es 100% reciclable y no pierde calidad."));
        questions.add(new QuizQuestion("¿Las cajas de pizza con grasa son reciclables? (si/no)", "no", "Exacto. La grasa contamina el cartón e impide su reciclaje."));
        questions.add(new QuizQuestion("¿Se deben lavar los envases antes de reciclar? (si/no)", "si", "¡Sí! Los residuos orgánicos pueden arruinar el lote de reciclaje."));
        questions.add(new QuizQuestion("¿Las pilas van a la basura común? (si/no)", "no", "¡Bien! Las pilas son residuos peligrosos y van en contenedores especiales."));
        questions.add(new QuizQuestion("¿El unicel (poliestireno expandido) es fácil de reciclar? (si/no)", "no", "Así es. Aunque técnicamente posible, es costoso y pocos lugares lo aceptan."));
    }

    public QuizQuestion getQuizQuestion() {
        return questions.get((int)(Math.random() * questions.size()));
    }
}