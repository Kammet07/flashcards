import {Component, EventEmitter, Output} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserEntity} from '../../model/user.entity';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username = '';
  password = '';
  @Output()
  userLoginChanged = new EventEmitter<UserEntity>();

  constructor(
    private readonly httpClient: HttpClient,
    private toastr: ToastrService
  ) {
  }

  login(): void {
    this.httpClient.post<UserEntity>('http://localhost:8080/api/authentication', {
      username: this.username,
      password: this.password
    })
      .subscribe(u => {
        this.userLoginChanged.emit(u);
        this.toastr.success('Logged In');
      }, error => {
        console.error(error);
        this.toastr.error('Wrong username or password');
      });
  }
}
