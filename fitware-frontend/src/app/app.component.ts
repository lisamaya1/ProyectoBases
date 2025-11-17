import { Component } from '@angular/core';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  navOpen = false;
  currentYear = new Date().getFullYear();

  toggleNav(): void {
    this.navOpen = !this.navOpen;
  }
}
