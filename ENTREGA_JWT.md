# Autenticación JWT

## Cómo probar

Desde `auth-service`:

```bash
mvn test
```

El endpoint protegido es `GET /api/auth/profile`. Como la aplicación configura el
context path `/auth-service`, la URL completa local es:

```text
http://localhost:8003/auth-service/api/auth/profile
```

Debe enviarse el encabezado `Authorization: Bearer <token>`.

Salida obtenida al ejecutar la suite:

```text
Tests run: 10, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Respuestas teóricas

1. **¿Qué diferencia hay entre decodificar y verificar un JWT?**  Decodificar solo
   transforma el payload de Base64URL a datos legibles; no demuestra que sean
   auténticos. Verificar comprueba la firma con la clave secreta y también valida
   restricciones como la expiración.
2. **¿Por qué no conviene guardar la contraseña dentro del token?**  El payload no
   está cifrado y cualquiera que tenga el token puede leerlo. Una contraseña nunca
   debe incluirse, ni siquiera como hash, porque aumenta innecesariamente el impacto
   de una filtración.
3. **¿Qué pasa si alguien modifica una parte del token?**  La firma deja de coincidir.
   `verifyToken` rechaza el token, aunque el payload modificado todavía pueda
   decodificarse.
4. **¿Para qué sirve la clave secreta?**  Permite al servidor crear y comprobar la
   firma HMAC. Solo quien conoce esa clave puede emitir tokens que el servidor acepte.
5. **¿Qué significa que un token esté expirado?**  Que la fecha actual superó el
   claim `exp`. Aunque la firma siga siendo correcta, el token ya no debe autorizar
   peticiones y es necesario iniciar sesión otra vez o renovarlo.
