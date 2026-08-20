document.addEventListener('DOMContentLoaded', initFormListeners);

function initFormListeners() {
    document.querySelector('#generate-xdcat').addEventListener('click', handleGenerateClick);
}

async function handleGenerateClick(event) {
    event.preventDefault();
    const form = document.querySelector('#dcat-form');
    const dcatJson = await buildDcatJson(form);
    submitDcatForm(dcatJson);
}

function submitDcatForm(dcatJson) {
    const editorValue = getEditorValue()
    const merged = mergeIntoOas(editorValue, dcatJson);
    setEditorValue(JSON.stringify(merged, null, 2));
}

function mergeIntoOas(editorValue, dcatJson) {
    const oas = parseOasOrDefault(editorValue);
    oas.info = oas.info || {};
    oas.info['x-dcat'] = dcatJson;
    return oas;
}

function parseOasOrDefault(editorValue) {
    if (!editorValue.trim()) {
        return emptyOasSkeleton();
    }

    try {
        const parsed = JSON.parse(editorValue);
        if (isOasDocument(parsed)) {
            return parsed;
        }
        // Editor contains valid JSON but not OAS
        return emptyOasSkeleton();
    } catch {
        return emptyOasSkeleton();
    }
}

function isOasDocument(json) {
    return json && typeof json === 'object' && typeof json.openapi === 'string';
}

function emptyOasSkeleton() {
    return {
        openapi: '3.0.3',
        info: { title: '', version: '1.0.0' }
    };
}

// Get minimal DCAT-AP-SE json template
async function fetchTemplate() {
    const response = await fetch('/dcat-template.json');
    return response.json();
}

async function buildDcatJson(form) {
    const dcatJson = await fetchTemplate();
    const formData = new FormData(form);
    
    for (const [fieldName, rawValue] of formData.entries()) {
        replaceInJson(dcatJson, fieldName, rawValue.trim());
    }

    let language = formData.get('catalog.language')
    pruneLanguageKeys(dcatJson, language);

    return dcatJson;
}

function replaceInJson(dcatJson, fieldName, value) {
    for (const key of Object.keys(dcatJson)) {
        const current = dcatJson[key];

        if (current === fieldName) {
            dcatJson[key] = value;
        } else if (typeof current === 'object' && current !== null) {
            replaceInJson(current, fieldName, value);
        }
    }
}

// Remove unsed language nodes, dcat-form only supports 1 choosen language (en, sv)
function pruneLanguageKeys(node, languageToKeep) {
    const suffixToRemove = languageToKeep === 'sv' ? '-en' : '-sv';
    for (const key of Object.keys(node)) {
        if (key.endsWith(suffixToRemove)) {
            delete node[key];
        } else if (typeof node[key] === 'object' && node[key] !== null) {
            pruneLanguageKeys(node[key], languageToKeep);
        }
    }
}
