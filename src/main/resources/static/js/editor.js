document.addEventListener('DOMContentLoaded', initEditor);

const sources = [];
let activeIndex = -1;
const UNTITLED_BASE = 'untitled';
const UNTITLED_FILE = UNTITLED_BASE + '.json';

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
      scrollBeyondLastLine: false,
    });
    addSource(UNTITLED_FILE, '');
  });
}

function addSource(name, content) {
  if (sources.length === 1 && isEmptyUntitled(sources[0])) {
    sources[0].model.dispose();
    sources.length = 0;
  }

  const model = monaco.editor.createModel(content, languageFor(name));
  sources.push({ name, model });
  selectSource(sources.length - 1);
}

function isEmptyUntitled(source) {
  return source.name === UNTITLED_FILE && source.model.getValue() === '';
}

function selectSource(index) {
  activeIndex = index;
  window.rdfEditor.setModel(sources[index].model);
  syncLanguageDropdown(sources[index].model.getLanguageId());
  renderTabs();
}

function removeSource(index) {
  sources[index].model.dispose();
  sources.splice(index, 1);

  if (sources.length === 0) {
    addSource(UNTITLED_FILE, '');
    return;
  }
  
  let nextIndex = activeIndex;
  if (index <= activeIndex) {
    // Keep the same tab selected after the splice.
    nextIndex = Math.max(0, activeIndex - 1);
  }
  selectSource(nextIndex);
}

function resetSources() {
  sources.forEach((source) => source.model.dispose());
  sources.length = 0;
  addSource(UNTITLED_FILE, '');
  window.rdfEditor.focus();
}

function getSources() {
  return sources.map((source) => ({
    name: sourceName(source),
    content: source.model.getValue(),
  }));
}

function languageFor(name) {
  const ext = name.split('.').pop().toLowerCase();
  const languageMap = { json: 'json', yaml: 'yaml', yml: 'yaml', raml: 'yaml' };
  return languageMap[ext] || 'plaintext';
}

function extensionFor(source) {
  const content = source.model.getValue().trimStart();
  if (content.startsWith('#%RAML')) {
    return 'raml';
  }
  if (content === '' || content.startsWith('{')) {
    return 'json';
  }
  return 'yaml';
}

function sourceName(source) {
  if (!source.name.startsWith(UNTITLED_BASE)) {
    return source.name;
  }
  return `${UNTITLED_BASE}.${extensionFor(source)}`;
}

function setEditorLanguage(language) {
  if (activeIndex < 0) return;
  monaco.editor.setModelLanguage(sources[activeIndex].model, language);
  syncLanguageDropdown(language);
}

function syncLanguageDropdown(language) {
  document.querySelectorAll('#language-dropdown a[data-value]').forEach((a) => {
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
