// Проверка авторизации и роли
async function getCurrentUser() {
  const response = await fetch('/api/users/me/', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  if (response.ok) {
    return await response.json(); // { id, username, role }
  } else {
    return null;
  }
}

function logout() {
  localStorage.removeItem('token');
  window.location.href = '/index.html';
}

function renderAuthLinks() {
  const authLinks = document.getElementById('auth-links');
  authLinks.innerHTML = '';

  const token = localStorage.getItem('token');
  if (token) {
    const logoutLink = document.createElement('a');
    logoutLink.href = '#';
    logoutLink.textContent = 'Выйти';
    logoutLink.onclick = logout;
    authLinks.appendChild(logoutLink);
  } else {
    const loginLink = document.createElement('a');
    loginLink.href = '/login.html';
    loginLink.textContent = 'Войти';
    authLinks.appendChild(loginLink);
  }
}

// Обработка отправки формы
async function handleAddBook(event) {
  event.preventDefault();

  const user = await getCurrentUser();
  if (!user || (user.role !== 'ADMIN' && user.role !== 'MODERATOR')) {
    alert('У вас нет прав для добавления книги.');
    return;
  }

  const title = document.getElementById('title').value.trim();
  const author = document.getElementById('author').value.trim();
  const price = parseFloat(document.getElementById('price').value);
  const description = document.getElementById('description').value.trim();

  const response = await fetch('/api/books/', {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify({
      title,
      author,
      price,
      description
    })
  });

  if (response.ok) {
    alert('Книга успешно добавлена.');
    window.location.href = '/index.html';
  } else {
    const error = await response.json();
    alert(error.message || 'Ошибка при добавлении книги.');
  }
}

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
  renderAuthLinks();

  const form = document.getElementById('edit-form');
  form.addEventListener('submit', handleAddBook);
});
