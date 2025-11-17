# Fitware Frontend

Aplicación Angular que consume el backend de Fitware (Spring Boot) para administrar clientes, rutinas, sesiones y seguimiento corporal.

## Requisitos
- Node.js 18+
- npm 9+

## Instalación
1. Abre una terminal dentro de la carpeta fitware-frontend.
2. Ejecuta npm install.

## Desarrollo
- npm start inicia el servidor de desarrollo en http://localhost:4200.
- El frontend apunta al backend en http://localhost:8080/api; ajusta el archivo src/environments/environment.development.ts si lo necesitas.

## Build
- npm run build genera los artefactos de producción en la carpeta dist/.

## Estructura destacada
- src/app/core/models: interfaces por entidad.
- src/app/core/services: clientes HTTP para cada endpoint REST.
- src/app/pages: vistas de dashboard, ejercicios, clientes, rutinas y sesiones.

## Buenas prácticas
- Separación backend/frontend en carpetas independientes.
- Formularios reactivos con validación y control de estado.
- Manejo básico de errores y loaders.
- Diseño responsivo con Bootstrap 5 y estilo personalizado.
