import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserEntity} from '../../model/user.entity';
import {ToastrService} from 'ngx-toastr';

@Component({
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent {
  username = '';
  password = '';
  mail = '';


  constructor(
    private readonly httpClient: HttpClient,
    private readonly toastr: ToastrService
  ) {
  }

  register(): void {
    this.httpClient.post<UserEntity>('http://localhost:8080/api/user', {
      username: this.username,
      password: this.password,
      mail: this.mail
    })
      .subscribe(() => {
        this.toastr.success('Success!', 'Nice');
      }, () => {
        this.toastr.error('There\'s user with same name or email or bad email format,', 'Error');
      });
  }
}
