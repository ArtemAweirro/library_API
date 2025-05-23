// Показываем форму регистрации
document.getElementById("show-register-btn")?.addEventListener("click", () => {
  document.getElementById("register-form").style.display = "block";
});

// Обработка формы входа
document.getElementById("login-form")?.addEventListener("submit", async function (e) {
  e.preventDefault();
  const username = document.getElementById("username").value;
  const password = document.getElementById("password").value;

  const response = await fetch("/api/auth/login/", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  });

  const data = await response.json();
  if (response.ok) {
    localStorage.setItem("token", data.token);
    window.location.href = "/books.html";
  } else {
    alert("Ошибка входа: " + (data.message || "Проверьте логин и пароль"));
  }
});

// Обработка формы регистрации
document.getElementById("register-form")?.addEventListener("submit", async function (e) {
  e.preventDefault();
  const username = document.getElementById("reg-username").value;
  const password = document.getElementById("reg-password").value;

  const response = await fetch("/api/auth/register/", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ username, password })
  });

  const data = await response.json();
  if (response.ok) {
    alert("Регистрация успешна. Можете войти.");
    document.getElementById("register-form").reset();
    document.getElementById("register-form").style.display = "none";
  } else {
    alert("Ошибка регистрации: " + (data.message || "Проверьте данные"));
  }
});