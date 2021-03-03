import {Component, OnInit, EventEmitter, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserEntity} from '../../model/user.entity';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username = 'pes';
  password = '';
  @Output()
  userLoginChanged = new EventEmitter<UserEntity>();

  constructor(
    private readonly httpClient: HttpClient
  ) {
  }

  login(): void {
    this.httpClient.post<UserEntity>('http://localhost:8080/api/authentication', {
      username: this.username,
      password: this.password
    })
      .subscribe(u => this.userLoginChanged.emit(u), error => console.error(error));
  }
}
