import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule, FormsModule } from '@angular/forms';
import { RouterModule } from '@angular/router';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';

import { AppComponent } from './app.component';
import { AppRoutingModule } from './app-routing.module';
import { DashboardComponent } from './pages/dashboard/dashboard.component';
import { EjerciciosComponent } from './pages/ejercicios/ejercicios.component';
import { ClientesComponent } from './pages/clientes/clientes.component';
import { ClienteProgresoComponent } from './pages/clientes/cliente-progreso.component';
import { RutinasComponent } from './pages/rutinas/rutinas.component';
import { SesionesComponent } from './pages/sesiones/sesiones.component';
import { NotFoundComponent } from './pages/not-found/not-found.component';
import { ReportesComponent } from './pages/reportes/reportes.component';

@NgModule({
  declarations: [
    AppComponent,
    DashboardComponent,
    EjerciciosComponent,
    ClientesComponent,
    ClienteProgresoComponent,
    RutinasComponent,
    SesionesComponent,
    NotFoundComponent,
    ReportesComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    FormsModule,
    ReactiveFormsModule,
    RouterModule,
    AppRoutingModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
