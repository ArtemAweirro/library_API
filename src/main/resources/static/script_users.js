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

        // Проверка роли администратора
        fetch('/api/users/me/', {
            headers: {
                'Authorization': 'Bearer ' + token
            }
        })
        .then(res => {
            if (!res.ok) throw new Error('Ошибка запроса профиля');
            return res.json();
        })
        .then(profile => {
            if (profile.role === 'ADMIN') {
                const navLeft = document.querySelector('.nav-left');
                if (navLeft && !navLeft.querySelector('a[href="/users.html"]')) {
                    const usersLink = document.createElement('a');
                    usersLink.href = '/users.html';
                    usersLink.textContent = 'Пользователи';
                    navLeft.appendChild(usersLink);
                }
            }
        })
        .catch(err => {
            console.error('Ошибка при загрузке профиля:', err);
        });

    } else {
        const loginLink = document.createElement('a');
        loginLink.href = '/login.html';
        loginLink.textContent = 'Войти';
        authLinks.appendChild(loginLink);
    }
}

// Выход пользователя
function logout() {
localStorage.removeItem('token');
window.location.href = '/index.html';
}


async function fetchUsers() {
  const response = await fetch('/api/users/', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  const list = document.getElementById('user-list');

  if (response.ok) {
    const users = await response.json();
    list.innerHTML = '';

    users.forEach(user => {
      const div = document.createElement('div');
      div.className = 'user';
      div.textContent = `${user.username} (${user.role})`;
      div.onclick = () => {
        window.location.href = `/user.html?id=${user.id}`;
      };
      list.appendChild(div);
    });

  } else if (response.status === 403) {
    list.textContent = 'Доступ запрещён. Только администраторы могут просматривать пользователей.';
  } else if (response.status === 401) {
    list.textContent = 'Пройдите авторизацию для дальнейших действий.';
  } else {
    list.textContent = 'Ошибка при загрузке пользователей.';
  }
}

document.addEventListener('DOMContentLoaded', () => {
    renderAuthLinks();
    fetchUsers();
});