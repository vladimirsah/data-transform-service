# data-transform-service

Подсистема валидации и трансформации сообщений XML/JSON (Spring Boot, RabbitMQ). Stateless-сервис: принимает payload, определяет формат, валидирует по стратегии, выполняет XML↔JSON преобразование.

## Требования

- JDK 17+
- Maven 3.8+
- RabbitMQ 3.x (порт `5672`) — если используется очередь `transform.queue`

## Сборка и тесты

```bash
mvn test
```

Оценочный тест матмодели пишет отчёты в каталог `results/` (CSV и Markdown).

## Запуск приложения

```bash
mvn spring-boot:run
```

- HTTP-порт: **8085** (`server.port` в `application.yml`)
- Health: `GET http://localhost:8085/actuator/health`

Параметры RabbitMQ по умолчанию: `localhost:5672`, пользователь `guest` / `guest`, очередь `transform.queue` (`app.rabbit.queue`).

## REST API

`POST /transform` — тело `TransformRequestDto` (JSON):

- `payload` — XML или JSON (определение по первому значимому символу: `<`, `{`, `[`)
- `xsdPath` — путь к XSD в classpath для XML-стратегий (например, `xsd/sample-order.xsd`)
- `validationStrategy` — `AUTO`, `V1_XML_XSD_BASIC`, `V2_XML_XSD_STRICT`, `V3_JSON_SCHEMA_BASIC`, `V4_JSON_SCHEMA_STRICT`

### Примеры curl

Валидация и конвертация XML → JSON (AUTO для XML выберет V1):

```bash
curl -s -X POST http://localhost:8085/transform \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "<order><id>1</id><status>NEW</status><amount>10.5</amount></order>",
    "xsdPath": "xsd/sample-order.xsd",
    "validationStrategy": "V2_XML_XSD_STRICT"
  }'
```

JSON → XML (строгая JSON-стратегия):

```bash
curl -s -X POST http://localhost:8085/transform \
  -H "Content-Type: application/json" \
  -d '{
    "payload": "{\"id\":\"j1\",\"status\":\"NEW\",\"amount\":100,\"createdDate\":\"2026-04-13\"}",
    "validationStrategy": "V4_JSON_SCHEMA_STRICT"
  }'
```

## Структура пакетов

- `api` — REST-контроллер
- `core/router` — определение формата сообщения
- `core/validator` — XSD, строгая XML-семантика, JSON basic/strict
- `core/transformer` — XML↔JSON
- `facade` — оркестрация pipeline и замер времени
- `listener` — потребитель RabbitMQ
- `resources/xsd` — XSD-схемы
