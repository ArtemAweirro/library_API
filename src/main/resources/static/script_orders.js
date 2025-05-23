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


async function fetchOrders() {
  const user = await getCurrentUser();
  if (!user) {
    document.getElementById('orders').textContent = "Войдите, чтобы просмотреть собственные заказы";
    return;
  }

  const isPrivileged = user.role === 'ADMIN' || user.role === 'MODERATOR';

  const response = await fetch('/api/orders/', {
      headers: {
          'Authorization': 'Bearer ' + localStorage.getItem('token')
      }
  });

  const container = document.getElementById('orders');

  if (response.ok) {
      const orders = await response.json();

      container.innerHTML = ''; // Очищаем контейнер перед отрисовкой

      if (orders.length === 0) {
          container.innerHTML = '<p>У вас пока нет заказов.</p>';
          return;
      }

      orders.forEach(order => {
          const div = document.createElement('div');
          div.className = 'order';
          div.style.border = '1px solid #ccc';
          div.style.padding = '10px';
          div.style.marginBottom = '10px';
          div.style.borderRadius = '6px';
          div.style.backgroundColor = '#f9f9f9';

          const booksHtml = order.books.map(book =>
              `<li><a href="/book.html?id=${book.id}">${book.title}</a> — ${book.price}₽</li>`
          ).join('');

          const userInfo = isPrivileged && order.user
            ? `<p><strong>Пользователь:</strong> ${order.user}</p>`
            : '';

          div.innerHTML = `
              <h3>Заказ #${order.id}</h3>
              <p><strong>Дата:</strong> ${order.createdAt}</p>
              ${userInfo}
              <p><strong>Сумма:</strong> ${order.totalPrice} ₽</p>
              <p><strong>Книги:</strong></p>
              <ul>${booksHtml}</ul>
              <div class="actions">
                <button class="edit-btn" data-id="${order.id}">Редактировать</button>
                <button class="delete-btn" data-id="${order.id}">Удалить</button>
              </div>
          `;
          container.appendChild(div);
      });
        // Назначаем обработчики после добавления элементов
        document.querySelectorAll('.edit-btn').forEach(button => {
          button.addEventListener('click', async (e) => {
            const orderId = e.target.dataset.id;
            window.location.href = `/edit_order.html?id=${orderId}/`;
          });
        });

        document.querySelectorAll('.delete-btn').forEach(button => {
          button.addEventListener('click', async (e) => {
            const orderId = e.target.dataset.id;
            if (confirm(`Удалить заказ #${orderId}?`)) {
              const deleteResponse = await fetch(`/api/orders/${orderId}/`, {
                method: 'DELETE',
                headers: {
                  'Authorization': 'Bearer ' + localStorage.getItem('token')
                }
              });

              if (deleteResponse.ok) {
                alert('Заказ удалён.');
                fetchOrders(); // Обновить список
              } else {
                alert('Ошибка при удалении заказа.');
              }
            }
          });
        });
  } else if (response.status === 401) {
      container.textContent = "Пройдите авторизацию для просмотра собственных заказов"
  } else {
      container.textContent = "Ошибка при загрузке заказов";
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

// Инициализация
document.addEventListener('DOMContentLoaded', () => {
fetchOrders();
renderAuthLinks();
});