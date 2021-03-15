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
    private readonly toastr: ToastrService
  ) {
  }

  login(): void {
    this.httpClient.post<UserEntity>('http://192.168.0.165:8080/api/authentication', {
      username: this.username,
      password: this.password
    })
      .subscribe(response => {
        this.userLoginChanged.emit(response);
        this.toastr.success('Logged In');
      }, error => {
        console.error(error);
        this.toastr.error('Wrong username or password');
      });
  }
}
