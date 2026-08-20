document.addEventListener('DOMContentLoaded', initListeners);

function initListeners(){
    document.getElementById('validate-btn').addEventListener('click', (e) => generateRdf(e.currentTarget));
    document.getElementById('copy-result').addEventListener('click', (e) => copyResult(e.currentTarget));
    document.getElementById('clear-btn').addEventListener('click', () => clearEditor());
    document.getElementById('btn-info').addEventListener('click', (e) => showInfo());
    document.getElementById('btn-dcat-form').addEventListener('click', (e) => showXDcatForm());
    document.getElementById('btn-result').addEventListener('click', (e) => showResult());
    
    initFileInput();
    initLanguageDropdown();
}

async function generateRdf(button) {
    const resultElement = document.getElementById('result');
    const resultContainer = document.getElementById('result-container');
    const errorContainer = document.getElementById('generate-error-container');
    
    let value = window.rdfEditor.getValue();
    let result = "";
    let error = false;

    errorContainer.classList.add('hidden')

    let infoContainer = document.querySelector('#info-container');
    if(infoContainer){
        infoContainer.remove();
    }

    if(value === ''){
        showError('Fel: Ingen API specifikation angiven.')
        showResult();
        return;
    }
    
    setLoading(button);
    resultElement.classList.add('updating')
    try {
        const res = await fetch('/api/dcat/generate', {
            method: 'POST',
            headers: { 'Content-Type': 'text/plain' },
            body: window.rdfEditor.getValue()
        });

        result = await res.text();
        if(!res.ok){
            error = true;
            showError(result)
        } 
    } catch (e) {
        error = true;
        showError('Fel: ' + e.message);
    }

    setTimeout(() => {
        resultContainer.classList.remove('hidden');
        resultElement.classList.remove('updating')

        if(!error) {
            resultElement.textContent = result    
            resultElement.classList.remove('hidden')
            document.getElementById('copy-result').classList.remove('hidden');
        } 
        clearLoading(button);
        showResult();
    }, 500);
}

function showError(message){
    let errorContainer = document.getElementById('generate-error-container');
    document.getElementById('result-container').classList.remove('hidden');
    document.getElementById('result').classList.add('hidden');
    document.getElementById('copy-result').classList.add('hidden');

    errorContainer.querySelector('.error-text').textContent = message;
    errorContainer.classList.remove('hidden');
}


function initFileInput() {
    const button = document.getElementById('add-file-btn');
    const input = document.getElementById('file-input');
    if (!button || !input) return;

    button.addEventListener('click', () => input.click());

    input.addEventListener('change', async () => {
        const file = input.files[0];
        if (!file) return;

        const text = await file.text();
        window.rdfEditor.setValue(text);

        const ext = file.name.split('.').pop().toLowerCase();
        const languageMap = { json: 'json', yaml: 'yaml', yml: 'yaml', raml: 'yaml' };
        const language = languageMap[ext] || 'plaintext';
        setEditorLanguage(language);

        window.rdfEditor.focus();
        input.value = '';
    });
}

function initLanguageDropdown() {
    const dropdown = document.getElementById('language-dropdown');
    if (!dropdown) return;

    dropdown.querySelectorAll('a[data-value]').forEach(link => {
        link.addEventListener('click', (e) => {
            e.preventDefault();
            setEditorLanguage(link.dataset.value);
            dropdown.removeAttribute('open');
        });
    });
}

async function copyResult(button) {
    if (!button) return;
    const result = document.getElementById('result').textContent;
    try {
        await navigator.clipboard.writeText(result);
        const original = button.textContent;
        button.textContent = 'Kopierat';
        setTimeout(() => { button.textContent = original; }, 1500);
    } catch (e) {
        console.error('Copy error:', e);
    }
}

function showResult(){
    document.querySelectorAll('#info-nav button').forEach(btn => { btn.classList.remove('selected')});
    hidePanels();
    document.getElementById('panel-result').classList.remove('hidden');
    document.getElementById('btn-result').classList.add('selected')
}

function showInfo(){
    document.querySelectorAll('#info-nav button').forEach(btn => { btn.classList.remove('selected')});
    hidePanels();
    document.getElementById('panel-user-guide').classList.remove('hidden');
    document.getElementById('btn-info').classList.add('selected');
}

function showXDcatForm(){
    document.querySelectorAll('#info-nav button').forEach(btn => { btn.classList.remove('selected')});
    hidePanels();
    document.getElementById('panel-dcat-form').classList.remove('hidden');
    document.getElementById('btn-dcat-form').classList.add('selected');
}

function hidePanels(){
    document.getElementById('panel-result').classList.add('hidden');
    document.getElementById('panel-user-guide').classList.add('hidden');
    document.getElementById('panel-dcat-form').classList.add('hidden');
}

function setLoading(element) {
    if (!element) return;
    element.setAttribute('aria-busy', 'true');
    element.disabled = true;
}

function clearLoading(element) {
    if (!element) return;
    element.removeAttribute('aria-busy');
    element.disabled = false;
}
