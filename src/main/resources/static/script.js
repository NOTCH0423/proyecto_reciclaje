let currentMode = 'auto';

function setMode(mode) {
    currentMode = mode;
    document.querySelectorAll('.nav-btn').forEach(btn => btn.classList.remove('active'));
    event.currentTarget.classList.add('active');
    
    const title = document.getElementById('chat-title');
    title.textContent = mode === 'ai' ? 'Asistente Experto (IA)' : 'Asistente de Reciclaje';
    
    addMessage('bot', `Modo cambiado a: ${mode === 'ai' ? 'Inteligencia Artificial' : 'Respuestas Automáticas'}`);
    
    if (window.innerWidth <= 768) {
        toggleSidebar(); // Close sidebar on mobile after selection
    }
}

function toggleSidebar() {
    const sidebar = document.getElementById('sidebar');
    const overlay = document.getElementById('menuOverlay');
    const toggleBtn = document.getElementById('menuToggleBtn');
    const willOpen = !sidebar.classList.contains('open');
    if (willOpen) {
        sidebar.classList.add('open');
        overlay.classList.add('open');
        sidebar.setAttribute('aria-hidden', 'false');
        overlay.setAttribute('aria-hidden', 'false');
        toggleBtn.setAttribute('aria-expanded', 'true');
        const first = sidebar.querySelector('.nav-btn, .close-sidebar');
        if (first) first.focus();
    } else {
        sidebar.classList.remove('open');
        overlay.classList.remove('open');
        sidebar.setAttribute('aria-hidden', 'true');
        overlay.setAttribute('aria-hidden', 'true');
        toggleBtn.setAttribute('aria-expanded', 'false');
        toggleBtn.focus();
    }
}

function handleEnter(e) {
    if (e.key === 'Enter') {
        sendMessage();
    }
}

function quickAsk(text) {
    document.getElementById('userInput').value = text;
    sendMessage();
    if (window.innerWidth <= 768) {
        toggleSidebar();
    }
}

async function sendMessage() {
    const input = document.getElementById('userInput');
    const text = input.value.trim();
    
    if (!text) return;
    
    // Add user message
    addMessage('user', text);
    input.value = '';
    
    // Show loading state (optional)
    const loadingId = addMessage('bot', '...');
    
    try {
        const response = await fetch('/api/chat/send', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                message: text,
                mode: currentMode
            })
        });
        
        const data = await response.json();
        
        // Remove loading and add real response
        const loadingEl = document.getElementById('msg-' + loadingId);
        if (loadingEl) loadingEl.remove();
        
        // Format response (convert newlines to br, basic markdown)
        let formatted = data.response
            .replace(/\n/g, '<br>')
            .replace(/\*\*(.*?)\*\*/g, '<strong>$1</strong>');
            
        addMessage('bot', formatted);
        
    } catch (error) {
        console.error('Error:', error);
        addMessage('bot', '⚠️ Error de conexión. Por favor intenta de nuevo.');
    }
}

function addMessage(sender, html) {
    const container = document.getElementById('messages');
    const id = Date.now();
    
    const div = document.createElement('div');
    div.className = `message ${sender}`;
    div.id = 'msg-' + id;
    
    const avatar = sender === 'bot' ? '<div class="avatar">♻️</div>' : '<div class="avatar">👤</div>';
    
    div.innerHTML = `
        ${avatar}
        <div class="bubble">${html}</div>
    `;
    
    container.appendChild(div);
    container.scrollTop = container.scrollHeight;
    return id;
}

// Touch enhancements
const isTouch = navigator.maxTouchPoints > 0 || window.matchMedia('(pointer: coarse)').matches;
let zoomScale = 1;
let pinchStartDistance = 0;
let isPinching = false;
let rafId = null;

function getDistance(t1, t2) {
    const dx = t2.clientX - t1.clientX;
    const dy = t2.clientY - t1.clientY;
    return Math.hypot(dx, dy);
}

function applyZoom(scale) {
    const container = document.getElementById('messages');
    if (rafId) cancelAnimationFrame(rafId);
    rafId = requestAnimationFrame(() => {
        container.style.transform = `scale(${scale})`;
    });
}

function resetZoom() {
    zoomScale = 1;
    applyZoom(zoomScale);
}

function attachRipple(el) {
    el.addEventListener('pointerdown', (e) => {
        const circle = document.createElement('span');
        const rect = el.getBoundingClientRect();
        const size = Math.max(rect.width, rect.height);
        circle.style.width = circle.style.height = `${size}px`;
        circle.style.position = 'absolute';
        circle.style.left = `${e.clientX - rect.left - size / 2}px`;
        circle.style.top = `${e.clientY - rect.top - size / 2}px`;
        circle.style.background = 'rgba(0,0,0,0.15)';
        circle.style.borderRadius = '50%';
        circle.style.transform = 'scale(0)';
        circle.style.pointerEvents = 'none';
        circle.style.opacity = '0.6';
        circle.style.transition = 'transform 250ms ease, opacity 400ms ease';
        el.style.position = 'relative';
        el.appendChild(circle);
        requestAnimationFrame(() => {
            circle.style.transform = 'scale(1)';
            circle.style.opacity = '0';
        });
        setTimeout(() => circle.remove(), 450);
    }, { passive: true });
}

function initTouchUI() {
    document.querySelectorAll('.touch-btn,.nav-btn,.send-btn').forEach(attachRipple);
    const messages = document.getElementById('messages');
    let startX = 0;
    let startY = 0;
    let swiping = false;

    messages.addEventListener('touchstart', (e) => {
        if (e.touches.length === 2) {
            isPinching = true;
            pinchStartDistance = getDistance(e.touches[0], e.touches[1]);
            return;
        }
        const t = e.touches[0];
        startX = t.clientX;
        startY = t.clientY;
        swiping = startX < 24;
    }, { passive: true });

    messages.addEventListener('touchmove', (e) => {
        if (isPinching && e.touches.length === 2) {
            const d = getDistance(e.touches[0], e.touches[1]);
            const delta = d / pinchStartDistance;
            zoomScale = Math.min(1.4, Math.max(0.85, delta));
            applyZoom(zoomScale);
            return;
        }
        if (!swiping) return;
        const t = e.touches[0];
        const dx = t.clientX - startX;
        const dy = t.clientY - startY;
        if (Math.abs(dx) > 40 && Math.abs(dy) < 30) {
            if (dx > 0) document.querySelector('.sidebar').classList.add('open');
            if (dx < 0) document.querySelector('.sidebar').classList.remove('open');
        }
    }, { passive: true });

    messages.addEventListener('touchend', () => {
        isPinching = false;
        swiping = false;
    }, { passive: true });

    const clearBtn = document.getElementById('clearBtn');
    let holdTimer = null;
    clearBtn.addEventListener('pointerdown', () => {
        clearBtn.textContent = 'Mantén presionado...';
        holdTimer = setTimeout(() => {
            document.getElementById('messages').innerHTML = '';
            addMessage('bot', 'Chat limpiado.');
            clearBtn.textContent = '🗑️ Limpiar Chat';
        }, 700);
    });
    clearBtn.addEventListener('pointerup', () => {
        clearTimeout(holdTimer);
        clearBtn.textContent = '🗑️ Limpiar Chat';
    });
    clearBtn.addEventListener('pointerleave', () => {
        clearTimeout(holdTimer);
        clearBtn.textContent = '🗑️ Limpiar Chat';
    });

    const sendBtn = document.getElementById('sendBtn');
    sendBtn.addEventListener('click', () => {});
}

document.addEventListener('DOMContentLoaded', () => {
    if (isTouch) initTouchUI();
    const resetBtn = document.getElementById('resetZoomBtn');
    if (resetBtn) resetBtn.addEventListener('click', resetZoom, { passive: true });
    const overlay = document.getElementById('menuOverlay');
    const closeBtn = document.getElementById('closeSidebarBtn');
    const toggleBtn = document.getElementById('menuToggleBtn');
    if (overlay) overlay.addEventListener('click', () => {
        const sidebar = document.getElementById('sidebar');
        if (sidebar.classList.contains('open')) toggleSidebar();
    }, { passive: true });
    if (closeBtn) closeBtn.addEventListener('click', () => {
        const sidebar = document.getElementById('sidebar');
        if (sidebar.classList.contains('open')) toggleSidebar();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape') {
            const sidebar = document.getElementById('sidebar');
            if (sidebar.classList.contains('open')) toggleSidebar();
        }
    });
});
