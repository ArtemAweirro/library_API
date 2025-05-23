const params = new URLSearchParams(window.location.search);
const bookId = params.get('id');

async function getCurrentUser() {
  const response = await fetch('/api/users/me/', {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  if (response.ok) {
    return await response.json();
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

    fetch('/api/users/me/', {
      headers: {
        'Authorization': 'Bearer ' + token
      }
    })
    .then(res => res.json())
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
    });
  } else {
    const loginLink = document.createElement('a');
    loginLink.href = '/login.html';
    loginLink.textContent = 'Войти';
    authLinks.appendChild(loginLink);
  }
}

async function fetchBookData() {
  const response = await fetch(`/api/books/${bookId}/`, {
    headers: {
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    }
  });

  if (response.ok) {
    const book = await response.json();
    document.getElementById('title').value = book.title;
    document.getElementById('author').value = book.author;
    document.getElementById('price').value = book.price;
    document.getElementById('description').value = book.description;
  } else {
    alert("Ошибка при загрузке книги");
    window.location.href = '/index.html';
  }
}

async function saveChanges(e) {
  e.preventDefault();

  const updatedBook = {
    title: document.getElementById('title').value,
    author: document.getElementById('author').value,
    price: Number(document.getElementById('price').value),
    description: document.getElementById('description').value
  };

  const response = await fetch(`/api/books/${bookId}/`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + localStorage.getItem('token')
    },
    body: JSON.stringify(updatedBook)
  });

  if (response.ok) {
    alert("Книга обновлена");
    window.location.href = `/book.html?id=${bookId}`;
  } else {
    const error = await response.json();
    alert(error.message || "Ошибка при обновлении");
  }
}

document.addEventListener('DOMContentLoaded', async () => {
  renderAuthLinks();
  const user = await getCurrentUser();
  if (!user || (user.role !== 'ADMIN' && user.role !== 'MODERATOR')) {
    alert("Доступ запрещён");
    window.location.href = '/index.html';
    return;
  }

  fetchBookData();
  document.getElementById('edit-form').addEventListener('submit', saveChanges);
});