import { Usuario } from './usuario.model';

export interface Cliente {
  usuarioCedula?: string;
  fechaRegistro: string;
  pesoInicial: number;
  alturaInicial: number;
  usuario?: Usuario;
  // Campos aplanados para compatibilidad con respuestas antiguas
  cedula?: string;
  nombre?: string;
  apellidos?: string;
  fechaNacimiento?: string;
  telefono?: string;
  genero?: string;
}

export interface RegistroClienteDTO {
  usuario: Usuario;
  cliente: {
    usuarioCedula: string;
    fechaRegistro: string;
    pesoInicial: number;
    alturaInicial: number;
  };
}
