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
    collectionEdit: CollectionEntity | null = null;
    flashcardCollection: CollectionEntity | null = null;
    collectionCreateActive = false;

    constructor(
        private readonly httpClient: HttpClient,
        private readonly toastr: ToastrService
    ) {
        httpClient.get<UserEntity>('/api/authentication', {withCredentials: true}).subscribe(response => this.user = response);
        this.loadCollections();
    }

    logout(): void {
        this.httpClient.delete('/api/authentication', {withCredentials: true}).subscribe(() => {
            this.user = null;
            this.toastr.success('Logged out');
        });
    }

    private loadCollections(): void {
        this.httpClient.get<CollectionEntity[]>('/api/collection')
            .subscribe(response => {
                this.collections = new Set(response);
            }, error => {
                this.toastr.error('Something went wrong while getting collections', error.status);
            });
    }

    deleteCollection(collection: CollectionEntity): void {
        this.httpClient.delete(`/api/collection/${collection.id}`, {withCredentials: true, responseType: 'text'})
            .subscribe(() => {
                this.toastr.success('Collection has been removed');
                this.collections?.delete(collection);
            }, error => {
                this.toastr.warning(error.statusText, error.status);
            });
    }
}
