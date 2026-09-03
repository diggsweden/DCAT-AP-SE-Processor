document.addEventListener('DOMContentLoaded', initListeners);

function initListeners() {
  document.getElementById('generate-btn').addEventListener('click', (e) => generateRdf(e.currentTarget));
  document.getElementById('copy-result').addEventListener('click', (e) => copyResult(e.currentTarget));
  document.getElementById('clear-btn').addEventListener('click', () => clearEditor());
  document.getElementById('btn-info').addEventListener('click', (e) => showInfoTab());
  document.getElementById('btn-dcat-form').addEventListener('click', (e) => showXDcatFormTab());
  document.getElementById('btn-result').addEventListener('click', (e) => showResultTab());

  initFileInput();
  initLanguageDropdown();
}

const MIN_LOADING_MS = 500;
const wait = (ms) => new Promise((r) => setTimeout(r, ms));

async function generateRdf(button) {
  const errorContainer = document.getElementById('generate-error-container');
  let infoContainer = document.querySelector('#info-container');

  let result = '';
  let error = false;
  let specs = getSources().filter((source) => source.content.trim() !== '');

  errorContainer.classList.add('hidden');
  infoContainer.classList.add('hidden');

  showResultTab();
  if (specs.length === 0) {
    showError('Fel: Ingen API specifikation angiven.');
    return;
  }

  setRDFLoading(button);
  const start = performance.now();

  try {
    const res = await fetch('/dcat-generation/spec', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ sources: specs }),
    });

    result = await res.text();
    if (!res.ok) {
      error = true;
    }
  } catch (e) {
    error = true;
    result = 'ERROR: ' + e.message;
  } finally {
    // Show loader for a minimum of MIN_LOADING_MS
    const elapsed = performance.now() - start;
    const remaining = Math.max(0, MIN_LOADING_MS - elapsed);
    await wait(remaining);
    clearRDFLoading(button);
  }

  if (error) {
    showError(result);
  } else {
    showRDFResult(result);
  }
}

function showRDFResult(result) {
  let resultElement = document.getElementById('result');
  resultElement.textContent = result;

  resultElement.classList.remove('hidden');
  document.getElementById('result-container').classList.remove('hidden');
  document.getElementById('generate-error-container').classList.add('hidden');
  document.getElementById('copy-result').classList.remove('hidden');
}

function showError(message) {
  let errorContainer = document.getElementById('generate-error-container');

  document.getElementById('result-container').classList.remove('hidden');
  document.getElementById('result').classList.add('hidden');
  document.getElementById('copy-result').classList.add('hidden');

  errorContainer.querySelector('.alert-text').textContent = message;
  errorContainer.classList.remove('hidden');
}

function initFileInput() {
  const button = document.getElementById('add-file-btn');
  const input = document.getElementById('file-input');
  if (!button || !input) return;

  button.addEventListener('click', () => input.click());

  input.addEventListener('change', async () => {
    for (const file of input.files) {
      addSource(file.name, await file.text());
    }
    input.value = '';
    window.rdfEditor.focus();
  });
}

function initLanguageDropdown() {
  const dropdown = document.getElementById('language-dropdown');
  if (!dropdown) return;

  dropdown.querySelectorAll('a[data-value]').forEach((link) => {
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
    button.classList.add('copied');

    setTimeout(() => {
      button.classList.remove('copied');
    }, 500);
  } catch (e) {
    console.error('Copy error:', e);
  }
}

function showResultTab() {
  unsetTabNav();
  hidePanels();
  document.getElementById('panel-result').classList.remove('hidden');
  document.getElementById('btn-result').classList.add('selected');
}

function showInfoTab() {
  unsetTabNav();
  hidePanels();
  document.getElementById('panel-user-guide').classList.remove('hidden');
  document.getElementById('btn-info').classList.add('selected');
}

function showXDcatFormTab() {
  unsetTabNav();
  hidePanels();
  document.getElementById('panel-dcat-form').classList.remove('hidden');
  document.getElementById('btn-dcat-form').classList.add('selected');
}

function unsetTabNav() {
  document.querySelectorAll('#tab-nav button').forEach((btn) => {
    btn.classList.remove('selected');
  });
}

function hidePanels() {
  document.getElementById('panel-result').classList.add('hidden');
  document.getElementById('panel-user-guide').classList.add('hidden');
  document.getElementById('panel-dcat-form').classList.add('hidden');
}

function setRDFLoading(button) {
  let skeleton = document.getElementById('resultSkeleton');
  let resultElement = document.getElementById('result');
  let copyBtn = document.getElementById('copy-result');
  let resultContainer = document.getElementById('result-container');

  addSkeletonLoader(skeleton, 25);

  copyBtn.classList.add('hidden');
  skeleton.classList.remove('hidden');
  resultContainer.classList.remove('hidden');
  resultElement.classList.add('hidden');
  button.setAttribute('aria-busy', 'true');
  button.disabled = true;
}

function addSkeletonLoader(parent, rows) {
  const SKELETON_WIDTHS = [
    40, 15, 50, 60, 70, 73, 75, 55, 57, 53, 65, 42, 77, 74, 73, 72, 60, 78, 40, 64, 60, 70, 82, 65, 80, 95, 100, 55, 59,
    60, 44,
  ];

  parent.replaceChildren();

  for (let i = 0; i < rows; i++) {
    const div = document.createElement('div');
    div.classList.add('skeleton');
    div.style.width = SKELETON_WIDTHS[i % SKELETON_WIDTHS.length] + '%';
    parent.appendChild(div);
  }
  parent.classList.remove('hidden');
}

function clearRDFLoading(button) {
  if (!button) return;
  let skeleton = document.getElementById('resultSkeleton');

  skeleton.classList.add('hidden');
  skeleton.replaceChildren();
  button.removeAttribute('aria-busy');
  button.disabled = false;
}

function renderTabs() {
  const bar = document.getElementById('file-tab-bar');
  if (!bar) return;
  bar.replaceChildren();

  // If only 1 default file, don't show tab bar.
  if (sources.length === 1 && sources[0].name === UNTITLED_FILE) {
    bar.classList.add('hidden');
    return;
  }

  bar.classList.remove('hidden');

  sources.forEach((source, index) => {
    const tab = document.createElement('div');
    tab.className = index === activeIndex ? 'tab active' : 'tab';

    const btn = document.createElement('button');
    btn.type = 'button';
    btn.className = 'tab-btn';
    btn.textContent = source.name;
    btn.addEventListener('click', () => selectSource(index));
    tab.appendChild(btn);

    const closeBtn = document.createElement('button');
    closeBtn.type = 'button';
    closeBtn.className = 'tab-close';
    closeBtn.textContent = '✕';
    closeBtn.setAttribute('aria-label', `Ta bort ${source.name}`);
    closeBtn.addEventListener('click', () => removeSource(index));
    tab.appendChild(closeBtn);

    bar.appendChild(tab);
  });
}

function clearEditor() {
  if (!window.rdfEditor) return;
  resetSources();
}
