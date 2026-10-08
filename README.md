# La vida no es un banano 🍌

> **Aplicación Android nativa para acompañamiento reflexivo y reducción del uso compulsivo del teléfono celular.**

* **Nombre de la aplicación**: La vida no es un banano
* **ApplicationId / Paquete base**: `com.lavidanoesunbanano`
* **Arquitectura**: Clean Architecture (capas `ui`, `domain`, `data`, `core`, `service`) + MVVM con StateFlow unificado.
* **Stack**: Kotlin 2.1.10, Jetpack Compose con Material 3, Hilt 2.55, Room 2.6.1 (con `exportSchema = true`), DataStore Preferences, Retrofit 2.11.0, kotlinx.serialization 1.8.0, Coroutines & Flow.
* **SDK**: `minSdk 26`, `targetSdk 35`, `compileSdk 35`.

---

## 1. Requisitos del Sistema

* **Android Studio**: Android Studio Ladybug (2024.2.1+) o superior con soporte para AGP 8.8.x.
* **JDK**: Java 17 o Java 21 (Eclipse Temurin, OpenJDK o JetBrains Runtime 17/21). *Nota: AGP 8.8 no soporta Java 25.*
* **Dispositivo físico o Emulador**: Android 8.0 (API 26) hasta Android 15 (API 35).

---

## 2. Cómo Compilar y Ejecutar

### Compilar desde la terminal
```bash
# Compilar versión debug
./gradlew assembleDebug

# Ejecutar pruebas unitarias locales
./gradlew testDebugUnitTest
```

### Abrir en Android Studio
1. Abre Android Studio.
2. Selecciona **Open** y escoge el directorio del proyecto:
   `C:\Users\Usuario\.gemini\antigravity\scratch\LaVidaNoEsUnBanano`
3. Espera la sincronización de Gradle con el catálogo `gradle/libs.versions.toml`.
4. Conecta un dispositivo físico o inicia un emulador y presiona **Run 'app'**.

---

## 3. Concesión de Permisos en un Dispositivo Real

La aplicación incluye un flujo guiado de bienvenida (**Onboarding**) que detecta el estado en vivo y se refresca automáticamente en `onResume`:

1. **Acceso a datos de uso (`PACKAGE_USAGE_STATS`)** *(Obligatorio)*:
   * Pulsa **Configurar** en la tarjeta.
   * Android te llevará a la lista de apps con acceso de uso. Busca **La vida no es un banano** y activa la casilla.
2. **Mostrar sobre otras apps (`SYSTEM_ALERT_WINDOW`)** *(Obligatorio)*:
   * Pulsa **Configurar**.
   * Activa el interruptor para permitir que la app muestre la pantalla de respiración cuando te distraigas.
3. **Notificaciones (`POST_NOTIFICATIONS`)** *(Obligatorio en Android 13+)*:
   * Pulsa **Configurar** y acepta el diálogo del sistema.
4. **Sin optimización de batería** *(Recomendado)*:
   * Evita que los fabricantes de dispositivos (Xiaomi, Samsung, etc.) cierren agresivamente el servicio en segundo plano.

> **Importante**: El monitoreo no se puede activar sin los permisos obligatorios. Si se revoca un permiso con el servicio en ejecución, la app detiene el servicio en el siguiente tick y muestra una notificación explicando el motivo.

---

## 4. Cómo Probar una Intervención Rápido

### Método A: Botón de Simulación Inmediata (Modo Debug)
1. Ve a **Ajustes** (ícono de engranaje en la esquina superior del panel).
2. Toca **Simular intervención**.
3. Se lanzará de inmediato la pantalla completa de respiración de 30 segundos, simulando una sesión de 12 minutos con la app de ejemplo, sin requerir esperar tiempo de uso real.

### Método B: Umbral de 1 Minuto (Modo Debug)
1. Entra a **Ajustes** $\rightarrow$ **Horario y umbral** (o durante la configuración inicial).
2. Selecciona el chip **1m** (disponible exclusivamente en `BuildConfig.DEBUG`).
3. Marca una app instalada (por ejemplo, Chrome o YouTube) como **app gatillo**.
4. Pulsa **Activar monitoreo**.
5. Abre la app gatillo y úsala por más de 60 segundos continuos: aparecerá la intervención en menos de 5 segundos.

---

## 5. Conexión con un Backend de IA Local en tu PC

Para probar la integración con un servidor Python/FastAPI local:

1. Levanta tu backend en la terminal de tu PC:
   ```bash
   uvicorn main:app --host 0.0.0.0 --port 8000
   ```
2. Obtén la dirección IP local de tu computador en tu red Wi-Fi (ejemplo: `192.168.1.15`).
   * *Si pruebas en el emulador de Android oficial, puedes usar `http://10.0.2.2:8000/`*.
3. En la raíz del proyecto Android, crea o edita el archivo `local.properties`:
   ```properties
   agent.baseUrl=http://192.168.1.15:8000/
   agent.debugHost=192.168.1.15
   ```
4. Recompila la app. La tarea Gradle generará automáticamente el archivo `network_security_config.xml` permitiendo tráfico cleartext hacia tu IP y hacia `10.0.2.2`.
5. En la app, abre **Ajustes**, activa el interruptor **Respuestas con IA** y acepta el aviso de privacidad.

### Contrato del API:
* **Endpoint**: `POST /v1/intervention`
* **Request Payload**:
  ```json
  {
    "user_id": "c71a396e-57b1-4f81-8153-bc97022fbef1",
    "app_category": "red social",
    "session_minutes": 14,
    "local_hour": 23,
    "reason": "TRIGGER_APP_IN_RISK_WINDOW",
    "emotion": "ansiedad",
    "user_text": "me siento sobrecargado",
    "relapsed_today": false
  }
  ```
* **Response Payload**:
  ```json
  {
    "message": "La ansiedad busca una salida rápida. Respira hondo y regálate calma antes de decidir.",
    "strategy": "breathing",
    "is_crisis": false
  }
  ```

---

## 6. Pruebas Manuales Pendientes (Dependientes de Dispositivo Físico)

Debido a que ciertos componentes interactúan con subsistemas reales del hardware y del sistema operativo Android, deben verificarse manualmente en dispositivos físicos:

1. **Apertura de Activity sobre apps en Android 12 y Android 15**:
   * Verificar en Android 12 y Android 15 que la pantalla de intervención se superponga directamente mediante `SYSTEM_ALERT_WINDOW` o se dispare el banner prioritario `fullScreenIntent` cuando la app gatillo está en primer plano.
2. **Reinicio del dispositivo (`BootReceiver`)**:
   * Reiniciar un teléfono físico con el monitoreo previamente activo y comprobar que la notificación persistente `La vida no es un banano está acompañándote` reaparezca al encender el terminal.
3. **Revocación dinámica de permisos en caliente**:
   * Con el servicio en ejecución, ir a Ajustes del sistema y revocar el permiso de "Acceso de uso". Comprobar que en menos de 2 segundos el servicio se detenga y emita la notificación de aviso.
4. **Pantalla apagada (`ACTION_SCREEN_OFF`) y reanudación**:
   * Bloquear la pantalla con una app gatillo abierta. Esperar 35 segundos y desbloquear. Verificar que la sesión previa se haya cerrado y se inicie una nueva.
5. **Comportamiento neutral durante los 30 s de respiración**:
   * Verificar que la aparición de la propia pantalla de intervención no cierre la sesión de la app gatillo mientras el usuario respira.

---

## 7. Limitaciones Conocidas

* **Fabricantes con gestión agresiva de memoria (OEMs)**: En dispositivos con capas de personalización estrictas (MIUI/HyperOS, OneUI), el usuario debe fijar la app en memoria y desactivar el ahorro de batería del fabricante para garantizar que el servicio corra indefinidamente.
* **Uso exclusivo de UsageStatsManager**: Por diseño no se utiliza `AccessibilityService`. Los eventos de cambio de app se leen cada 2 segundos con solapamiento de 1 segundo mediante `UsageStatsManager.queryEvents`.
* **Modo Release**: En compilaciones `release`, la URL del backend (`agent.baseUrl`) debe usar obligatoriamente HTTPS y no se permite ningún tráfico cleartext HTTP.
