const params = new URLSearchParams(window.location.search);
const bookId = params.get('id');

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

async function fetchBook() {
  const [bookResponse, user] = await Promise.all([
    fetch(`/api/books/${bookId}/`, {
      headers: {
        'Authorization': 'Bearer ' + localStorage.getItem('token')
      }
    }),
    getCurrentUser()
  ]);

  const container = document.getElementById('book-details');

  if (!bookResponse.ok) {
    container.textContent = "Книга не найдена";
    return;
  }

  const book = await bookResponse.json();

  container.innerHTML = `
    <h2>${book.title}</h2>
    <p><strong>Автор:</strong> ${book.author}</p>
    <p><strong>Цена:</strong> ${book.price} руб.</p>
    <p><strong>Описание:</strong> ${book.description}</p>
  `;

  const actionsDiv = document.createElement('div');
  actionsDiv.className = 'actions';

  if (user && (user.role === 'ADMIN' || user.role === 'MODERATOR')) {
    const editButton = document.createElement('button');
    editButton.textContent = 'Редактировать книгу';
    editButton.onclick = () => {
      window.location.href = `/edit_book.html?id=${bookId}`;
    };

    const deleteButton = document.createElement('button');
    deleteButton.textContent = 'Удалить книгу';
    deleteButton.onclick = async () => {
      if (confirm('Удалить эту книгу?')) {
        const deleteResponse = await fetch(`/api/books/${bookId}/`, {
          method: 'DELETE',
          headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
          }
        });

        if (deleteResponse.ok) {
          alert('Книга удалена');
          window.location.href = '/index.html';
        } else {
          const error = await deleteResponse.json();
          alert(error.message || 'Ошибка при удалении');
        }
      }
    };

    actionsDiv.appendChild(editButton);
    actionsDiv.appendChild(deleteButton);
  } else if (user && (user.role === 'USER')) {
    const orderButton = document.createElement('button');
    orderButton.textContent = 'Оформить заказ';
    orderButton.onclick = createOrder;
    actionsDiv.appendChild(orderButton);
  }

  container.appendChild(actionsDiv);
}


async function createOrder() {
    const response = await fetch('/api/orders/', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({ bookIds: [Number(bookId)] })
    });

    if (response.ok) {
        alert("Заказ успешно создан!");
        window.location.href = '/orders.html';
    } else {
        const error = await response.json();
        alert(error.message || "Ошибка при создании заказа");
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


document.addEventListener('DOMContentLoaded', () => {
    renderAuthLinks();
    fetchBook();
});