# DamageOn - Mod de Fabric para 1.21

## Qué hace
- Comando `/damageon`: activa/desactiva el modo (es un interruptor, escríbelo otra vez para desactivarlo).
- Con el modo activado, la próxima vez que recibas cualquier daño:
  1. Se muestran los límites del chunk en el que estás con partículas de llama.
  2. Empieza una cuenta atrás de 7 segundos (verás el aviso en pantalla).
  3. Al terminar, **todo el chunk se vacía** (todos los bloques de ese chunk, desde abajo hasta arriba, se convierten en aire).

⚠️ Nota: no es técnicamente posible "eliminar" un chunk del archivo de región mientras el mundo está cargado sin
riesgo de corromper el guardado. Por eso el mod vacía el chunk (lo deja en aire), que es el efecto equivalente
para el juego. Si prefieres otro comportamiento (por ejemplo, rellenarlo de lava, o solo la zona alrededor del
jugador), dímelo y lo ajusto.

## Opción fácil: que GitHub lo compile por ti (sin instalar nada)
Este proyecto ya incluye `.github/workflows/build.yml`, que hace que GitHub compile el mod automáticamente
en sus propios servidores (gratis) en cuanto subas los archivos:

1. Crea cuenta en https://github.com
2. "New" → crea un repositorio (público o privado, el nombre que quieras).
3. En esa página, usa el enlace "uploading an existing file" y arrastra TODO el contenido descomprimido
   de esta carpeta (incluida `.github`).
4. Ve a la pestaña "Actions" del repo, espera a que termine (✅ verde).
5. Entra en ese resultado, baja a "Artifacts" y descarga `damageon-mod-jar` → ahí está tu `.jar`.
6. Cópialo a tu carpeta `mods` de Minecraft.

## Alternativa: compilarlo en tu propio PC (necesitas Java 21)
Este entorno donde te escribo no tiene acceso a los repositorios de Maven de Fabric, así que no puedo compilar
el `.jar` final aquí. Si prefieres hacerlo local en vez de con GitHub:

1. Instala **Java 21** (JDK) si no lo tienes.
2. Abre esta carpeta (`damageon-mod`) con **IntelliJ IDEA** (recomendado, tiene el plugin de Fabric) o usa
   la terminal.
3. Antes de compilar, comprueba en https://fabricmc.net/develop/ los números exactos de:
   - `yarn_mappings`
   - `loader_version`
   - `fabric_version`
   para la versión 1.21.x que uses, y ajústalos en `gradle.properties` si no coinciden.
4. Compila con:
   ```
   ./gradlew build
   ```
   (en Windows: `gradlew.bat build`)
5. El `.jar` final aparecerá en `build/libs/damageon-1.0.0.jar`.

## Dónde ponerlo
Copia ese `.jar` a la carpeta **`mods`** de tu instancia (la misma que aparece en tu captura de pantalla,
junto a `.fabric`, `config`, `saves`, etc.). También necesitas tener instalado ahí el mod **Fabric API**
(el .jar oficial, descárgalo de Modrinth o CurseForge para tu versión de 1.21), porque este mod depende de él.

Estructura esperada dentro de tu carpeta de instancia:
```
mods/
  fabric-api-x.x.x.jar
  damageon-1.0.0.jar
```

Reinicia el mundo/servidor y prueba con `/damageon`.
