# API онлайн-магазина книг
Реализован на языке Java с применением фреймворка Spring
## Подготовка к запуску
Выполните клонирование репозитория
```
git clone https://github.com/ArtemAweirro/library_API.git
```
Установите Java и JDK. В VS Studio воспользуйтесь расширением `Language Support for Java(TM) by Red Hat`

По умолчанию проект запускается на localhost:8081. Данную настройку можно изменить в resources/application.yml<br/>
Там же рекомендую изменить настройки для создания и подключения к СУБД PostgreSQL!
## Запуск проекта
Перейдите в src/main/java/ru/artemaweirro/rest_api/ и запустите LibraryApp.java<br/>
При успешном запуске проекта в консоли будет написано `Started LibraryApp in [...] seconds`.<br/>

Теперь backend можно протестировать в Postman. Frontend также реализован, просто перейдите в браузере на localhost:8081