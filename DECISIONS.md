# DECISIONS.md - Registro de Decisiones de Arquitectura y Producto

1. Versiones fijadas en version catalog: AGP 8.8.2, Kotlin 2.1.10, KSP 2.1.10-1.0.31, Compose BOM 2025.02.00, Hilt 2.55, Room 2.6.1, Navigation Compose 2.8.8, DataStore 1.1.3, Retrofit 2.11.0, Serialization 1.8.0.
2. Identificador de eventos Room: InterventionEvent utiliza clave primaria Long autoincremental para rendimiento y orden secuencial.
3. Día lógico: Inicia a las 04:00 AM hora local mediante LogicalDateUtils (ZoneId.systemDefault()) para evitar fragmentar sesiones nocturnas 22:00-02:00.
4. Precedencia de RuleEngine: Función pura de dominio evaluada en orden estricto (Pausa Global > Posponer > Gracia > Cooldown > Límite horario > Disparadores).
5. Intervención visual: Animación de respiración con Canvas de Compose de 30s sin bloqueo de botones de salida ni decisión.
6. Manejo del botón Atrás en intervención: BackHandler mapeado a SNOOZE (Posponer 15 min) cerrando hacia el Launcher (CATEGORY_HOME).
7. Reconstrucción de sesión: Al iniciar el servicio se repasan 10 min de eventos de UsageStatsManager y se restaura ActiveSession desde Room.
8. Filtro de crisis: Evaluación pura en memoria previa a llamadas de red; si detecta palabras clave activa tarjeta de auxilio y cancela envío remoto.
9. Configuración de seguridad de red: En debug permite 10.0.2.2 y agent.debugHost; en release exige HTTPS y bloquea cleartext.
10. Fallback de apertura de Activity: UsageMonitorService usa SYSTEM_ALERT_WINDOW con notificación fullScreenIntent de respaldo para compatibilidad Android 10-15.
