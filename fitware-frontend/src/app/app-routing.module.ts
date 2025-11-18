import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { EjerciciosComponent } from './pages/ejercicios/ejercicios.component';
import { ClientesComponent } from './pages/clientes/clientes.component';
import { ClienteProgresoComponent } from './pages/clientes/cliente-progreso.component';
import { RutinasComponent } from './pages/rutinas/rutinas.component';
import { SesionesComponent } from './pages/sesiones/sesiones.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { ReportesComponent } from './pages/reportes/reportes.component';
import { LoginComponent } from './pages/login/login.component';

const routes: Routes = [
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: 'ejercicios', component: EjerciciosComponent },
  { path: 'clientes', component: ClientesComponent },
  { path: 'clientes/:cedula/progreso', component: ClienteProgresoComponent },
  { path: 'rutinas', component: RutinasComponent },
  { path: 'sesiones', component: SesionesComponent },
  { path: 'reportes', component: ReportesComponent },
  { path: 'login', component: LoginComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: 'inicio', component: DashboardComponent },
  { path: '**', component: NotFoundComponent }
];

@NgModule({
  imports: [RouterModule.forRoot(routes, { scrollPositionRestoration: 'enabled' })],
  exports: [RouterModule]
})
export class AppRoutingModule { }
