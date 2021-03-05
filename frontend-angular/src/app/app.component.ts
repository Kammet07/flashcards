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
    this.loadCollections();
  }

  logout(): void {
    this.httpClient.delete('http://localhost:8080/api/authentication').subscribe(() => {
      this.user = null;
      this.toastr.success('Logged out');
    });
  }

  private loadCollections(): void {
    this.httpClient.get<CollectionEntity[]>('http://0.0.0.0:8080/api/collection')
      .subscribe(u => {
        this.collections = u;
      }, error => {
        console.error(error);
        this.toastr.error('Something went wrong while getting collections', error.status);
      });
  }
}
