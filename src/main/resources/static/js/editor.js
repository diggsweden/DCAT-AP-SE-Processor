document.addEventListener('DOMContentLoaded', initEditor);

function initEditor() {
    const container = document.getElementById('editor');
    if (!container) return;

    const vsPath = container.dataset.vsPath;
    require.config({ paths: { vs: vsPath } });

    require(['vs/editor/editor.main'], function () {
        window.rdfEditor = monaco.editor.create(container, {
            value: '',
            language: 'json',
            theme: 'vs-light',
            automaticLayout: true,
            minimap: { enabled: false },
            scrollBeyondLastLine: false  
        });
    });
};

function setEditorLanguage(language) {
    if (!window.rdfEditor) return;

    monaco.editor.setModelLanguage(window.rdfEditor.getModel(), language);

    document.querySelectorAll('#language-dropdown a[data-value]').forEach(a => {
        a.classList.toggle('selected', a.dataset.value === language);
    });
}

function getEditorValue() {
    if (!window.rdfEditor) return '';
    return window.rdfEditor.getValue();
}

function setEditorValue(value) {
    if (!window.rdfEditor) return;
    window.rdfEditor.setValue(value);
}

function clearEditor() {
    if (!window.rdfEditor) return;
    window.rdfEditor.setValue('');
    window.rdfEditor.focus();
    document.getElementById('result-container').classList.add('hidden');
}
