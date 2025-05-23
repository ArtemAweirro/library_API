const params = new URLSearchParams(window.location.search);
const userId = params.get('id');

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

async function fetchUser() {
  const response = await fetch(`/api/users/${userId}/`, {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  if (!response.ok) {
    alert('Ошибка при загрузке пользователя');
    return;
  }

  const user = await response.json();

  document.getElementById('username').value = user.username;
  document.getElementById('role').value = user.role;
}

async function updateUser(e) {
  e.preventDefault();

  const data = {
    username: document.getElementById('username').value,
    role: document.getElementById('role').value
  };

  const response = await fetch(`/api/users/${userId}/`, {
    method: 'PATCH',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify(data)
  });

  if (response.ok) {
    alert('Пользователь обновлён.');
    window.location.href = `/user.html?id=${userId}`;
  } else {
    const err = await response.json();
    alert(err.message || 'Ошибка при сохранении.');
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderAuthLinks();
  fetchUser();

  document.getElementById('edit-user-form')
          .addEventListener('submit', updateUser);
});
