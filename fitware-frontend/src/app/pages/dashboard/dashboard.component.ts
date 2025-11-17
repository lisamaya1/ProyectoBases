import { Component } from '@angular/core';

@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {
  highlights = [
    {
      title: 'Gestión de clientes',
      description: 'Registra clientes y haz seguimiento a su progreso corporal en un mismo lugar.',
      icon: 'bi-people'
    },
    {
      title: 'Rutinas y planes',
      description: 'Crea rutinas, planes alimenticios y asigna alimentos por comida fácilmente.',
      icon: 'bi-clipboard-check'
    },
    {
      title: 'Sesiones personalizadas',
      description: 'Planifica sesiones y asigna ejercicios con repeticiones y series controladas.',
      icon: 'bi-activity'
    }
  ];
}
