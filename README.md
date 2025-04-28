# 🚀 Запуск Telegram-бота с GigaChat и TestIT 🧠⚡
## 1. 🔍 Выгрузите код чат бота по ссылке:
- https://github.com/stillwork-bit/telegram-bot-gigachat-gpt
## 2. 🤖 Создание бота в телеграм:
- 2.1  🧙‍♂️ Создайте бота через BotFather
    - Откройте Telegram
    - Найдите @BotFather
    - Следуйте инструкциям для создания нового бота
- 2.2 🔑 Вставьте токен бота в переменную “BOT_TOKEN“ в .\telegram-bot-gigachat-gpt\src\main\java\org\tan\Constants.java
## 3. 🧠 Для получения доступа к тестовой модели giga chat вам потребуется:
- 3.1 📝 Зарегистрироваться в портале https://developers.sber.ru/studio/registration
- 3.2 🔐 Выбрать "Мой GigaChat API"
- 3.3 🛠 В разделе “Настройка API” создайте свой “Authorization key” в .\telegram-bot-gigachat-gpt\src\main\java\org\tan\Constants.java
- 3.4 🔑 Вставьте сгенерированный ключ в переменную “GIGA_CHAT_AUTH_KEY” в .\telegram-bot-gigachat-gpt\src\main\java\org\tan\Constants.java
- Полезное дополнение: GigaChat API https://developers.sber.ru/docs/ru/gigachat/api/reference/rest/gigachat-api
## 4. 🧪 Подготовка данных TestIT
- 4.1 🔑 Сгенерируйте токен в TestIT - https://docs.testit.software/user-guide/user-settings.html#%D1%81%D0%BE%D0%B7%D0%B4%D0%B0%D0%BD%D0%B8%D0%B5-%D0%BF%D1%80%D0%B8%D0%B2%D0%B0%D1%82%D0%BD%D0%BE%D0%B3%D0%BE-api-%D1%82%D0%BE%D0%BA%D0%B5%D0%BD%D0%B0
- 4.2 ⚙️ Вставьте токен TestIT в “PRIVATE_TOKEN”, ProjectId и sectinId вставьте в соответствующие переменные PROJECT_ID и SECTION_ID  в .\telegram-bot-gigachat-gpt\src\main\java\org\tan\Constants.java
- 4.3 📊 Полезное дополнение: swagger testit https://[ВАШЕ ПРОСТРАНСТВО TEST IT].testit.software/swagger/index.html
## 5. 🎉 Соберите проект и запустите его.
