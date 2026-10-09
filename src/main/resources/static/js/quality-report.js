const QUALITY_NO_ISSUES = 'No quality issues found';

const QUALITY_ALERT_NOT_COMPLETED =
  'Kvalitetskontrollen av den genererade RDF:en kunde inte genomföras. ' +
  'RDF:en har genererats och kan användas, men dess kvalitet har inte bedömts.';

const QUALITY_ALERT_NO_ISSUES =
  'Den genererade RDF:en följer DCAT-AP-SE och uppfyller samtliga kontrollerade kvalitetsrekommendationer.';

const QUALITY_ALERT_FEW_ISSUES =
  'Den genererade RDF:en följer DCAT-AP-SE, men alla kvalitetsrekommendationer är inte uppfyllda ' +
  '(%s). Åtgärda bristerna nedan för att göra metadatan lättare att hitta och återanvända.';

const QUALITY_ALERT_MANY_ISSUES =
  'Den genererade RDF:en följer DCAT-AP-SE, men flera kvalitetsrekommendationer är inte uppfyllda ' +
  '(%s). Metadata med många brister blir svår att hitta och återanvända. ' +
  'Gå igenom bristerna per huvudklass nedan.';

function updateQualityReport(report) {
  const container = document.getElementById('quality-report-container');
  const content = document.getElementById('quality-report-content');
  const defaultAlert = document.getElementById('quality-report-default-alert');
  const heading = container.querySelector('h3');

  content.replaceChildren();
  clearReportBadge();

  if (!report) {
    container.classList.add('hidden');
    defaultAlert.classList.remove('hidden');
    return;
  }

  defaultAlert.classList.add('hidden');
  setReportAlert(report);
  setReportBadge();
  heading.classList.toggle('hidden', !report.completed);

  if (report.completed) {
    const groups = groupByMainClass(report.mainClasses);
    for (const [mainClassName, mainClasses] of groups) {
      const group = createMainClassGroup(mainClassName, mainClasses);
      content.append(group);
    }
  }
  container.classList.remove('hidden');
}

function setReportBadge() {
  let parent = document.querySelector('#btn-quality-report');
  let badge = document.createElement('span');
  badge.className = 'badge';
  badge.textContent = '1';

  const hidden = document.createElement('span');
  hidden.className = 'visually-hidden';
  hidden.textContent = ' oläst';

  badge.append(hidden);
  parent.appendChild(badge);
}

function clearReportBadge() {
  let badge = document.querySelector('#btn-quality-report .badge');
  if (badge) {
    badge.remove();
  }
}

// The report is ordered by main class, and a Map keeps that order
function groupByMainClass(mainClasses) {
  const groups = new Map();
  for (const mainClass of mainClasses) {
    if (!groups.has(mainClass.mainClass)) {
      groups.set(mainClass.mainClass, []);
    }
    groups.get(mainClass.mainClass).push(mainClass);
  }
  return groups;
}

function createMainClassGroup(mainClassName, mainClasses) {
  const hasIssues = mainClasses.some((mainClass) => mainClass.issues.length > 0);

  const details = document.createElement('details');
  details.className = 'quality-group';

  const summary = document.createElement('summary');
  const span = document.createElement('span');
  span.textContent = mainClassName;

  summary.append(createStatus(hasIssues), span);
  details.append(summary);

  for (const mainClass of mainClasses) {
    details.append(createMainClassSection(mainClass));
  }
  return details;
}

function createStatus(hasIssues) {
  const icon = document.createElement('i');
  const text = document.createElement('span');
  icon.setAttribute('aria-hidden', 'true');
  text.className = 'visually-hidden';

  if (hasIssues) {
    icon.className = 'fa-solid fa-triangle-exclamation color-warn mr-2';
    text.textContent = 'Har brister: ';
  } else {
    icon.className = 'fa-solid fa-circle-check color-success mr-2';
    text.textContent = 'Inga brister: ';
  }

  const status = document.createElement('span');
  status.append(icon, text);
  return status;
}

function createMainClassSection(mainClass) {
  const section = document.createElement('section');
  const p = createParagraph('URI: ' + mainClass.uri);
  p.classList.add('section-header');
  section.append(p);

  if (mainClass.issues.length === 0) {
    const container = document.createElement('div');
    container.className = 'd-flex align-items-baseline';

    const icon = document.createElement('i');
    icon.setAttribute('aria-hidden', 'true');
    icon.className = 'fa-solid fa-check color-success';

    const paragraph = createParagraph(QUALITY_NO_ISSUES);
    paragraph.className = 'mr-2 mb-0';
    container.append(paragraph, icon);

    section.append(container);
    return section;
  }

  section.append(createIssueTable(mainClass.issues));
  return section;
}

function createParagraph(text) {
  const paragraph = document.createElement('p');
  paragraph.textContent = text;
  return paragraph;
}

function setReportAlert(report) {
  const container = document.getElementById('quality-report-alert');
  container.classList.remove('alert-danger', 'alert-info', 'alert-warn', 'alert-success');
  const alertParagraph = container.querySelector('.alert-text');

  let issueCount = 0;
  for (const mainClass of report.mainClasses) {
    issueCount += mainClass.issues.length;
  }

  let template;
  let alertCss;

  if (!report.completed) {
    alertCss = 'alert-danger';
    template = QUALITY_ALERT_NOT_COMPLETED;
  } else if (issueCount === 0) {
    alertCss = 'alert-success';
    template = QUALITY_ALERT_NO_ISSUES;
  } else if (issueCount < 4) {
    alertCss = 'alert-info';
    template = QUALITY_ALERT_FEW_ISSUES;
  } else {
    alertCss = 'alert-warn';
    template = QUALITY_ALERT_MANY_ISSUES;
  }

  if (template.includes('%s')) {
    const [before, after] = template.split('%s');
    const count = document.createElement('strong');
    count.textContent = 'antal brister: ' + issueCount;
    alertParagraph.replaceChildren(before, count, after);
  } else {
    alertParagraph.textContent = template;
  }

  container.classList.add(alertCss);
}

function createIssueTable(issues) {
  const table = document.createElement('table');
  table.className = 'quality-table striped';

  const header = document.createElement('tr');
  header.append(createCell('th', 'Property', 'col'), createCell('th', 'Issue', 'col'));

  const thead = document.createElement('thead');
  thead.append(header);

  const tbody = document.createElement('tbody');
  for (const issue of issues) {
    const row = document.createElement('tr');
    row.append(createCell('td', issue.property), createCell('td', issue.message));
    tbody.append(row);
  }

  table.append(thead, tbody);
  return table;
}

function createCell(type, text, scope) {
  const cell = document.createElement(type);
  cell.textContent = text;
  if (scope) {
    cell.scope = scope;
  }
  return cell;
}
