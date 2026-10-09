# LaVidaNoEsUnBanano

**LaVidaNoEsUnBanano** es un MVP de agente nativo para Android diseñado para ayudar a reducir el uso compulsivo del teléfono celular mediante la detección inteligente de hábitos de uso y mecanismos de intervención en tiempo real.

---

## Índice

1. [Stack Tecnológico](#stack-tecnológico)
2. [Arquitectura del Proyecto](#arquitectura-detallada)
3. [Requisitos Previos](#requisitos-previos)
4. [Configuración y Compilación](#configuración-y-compilación)
5. [Permisos del Sistema](#permisos-del-sistema)
6. [Cómo Probar la Aplicación (Modo Debug)](#cómo-probar-la-aplicación-modo-debug)
7. [Conexión con el Backend Local](#conexión-con-el-backend-local)
8. [Pruebas Unitarias](#pruebas-unitarias)
9. [Pruebas Manuales Pendientes y Limitaciones Conocidas](#pruebas-manuales-pendientes-y-limitaciones-conocidas)

---

## Stack Tecnológico

* **Lenguaje:** Kotlin
* **Interfaz de Usuario:** Jetpack Compose, Material 3, Navigation Compose
* **Arquitectura:** MVVM (Model-View-ViewModel) con separación estricta en capas (`ui`, `domain`, `data`)
* **Inyección de Dependencias:** Hilt (incluyendo `@AndroidEntryPoint` en Servicios y BroadcastReceivers)
* **Persistencia:** Room (con esquemas exportados) y DataStore Preferences
* **Concurrencia:** Coroutines y Flows de Kotlin
* **Red:** Retrofit + kotlinx.serialization (exclusivo para `RemoteAgentClient`)
* **Versiones Mínimas:** `minSdk 26`, `targetSdk 35`, `compileSdk 35`
* **Gestión de Dependencias:** Gradle Kotlin DSL + Version Catalog (`libs.versions.toml`)

---

## Arquitectura Detallada

* **Capa de Dominio (`domain/`):** Código Kotlin puro sin dependencias del framework Android (`android.*`). Contiene el motor de reglas puras (`RuleEngine`), modelos de datos, interfaces de repositorios, el `CrisisFilter` y la abstracción del tiempo (`Clock`).
* **Orquestador de Servicio (`UsageMonitorService`):** Servicio en primer plano desacoplado y testeable mediante componentes individuales:
  * `UsageEventSource`: Consulta de eventos de uso.
  * `SessionTracker`: Gestión lógica de los estados de sesión.
  * `InterventionLauncher`: Emisión de la actividad de interrupción.
* **Fuente Única de Verdad:** Configuración unificada mediante un repositorio compartido consumido de forma idéntica por el servicio en segundo plano y la interfaz de usuario.

---

## Requisitos Previos

* Android Studio (Koala o versión más reciente recomendada).
* JDK 17 o superior configurado en el entorno de desarrollo.
* Dispositivo físico o emulador con soporte para Android 8.0 (`minSdk 26`) en adelante. *Nota: Las pruebas de comportamiento de notificaciones y permisos requieren obligatoriamente un dispositivo físico con Android 12 o superior.*

---

## Configuración y Compilación

1. Clona el repositorio en tu máquina local.
2. Crea un archivo denominado `local.properties` en la raíz del proyecto (este archivo se encuentra ignorado por `.gitignore`) si deseas configurar parámetros locales o la URL del agente de IA:
   ```properties
   agent.baseUrl=http://10.0.2.2:8000/
   agent.debugHost=10.0.2.2
   ```
3. Sincroniza el proyecto con los archivos de Gradle.
4. Compila la aplicación en modo depuración ejecutando el siguiente comando en la terminal:
   ```bash
   ./gradlew assembleDebug
   ```

---

## Permisos del Sistema

La aplicación requiere la concesión explícita de determinados permisos críticos para operar correctamente. El onboarding integrado validará los siguientes accesos:

* **Acceso a estadísticas de uso (`PACKAGE_USAGE_STATS`):** Permite detectar qué aplicación se encuentra en primer plano. Se solicita mediante `ACTION_USAGE_ACCESS_SETTINGS`.
* **Mostrar sobre otras aplicaciones (`SYSTEM_ALERT_WINDOW`):** Esencial para lanzar la pantalla de intervención en menos de 5 segundos.
* **Notificaciones (`POST_NOTIFICATIONS`):** Requerido a partir de Android 13 (API 33) para mantener activa la notificación persistente del servicio en primer plano.
* **Eximición de optimización de batería (Recomendado):** Evita que el sistema operativo suspenda el servicio de monitoreo en segundo plano.

---

## Cómo Probar la Aplicación (Modo Debug)

Para facilitar la verificación del flujo completo sin necesidad de esperar largos periodos de uso real:

1. Compila la aplicación utilizando un Build Variant de tipo **debug**.
2. Configura una app gatillo durante el onboarding o ajustes.
3. El umbral de sesión se reducirá automáticamente a **1 minuto** bajo condiciones de compilación de depuración.
4. Utiliza el botón **"Simular intervención"** disponible en la interfaz de pruebas para disparar de inmediato la pantalla de intervención a pantalla completa.

---

## Conexión con el Backend Local

Si deseas probar el cliente remoto de inteligencia artificial (`RemoteAgentClient`) utilizando un servidor local en tu ordenador:

1. Ejecuta tu servidor local (por ejemplo, con FastAPI):
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000
   ```
2. Asegúrate de que tu dispositivo físico se encuentre conectado a la misma red Wi-Fi que tu PC.
3. Actualiza la propiedad `agent.baseUrl` en tu archivo `local.properties` apuntando a la dirección IP local de tu ordenador (por ejemplo: `http://192.168.1.50:8000/`).
4. Habilita el interruptor **"Respuestas con IA"** en la sección de Ajustes de la aplicación.

---

## Pruebas Unitarias

El proyecto cuenta con un conjunto robusto de pruebas unitarias que validan la lógica de negocio aislada del framework. Puedes ejecutarlas mediante el comando de Gradle:

```bash
./gradlew testDebugUnitTest
```

Las suites de pruebas incluyen:
* **`RuleEngineTest`**: Valida cruces de medianoche, umbrales, periodos de gracia, penalizaciones por cooldown, límites por hora y precedencia de reglas.
* **`SessionTrackerTest`**: Comprueba la correcta administración del tiempo de sesión, tolerancia de inactividad de 30 segundos, excepciones de apps neutras y restauración tras reinicios.
* **`CrisisFilterTest`**: Evalúa de forma estricta la detección de palabras clave asociadas a crisis, normalización de textos (mayúsculas y tildes) y prevención de falsos positivos.

---

## Pruebas Manuales Pendientes y Limitaciones Conocidas

Debido a restricciones impuestas por el sistema operativo Android, los siguientes comportamientos deben validarse de forma manual en dispositivos físicos específicos:

1. **Lanzamiento de Activities desde segundo plano en Android 14 y 15:** Comprobar que el uso combinado de `SYSTEM_ALERT_WINDOW` y notificaciones con `fullScreenIntent` ejecute la pantalla de intervención correctamente sin bloqueos de restricciones del sistema.
2. **Reinicio tras un `BOOT_COMPLETED`:** Verificar que el receptor de arranque reactive de forma transparente el servicio de monitoreo siempre que el usuario mantenga los permisos activos.
3. **Revocación dinámica de permisos:** Comprobar que si el usuario revoca el permiso de estadísticas de uso o ventanas flotantes mientras el servicio corre, la aplicación se detenga de manera controlada y emita una alerta notificando el motivo.