import {Component} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {UserEntity} from './model/user.entity';
import {ToastrService} from 'ngx-toastr';
import {CollectionEntity} from './model/collection.entity';

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.scss']
})
export class AppComponent {
  title = 'frontend-angular';
  user: UserEntity | null = null;
  collections: CollectionEntity[] | null = null;
  navActive = false;
  loginActive = false;
  registrationActive = false;
  flashcardCreateActive = false;
  flashcardActive = false;
  collectionCreateActive = false;

  constructor(
    private readonly httpClient: HttpClient,
    private toastr: ToastrService
  ) {
    httpClient.get<UserEntity>('http://localhost:8080/api/authentication').subscribe(u => this.user = u);
  }

  logout(): void {
    this.httpClient.delete('http://localhost:8080/api/authentication').subscribe(() => {
      this.user = null;
      this.toastr.success('Logged out');
    });
  }

  getUserById(id: number): string {
    let result = '';
    this.httpClient.get<UserEntity>(`http://localhost:8080/api/user/${id}`).subscribe(u => {
      result = u.username;
    }, error => {
      console.error(error);
      result = 'Error';
    });
    return result;
  }
}
