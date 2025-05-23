async function fetchBooks() {
try {
  const response = await fetch('/api/books/', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  const list = document.getElementById('book-list');

  if (response.ok) {
    const books = await response.json();
    list.innerHTML = '';

    books.forEach(book => {
      const div = document.createElement('div');
      div.className = 'book';
      div.textContent = book.title;
      div.onclick = () => {
        window.location.href = `/book.html?id=${book.id}`;
      };
      list.appendChild(div);
    });
  } else {
    list.innerHTML = '<p>Не удалось загрузить книги</p>';
  }

} catch (error) {
  console.error('Ошибка при загрузке книг:', error);
}
}

// Выход пользователя
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
            const navLeft = document.querySelector('.nav-left');
            if (profile.role === 'ADMIN') {
                // Добавление ссылки "Пользователи"
                if (navLeft && !navLeft.querySelector('a[href="/users.html"]')) {
                    const usersLink = document.createElement('a');
                    usersLink.href = '/users.html';
                    usersLink.textContent = 'Пользователи';
                    navLeft.appendChild(usersLink);
                }
            }
            // Добавление кнопки "Добавить книгу" для ADMIN и MODERATOR
            if (profile.role === 'ADMIN' || profile.role === 'MODERATOR') {
                const addButtonContainer = document.getElementById('add-book-container');
                if (addButtonContainer && !addButtonContainer.querySelector('button')) {
                    const addButton = document.createElement('button');
                    addButton.textContent = 'Добавить книгу';
                    addButton.className = 'add-book-btn';
                    addButton.onclick = () => {
                        window.location.href = '/add_book.html';
                    };
                    addButtonContainer.appendChild(addButton);
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

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
    fetchBooks();
    renderAuthLinks();
});
