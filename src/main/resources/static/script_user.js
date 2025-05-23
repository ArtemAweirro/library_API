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

async function getCurrentUser() {
  const response = await fetch('/api/users/me/', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });
  return response.ok ? await response.json() : null;
}

async function fetchUser() {
  const response = await fetch(`/api/users/${userId}/`, {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  const container = document.getElementById('user-details');

  if (response.ok) {
    const user = await response.json();
    container.innerHTML = `
      <p><strong>ID:</strong> ${user.id}</p>
      <p><strong>Имя пользователя:</strong> ${user.username}</p>
      <p><strong>Роль:</strong> ${user.role}</p>
    `;

    const actions = document.getElementById('actions');
    actions.innerHTML = `
      <button id="edit-user">Редактировать</button>
      <button id="delete-user">Удалить</button>
    `;

    document.getElementById('edit-user').addEventListener('click', () => {
      window.location.href = `/edit_user.html?id=${user.id}`;
    });

    document.getElementById('delete-user').addEventListener('click', async () => {
      if (confirm(`Удалить пользователя ${user.username}?`)) {
        const deleteRes = await fetch(`/api/users/${user.id}/`, {
          method: 'DELETE',
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });

        if (deleteRes.ok) {
          alert('Пользователь удалён.');
          window.location.href = '/users.html';
        } else {
          const err = await deleteRes.json();
          alert(err.message || 'Ошибка при удалении пользователя.');
        }
      }
    });

  } else {
    container.innerHTML = '<p>Ошибка при загрузке данных пользователя.</p>';
  }
}

document.addEventListener('DOMContentLoaded', () => {
  renderAuthLinks();
  fetchUser();
});
