# Real Health

Mod de **Fabric** (solo cliente) para Minecraft **26.1.2** que muestra encima
del nombre de cada jugador y cada mob su vida real, tal cual: `❤ 10` si tiene
10 corazones, `❤ 7.5` si tiene 15 puntos de vida, etc. Con color según el
porcentaje de vida (verde / amarillo / rojo).

## ¿Por qué funciona sin ser admin?

Cuando juegas en un servidor, este ya le envía a tu cliente la vida exacta de
cualquier jugador o mob que puedas ver (es el mismo dato que usa el juego
internamente para la animación de "daño" en rojo). Este mod simplemente **lee
ese dato en tu propio cliente y lo dibuja en pantalla** — no manda nada nuevo
al servidor, no necesita comandos, permisos ni que el servidor tenga nada
instalado. Por eso funciona en cualquier server (vanilla, Spigot, Paper...)
solo con instalarlo en tu carpeta `mods`, exactamente como pediste.

**Aviso:** aunque técnicamente es indetectable para el servidor (no cambia tu
comportamiento ni manda paquetes distintos), revisa las reglas del servidor
donde juegues por si prohíben explícitamente mods de este tipo.

## ⚠️ Importante sobre la versión

26.1.2 se publicó el 9 de abril de 2026, y es una de las primeras versiones
"desofuscadas" de Minecraft (Mojang publicó los nombres reales de las clases
en vez de nombres ofuscados, y Fabric dejó de usar Yarn). Es una versión
**muy reciente**, así que hay un pequeño riesgo de que algún import tenga el
paquete ligeramente distinto al que escribí aquí (por ejemplo, si Mojang
movió alguna clase de paquete en un hotfix posterior). Si al compilar te sale
un error tipo `cannot find symbol` o `package no existe`, **pégame el error
tal cual** y te doy la línea corregida en el momento — normalmente es un
cambio de una sola palabra.

## Cómo subirlo a GitHub y generar el .jar automáticamente

1. Crea un repositorio nuevo en GitHub (público o privado, da igual).
2. Sube **todos** estos archivos y carpetas tal cual están (incluyendo la
   carpeta oculta `.github`), manteniendo la misma estructura de carpetas.
   - Más fácil: en la página del repo, "Add file" → "Upload files", arrastra
     todo, y confirma el commit. GitHub respeta las carpetas si arrastras la
     estructura completa (o usa `git push` si prefieres la terminal).
3. En cuanto hagas el commit, la pestaña **Actions** del repo compilará el
   mod automáticamente (tarda 2-4 minutos).
4. Cuando termine (icono verde ✅), entra en esa ejecución y baja hasta
   "Artifacts": ahí está `realhealth-jar`, descárgalo y descomprímelo — dentro
   tienes el `.jar` listo para usar.

Si algún día quieres compilarlo tú en tu PC en vez de con GitHub Actions,
necesitas Java 25 y Gradle 9.5+ instalados, y basta con ejecutar
`gradle build` dentro de la carpeta del proyecto (no hace falta más
configuración: Loom descarga Minecraft y Fabric API automáticamente).

## Cómo instalarlo en tu Minecraft

1. Instala **Fabric Loader** para la 26.1.2 desde fabricmc.net (el instalador
   oficial).
2. Descarga **Fabric API** para 26.1.2 (Modrinth o CurseForge) y ponlo en tu
   carpeta `mods` (`%appdata%\.minecraft\mods` en Windows).
3. Pon ahí también el `.jar` que generaste con GitHub Actions.
4. Abre el launcher, selecciona el perfil "fabric-loader-26.1.2" y juega.

## Estructura del proyecto

```
realhealth/
├── build.gradle              # configuración de compilación (Loom)
├── gradle.properties         # versiones de Minecraft / Fabric usadas
├── settings.gradle
├── LICENSE
├── .github/workflows/build.yml   # compila el .jar automáticamente
└── src/
    ├── main/java/com/realhealth/RealHealthMod.java      # entrypoint común (casi vacío)
    ├── main/resources/fabric.mod.json                    # manifiesto del mod
    └── client/java/com/realhealth/client/
        ├── RealHealthClient.java   # entrypoint de cliente
        └── HealthRenderer.java     # aquí está toda la lógica de dibujado
```

Todo el código relevante está en `HealthRenderer.java`. No usa mixins ni toca
clases internas del juego: solo usa la API pública de Fabric
(`LevelRenderEvents.AFTER_ENTITIES`) para dibujar texto en el mundo, así que
es sencillo de leer y de modificar si quieres, por ejemplo, cambiar la
distancia máxima, los colores, o mostrar también la vida máxima
(`❤ 10/10`).
