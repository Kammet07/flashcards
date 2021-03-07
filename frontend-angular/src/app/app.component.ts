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
  collections: Set<CollectionEntity> | null = null;
  navActive = false;
  loginActive = false;
  registrationActive = false;
  flashcardCreateActive = false;
  flashcardActive = false;
  flashcardCollection: CollectionEntity | null = null;
  collectionCreateActive = false;

  constructor(
    private readonly httpClient: HttpClient,
    private toastr: ToastrService
  ) {
    httpClient.get<UserEntity>('http://192.168.0.165:8080/api/authentication', {withCredentials: true}).subscribe(u => this.user = u);
    this.loadCollections();
  }

  logout(): void {
    this.httpClient.delete('http://192.168.0.165:8080/api/authentication', {withCredentials: true}).subscribe(() => {
      this.user = null;
      this.toastr.success('Logged out');
    });
  }

  private loadCollections(): void {
    this.httpClient.get<CollectionEntity[]>('http://192.168.0.165:8080/api/collection')
      .subscribe(u => {
        this.collections = new Set(u);
      }, error => {
        console.error(error);
        this.toastr.error('Something went wrong while getting collections', error.status);
      });
  }

  deleteCollection(collection: CollectionEntity): void {
    this.httpClient.delete(`http://192.168.0.165:8080/api/collection/${collection.id}`, {withCredentials: true, responseType: 'text'})
      .subscribe(u => {
        console.log(u);
        // this.toastr.success();
        this.toastr.success('Collection has been removed');
        this.collections?.delete(collection);
      }, error => {
        console.error(error);
        this.toastr.warning(error.statusText, error.status);
      });
  }
}
