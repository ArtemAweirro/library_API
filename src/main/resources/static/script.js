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

function logout() {
localStorage.removeItem('token');
window.location.href = '/index.html';
}

renderAuthLinks();