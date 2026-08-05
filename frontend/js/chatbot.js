const API_BASE_URL = (window.CONFIG && window.CONFIG.API_BASE_URL)
    ? window.CONFIG.API_BASE_URL
    : window.location.origin;
const CHATBOT_API_URL = API_BASE_URL.replace(/\/$/, '') + '/chatbot/query';

function createChatbotWidget() {
    if (document.getElementById('chatbot-widget')) return;

    const widget = document.createElement('div');
    widget.id = 'chatbot-widget';
    widget.innerHTML = `
        <button id="chatbot-toggle" aria-label="Open chatbot">Ask SeventMS</button>
        <div id="chatbot-panel" aria-hidden="true">
            <div class="chatbot-header">
                <strong>SeventMS Assistant</strong>
                <button id="chatbot-close" aria-label="Close chatbot">×</button>
            </div>
            <div id="chatbot-body">
                <div id="chatbot-messages" class="chatbot-messages"></div>
                <form id="chatbot-form" class="chatbot-form">
                    <input id="chatbot-input" type="text" placeholder="Ask about events, bookings, or admin" autocomplete="off" />
                    <button type="submit">Send</button>
                </form>
            </div>
        </div>
    `;
    document.body.appendChild(widget);

    const toggle = document.getElementById('chatbot-toggle');
    const close = document.getElementById('chatbot-close');
    const panel = document.getElementById('chatbot-panel');
    const form = document.getElementById('chatbot-form');
    const input = document.getElementById('chatbot-input');
    const messages = document.getElementById('chatbot-messages');

    toggle.addEventListener('click', () => {
        const isOpen = panel.classList.toggle('open');
        panel.setAttribute('aria-hidden', String(!isOpen));
        if (isOpen) {
            input.focus();
        }
    });

    close.addEventListener('click', () => {
        panel.classList.remove('open');
        panel.setAttribute('aria-hidden', 'true');
    });

    form.addEventListener('submit', async (event) => {
        event.preventDefault();
        const question = input.value.trim();
        if (!question) return;
        appendMessage('user', question);
        input.value = '';
        appendMessage('system', 'Searching the project knowledge base...');
        try {
            const response = await fetch(CHATBOT_API_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                credentials: 'include',
                body: JSON.stringify({ query: question })
            });
            const payload = await response.json();
            removeLastSystemMessage();
            if (payload && payload.success) {
                appendMessage('assistant', payload.answer);
                if (payload.references && payload.references.length) {
                    appendMessage('assistant', renderReferences(payload.references));
                }
            } else {
                appendMessage('assistant', payload.message || 'I could not answer that right now.');
            }
        } catch (error) {
            removeLastSystemMessage();
            appendMessage('assistant', 'Chatbot request failed. Please try again later.');
            console.error('Chatbot error:', error);
        }
    });

    function appendMessage(role, text) {
        const message = document.createElement('div');
        message.className = `chatbot-message chatbot-${role}`;
        if (typeof text === 'string') {
            message.textContent = text;
        } else {
            message.appendChild(text);
        }
        messages.appendChild(message);
        messages.scrollTop = messages.scrollHeight;
    }

    function removeLastSystemMessage() {
        const systemMessages = messages.querySelectorAll('.chatbot-system');
        if (systemMessages.length > 0) {
            systemMessages[systemMessages.length - 1].remove();
        }
    }

    function renderReferences(references) {
        const container = document.createElement('div');
        container.className = 'chatbot-references';
        const heading = document.createElement('div');
        heading.className = 'chatbot-references-heading';
        heading.textContent = 'Referenced files:';
        container.appendChild(heading);

        const list = document.createElement('ul');
        references.forEach(ref => {
            const item = document.createElement('li');
            item.textContent = `${ref.file}${ref.line ? ` (line ${ref.line})` : ''}`;
            list.appendChild(item);
        });
        container.appendChild(list);
        return container;
    }
}

function initializeChatbot() {
    if (document.readyState !== 'loading') {
        createChatbotWidget();
    } else {
        document.addEventListener('DOMContentLoaded', createChatbotWidget);
    }
}

initializeChatbot();
