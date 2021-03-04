import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserEntity} from './model/user.entity';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frontend-angular';
  user: UserEntity | null = null;
  navActive = false;
  loginActive = false;
  registrationActive = false;

  constructor(
    private readonly httpClient: HttpClient
  ) {
    httpClient.get<UserEntity>('http://localhost:8080/api/authentication').subscribe(u => this.user = u);
  }


  logout(): void {
    this.httpClient.delete('http://localhost:8080/api/authentication').subscribe(() => this.user = null);
  }
}
