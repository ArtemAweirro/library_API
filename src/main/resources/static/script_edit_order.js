document.addEventListener('DOMContentLoaded', () => {
    renderAuthLinks()

    const orderId = getOrderIdFromUrl();
    if (!orderId) {
        document.getElementById('order-info').textContent = 'ID заказа не указан в URL.';
        return;
    }

    fetchOrder(orderId);
    fetchBooks(); // загрузим список книг

    document.getElementById('save-btn').addEventListener('click', () => {
        saveOrder(orderId);
    });
});

let selectedBookIds = [];

function getOrderIdFromUrl() {
    const params = new URLSearchParams(window.location.search);
    return params.get('id');
}

async function fetchOrder(id) {
    const response = await fetch(`/api/orders/${id}`, {
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    });

    const container = document.getElementById('order-info');
    if (response.ok) {
        const order = await response.json();

        selectedBookIds = order.books.map(book => book.id); // для чекбоксов

        const booksHtml = order.books.map(book =>
            `<li class="book-item">${book.title} — ${book.price}₽</li>`
        ).join('');

        container.innerHTML = `
            <p><strong>Заказ #${order.id}</strong></p>
            <p><strong>Дата:</strong> ${order.createdAt}</p>
            <p><strong>Сумма:</strong> ${order.totalPrice} ₽</p>
            <p><strong>Книги:</strong></p>
            <ul>${booksHtml}</ul>
        `;

        // после загрузки заказа обновим список книг
        renderBookCheckboxes();
    } else if (response.status === 403 || response.status === 401) {
        container.textContent = "Нет доступа. Пожалуйста, войдите в систему.";
    } else {
        container.textContent = "Ошибка при загрузке заказа.";
    }
}

let allBooks = [];

async function fetchBooks() {
    const response = await fetch('/api/books/', {
        headers: {
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        }
    });

    if (response.ok) {
        allBooks = await response.json();
        renderBookCheckboxes();
    } else {
        document.getElementById('book-form').textContent = 'Ошибка при загрузке списка книг.';
    }
}

function renderBookCheckboxes() {
    const form = document.getElementById('book-form');
    form.innerHTML = '';

    allBooks.forEach(book => {
        const label = document.createElement('label');
        const checkbox = document.createElement('input');
        checkbox.type = 'checkbox';
        checkbox.value = book.id;
        checkbox.checked = selectedBookIds.includes(book.id);

        checkbox.addEventListener('change', () => {
            if (checkbox.checked) {
                selectedBookIds.push(book.id);
            } else {
                selectedBookIds = selectedBookIds.filter(id => id !== book.id);
            }
        });

        label.appendChild(checkbox);
        label.append(` ${book.title} — ${book.price}₽`);
        form.appendChild(label);
        form.appendChild(document.createElement('br'));
    });
}

async function saveOrder(id) {
    const response = await fetch(`/api/orders/${id}`, {
        method: 'PUT',
        headers: {
            'Content-Type': 'application/json',
            'Authorization': 'Bearer ' + localStorage.getItem('token')
        },
        body: JSON.stringify({ bookIds: selectedBookIds })
    });

    if (response.ok) {
        alert('Заказ обновлён!');
        window.location.href = '/orders.html';
    } else {
        alert('Не удалось сохранить изменения (заказ должен содержать хотя бы 1 книгу).');
    }
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

// Выход пользователя
function logout() {
localStorage.removeItem('token');
window.location.href = '/index.html';
}
