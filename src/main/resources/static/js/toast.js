/**
 * Show a toast notification.
 * type must be one of: "success", "error", "info"
 */
function showToast(message, type) {
  const container = document.getElementById('toast-container');
  if (!container) {
    return;
  }
  container.replaceChildren(); //clear existing toasts

  const TOAST_DURATION = 3500;
  const toast = document.createElement('div');
  toast.className = 'toast toast-' + type;

  // Errors should interrupt screen readers immediately, others can wait
  if (type === 'error') {
    toast.setAttribute('role', 'alert');
    let icon = document.createElement('span')
    icon.className = 'alert-icon icon-alert-triangle'
    toast.appendChild(icon);

  } else {
    toast.setAttribute('role', 'status');
    let icon = document.createElement('i')
    icon.className = 'toast-icon fa-solid fa-check'
    toast.appendChild(icon);
  }

  const messageSpan = document.createElement('span');
  messageSpan.className = 'toast-message';
  messageSpan.textContent = message;

  const closeButton = document.createElement('button');
  closeButton.className = 'toast-close';
  closeButton.setAttribute('aria-label', 'Close notification');
  closeButton.textContent = '\u2716'; // ✖

  toast.appendChild(messageSpan);
  toast.appendChild(closeButton);
  container.appendChild(toast);
  
  const timerId = setTimeout(function () {
    dismissToast(toast);
  }, TOAST_DURATION);

  closeButton.addEventListener('click', function () {
    clearTimeout(timerId);
    dismissToast(toast);
  });
}

function dismissToast(toast) {
  toast.classList.add('toast-closing');
  toast.addEventListener('animationend', function () {
    toast.remove();
  });
}